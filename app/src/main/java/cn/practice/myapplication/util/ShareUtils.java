package cn.practice.myapplication.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.orhanobut.logger.Logger;

import java.util.Set;

public class ShareUtils {
    public static void setPlayMode(Context context, String key, int mode) {
        SharedPreferences playMode = context.getSharedPreferences("play_mode", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = playMode.edit();
        editor.putInt(key, mode);
        editor.apply();
    }
    public static int getPlayMode(Context context, String key) {
        SharedPreferences playMode = context.getSharedPreferences("play_mode", Context.MODE_PRIVATE);
        return playMode.getInt(key, 0);
    }
    public static void setSongNamePlayed(Context context, String key){
        SharedPreferences spf = context.getSharedPreferences("songsPlayed_set", Context.MODE_PRIVATE);
        Set<String> songsPlayedSet = spf.getStringSet("songsPlayed_set", null);

        if(songsPlayedSet == null) songsPlayedSet = new java.util.HashSet<>();
        for(String songName : songsPlayedSet)
            if(songName.equals(key))
                return;

        songsPlayedSet.add(key);
        spf.edit().putStringSet("songsPlayed_set", songsPlayedSet).apply();
    }

    public static Set<String> getSongsPlayedSet(Context context){
        SharedPreferences spf = context.getSharedPreferences("songsPlayed_set", Context.MODE_PRIVATE);
        return spf.getStringSet("songsPlayed_set", null);
    }

}
