// IMusicPlayerService.aidl
package cn.practice.myapplication.service;


// Declare any non-default types here with import statements

interface IMusicPlayerService {
        // 打开媒体播放器
        void openMediaPlayer(int position);

        // 播放音乐
        void playMusic();

        void pauseMusic();

        void stopMusic();

        // 获取当前播放进度
        int getCurrentPosition() ;

        // 获取当前音乐下标
        int getMusicPosition() ;

        // 下一曲
        void nextMusic() ;

        // 上一曲
        void previousMusic() ;

        // 设置播放模式
        void setPlayMode(int mode) ;

        // 获取播放模式
        int getPlayMode() ;

        // 是否正在播放
        boolean getIsPlaying();

        String getMusicName();

        String getSingerName();

        int getDuration();

        int getCurrDuration();

        String getCurMusicPath();

        void changeMusic(int position);

        void getMusicList();

        // 拖动进度条
        void seekTo(int position);
}