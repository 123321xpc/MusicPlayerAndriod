package cn.practice.myapplication.util;

import com.orhanobut.logger.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import cn.practice.myapplication.bean.Lyric;

public class LyricUtils {

    private ArrayList<Lyric> lyricList = new ArrayList<>();

    public void readLyricFile(File file) {
        if(file == null ||!file.exists()) {

        }else{
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String line = "";
                while((line = reader.readLine())!= null) {
                    parseLyricLine(line);
                }

                // 计算每一句歌词高亮时间
                for(int i = 0; i < lyricList.size(); i++) {
                    Lyric lyric = lyricList.get(i);
                    if(i < lyricList.size() - 1) {
                        Lyric nextLyric = lyricList.get(i+1);
                        lyric.setSleepTime(nextLyric.getTime() - lyric.getTime());
                    }
                }


                Logger.d(lyricList.toString());

                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 解析歌词行
    private void parseLyricLine(String line) {
        // 存在返回位置，没有返回-1
        int pos1 = line.indexOf("[");
        int pos2 = line.indexOf("]");

        if(pos1 == 0 && pos2 == 9){  // 时间标签
            Lyric lyric = new Lyric();
            lyric.setContent(line.substring(pos2+1));

            lyric.setTime(stringToInt(line.substring(1, pos2)));

            lyricList.add(lyric);
        }
    }

    private int stringToInt(String times) {
        int res = -1;
        try{
            String s1[] =times.split(":");
            String s2[] = s1[1].split("\\.");

            int minute = Integer.parseInt(s1[0]);
            int second = Integer.parseInt(s2[0]);
            int millisecond = Integer.parseInt(s2[1]);
            res = minute * 60 * 1000 + second * 1000 + millisecond;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            return res;
        }
    }

    public ArrayList<Lyric> getLyricList() {
        return lyricList;
    }


}
