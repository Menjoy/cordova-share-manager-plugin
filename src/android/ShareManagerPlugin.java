package com.cordova.plugin;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.content.ClipData;
import android.os.Build;
import android.net.Uri;
import java.io.File;

import android.graphinikics.BitmapFactory;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;

import android.content.ContentResolver;
import android.webkit.MimeTypeMap;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

public class ShareManagerPlugin extends CordovaPlugin {

    private CallbackContext subscribeContext = null;

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("read")) {
            this.readCurrentIntent(callbackContext);
        } else if (action.equals("subscribe")) {
            this.setIntentCallback(callbackContext);
        }

        return false;
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (this.subscribeContext != null) {
            JSONObject data = toJSON(intent);
            PluginResult result = new PluginResult(PluginResult.Status.OK, data);

            result.setKeepCallback(true);

            this.subscribeContext.sendPluginResult(result);
        }
    }

    private boolean readCurrentIntent(final CallbackContext context) {
        Intent intent = this.cordova.getActivity().getIntent();
        onNewIntent(intent);

        return true;
    }

    private boolean setIntentCallback(CallbackContext context) {
        this.subscribeContext = context;

        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);

        context.sendPluginResult(result);

        return true;
    }

    private JSONObject toJSON(Intent intent) {
        JSONObject result = null;
        ClipData clipData = null;
        JSONObject[] items = null;
        ContentResolver contentResolver = this.cordova.getActivity().getApplicationContext().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            clipData = intent.getClipData();

            if (clipData != null) {
                int clipItemCount = clipData.getItemCount();
                items = new JSONObject[clipItemCount];

                for (int i = 0; i < clipItemCount; i++) {

                    ClipData.Item item = clipData.getItemAt(i);

                    try {
                        Uri uri = item.getUri();

                        items[i] = new JSONObject();
                        items[i].put("htmlText", item.getHtmlText());
                        items[i].put("intent", item.getIntent());
                        items[i].put("text", item.getText());
                        items[i].put("uri", uri);

                        if (uri != null) {
                            String mimeType = contentResolver.getType(item.getUri());
                            String extension = mime.getExtensionFromMimeType(mimeType);

                            items[i].put("type", mimeType);
                            items[i].put("extension", extension);

                            // get image width and height
                            File file = new File(uri.getPath());
                            BitmapFactory.Options options = new BitmapFactory.Options();

                            options.inJustDecodeBounds = true;

                            BitmapFactory.decodeFile(file.getAbsolutePath(), options);

                            items[i].put("width", options.outWidth);
                            items[i].put("height", options.outHeight);
                            items[i].put("size", file.length());
                        }

                    } catch (JSONException e) {}
                }
            }
        }

        try {
            result = new JSONObject();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (items != null) {
                    result.put("items", new JSONArray(items));
                }
            }

            result.put("type", intent.getType());
        } catch (JSONException e) {}

        return result;
    }

}
