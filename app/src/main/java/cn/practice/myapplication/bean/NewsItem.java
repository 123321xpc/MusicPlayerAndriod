package cn.practice.myapplication.bean;

public class NewsItem {
    public String title;

    public String url;

    @Override
    public String toString() {
        return "NewsItem{" +
                "title='" + title + '\'' +
                ", url='" + url + '\'' +
                '}' + '\n';
    }

    public NewsItem(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
