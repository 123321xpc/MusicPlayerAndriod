package cn.practice.myapplication.bean;


import androidx.annotation.NonNull;

import java.text.DecimalFormat;



public class MusicItem {
    private String name;
    private String singer;
    private int duration;
    private double size;
    private String path;

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setSize(double size) {
        DecimalFormat df = new DecimalFormat("#.##");
        this.size = Double.parseDouble(df.format(size));
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public int getDuration() {
        return duration;
    }


    public double getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getSinger() {
        return singer;
    }

    @NonNull
    @Override
    public String toString() {
        return "歌名: " + name + "\n" +
                "歌手: " + singer + "\n" +
                "时长: " + duration + "\n" +
                "大小: " + size + "MB\n" +
                "路径: " + path + "\n";
    }
}
