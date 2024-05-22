package cn.practice.myapplication.bean;

import java.io.Serializable;

public class NetMusicItem implements Serializable {

    public String id;

    public String name;
    public String singer;

    public String fullname;
    public String category;

    public NetMusicItem() {
    }

    public NetMusicItem(String id, String name, String singer, String fullname, String category) {
        this.id = id;
        this.name = name;
        this.singer = singer;
        this.fullname = fullname;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "NetMusicItem{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", singer='" + singer + '\'' +
                ", fullname='" + fullname + '\'' +
                ", category='" + category + '\'' +
                '}' + '\n';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
