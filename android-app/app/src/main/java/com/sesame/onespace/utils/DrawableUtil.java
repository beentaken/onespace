package com.sesame.onespace.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

/**
 * Created by chongos on 11/13/15 AD.
 */
public class DrawableUtil {

    public static Drawable getTextDrawable(Context context, String str) {
        return TextDrawable.builder()
                .beginConfig()
                    .textColor(Color.WHITE)
                    .useFont(Typeface.DEFAULT)
                    .toUpperCase()
                .endConfig()
                .buildRound(str.substring(0, 1),
                        ColorGenerator.MATERIAL.getColor(str));
    }

}
