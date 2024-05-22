package cn.practice.myapplication.bean;

public class Lyric {
    private String content; // 歌词内容
    private int time; // 歌词时间
    private int sleepTime; // 高亮时间

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }
}
