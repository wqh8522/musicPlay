package com.wqh.musicplayer.music;

import com.wqh.musicplayer.bean.Lrc;
import com.wqh.musicplayer.bean.lrcUtil;
import com.wqh.musicplayer.bean.musicInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wqh on 2016/12/26.
 */

public class LrcProcess {


    private long currentTime = 0;//存放临时时间
    private String currentContent = null;//存放临时歌词
    private Lrc lrc;

    private List<lrcUtil> lrcMsg = new ArrayList<lrcUtil>();

    private InputStream readLrcFile(String path) throws FileNotFoundException {
        File f = new File(path);
        if(!f.exists()){
            return null;
        }
        InputStream ins = new FileInputStream(f);
        return ins;

    }
    public Lrc parser(musicInfo music) throws Exception {
        lrc = new Lrc();
        String path = music.getUrl();
        if (path.contains("Download")) {
            path = path.substring(0, path.indexOf("c/") + 1);
            path = path + "/Lyric/" + music.getArtist() + " - " + music.getName();
        }else if ( path.contains("cloudmusic")){
            path = path.substring(0,path.indexOf("0/")+1);
            path = path +"/Music/Lyric/"+music.getName();
        }else {
            return null;
        }
        path = path.replace(".mp3", ".lrc");
        InputStream in = readLrcFile(path);
        if(in == null){
            return null;
        }
        lrc = parser(in);
        return lrc;
    }
    public Lrc parser(InputStream inputStream) throws IOException {
        // 三层包装
        InputStreamReader inr = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(inr);
        // 一行一行的读，每读一行，解析一行
        String line = null;
        while ((line = reader.readLine()) != null) {
            parserLine(line);
        }
        // 全部解析完后，设置info
        lrc.setInfos(lrcMsg);
        return lrc;
    }
    private void parserLine(String str) {
        lrcUtil lrcUtil = new lrcUtil();
        // 取得歌曲名信息
        if (str.startsWith("[ti:")) {
            String title = str.substring(4, str.length() - 1);
            lrc.setTitle(title);
        }else if (str.startsWith("[ar:")) {
            // 取得歌手信息
            String singer = str.substring(4, str.length() - 1);
            lrc.setSinger(singer);
        }// 取得专辑信息
        else if (str.startsWith("[al:")) {
            String album = str.substring(4, str.length() - 1);
            lrc.setAlbum(album);
        } else {
            // 通过正则取得每句歌词信息
            // 设置正则规则
            String reg = "\\[(\\d{2}:\\d{2}\\.\\d{2})\\]";
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(str);
            // 如果存在匹配项，则执行以下操作
            while (matcher.find()) {
                // 得到匹配的所有内容
                String msg = matcher.group();
                // 得到这个匹配项开始的索引
                int start = matcher.start();
                // 得到这个匹配项结束的索引
                int end = matcher.end();

                // 得到这个匹配项中的组数
                int groupCount = matcher.groupCount();
                // 得到每个组中内容
                for (int i = 0; i <= groupCount; i++) {
                    String timeStr = matcher.group(i);
                    if (i == 1) {
                        // 将第二组中的内容设置为当前的一个时间点
                        currentTime = strToLong(timeStr);
                    }
                }
                // 得到时间点后的内容
                String[] content = pattern.split(str);
                // 输出数组内容
                for (int i = 0; i < content.length; i++) {
                    if (i == content.length - 1) {
                        // 将内容设置为当前内容
                        currentContent = content[i];
                    }
                }
                // 设置时间点和内容的映射
                if(currentContent!=null){
                    lrcUtil.setCurrTime(currentTime);
                    lrcUtil.setCurrLrc(currentContent);
                }
            }
            lrcMsg.add(lrcUtil);
        }
    }

//    将解析得到的表示时间的字符转化为Long型

    private long strToLong(String timeStr) {
        // 因为给如的字符串的时间格式为XX:XX.XX,返回的long要求是以毫秒为单位
        // 1:使用：分割 2：使用.分割
        String[] s = timeStr.split(":");
        int min = Integer.parseInt(s[0]);
        String[] ss = s[1].split("\\.");
        int sec = Integer.parseInt(ss[0]);
        int mill = Integer.parseInt(ss[1]);
        return min * 60 * 1000 + sec * 1000 + mill * 10;
    }
}