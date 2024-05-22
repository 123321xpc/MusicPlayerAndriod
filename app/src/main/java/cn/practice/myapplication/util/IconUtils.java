package cn.practice.myapplication.util;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import cn.practice.myapplication.R;

public class  IconUtils {
    public static void setFontIcon(Context context, TextView view, String iconCode) {

        Typeface typeface = Typeface.createFromAsset(context.getAssets(), "iconfont.ttf");
        // 在TextView中应用该字体
        view.setTypeface(typeface);

        // 设置文本为字体图标
        view.setText(iconCode); // Unicode value for the icon you want to display
    }
}
