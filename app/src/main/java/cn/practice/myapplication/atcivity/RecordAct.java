package cn.practice.myapplication.atcivity;

import static android.app.PendingIntent.getActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.xutils.x;
import cn.practice.myapplication.R;
import cn.practice.myapplication.bean.MusicItem;
import cn.practice.myapplication.util.IconUtils;
import cn.practice.myapplication.util.MusicUtils;
import cn.practice.myapplication.util.ShareUtils;

public class RecordAct extends AppCompatActivity {

    @ViewInject(R.id.record_list_view)
    public ListView listview;

    @ViewInject(R.id.record_back)
    public TextView record_back;

    public ArrayList<MusicItem> recordList;

    public List<MusicItem> localList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        x.view().inject(this);

        IconUtils.setFontIcon(getApplicationContext(), record_back, "\ue634");

        record_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        initData();
    }

    public void initData() {
        recordList = new ArrayList<>();
        localList = new MusicUtils().scanLocalMusic(getApplicationContext().getContentResolver());
        Set<String> songsPlayedSet = ShareUtils.getSongsPlayedSet(getApplicationContext());
        for(String songsPlayed : songsPlayedSet){
            for(MusicItem musicItem : localList){
                if(musicItem.getName().equals(songsPlayed)){
                    recordList.add(musicItem);
                }
            }
        }




        ListmusicListAdapter listmusicListAdapter = new ListmusicListAdapter();
        listview.setAdapter(listmusicListAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MusicItem musicItem = recordList.get(i);
                Intent intent = new Intent(getApplicationContext(), MusicPlayerAct.class);
                intent.putExtra("name", musicItem.getName());

                startActivity(intent);
            }
        });
    }

    private class ListmusicListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return recordList.size();
        }

        @Override
        public Object getItem(int i) {
            return recordList.get(i);
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

            MusicItem musicItem = recordList.get(i);
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