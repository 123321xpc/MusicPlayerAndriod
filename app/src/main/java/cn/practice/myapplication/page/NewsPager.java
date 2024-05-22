package cn.practice.myapplication.page;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;

import cn.practice.myapplication.R;
import cn.practice.myapplication.bean.NewsItem;

public class NewsPager extends BasePager {

    private ArrayList<NewsItem> newsList = new ArrayList<>();

    @ViewInject(R.id.news_list)
    private ListView newsListView;
    private static final int DISPLAY_NEWS = 1;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DISPLAY_NEWS:
                    newsListView.setAdapter(new NewsListAdapter());
                    break;
            }
        }
    };


    public void initData() {
        RequestParams params = new RequestParams("http://v.juhe.cn/toutiao/index");
        params.addHeader("Content-Type", "application/x-www-form-urlencoded");
        params.addQueryStringParameter("type", "yule");
        params.addQueryStringParameter("key", "bbebe4c0291723b3a2ca55be1d3f5433");

        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String data) {

                int index = 0;

                while (true) {
                    int titleIndex = data.indexOf("\"title\":\"", index);
                    if (titleIndex == -1) break;  // 没有更多的标题
                    titleIndex += "\"title\":\"".length();
                    int titleEndIndex = data.indexOf("\"", titleIndex);
                    String title = data.substring(titleIndex, titleEndIndex);

                    int urlIndex = data.indexOf("\"url\":\"", titleEndIndex);
                    urlIndex += "\"url\":\"".length();
                    int urlEndIndex = data.indexOf("\"", urlIndex);
                    String url = data.substring(urlIndex, urlEndIndex);

                    NewsItem newsItem = new NewsItem(title, url);
                    newsList.add(newsItem);

                    index = urlEndIndex;  // 更新索引，继续寻找下一个
                }

                Logger.d("newsList: " + newsList.toString());
                handler.sendEmptyMessage(DISPLAY_NEWS);  // 通知主线程更新

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                System.out.println("Error: " + ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {
                System.out.println("Cancelled");
            }

            @Override
            public void onFinished() {
                System.out.println("Request finished");
            }
        });
    }

    // onCreateView 方法用于创建 Fragment 的视图
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 使用布局文件来定义 Fragment 的视图
        rootView = inflater.inflate(R.layout.fragment_news, container, false);
        x.view().inject(this, rootView);
        context = getActivity();
        initData();
        initView();
        return rootView;
    }

    private void initView() {
        newsListView.setOnItemClickListener((parent, view, position, id) -> {
            NewsItem newsItem = newsList.get(position);
            // 跳转到新闻详情页
            // 解析网址并启动意图
            Logger.d("newsItem: " + newsItem.toString());
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(newsItem.getUrl()));

            // 获取 Package Manager
            PackageManager packageManager = context.getPackageManager();

            // 使用 resolveActivity() 方法检查
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent);
            } else {
                // 没有应用可以处理这个意图
                Toast.makeText(getContext(), "No application to handle URL", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 自定义适配器
    private class NewsListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return newsList.size();
        }

        @Override
        public Object getItem(int i) {
            return newsList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = View.inflate(context, R.layout.item_news, null);
                viewHolder = new ViewHolder();
                viewHolder.title = (TextView)view.findViewById(R.id.news_title);
                viewHolder.index = (TextView)view.findViewById(R.id.news_index);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            NewsItem newsItem = newsList.get(i);
            viewHolder.title.setText(newsItem.getTitle());
            viewHolder.index.setText(String.valueOf(i+1) + " | ");
            return view;
        }
    }

    // ViewHolder 用于显示控件
    class ViewHolder{

        TextView index;

        TextView title;

    }

}
