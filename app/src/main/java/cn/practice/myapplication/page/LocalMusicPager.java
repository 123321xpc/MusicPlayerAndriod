package cn.practice.myapplication.page;


import static androidx.core.content.ContextCompat.registerReceiver;
import static cn.practice.myapplication.atcivity.MainAct.SEARCH_MUSIC;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import com.orhanobut.logger.Logger;

import org.xutils.x;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;

import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

import cn.practice.myapplication.R;
import cn.practice.myapplication.atcivity.MusicPlayerAct;
import cn.practice.myapplication.bean.MusicItem;
import cn.practice.myapplication.util.MusicUtils;

public class LocalMusicPager extends BasePager {

    @ViewInject(R.id.finding_layout)
    private LinearLayout searchingLayout;

    // 本地音乐列表
    List<MusicItem> localMusicList;

    @ViewInject(R.id.local_music_list)
    private ListView listView;

    @ViewInject(R.id.empty_text)
    private TextView emptyTextView;

    public Context context;

    private MusicListAdapter adapter;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            if(localMusicList!= null && localMusicList.size() > 0){
                // 显示本地音乐列表
                adapter = new MusicListAdapter();
                listView.setAdapter(adapter);
                // 隐藏空页面
                emptyTextView.setVisibility(View.GONE);

            }else{
                // 显示空页面
                emptyTextView.setVisibility(View.VISIBLE);
            }
            // 隐藏正在搜索提示
            searchingLayout.setVisibility(View.GONE);
        }

        ;
    };


    @Override
    public void initData() {

        localMusicList = new MusicUtils().scanLocalMusic(getActivity().getContentResolver());
        mHandler.sendEmptyMessage(0);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), MusicPlayerAct.class);
                intent.putExtra("name", localMusicList.get(i).getName());
                startActivity(intent);
            }
        });

        // 创建一个广播接收器实例
        MyBroadcastReceiver myReceiver = new MyBroadcastReceiver();

        // 创建一个 IntentFilter 来过滤特定的广播消息
        IntentFilter intentFilter = new IntentFilter(SEARCH_MUSIC);

        // 注册广播接收器
        registerReceiver(context.getApplicationContext(), myReceiver, intentFilter, ContextCompat.RECEIVER_VISIBLE_TO_INSTANT_APPS);
    }

    // onCreateView 方法用于创建 Fragment 的视图
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            context = getContext();

        // 使用布局文件来定义 Fragment 的视图
            rootView = inflater.inflate(R.layout.fragment_local_music, container, false);

            x.view().inject(this, rootView);

            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // 如果应用没有权限，则发起权限请求
                requestPermissionLauncher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE});
            } else {
                // 应用已经具有权限，可以执行需要读取音乐数据的操作
                initData();
            }




            return rootView;
        }
    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (result.containsValue(false)) {
                    // 用户拒绝了权限请求，您可以根据需要采取适当的措施
                } else {
                    initData();
                }
            });





    // 自定义适配器
    private class MusicListAdapter extends BaseAdapter {
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
                view = View.inflate(context, R.layout.item_local_music, null);
                viewHolder = new ViewHolder();
                viewHolder.musicImage = (ImageView) view.findViewById(R.id.music_item_img);
                viewHolder.musicName = (TextView) view.findViewById(R.id.music_name);
                viewHolder.musicSinger = (TextView) view.findViewById(R.id.music_singer);
                viewHolder.musicSize = (TextView) view.findViewById(R.id.music_size);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            MusicItem musicItem = localMusicList.get(i);
            viewHolder.musicImage.setImageResource(R.mipmap.music_item_img);
            viewHolder.musicName.setText(musicItem.getName());
            viewHolder.musicSinger.setText("歌手: " + musicItem.getSinger());
            String size = musicItem.getSize() + "MB";
            viewHolder.musicSize.setText(size);

            return view;
        }
    }

    // ViewHolder 用于显示控件
    class ViewHolder{

        ImageView musicImage;
        TextView musicName;
        TextView musicSinger;
        TextView musicSize;
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(SEARCH_MUSIC)) {
                String searchContent = intent.getStringExtra("search_content");

                if(searchContent == null || searchContent.equals("")){
                    localMusicList = new MusicUtils().scanLocalMusic(getActivity().getContentResolver());
                }else{

                    ArrayList<MusicItem> searchResult = new ArrayList<>();
                    for (MusicItem item : localMusicList) {
                        if (item.getName().contains(searchContent) || item.getSinger().contains(searchContent)) {
                            searchResult.add(item);
                        }
                    }

                    localMusicList = searchResult;
                }


                adapter.notifyDataSetChanged();

            }
        }
    }




}
