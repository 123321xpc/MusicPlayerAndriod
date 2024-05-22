package cn.practice.myapplication.bean;

import java.util.ArrayList;

public interface MusicListCallback {
    void onMusicListLoaded(ArrayList<NetMusicItem> musicList);
}

