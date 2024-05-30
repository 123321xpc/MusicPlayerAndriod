package cn.practice.myapplication.util;

import android.content.ContentResolver;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.http.annotation.HttpResponse;
import org.xutils.x;


import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cn.practice.myapplication.bean.MusicItem;
import cn.practice.myapplication.bean.MusicListCallback;
import cn.practice.myapplication.bean.NetMusicItem;

public class MusicUtils {

    public static final int SHOW_MUSIC = 1;
    public final static String ip = "10.136.11.177";
//    public String ip = "10.81.66.218";    // wifi



    public List<MusicItem> scanLocalMusic(ContentResolver contentResolver) {

        File externalStorage = Environment.getExternalStorageDirectory();
        List<MusicItem> musicFiles = scanLocalMusicFiles(externalStorage);
        return musicFiles;
    }
    private List<MusicItem> scanLocalMusicFiles(File dir) {
        List<MusicItem> musicFiles = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    musicFiles.addAll(scanLocalMusicFiles(file));
                } else {
                    // 判断文件扩展名是否为音频格式，比如.mp3、.wav等
                    if (file.getName().toLowerCase().endsWith(".mp3") || file.getName().toLowerCase().endsWith(".flac") /* 其他音频格式扩展名 */) {
                        MusicItem musicItem = new MusicItem();
                        String songInfo[] = file.getName().split("/.");
                        String[] title = songInfo[0].split("-");
                        if(title.length > 1) {
                            musicItem.setName(title[0]);
                            musicItem.setSinger(title[1].split("\\.")[0]);
                        }else{
                            musicItem.setSinger("未知");
                            musicItem.setName(title[0].split("\\.")[0]);
                        }

                        double size = (double)file.length() / 1024.0 / 1024.0;
                        musicItem.setSize(size);
                        musicItem.setPath(file.getAbsolutePath());
                        musicItem.setDuration(getMusicDuration(file.getAbsolutePath()));
                        musicFiles.add(musicItem);
                    }
                }
            }
        }
        return musicFiles;
    }

    public int getMusicDuration(String filePath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);
        String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        int duration = Integer.parseInt(durationStr);
        try {
            retriever.release();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return duration;
    }

    public static String formatDuration(long duration) {
        long minutes = (duration / 1000) / 60;
        long seconds = (duration / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // 获取随机音乐
    public static int getRandomPosition(int musicListSize) {
        return new Random().nextInt(musicListSize);
    }

    public void getMusicByCategory(Context context, String category, MusicListCallback callback) {

        RequestParams params = new RequestParams("http://" + ip + ":8080/music/" + category);
        x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                ArrayList<NetMusicItem> netMusicList = new ArrayList<>();
                Logger.d(result);

                // 移除首尾的中括号
                String json = result.substring(1, result.length() - 1);

                // 分割多个JSON对象
                String[] objects = json.split("\\},\\{");
                for (String object : objects) {
                    // 处理花括号
                    object = object.replaceAll("[{}]", "");


                    NetMusicItem item = new NetMusicItem();
                    item.setSinger(extractValue(object, "singer"));
                    item.setName(extractValue(object, "name"));
                    item.setCategory(extractValue(object, "category"));
                    item.setId(extractValue(object, "id"));
                    item.setFullname(extractValue(object, "fullname"));

                    netMusicList.add(item);
                }


                callback.onMusicListLoaded(netMusicList);

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Toast.makeText(context, "获取网络音乐列表失败，请重新进入页面！", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(CancelledException cex) {
                // 请求被取消时的处理
            }

            @Override
            public void onFinished() {
                // 无论请求成功或失败都会执行的处理
            }
        });
    }


    public void getNetMusicList(Context context, MusicListCallback callback) {
        RequestParams params = new RequestParams("http://" + ip + ":8080/music/allmusic");
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                ArrayList<NetMusicItem> netMusicList = new ArrayList<>();

                // 移除首尾的中括号
                String json = result.substring(1, result.length() - 1);

                // 分割多个JSON对象
                String[] objects = json.split("\\},\\{");
                for (String object : objects) {
                    // 处理花括号
                    object = object.replaceAll("[{}]", "");


                    NetMusicItem item = new NetMusicItem();
                    item.setSinger(extractValue(object, "singer"));
                    item.setName(extractValue(object, "name"));
                    item.setCategory(extractValue(object, "category"));
                    item.setId(extractValue(object, "id"));
                    item.setFullname(extractValue(object, "fullname"));

                    netMusicList.add(item);
                }


                callback.onMusicListLoaded(netMusicList);

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Toast.makeText(context, "获取网络音乐列表失败，请重新进入页面！", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(CancelledException cex) {
                // 请求被取消时的处理
            }

            @Override
            public void onFinished() {
                // 无论请求成功或失败都会执行的处理
            }
        });

    }

    // 下载音乐
    public void downloadMusicById(String fileName, String id, Context context, MusicListCallback callback) {
        String baseUrl = "http://" + ip + ":8080/music/";
        String localpath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/";

        // 创建请求参数对象
        RequestParams params = new RequestParams(baseUrl + id);
        params.setSaveFilePath(localpath + fileName + ".mp3");


        // 发送GET请求并处理回调
        Callback.Cancelable cancelable = x.http().get(params, new Callback.ProgressCallback<File>() {
            @Override
            public void onSuccess(File result) {
                callback.onMusicListLoaded(null);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Toast.makeText(context, "下载文件出错，请稍后重试！", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(CancelledException cex) {
                System.out.println("音乐文件下载被取消。");
            }

            @Override
            public void onFinished() {
                // 下载完成后的清理工作
            }

            @Override
            public void onWaiting() {
                // 等待下载开始
            }

            @Override
            public void onStarted() {
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                long progress = (current * 100) / total - 30;
                if(progress > 0)
                    Toast.makeText(context, "正在下载音乐文件，已完成" + progress + "%", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void downloadLyricById(String fullname, String id, Context context) {
        String baseUrl = "http://" + ip + ":8080/music/lyric/";
        String localpath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/";

        // 创建请求参数对象
        RequestParams params = new RequestParams(baseUrl + id);
        params.setSaveFilePath(localpath + fullname + ".lrc");


        // 发送GET请求并处理回调
        Callback.Cancelable cancelable = x.http().get(params, new Callback.ProgressCallback<File>() {
            @Override
            public void onSuccess(File result) {
                Toast.makeText(context, "下载音乐文件成功！", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Toast.makeText(context, "下载文件出错，请稍后重试！", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
                // 下载完成后的清理工作
            }

            @Override
            public void onWaiting() {
                // 等待下载开始
            }

            @Override
            public void onStarted() {
                // 开始下载时调用
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                long progress = (current * 100) / total + 30;
                if(progress < 100)
                    Toast.makeText(context, "正在文件，已完成" + progress + "%", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 解析JSON字符串
    private static String extractValue(String object, String fieldName) {
        String[] keyValuePairs = object.split(",");
        for (String pair : keyValuePairs) {
            String[] keyValue = pair.split(":");
            if (keyValue[0].trim().replaceAll("\"", "").equals(fieldName)) {
                return keyValue[1].trim().replaceAll("\"", "");
            }
        }
        return "Field not found";
    }
}

