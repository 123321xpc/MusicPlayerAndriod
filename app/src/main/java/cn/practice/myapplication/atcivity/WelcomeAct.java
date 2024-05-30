package cn.practice.myapplication.atcivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;

import cn.practice.myapplication.R;

public class WelcomeAct extends AppCompatActivity {

    public boolean AllreadyStartAct = false; // 判断是否已经startAct


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
               startMainAct();
            }
        }, 1500);



    }

    // 监听触摸事件:单机页面，进入主页面
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(event.getAction() == MotionEvent.ACTION_DOWN)
            startMainAct();
        return true;
    }

    // 启动主界面
    public void startMainAct(){
        // 判断是否已经startAct
        if(AllreadyStartAct) return;

        AllreadyStartAct = true;
        startActivity(new Intent(WelcomeAct.this, LoginActivity.class));
        finish();
    }
}