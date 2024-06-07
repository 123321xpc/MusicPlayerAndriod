package cn.practice.myapplication.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.util.ArrayList;

import cn.practice.myapplication.R;
import cn.practice.myapplication.bean.Lyric;
import cn.practice.myapplication.util.LyricUtils;

public class LyricView extends androidx.appcompat.widget.AppCompatTextView {

    private ArrayList<Lyric> lyrics = new ArrayList<>();

    // 控件的宽和高
    private int width;
    private int height;

    private int index = 0;

    private Paint paint;

    private Paint subPaint; //白画笔
    private float lineHeight = 30;

    private float currentPosition;
    private float time;
    private float sleepTime;

    public LyricView(Context context) {
        super(context);
        initView();
    }

    public LyricView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public LyricView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    private void initView() {
        paint = new Paint();
        paint.setColor(ContextCompat.getColor(getContext(), R.color.theme_color));
        paint.setTextSize(lineHeight);
        paint.setAntiAlias(true); // 抗锯齿
        paint.setTextAlign(Paint.Align.CENTER); // 居中对齐

        subPaint = new Paint();
        subPaint.setColor(Color.WHITE);
        subPaint.setTextSize(lineHeight);
        subPaint.setAntiAlias(true); // 抗锯齿
        subPaint.setTextAlign(Paint.Align.CENTER); // 居中对齐

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(lyrics!= null && lyrics.size() > 0){

            // 歌词上移
            float plush = 0;
            if(sleepTime == 0) plush = 0;
            else{
                float delta = (float)((currentPosition - time) / sleepTime * lineHeight);
                plush = (float)(lineHeight) + delta;
                canvas.translate(0, -plush);
            }

            // 绘制当前歌词
            String content = lyrics.get(index).getContent();
            canvas.drawText(content, (float)width / 2, (float)height / 2, paint);

            // 绘制之前歌词
            float Y = (float)height / 2;
            for(int i=index-1;i>=0;i--) {
                Lyric lyric = lyrics.get(i);
                String text = lyric.getContent();
                Y -= lineHeight * 1.5;
                if(Y < 0) break;

                canvas.drawText(text, (float)width / 2, Y, subPaint);
            }

            // 绘制之后歌词
            Y = (float)height / 2;
            for(int i=index+1;i<lyrics.size();i++) {
                Lyric lyric = lyrics.get(i);
                String text = lyric.getContent();
                Y += lineHeight * 1.5;
                if(Y > height) break;

                canvas.drawText(text, (float)width / 2, Y, subPaint);
            }
        }else{
            canvas.drawText("暂无歌词，若本地有歌词文件，请重命名为歌曲名", (float)width / 2, (float)height / 2, paint);
        }
    }

    public void setNextLyric(int currentPosition) {
        this.currentPosition = currentPosition;
        if(lyrics == null || lyrics.size() == 0) {
            invalidate();
            return;
        }

        for(int i=1;i<lyrics.size();i++) {
            if(currentPosition < lyrics.get(i).getTime()){
                // tmpIndex 指向当前歌词的前一句
                int tmpIndex = i - 1;

                // 如果前一句歌词的时间恰好大于current，说明i为正在唱的歌词
                if(currentPosition >= lyrics.get(tmpIndex).getTime()){
                    index = tmpIndex;
                    sleepTime = lyrics.get(index).getSleepTime();
                    time = lyrics.get(index).getTime();
                    invalidate();
                    break;
                }
            }
        }
    }

    public void setLyrics(ArrayList<Lyric> lyrics) {
        this.lyrics = lyrics;
    }
}
