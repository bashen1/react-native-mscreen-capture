package com.lewin.qrcode;



import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;



/**
 * Created by lewin on 2018/3/14.
 */

public class ScreenCapture extends ReactContextBaseJavaModule {

    private ReactApplicationContext reactContext;

    private ScreenCapturetListenManager manager;
    private final Handler mainHandler = new Handler((Looper.getMainLooper()));

    private final static String path = "/screen-capture/";

    public ScreenCapture(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "ScreenCapture";
    }

    @ReactMethod
    public void startListener(String keywords,Promise promise) {
        String[] keys = null;
        if (keywords != null && keywords.length() > 0) {
            keys = keywords.split(",");
        }

        this.startListenerCapture(promise, keys);
    }

    @ReactMethod
    public void stopListener(final Promise promise) {
        try{
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (manager != null) {
                        manager.stopListen();
                        manager = null;
                    }

                }
            });
            promise.resolve("true");
        }catch (Exception ex) {
            ex.printStackTrace();
            promise.reject("500", ex.getMessage());
        }
    }

    @ReactMethod
    public void screenCapture(Boolean isHiddenStatus,Promise promise) {
        promise.resolve(shotActivity(getCurrentActivity(), isHiddenStatus));
    }

    @ReactMethod
    public void clearCache(Promise promise) {
        WritableMap map = Arguments.createMap();
        try{
            File file = new File(Environment.getExternalStorageDirectory() + path);
            deleteFile(file);
            map.putString("code", "200");
            promise.resolve(map);
        }catch (Exception ex) {
            ex.printStackTrace();
            promise.reject("500", ex.getMessage());
        }


    }
    private void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                deleteFile(f);
            }
            file.delete();//?????????????????????????????????????????????????????????
        } else if (file.exists()) {
            file.delete();
        }
    }


    private void startListenerCapture(final Promise promise, final String[] keywords) {
        try {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    //???????????????????????????????????????UI???
                    // ????????????
                    manager = ScreenCapturetListenManager.newInstance(reactContext, keywords);
                    manager.setListener(
                            new ScreenCapturetListenManager.OnScreenCapturetListen() {
                                public void onShot(String imagePath) {
                                    // ?????????????????????
                                    WritableMap map = Arguments.createMap();
                                    map.putString("code", "200");
                                    map.putString("uri", imagePath.indexOf("file://") == 0 ? imagePath : "file://" + imagePath);
                                    map.putString("base64", bitmapToBase64(BitmapFactory.decodeFile(imagePath)));
                                    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("ScreenCapture", map);
                                }
                            }
                    );
                    manager.startListen();
                    promise.resolve("success");
                }
            });
        }catch (Exception ex) {
            ex.printStackTrace();
            promise.reject("500", ex.getMessage());
        }
    }

    /**
     * ???????????????Activity?????????????????????????????????
     *
     * @param context ????????????Activity
     * @return Bitmap
     */
    public static  WritableMap shotActivity(Activity context, Boolean isHiddenStatus) {
        WritableMap map = Arguments.createMap();
        Bitmap bitmap = isHiddenStatus ? ScreenUtils.snapShotWithoutStatusBar(context) : ScreenUtils.snapShotWithStatusBar(context);
        Calendar now = new GregorianCalendar();
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String fileName = Environment.getExternalStorageDirectory() + path + simpleDate.format(now.getTime()) + ".png";
        try {
            File fileDir = new File(Environment.getExternalStorageDirectory() + path);
            if(!fileDir.exists()) {
                fileDir.mkdir();
            }
            File file = new File(fileName);
            if(file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            map.putString("code", "200");
            map.putString("uri", "file://" + fileName);
            map.putString("base64", bitmapToBase64(bitmap));
        } catch (Exception e) {
            e.printStackTrace();
            map.putString("code", "500");
        }


        return map;
    }


    /**
     * bitmap??????base64
     *
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}
