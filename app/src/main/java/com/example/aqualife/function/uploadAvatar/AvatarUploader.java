package com.example.aqualife.function.uploadAvatar;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.aqualife.services.FiveManageAPI;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AvatarUploader {
    private static final String BASE_URL = "https://api.fivemanage.com/api/";
    private static final String TAG = "ImageUploader";
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private FiveManageAPI apiService;
    private String apiToken;

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onError(String error);
    }

    public AvatarUploader(String apiToken) {
        // FiveManage không cần "Bearer " prefix
        this.apiToken = apiToken;
        setupRetrofit();
    }

    private void setupRetrofit() {
        // Tạo HttpLoggingInterceptor để debug
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Cấu hình OkHttpClient với các tối ưu hóa
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                // Chỉ sử dụng HTTP/1.1 để tránh lỗi HTTP/2
                .protocols(Arrays.asList(Protocol.HTTP_1_1))
                // Tăng timeout
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                // Cho phép retry khi connection fail
                .retryOnConnectionFailure(true)
                // Thêm interceptor để log requests
                .addInterceptor(loggingInterceptor)
                // Thêm header Connection: close để tránh connection reuse issues
                .addInterceptor(chain -> {
                    okhttp3.Request original = chain.request();
                    okhttp3.Request request = original.newBuilder()
                            .header("Connection", "close")
                            .header("User-Agent", "AquaLife-Android-App/1.0")
                            .build();
                    return chain.proceed(request);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .build();

        apiService = retrofit.create(FiveManageAPI.class);
    }

    public void uploadImage(Context context, Uri imageUri, UploadCallback callback) {
        uploadImageWithRetry(context, imageUri, callback, 1);
    }

    private void uploadImageWithRetry(Context context, Uri imageUri, UploadCallback callback, int attempt) {
        try {
            Log.d(TAG, "Upload attempt: " + attempt);

            // Chuyển đổi Uri thành File
            File imageFile = createFileFromUri(context, imageUri);
            Log.d(TAG, "Created temp file: " + imageFile.getAbsolutePath() + " (Size: " + imageFile.length() + " bytes)");

            // Kiểm tra kích thước file (giới hạn 10MB)
            if (imageFile.length() > 10 * 1024 * 1024) { // 10MB
                callback.onError("File quá lớn. Vui lòng chọn ảnh nhỏ hơn 10MB");
                imageFile.delete();
                return;
            }

            // Tạo RequestBody cho file
            RequestBody requestFile = RequestBody.create(
                    MediaType.parse("image/*"),
                    imageFile
            );

            // Tạo MultipartBody.Part với field name đúng
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData(
                    "image", // FiveManage yêu cầu field name là "image"
                    imageFile.getName(),
                    requestFile
            );

            // Tạo metadata (đơn giản hóa)
            RequestBody metadata = RequestBody.create(
                    MediaType.parse("text/plain"),
                    "image_upload"
            );

            // Gọi API upload
            Call<ResponseBody> call = apiService.uploadImage(apiToken, imagePart, metadata);
            Log.d(TAG, "Making API call...");

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.d(TAG, "Response received: " + response.code());

                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseBody = response.body().string();
                            Log.d(TAG, "Upload success: " + responseBody);

                            // Parse response để lấy URL hình ảnh
                            String imageUrl = parseImageUrl(responseBody);
                            if (imageUrl != null && !imageUrl.isEmpty() && imageUrl.startsWith("http")) {
                                callback.onSuccess(imageUrl);
                            } else {
                                callback.onError("Không thể lấy URL ảnh từ response: " + responseBody);
                            }

                        } catch (IOException e) {
                            Log.e(TAG, "Error reading response", e);
                            callback.onError("Lỗi đọc phản hồi từ server: " + e.getMessage());
                        }
                    } else {
                        String errorMsg = "Upload thất bại với mã lỗi: " + response.code();
                        try {
                            if (response.errorBody() != null) {
                                String errorBody = response.errorBody().string();
                                Log.e(TAG, "Error response: " + errorBody);
                                errorMsg += "\nChi tiết: " + errorBody;
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Error reading error response", e);
                        }

                        // Retry cho một số lỗi server
                        if (response.code() >= 500 && attempt < MAX_RETRY_ATTEMPTS) {
                            Log.d(TAG, "Server error, retrying...");
                            retryAfterDelay(context, imageUri, callback, attempt + 1);
                        } else {
                            callback.onError(errorMsg);
                        }
                    }

                    // Xóa file tạm
                    cleanupTempFile(imageFile);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e(TAG, "Upload error (attempt " + attempt + ")", t);

                    String errorMessage = "Lỗi kết nối: " + t.getMessage();

                    // Kiểm tra loại lỗi và quyết định có retry hay không
                    boolean shouldRetry = false;

                    if (t instanceof okhttp3.internal.http2.StreamResetException) {
                        errorMessage = "Lỗi kết nối HTTP/2. Đang thử lại...";
                        shouldRetry = true;
                    } else if (t instanceof java.net.SocketTimeoutException) {
                        errorMessage = "Timeout khi upload. Đang thử lại...";
                        shouldRetry = true;
                    } else if (t instanceof java.net.ConnectException) {
                        errorMessage = "Không thể kết nối đến server. Đang thử lại...";
                        shouldRetry = true;
                    }

                    if (shouldRetry && attempt < MAX_RETRY_ATTEMPTS) {
                        Log.d(TAG, "Retrying upload due to: " + t.getClass().getSimpleName());
                        retryAfterDelay(context, imageUri, callback, attempt + 1);
                    } else {
                        callback.onError(errorMessage);
                        cleanupTempFile(imageFile);
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error preparing upload", e);
            callback.onError("Lỗi chuẩn bị upload: " + e.getMessage());
        }
    }

    private void retryAfterDelay(Context context, Uri imageUri, UploadCallback callback, int nextAttempt) {
        int delayMs = 2000 * nextAttempt; // 2s, 4s, 6s
        Log.d(TAG, "Retrying in " + delayMs + "ms...");

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            uploadImageWithRetry(context, imageUri, callback, nextAttempt);
        }, delayMs);
    }

    private void cleanupTempFile(File file) {
        try {
            if (file != null && file.exists()) {
                boolean deleted = file.delete();
                Log.d(TAG, "Temp file deleted: " + deleted);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting temp file", e);
        }
    }

    private File createFileFromUri(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new IOException("Cannot open input stream from URI");
        }

        // Tạo tên file với timestamp để tránh conflict
        String fileName = "temp_image_" + System.currentTimeMillis() + ".jpg";
        File tempFile = new File(context.getCacheDir(), fileName);

        FileOutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[4096]; // Tăng buffer size
        int length;

        try {
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
        } finally {
            outputStream.close();
            inputStream.close();
        }

        Log.d(TAG, "Created temp file: " + tempFile.length() + " bytes");
        return tempFile;
    }

    private String parseImageUrl(String responseBody) {
        try {
            Log.d(TAG, "Parsing response: " + responseBody);

            // Sử dụng JSONObject để parse chính xác response từ FiveManage
            JSONObject jsonResponse = new JSONObject(responseBody);

            // Dựa trên response bạn cung cấp: {"id":"DUc6CmH08BMmXYtr1Pl5n","url":"https://r2.fivemanage.com/image/3l9CHNoyVUph.jpg"}
            if (jsonResponse.has("url")) {
                String imageUrl = jsonResponse.getString("url");
                Log.d(TAG, "Successfully parsed image URL: " + imageUrl);
                return imageUrl;
            }

            // Thử các pattern khác nếu cần
            if (jsonResponse.has("link")) {
                String imageUrl = jsonResponse.getString("link");
                Log.d(TAG, "Successfully parsed image link: " + imageUrl);
                return imageUrl;
            }

            // Thử pattern nested data
            if (jsonResponse.has("data")) {
                JSONObject dataObject = jsonResponse.getJSONObject("data");
                if (dataObject.has("url")) {
                    String imageUrl = dataObject.getString("url");
                    Log.d(TAG, "Successfully parsed nested image URL: " + imageUrl);
                    return imageUrl;
                }
            }

            Log.w(TAG, "Could not find URL field in response");
            return null;

        } catch (Exception e) {
            Log.e(TAG, "Error parsing JSON response", e);

            // Fallback to string parsing if JSON parsing fails
            try {
                // Pattern 1: {"url": "https://..."}
                if (responseBody.contains("\"url\"")) {
                    int startIndex = responseBody.indexOf("\"url\":\"") + 7;
                    int endIndex = responseBody.indexOf("\"", startIndex);
                    if (startIndex > 6 && endIndex > startIndex) {
                        String url = responseBody.substring(startIndex, endIndex);
                        Log.d(TAG, "Fallback parsing successful: " + url);
                        return url;
                    }
                }
            } catch (Exception fallbackException) {
                Log.e(TAG, "Fallback parsing also failed", fallbackException);
            }

            return null;
        }
    }
}