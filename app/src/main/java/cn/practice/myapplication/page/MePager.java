package cn.practice.myapplication.page;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import cn.practice.myapplication.R;

public class MePager extends BasePager {

    public TextView mTextView;



    @Override
    public void initData() {
        this.LayerId = 2;
    }

    // onCreateView 方法用于创建 Fragment 的视图
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 使用布局文件来定义 Fragment 的视图
        rootView = inflater.inflate(R.layout.fragment_me_music, container, false);
        initData();
        return rootView;
    }

}
