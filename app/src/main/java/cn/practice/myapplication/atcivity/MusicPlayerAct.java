package cn.practice.myapplication.atcivity;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import cn.practice.myapplication.R;
import cn.practice.myapplication.bean.MusicItem;
import cn.practice.myapplication.bean.PlayMode;
import cn.practice.myapplication.service.IMusicPlayerService;
import cn.practice.myapplication.service.MusicPlayerService;
import cn.practice.myapplication.util.IconUtils;
import cn.practice.myapplication.util.LyricUtils;
import cn.practice.myapplication.util.MusicUtils;
import cn.practice.myapplication.util.ShareUtils;
import cn.practice.myapplication.view.LyricView;

public class MusicPlayerAct extends AppCompatActivity {
    @ViewInject(R.id.tv_time)
    private TextView time;
    @ViewInject(R.id.tv_mode)
    private TextView mode;
    @ViewInject(R.id.tv_last)
    private TextView previous;
    @ViewInject(R.id.tv_play)
    private TextView play;
    @ViewInject(R.id.tv_next)
    private TextView next;
    @ViewInject(R.id.tv_list)
    private TextView list;
    @ViewInject(R.id.tv_seekbar)
    private SeekBar seekBar;
    @ViewInject(R.id.tv_name)
    private TextView MusicName;
    @ViewInject(R.id.tv_singer)
    private TextView singer;
    @ViewInject(R.id.tv_lyric)
    private LyricView lyricView;
    private BottomSheetDialog bottomSheetDialog;
    List<MusicItem> localMusicList;
    private ListView listView;
    private int position;
    private int playMode;
    private boolean comeFromNotification;
    private IMusicPlayerService service;
    private static final int PROGRESS_UPDATE = 1;
    private static final int GET_ALL_LOCAL_MUSIC =4;
    private static final int SHOW_LYRIC = 2;
    private static final int GET_LYRIC = 3;
    private ServiceConnection connection = new ServiceConnection() {
        // 绑定服务成功时回调
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            service = IMusicPlayerService.Stub.asInterface(iBinder);
            if(service!= null){
                try {
                    if(!comeFromNotification) service.openMediaPlayer(position);
                    else{
                        showData();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        // 解绑服务时回调
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            try {
                if(service!= null){
                    service.stopMusic();
                    service = null;
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };
    private void showData() {
        try{
            singer.setText(service.getSingerName());
            MusicName.setText(service.getMusicName());
            seekBar.setMax(service.getDuration());
            handler.sendEmptyMessage(PROGRESS_UPDATE);
        }catch(Exception e){
            e.printStackTrace();
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        x.view().inject(this);
        bindAndStartService();
        initData();
        initView();
    }
    private void bindAndStartService() {
        Intent intent = new Intent(this, MusicPlayerService.class);
        // 绑定服务
        intent.setAction("cn.practice.musicplayer.OPEN_MUSIC_PLAYER");
        bindService(intent, connection, BIND_AUTO_CREATE);
        // 启动服务
        startService(intent);
    }

    public void initData() {
        comeFromNotification = getIntent().getBooleanExtra("Notification", false);
        localMusicList = new MusicUtils().scanLocalMusic(getApplicationContext().getContentResolver());
        if(!comeFromNotification) {
            String name = getIntent().getStringExtra("name");
            if (name != null) {
                for (int i=0;i<localMusicList.size();i++) {
                    MusicItem musicItem = localMusicList.get(i);
                    if (musicItem.getName().equals(name)) {
                        position = i;
                        break;
                    }
                }

            }else{
                position = getIntent().getIntExtra("position", 0);
            }
        }
        playMode = ShareUtils.getPlayMode(this, "play_mode");
        // 注册
        EventBus.getDefault().register(this);
        // 发消息：更新歌词
        handler.sendEmptyMessage(GET_LYRIC);
        // 发消息: 开始歌词同步
        handler.sendEmptyMessage(SHOW_LYRIC);
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void ChangeMusic(Intent intent){
        String action = intent.getAction();
        if (MusicPlayerService.MUSIC_CHANGE_ACTION.equals(action)) {
            try{
                // 发消息：更新歌词
                handler.sendEmptyMessage(GET_LYRIC);
                // 发消息: 开始歌词同步
                handler.sendEmptyMessage(SHOW_LYRIC);
                // 记录播放历史
                ShareUtils.setSongNamePlayed(getApplicationContext(), service.getMusicName());

                showData();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                if(msg.what == PROGRESS_UPDATE){
                    int currentPosition = service.getCurrentPosition();
                    int duration = service.getDuration();
                    seekBar.setProgress(currentPosition);
                    time.setText(MusicUtils.formatDuration(currentPosition) + "/" + MusicUtils.formatDuration(duration));
                    // 每秒更新一次进度
                    handler.removeMessages(PROGRESS_UPDATE);
                    handler.sendEmptyMessageDelayed(PROGRESS_UPDATE, 1000);
                }
                else if(msg.what == SHOW_LYRIC){
                    int currentPosition = service.getCurrentPosition();
                    lyricView.setNextLyric(currentPosition);

                    handler.removeMessages(SHOW_LYRIC);
                    handler.sendEmptyMessage(SHOW_LYRIC);
                }
                else if(msg.what == GET_LYRIC){
                    LyricUtils lyricUtils = new LyricUtils();

                    String songPath = service.getCurMusicPath();
                    String lyricPath = songPath.replace(".mp3", ".lrc");
                    File file = new File(lyricPath);
                    lyricUtils.readLyricFile(file);
                    lyricView.setLyrics(lyricUtils.getLyricList());
                }
                else if(msg.what == GET_ALL_LOCAL_MUSIC){
                    bottomSheetDialog = new BottomSheetDialog(MusicPlayerAct.this);
                    bottomSheetDialog.setContentView(R.layout.drawer_view);
                    ListmusicListAdapter adapter = new ListmusicListAdapter();
                    listView = bottomSheetDialog.findViewById(R.id.list_music);
                    listView.setAdapter(adapter);
                    bottomSheetDialog.setCancelable(true);
                    list.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            bottomSheetDialog.show();
                        }
                    });

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            try {
                                service.changeMusic(i);
                            } catch (RemoteException e) {
                                Toast.makeText(MusicPlayerAct.this, "获取歌曲失败, 请重试!", Toast.LENGTH_SHORT).show();
                            }finally {
                                bottomSheetDialog.dismiss();
                            }
                        }
                    });

                }

            }catch (Exception e){e.printStackTrace();}

        }
    };

    public void initView() {
        IconUtils.setFontIcon(getApplicationContext(), mode, PlayMode.getPlayMode(playMode).getIcon());
        IconUtils.setFontIcon(getApplicationContext(), previous, "\ue78a");
        IconUtils.setFontIcon(getApplicationContext(), play, "\ue633");
        IconUtils.setFontIcon(getApplicationContext(), next, "\ue7a5");
        IconUtils.setFontIcon(getApplicationContext(), list, "\ue86a");

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(service!= null) {
                    try {
                        if(service.getIsPlaying()){      // 正在播放
                            service.pauseMusic();
                            // 图标换成播放图标
                            play.setText("\ue632");
                        }else{                       // 暂停播放
                            service.playMusic();
                            // 图标换成播放图标
                            play.setText("\ue633");
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int index = (playMode + 1) % PlayMode.values().length;
                PlayMode currMode = PlayMode.getPlayMode(index);

                playMode = index;
                mode.setText(currMode.getIcon());
                Toast.makeText(getApplicationContext(), currMode.getMode(), Toast.LENGTH_SHORT).show();

                try {
                    service.setPlayMode(index);
                    ShareUtils.setPlayMode(MusicPlayerAct.this, "play_mode", playMode);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    service.previousMusic();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    service.nextMusic();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        // 设置进度条拖动
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                try{
                    if(b){
                        service.seekTo(i);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        handler.sendEmptyMessage(GET_ALL_LOCAL_MUSIC);
    }
    @Override
    protected void onDestroy() {
        // 停止发送消息
        handler.removeCallbacksAndMessages(null);
        // 解绑服务
        if(connection!= null){
            unbindService(connection);
            connection = null;
        }
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private class ListmusicListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return localMusicList.size();
        }

        @Override
        public Object getItem(int i) {
            return localMusicList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view= getLayoutInflater().inflate((R.layout.list_local_music), null);
                viewHolder = new ViewHolder();
                viewHolder.musicName = (TextView) view.findViewById(R.id.list_music_name);
                viewHolder.musicSinger = (TextView) view.findViewById(R.id.list_music_singer);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            MusicItem musicItem = localMusicList.get(i);
            viewHolder.musicName.setText(musicItem.getName());
            viewHolder.musicSinger.setText("歌手: " + musicItem.getSinger());

            return view;
        }
    }

    class ViewHolder{
        TextView musicName;
        TextView musicSinger;
    }
}