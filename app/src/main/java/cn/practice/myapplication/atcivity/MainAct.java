package cn.practice.myapplication.atcivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import org.xutils.x;
import org.xutils.view.annotation.ViewInject;

import cn.practice.myapplication.R;
import cn.practice.myapplication.page.LocalMusicPager;
import cn.practice.myapplication.page.MePager;
import cn.practice.myapplication.page.NetMusicPager;
import cn.practice.myapplication.page.NewsPager;
import cn.practice.myapplication.util.IconUtils;

public class MainAct extends AppCompatActivity {


    @ViewInject(R.id.local_music_btn)
    private RadioButton localMusicBtn;
    @ViewInject(R.id.net_music_btn)
    private RadioButton netMusicBtn;
    @ViewInject(R.id.me_btn)
    private RadioButton me_btn;

    @ViewInject(R.id.news_btn)
    private RadioButton news_btn;

    @ViewInject(R.id.search_icon)
    private TextView search_btn;

    @ViewInject(R.id.record_icon)
    private TextView record;

    @ViewInject(R.id.search)
    private EditText search_el;

    private FragmentManager fragmentManager;

    @ViewInject(R.id.botton_ui_rg)
    private RadioGroup radioGroup;

    private String TAG;

    @ViewInject(R.id.title_bar)
    private LinearLayout title_bar;

    public static final String SEARCH_MUSIC  = "com.example.SEARCH_MUSIC";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        x.view().inject(this);

        initView();

        changeFragment(new LocalMusicPager());
        radioGroup.check(R.id.local_music_btn); // 默认选择本地音乐
        TAG = LocalMusicPager.class.getSimpleName();
        changeBtnBg(R.id.local_music_btn);

        // 点击按钮切换页面
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 切换按钮背景
                changeBtnBg(checkedId);

                // 切换页面
                switch (checkedId)
                {
                    case R.id.local_music_btn:
                        changeFragment(new LocalMusicPager());
                        TAG = LocalMusicPager.class.getSimpleName();
                        break;
                    case R.id.net_music_btn:
                        changeFragment(new NetMusicPager());
                        TAG = NetMusicPager.class.getSimpleName();
                        break;
                    case R.id.me_btn:
                        changeFragment(new MePager());
                        TAG = MePager.class.getSimpleName();
                        break;
                    case R.id.news_btn:
                        changeFragment(new NewsPager());
                        TAG = NewsPager.class.getSimpleName();
                        break;
                    default:
                        break;
                }


                if(!TAG.equals(LocalMusicPager.class.getSimpleName()) ){
                    title_bar.setVisibility(View.GONE);
                }else{
                    title_bar.setVisibility(View.VISIBLE);
                }
            }
        });

    }



    public void changeFragment(Fragment fragment){
        fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fl_container);
        if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
            // 如果当前显示的页面就是要切换的页面，则不执行后续的页面切换逻辑
            return;
        }

        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.fl_container, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    // 切换按钮背景
    public void changeBtnBg(int checkId) {
        for(int i=0;i<radioGroup.getChildCount();i++){

            RadioButton childAt = (RadioButton) radioGroup.getChildAt(i);

            if(childAt.getId() == checkId)
                childAt.setBackgroundResource(R.drawable.btn_selected);
            else
                childAt.setBackgroundResource(R.drawable.btn_unselected);
        }
    }

    public void initView() {
        IconUtils.setFontIcon(getApplicationContext(), record, "\ue648");
        IconUtils.setFontIcon(getApplicationContext(), search_btn, "\ue625");
        IconUtils.setFontIcon(getApplicationContext(), localMusicBtn, "\ue6ab" + "本地音乐");
        IconUtils.setFontIcon(getApplicationContext(), netMusicBtn, "\ue694" + "在线音乐");
        IconUtils.setFontIcon(getApplicationContext(), me_btn, "\ue626" + "我 的");
        IconUtils.setFontIcon(getApplicationContext(), news_btn, "\ue7c6" + "资 讯");


        record.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainAct.this, RecordAct.class);
                startActivity(intent);
            }
        });

        search_el.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int i, KeyEvent event) {
                // 在这里执行按下回车键后的操作
                String searchText = search_el.getText().toString();
                performSearch(searchText);  // 例如执行搜索操作
                return true;
            }
        });

        search_btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                performSearch(search_el.getText().toString());
            }
        });

    }

    private void performSearch(String searchText) {
        Intent intent = new Intent(SEARCH_MUSIC);
        intent.putExtra("search_content", searchText);
        sendBroadcast(intent);
    }


}