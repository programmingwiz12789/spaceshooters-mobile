package com.bryanwijaya.spaceshooters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.lang.reflect.Field;
import java.util.HashMap;

public class Utils {
    public static HashMap<String, Bitmap> GetAllImages(Activity activity) {
        HashMap<String, Bitmap> images = new HashMap<>();
        for (Field field : R.drawable.class.getFields()) {
            String fileName = field.getName();
            int resourceId = activity.getResources().getIdentifier(fileName, "drawable", activity.getPackageName());
            images.put(fileName, BitmapFactory.decodeResource(activity.getResources(), resourceId));
        }
        return images;
    }
}
