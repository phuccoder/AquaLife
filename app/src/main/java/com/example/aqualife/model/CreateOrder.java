package com.example.aqualife.model;

import android.util.Log;

import com.example.aqualife.constant.AppInfo;
import com.example.aqualife.helper.Helpers;
import com.example.aqualife.provider.HttpProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class CreateOrder {
    private class CreateOrderData {
        String AppId;
        String AppUser;
        String AppTime;
        String Amount;
        String AppTransId;
        String EmbedData;
        String Items;
        String BankCode;
        String Description;
        String Mac;

        private CreateOrderData(String amount, String itemsJson) throws Exception {
            long appTime = new Date().getTime();
            AppId = String.valueOf(AppInfo.APP_ID);
            AppUser = "Android_Demo";
            AppTime = String.valueOf(appTime);
            Amount = amount;
            AppTransId = Helpers.getAppTransId();
            EmbedData = "{}";
            Items = itemsJson;
//            Items = "[]";
            BankCode = "zalopayapp";
            Description = "Merchant pay for order #" + AppTransId;
            String inputHMac = String.format("%s|%s|%s|%s|%s|%s|%s",
                    this.AppId,
                    this.AppTransId,
                    this.AppUser,
                    this.Amount,
                    this.AppTime,
                    this.EmbedData,
                    this.Items);
            Log.d("ZaloPayDebug", "Input HMac String: " + inputHMac);

            Mac = Helpers.getMac(AppInfo.MAC_KEY, inputHMac);
            Log.d("ZaloPayDebug", "Calculated MAC: " + Mac);
        }
    }

    public JSONObject createOrder(String amount, List<CartItemResponse> items) throws Exception {
        String itemJson = generateItemsJson(items);
        CreateOrderData input = new CreateOrderData(amount, itemJson);
        RequestBody formBody = new FormBody.Builder()
                .add("app_id", input.AppId)
                .add("app_user", input.AppUser)
                .add("app_time", input.AppTime)
                .add("amount", input.Amount)
                .add("app_trans_id", input.AppTransId)
                .add("embed_data", input.EmbedData)
                .add("item", input.Items)
                .add("bank_code", input.BankCode)
                .add("description", input.Description)
                .add("mac", input.Mac)
                .build();

        JSONObject data = HttpProvider.sendPost(AppInfo.URL_CREATE_ORDER, formBody);
        return data;
    }
    private String generateItemsJson(List<CartItemResponse> items) throws JSONException {
        JSONArray itemsArray = new JSONArray();
        for (CartItemResponse item: items
             ) {
            JSONObject obj = new JSONObject();
            obj.put("itemid", item.getProductId());
            obj.put("itemname", Helpers.removeAccents(item.getProduct().getProductName())); // Có thể thay bằng tên thực
            obj.put("itemprice", item.getPrice().intValue());
            obj.put("itemquantity", item.getQuantity());
            itemsArray.put(obj);
        }
        return itemsArray.toString();
    }
}

