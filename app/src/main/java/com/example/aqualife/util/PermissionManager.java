package com.example.aqualife.util;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionManager {
    private static final String TAG = "PermissionManager";

    public static final int PERMISSION_REQUEST_CODE = 1001;
    public static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1002;
    public static final int DO_NOT_DISTURB_REQUEST_CODE = 1003;

    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.VIBRATE,
            Manifest.permission.WAKE_LOCK
    };

    public interface PermissionCallback {
        void onPermissionGranted();
        void onPermissionDenied(String[] deniedPermissions);
    }

    public static void checkAndRequestAllPermissions(Activity activity, PermissionCallback callback) {
        Log.d(TAG, "Checking all required permissions");

        if (!hasAllBasicPermissions(activity)) {
            requestBasicPermissions(activity, callback);
            return;
        }

        if (!hasNotificationPermission(activity)) {
            requestNotificationPermission(activity, callback);
            return;
        }

        if (!hasDoNotDisturbPermission(activity)) {
            requestDoNotDisturbPermission(activity, callback);
            return;
        }

        callback.onPermissionGranted();
    }

    private static boolean hasAllBasicPermissions(Context context) {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Missing permission: " + permission);
                return false;
            }
        }
        return true;
    }

    private static void requestBasicPermissions(Activity activity, PermissionCallback callback) {
        Log.d(TAG, "Requesting basic permissions");

        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    activity,
                    permissionsToRequest.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE
            );
        }
    }

    private static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private static void requestNotificationPermission(Activity activity, PermissionCallback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d(TAG, "Requesting notification permission");
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    private static boolean hasDoNotDisturbPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            return notificationManager.isNotificationPolicyAccessGranted();
        }
        return true;
    }

    private static void requestDoNotDisturbPermission(Activity activity, PermissionCallback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "Requesting Do Not Disturb permission");
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            activity.startActivityForResult(intent, DO_NOT_DISTURB_REQUEST_CODE);
        }
    }

    public static void handlePermissionResult(Activity activity, int requestCode,
                                              String[] permissions, int[] grantResults,
                                              PermissionCallback callback) {

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                handleBasicPermissionResult(activity, permissions, grantResults, callback);
                break;

            case NOTIFICATION_PERMISSION_REQUEST_CODE:
                handleNotificationPermissionResult(activity, permissions, grantResults, callback);
                break;

            default:
                Log.w(TAG, "Unknown request code: " + requestCode);
                break;
        }
    }

    private static void handleBasicPermissionResult(Activity activity, String[] permissions,
                                                    int[] grantResults, PermissionCallback callback) {
        List<String> deniedPermissions = new ArrayList<>();

        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permissions[i]);
            }
        }

        if (deniedPermissions.isEmpty()) {
            checkAndRequestAllPermissions(activity, callback);
        } else {
            callback.onPermissionDenied(deniedPermissions.toArray(new String[0]));
        }
    }

    private static void handleNotificationPermissionResult(Activity activity, String[] permissions,
                                                           int[] grantResults, PermissionCallback callback) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkAndRequestAllPermissions(activity, callback);
        } else {
            callback.onPermissionDenied(new String[]{Manifest.permission.POST_NOTIFICATIONS});
        }
    }

    public static void handleActivityResult(Activity activity, int requestCode,
                                            PermissionCallback callback) {
        if (requestCode == DO_NOT_DISTURB_REQUEST_CODE) {
            if (hasDoNotDisturbPermission(activity)) {
                callback.onPermissionGranted();
            } else {
                callback.onPermissionDenied(new String[]{"Do Not Disturb Access"});
            }
        }
    }

    public static boolean shouldShowRequestPermissionRationale(Activity activity, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    public static void openAppSettings(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }
}
