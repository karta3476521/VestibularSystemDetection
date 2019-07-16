package com.boss.vestibularsystemdetection;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.view.inputmethod.InputMethodManager;

public class MyUtils {

    static boolean checkPermission(Context context){
        int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED)
            return true;
        else
            return false;

    }

    static void closeKeyboard(Activity mActivity){
        InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mActivity.getWindow().getDecorView().getWindowToken(), 0);
    }

}
