package com.example.evshop.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import vn.vietmap.vietmapsdk.annotations.Icon;
import vn.vietmap.vietmapsdk.annotations.IconFactory;

public class IconUtils {

    @Nullable
    public Icon drawableToIcon(@NonNull Context context,
                               @DrawableRes int id,
                               @ColorInt int color) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(
                context.getResources(), id, context.getTheme()
        );
        if (vectorDrawable == null) {
            return null; // giống Kotlin: Icon?
        }

        // Phòng trường hợp intrinsic size = 0 gây crash
        int width  = Math.max(1, vectorDrawable.getIntrinsicWidth());
        int height = Math.max(1, vectorDrawable.getIntrinsicHeight());

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawableCompat.setTint(vectorDrawable, color);
        vectorDrawable.draw(canvas);

        return IconFactory.getInstance(context).fromBitmap(bitmap);
    }
}
