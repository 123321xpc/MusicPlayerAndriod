package cn.practice.myapplication.page;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import cn.practice.myapplication.R;

public abstract class BasePager extends Fragment {

    public Context context;

    public int LayerId;

    public View rootView;

    public BasePager() {
        this.context = getContext();
    }

    public void initData(){

    }



}
