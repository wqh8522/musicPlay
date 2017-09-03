package com.wqh.musicplayer.web;

import android.os.Handler;
import android.os.Message;

import com.wqh.musicplayer.bean.musicInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by wqh on 2016/12/29.
 */

public class WebMusic {

    //    搜索歌曲名或歌手
//    http://tingapi.ting.baidu.com/v1/restserver/ting?from=
//    // webapp_music&method=baidu.ting.search.catalogSug&format=json&callback=&query=关键字&_=时间戳
    private String searchName;
    public String string = "http://tingapi.ting.baidu.com/v1/restserver/ting?" +
            "from=webapp_music&method=baidu.ting.search.catalogSug&format=json&callback=&query=";
    //    private  String string = "http://tingapi.ting.baidu.com/v1/restserver/ting?size=20&type=2&callback=cb_list&_t=1468380543284&format=json&method=baidu.ting.billboard.billList";
    private URL url;
    private List<musicInfo> musicInfoList = new ArrayList<musicInfo>();
    private web web;
    private String adress;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    web.CompleteListener(musicInfoList);
                    break;
            }
        }
    };
    public void setListner(web web) {
        this.web = web;
    }
    public WebMusic() {
    }

    public WebMusic(String name) {
        this.searchName = name;

        try {
            searchName = URLEncoder.encode(searchName.trim(), "UTF-8");
//            searchName = new String(searchName.getBytes("UTF-8"));
            string = string + searchName + "&_=" + new Date().getTime();
            url = new URL(string);
            sendRequestWitchHttpSearchForName();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendRequestWitchHttpSearchForName() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection connection;
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(60 * 1000);
                    connection.setReadTimeout(60 * 1000);
                    connection.connect();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String s;
                    while ((s = reader.readLine()) != null) {
                        parseJsonWithJSONObject(s);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private List<musicInfo> parseJsonWithJSONObject(String json) throws Exception {
        JSONObject jsonObject = null;
        try {
            //去掉括号
            json = json.replace("(", "");
            json = json.replace(")", "");
            jsonObject = new JSONObject(json);
            JSONArray jsonArray = new JSONArray(jsonObject.getString("song"));

            for (int i = 0; i < jsonArray.length(); i++) {
                musicInfo music = new musicInfo();
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                music.setTitle(jsonObject1.getString("songname"));
                music.setArtist(jsonObject1.getString("artistname"));
                music.setMusicId(Long.parseLong(jsonObject1.getString("songid")));
                music.setUrl(getMusicAdress(jsonObject1.getString("songid")));
                musicInfoList.add(music);
            }
            Message msg = new Message();
            msg.what = 2;
            handler.sendMessage(msg);
        }catch (Exception e){
            e.printStackTrace();
        }
        return musicInfoList;
    }
    public String getMusicAdress(final String songid){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection connection;
                    URL url = new URL("http://ting.baidu.com/data/music/links?songIds="+songid);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(60*1000);
                    connection.setReadTimeout(60*1000);
                    connection.connect();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String s;
                    if ((s=reader.readLine())!=null){
                        s = s.replace("\\","");//去掉\\
                        try {
                            JSONObject object = new JSONObject(s);
                            JSONObject object1 = object.getJSONObject("data");
                            JSONArray array = object1.getJSONArray("songList");
                            JSONObject object2 = array.getJSONObject(0);
                            adress = object2.getString("songLink");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        try {
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return adress;
    }
}