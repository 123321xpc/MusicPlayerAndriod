package cn.practice.myapplication.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import cn.practice.myapplication.R;
import cn.practice.myapplication.atcivity.MusicPlayerAct;
import cn.practice.myapplication.bean.MusicItem;
import cn.practice.myapplication.bean.PlayMode;
import cn.practice.myapplication.util.MusicUtils;
import cn.practice.myapplication.util.ShareUtils;

public class MusicPlayerService extends Service {

    private List<MusicItem> musicList;
    private int position;

    private int playMode = PlayMode.LOOP.getIndex();

    private MusicItem currentMusic;

    private MediaPlayer mediaPlayer;

    private NotificationManager notificationManager;

    private boolean isPlaying = false;
    public static final String MUSIC_CHANGE_ACTION = "cn.practice.musicplayer.MUSIC_CHANGE_ACTION";

    public MusicPlayerService() {
    }

    private IMusicPlayerService.Stub stub = new IMusicPlayerService.Stub(){

        MusicPlayerService service = MusicPlayerService.this;



        @Override
        public void openMediaPlayer(int position) throws RemoteException {
            service.openMediaPlayer(position);
        }

        @Override
        public void playMusic() throws RemoteException {
            service.playMusic();
        }

        @Override
        public void pauseMusic() throws RemoteException {
            service.pauseMusic();
        }

        @Override
        public void stopMusic() throws RemoteException {
            service.stopMusic();
        }

        @Override
        public int getCurrentPosition() throws RemoteException {
            return service.getCurrentPosition();
        }

        @Override
        public int getMusicPosition() throws RemoteException {
            return service.getMusicPosition();
        }

        @Override
        public void nextMusic() throws RemoteException {
            service.nextMusic();
        }

        @Override
        public void previousMusic() throws RemoteException {
            service.previousMusic();
        }

        @Override
        public void setPlayMode(int playMode) throws RemoteException {
            service.setPlayMode(playMode);
        }

        @Override
        public int getPlayMode() throws RemoteException {
            return service.getPlayMode();
        }

        @Override
        public boolean getIsPlaying() throws RemoteException {
            return service.getIsPlaying();
        }

        @Override
        public String getMusicName() throws RemoteException {
            return currentMusic.getName();
        }

        @Override
        public String getSingerName() throws RemoteException {
            return currentMusic.getSinger();
        }

        @Override
        public int getDuration() throws RemoteException {
            return currentMusic.getDuration();
        }

        @Override
        public int getCurrDuration() throws RemoteException {
            return mediaPlayer.getCurrentPosition();
        }

        @Override
        public String getCurMusicPath() throws RemoteException {
            return service.getCurMusicPath();
        }

        @Override
        public void changeMusic(int position) throws RemoteException {
            service.changeMusic(position);
        }

        @Override
        public void getMusicList() throws RemoteException {
            service.getMusicList();
        }

        @Override
        public void seekTo(int position) throws RemoteException {
            service.seekTo(position);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 加载音乐列表
        getMusicList();

        playMode = ShareUtils.getPlayMode(this, "play_mode");

    }

    private void getMusicList() {
        musicList = new MusicUtils().scanLocalMusic(MusicPlayerService.this.getContentResolver());
    };

    // 打开媒体播放器
    private void openMediaPlayer(int position) {
        this.position = position;

            getMusicList();
            currentMusic = musicList.get(position);

            if (mediaPlayer != null) {
                mediaPlayer.reset();
            }

            mediaPlayer = new MediaPlayer();

            try {

                mediaPlayer.setDataSource(currentMusic.getPath());

                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        // 获取歌曲信息
                        notifyMusicInfoChange(MUSIC_CHANGE_ACTION);
                        playMusic();
                    }
                });

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        if(playMode == PlayMode.SINGLE.getIndex())
                            playMusic();
                        else
                            nextMusic();
                    }
                });

                mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        nextMusic();
                        return true;
                    }
                });

                mediaPlayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
            }

    }

    // 通知音乐信息变化
    private void notifyMusicInfoChange(String action) {
        Intent intent = new Intent(action);
        EventBus.getDefault().post(intent);
    }




    // 播放音乐
    private void playMusic() {
        mediaPlayer.start();
        isPlaying = true;

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(getApplicationContext(), MusicPlayerAct.class);
        // 状态：用以区分是否是从通知栏进入播放页面
        intent.putExtra("Notification", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String channelId = "xhf_music_player_channel";
        NotificationChannel channel = new NotificationChannel(channelId, "xhf音乐播放器", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);
        Notification.Builder builder = new Notification.Builder(getApplicationContext(), channelId);


        Notification notification = builder
                .setContentTitle("xhf音乐播放器")
                .setContentText("正在播放: " + currentMusic.getName() + " - " + currentMusic.getSinger())
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();

        notificationManager.notify(1, notification);
    };


    private void pauseMusic() {
        notificationManager.cancel(1);
        mediaPlayer.pause();
        isPlaying = false;
    }

    private void stopMusic() {
        mediaPlayer.stop();
        isPlaying = false;
    }

    private void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    // 获取当前播放进度
    private int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    // 获取音乐名
    private int getMusicPosition() {
        return position;
    }

    // 下一曲
    private void nextMusic() {
        Toast.makeText(this, PlayMode.getPlayMode(playMode).getMode(), Toast.LENGTH_SHORT).show();
        if (playMode != PlayMode.RANDOM.getIndex())
            position = (position + 1) % musicList.size();
        else{
            int currentPosition = 0;
            while (currentPosition == position)
                currentPosition = MusicUtils.getRandomPosition(musicList.size());
            position = currentPosition;
        }

        openMediaPlayer(position);
    }

    // 上一曲
    private void previousMusic() {
        if(position == 0) Toast.makeText(this, "已经是第一首歌了", Toast.LENGTH_SHORT).show();
        else{
            position -= 1;
            openMediaPlayer(position);
        }
    }

    // 设置播放模式
    private void setPlayMode(int playMode) {
        this.playMode = playMode;
    }

    // 获取播放模式
    private int getPlayMode() {
        return playMode;
    }

    private boolean getIsPlaying(){
        return isPlaying;
    }

    public String getMusicName(){
        return currentMusic.getName();
    }

    public String getSingerName(){
        return currentMusic.getSinger();
    }

    public int getDuration(){
        return mediaPlayer.getDuration();
    }

    public int getCurrDuration(){
        return mediaPlayer.getCurrentPosition();
    }

    public String getCurMusicPath(){
        return currentMusic.getPath();
    }

    public void changeMusic(int position){
        this.position = position;
        openMediaPlayer(position);
    }

}