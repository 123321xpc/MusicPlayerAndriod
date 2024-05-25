package cn.practice.myapplication;

import static java.security.AccessController.getContext;

import android.app.Application;
import android.graphics.Typeface;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import org.xutils.x;

import cn.practice.myapplication.util.MusicUtils;

public class MyApplication extends Application {
    private Typeface typeface;
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化 Logger日志打印库
        Logger.addLogAdapter(new AndroidLogAdapter());
        // 初始化 xUtils3
        x.Ext.init(this);
        x.Ext.setDebug(true);
    }
}
