package cn.practice.myapplication.page;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xutils.x;

import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import org.xutils.view.annotation.ViewInject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import cn.practice.myapplication.R;
import cn.practice.myapplication.atcivity.RankAct;
import cn.practice.myapplication.bean.MusicItem;
import cn.practice.myapplication.bean.MusicListCallback;
import cn.practice.myapplication.bean.NetMusicItem;
import cn.practice.myapplication.util.MusicUtils;

public class NetMusicPager extends BasePager {

    private static final String HOT = "hot";
    private static final String NEW = "new";
    private static final String RISE = "rise";

    @ViewInject(R.id.hot_music_list)
    public ListView hotListView;
    @ViewInject(R.id.rise_music_list)
    public ListView riseListView;
    @ViewInject(R.id.new_music_list)
    public ListView newListView;

    @ViewInject(R.id.hot_music_image)
    public ImageView hotImageView;
    @ViewInject(R.id.new_music_image)
    public ImageView newImageView;
    @ViewInject(R.id.rise_music_image)
    public ImageView riseImageView;

    private ArrayList<String> newMusicList = new ArrayList<>();

    public List<MusicItem> localMusicList = new ArrayList<>();

    public MusicUtils musicUtils = new MusicUtils();

    private ArrayList<NetMusicItem> hotList = new ArrayList<>();
    private ArrayList<NetMusicItem> newList = new ArrayList<>();
    private ArrayList<NetMusicItem> riseList = new ArrayList<>();
    private ArrayList<NetMusicItem> allList = new ArrayList<>();


    public Handler handler = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MusicUtils.SHOW_MUSIC:
                    for (NetMusicItem item : allList) {
                        if (item.getCategory().equals(HOT) ) {
                            hotList.add(item);
                        } else if (item.getCategory().equals(NEW)) {
                            newList.add(item);
                        } else if (item.getCategory().equals(RISE)) {
                            riseList.add(item);
                        }
                    }

                    hotListView.setAdapter(new HotMusicListAdapter());
                    newListView.setAdapter(new NewMusicListAdapter());
                    riseListView.setAdapter(new RiseMusicListAdapter());

                    setEvents(hotListView, hotList);
                    setEvents(newListView, newList);
                    setEvents(riseListView, riseList);

                    break;
            }
        }
    };

    private void setEvents(ListView view, ArrayList<NetMusicItem> list) {
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                NetMusicItem item = list.get(i);

                for (String s : newMusicList) {
                    if (s.equals(item.getId())) {
                        Toast.makeText(context, "该歌曲已经在本地列表中了！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                for (MusicItem musicItem : localMusicList) {
                    if (musicItem.getName().equals(item.getName())) {
                        Toast.makeText(context, "该歌曲已经在本地列表中了！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                musicUtils.downloadMusicById(item.getFullname(), item.getId(), context, new MusicListCallback(){
                    @Override
                    public void onMusicListLoaded(ArrayList<NetMusicItem> musicList) {
                        newMusicList.add(item.getId());
                        musicUtils.downloadLyricById(item.getFullname(),item.getId(), context);
                    }

                });

            }
        });
    }

    @Override
    public void initData() {

        newMusicList = new ArrayList<>();

        localMusicList = musicUtils.scanLocalMusic(getActivity().getContentResolver());

        musicUtils.getNetMusicList(context,new MusicListCallback(){
            @Override
            public void onMusicListLoaded(ArrayList<NetMusicItem> musicList) {
                allList = musicList;
                handler.sendEmptyMessage(MusicUtils.SHOW_MUSIC);
            }

        });

    }

    // onCreateView 方法用于创建 Fragment 的视图
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 使用布局文件来定义 Fragment 的视图
        rootView = inflater.inflate(R.layout.fragment_net_music, container, false);
        x.view().inject(this, rootView);
        context = getActivity();
        initData();

        initView();

        return rootView;
    }

    private void initView() {

        // 给每个图片设置点击事件
        setClickForImageView(hotImageView, HOT);
        setClickForImageView(newImageView, NEW);
        setClickForImageView(riseImageView, RISE);
    }

    void setClickForImageView(ImageView imageView, String category) {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), RankAct.class);
                intent.putExtra("category", category);
                startActivity(intent);
            }
        });
    }

    // 自定义适配器
    private class HotMusicListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return hotList.size();
        }

        @Override
        public Object getItem(int i) {
            return hotList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = View.inflate(context, R.layout.list_net_music_item, null);
                viewHolder = new ViewHolder();
                viewHolder.index = (TextView) view.findViewById(R.id.net_music_index);
                viewHolder.musicName = (TextView) view.findViewById(R.id.net_music_name);
                viewHolder.musicSinger = (TextView) view.findViewById(R.id.net_music_singer);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            NetMusicItem musicItem = hotList.get(i);
            viewHolder.musicName.setText(musicItem.getName());
            viewHolder.musicSinger.setText("歌手: " + musicItem.getSinger());
            viewHolder.index.setText(String.valueOf(i + 1));

            return view;
        }
    }

    private class NewMusicListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return newList.size();
        }

        @Override
        public Object getItem(int i) {
            return newList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = View.inflate(context, R.layout.list_net_music_item, null);
                viewHolder = new ViewHolder();
                viewHolder.index = (TextView) view.findViewById(R.id.net_music_index);
                viewHolder.musicName = (TextView) view.findViewById(R.id.net_music_name);
                viewHolder.musicSinger = (TextView) view.findViewById(R.id.net_music_singer);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            NetMusicItem musicItem = newList.get(i);
            viewHolder.musicName.setText(musicItem.getName());
            viewHolder.musicSinger.setText("歌手: " + musicItem.getSinger());
            viewHolder.index.setText(String.valueOf(i + 1));

            return view;
        }
    }

    private class RiseMusicListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return riseList.size();
        }

        @Override
        public Object getItem(int i) {
            return riseList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = View.inflate(context, R.layout.list_net_music_item, null);
                viewHolder = new ViewHolder();
                viewHolder.index = (TextView) view.findViewById(R.id.net_music_index);
                viewHolder.musicName = (TextView) view.findViewById(R.id.net_music_name);
                viewHolder.musicSinger = (TextView) view.findViewById(R.id.net_music_singer);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            NetMusicItem musicItem = riseList.get(i);
            viewHolder.musicName.setText(musicItem.getName());
            viewHolder.musicSinger.setText("歌手: " + musicItem.getSinger());
            viewHolder.index.setText(String.valueOf(i + 1));

            return view;
        }
    }

    // ViewHolder 用于显示控件
    class ViewHolder{

        TextView index;
        TextView musicName;
        TextView musicSinger;
    }

}
