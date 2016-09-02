package com.cordova.plugin;

import android.content.pm.PackageManager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.content.ClipData;
import android.os.Build;
import android.net.Uri;
import android.provider.MediaStore;
import android.database.Cursor;
import java.io.File;

import android.graphics.BitmapFactory;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PermissionHelper;

import android.content.ContentResolver;
import android.webkit.MimeTypeMap;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

public class ShareManagerPlugin extends CordovaPlugin {

    private CallbackContext subscribeContext = null;
    private final String READ = Manifest.permission.READ_EXTERNAL_STORAGE;
    private PendingRequests pendingRequests;

    /**
     * Action code
     */
    public static int READ_INTENT = 0;

    /**
     * Errors
     */
    public static int FORBIDDEN = 403;

    /**
     * @param cordova
     * @param webView
     */
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.pendingRequests = new PendingRequests();
    }


    /**
     * @param action
     * @param args
     * @param callbackContext
     * @return
     * @throws JSONException
     */
    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("read")) {
            this.readCurrentIntent();
        } else if (action.equals("subscribe")) {
            this.setIntentCallback(callbackContext);
        } else if (action.equals("finish")) {
            this.finishActivity();
        }

        return true;
    }

    /**
     * @param intent
     */
    @Override
    public void onNewIntent(Intent intent) {
        if (this.subscribeContext != null) {
            String type = intent.getType();

            if (type != null && type.startsWith("image/") && !hasPermission(READ)) {
                getReadPermission(READ_INTENT, this.subscribeContext);
            } else {
                parseAndSend(intent, this.subscribeContext);
            }
        }
    }

    /**
     * @param intent
     * @param context
     */
    private void parseAndSend(Intent intent, CallbackContext context) {
        JSONObject data = this.toJSON(intent);
        PluginResult result = new PluginResult(PluginResult.Status.OK, data);
        result.setKeepCallback(true);
        context.sendPluginResult(result);
    }

    /**
     * @param context
     * @return
     */
    private boolean readCurrentIntent() {
        Intent intent = this.cordova.getActivity().getIntent();
        this.onNewIntent(intent);

        return true;
    }

    /**
     * @param context
     * @return
     */
    private boolean setIntentCallback(CallbackContext context) {
        this.subscribeContext = context;

        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);

        context.sendPluginResult(result);

        return true;
    }

    /**
     * @param intent
     * @return
     */
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

                            String path = this.getRealPathFromURI(contentResolver, uri);

                            File file = new File(path);
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

    /**
     * @param permission
     * @return
     */
    private boolean hasPermission(String permission) {
        return PermissionHelper.hasPermission(this, permission);
    }


    private void getReadPermission(int action, CallbackContext context) {
        boolean needPermission = PermissionHelper.hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (!needPermission) {
            int requestCode = pendingRequests.create(action, context);
            PermissionHelper.requestPermission(this, requestCode, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        final PendingRequests.Request request = pendingRequests.splice(requestCode);

        if (request != null) {
            for (int result:grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, FORBIDDEN);
                    request.getContext().sendPluginResult(pluginResult);
                    return;
                }
            }

            int action = request.getAction();

            if (action == READ_INTENT) {
                readCurrentIntent();
            }
        }
    }

    /**
     * @param context
     * @param contentUri
     * @return
     */
    private String getRealPathFromURI(ContentResolver contentResolver, Uri contentUri) {
        Cursor cursor = null;

        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = contentResolver.query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();

            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void finishActivity() {
        this.cordova.getActivity().finish();
    }

}
