package cn.practice.myapplication.atcivity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import cn.practice.myapplication.R;
import cn.practice.myapplication.bean.MusicItem;
import cn.practice.myapplication.bean.MusicListCallback;
import cn.practice.myapplication.bean.NetMusicItem;
import cn.practice.myapplication.page.NetMusicPager;
import cn.practice.myapplication.util.IconUtils;
import cn.practice.myapplication.util.MusicUtils;

public class RankAct extends AppCompatActivity {

    @ViewInject(R.id.back_btn)
    private TextView back;

    @ViewInject(R.id.rank_title)
    private TextView title;

    @ViewInject(R.id.rank_list)
    private ListView rankListView;

    private String category;

    private ArrayList<NetMusicItem> musicList;

    private List<MusicItem> localMusicList;

    private List<String> newMusicList = new ArrayList<>();

    private MusicUtils musicUtils = new MusicUtils();

    private final static int SHOW_VIEW = 1;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case SHOW_VIEW:
                    rankListView.setAdapter(new MusicListAdapter());
                    setOnClick(rankListView);
                    break;
            }

        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);
        x.view().inject(this);
        category = getIntent().getStringExtra("category");
        if(category.equals("hot") ) title.setText("热歌榜");
        else if(category.equals("new")) title.setText("新歌榜");
        else title.setText("飙升榜");
        IconUtils.setFontIcon(this, back, "\ue634");
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        localMusicList = musicUtils.scanLocalMusic(getApplicationContext().getContentResolver());


        initData();

    }

    private void initData() {
        new MusicUtils().getMusicByCategory(getApplicationContext(), category, new MusicListCallback(){
            @Override
            public void onMusicListLoaded(ArrayList<NetMusicItem> res) {
                musicList = res;
                handler.sendEmptyMessage(SHOW_VIEW);
            }
        });
    }


    private void setOnClick(ListView listView){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                NetMusicItem item = musicList.get(i);

                for (String s : newMusicList) {
                    if (s.equals(item.getId())) {
                        Toast.makeText(getApplicationContext(), "该歌曲已经在本地列表中了！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                for (MusicItem musicItem : localMusicList) {
                    if (musicItem.getName().equals(item.getName())) {
                        Toast.makeText(getApplicationContext(), "该歌曲已经在本地列表中了！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                musicUtils.downloadMusicById(item.getFullname(), item.getId(), getApplicationContext(), new MusicListCallback(){
                    @Override
                    public void onMusicListLoaded(ArrayList<NetMusicItem> musicList) {
                        newMusicList.add(item.getId());
                        musicUtils.downloadLyricById(item.getFullname(),item.getId(), getApplicationContext());
                    }

                });

            }
        });
    }


    private class MusicListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return musicList.size();
        }

        @Override
        public Object getItem(int i) {
            return musicList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = View.inflate(getApplicationContext(), R.layout.list_net_music_item, null);
                viewHolder = new ViewHolder();
                viewHolder.index = (TextView) view.findViewById(R.id.net_music_index);
                viewHolder.musicName = (TextView) view.findViewById(R.id.net_music_name);
                viewHolder.musicSinger = (TextView) view.findViewById(R.id.net_music_singer);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            NetMusicItem musicItem = musicList.get(i);
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