package cn.practice.myapplication.bean;

public enum PlayMode {

    LOOP("列表循环", "\ue67b", 0),
    RANDOM("随机播放", "\uea75", 1),
    SINGLE("单曲循环", "\uea76", 2);


    private int index;
    private String mode;
    private String icon;

    public static PlayMode getPlayMode(int index) {
        for (PlayMode playMode : PlayMode.values()) {
            if (playMode.getIndex() == index) {
                return playMode;
            }
        }
        return LOOP;
    }



    PlayMode(String mode, String icon, int index) {
        this.mode = mode;
        this.icon = icon;
        this.index = index;
    }

    public String getMode() {
        return mode;
    }

    public int getIndex() {
        return index;
    }


    public void setIndex(int index) {
        this.index = index;
    }

    public String getIcon() {
        return icon;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
