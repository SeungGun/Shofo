package com.example.shortinfo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public StringBuilder str;
    private TextView textView1;
    private TextView textView2;
    private TextView textView3;
    private TextView textView4;
    private TextView textView5;
    private TextView textView6;
    private ArrayList<String> distanceList;
    private int defaultRegionNumber = 8; //경기도

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.activity_main);

        distanceList = new ArrayList<>();

        Intent intent = new Intent(getApplicationContext(), ScreenService.class);
        startService(intent);

        final Bundle bundle = new Bundle();

        textView1 = findViewById(R.id.corona_text_confirmed);
        textView2 = findViewById(R.id.corona_text_release);
        textView3 = findViewById(R.id.corona_text_dead);
        textView4 = findViewById(R.id.corona_std_date);
        textView5 = findViewById(R.id.corona_text_vaccine);
        textView6 = findViewById(R.id.corona_text_distancing);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Document coronaUrl = null;
                try {
                    coronaUrl = Jsoup.connect("http://ncov.mohw.go.kr/regSocdisBoardView.do?brdId=6&brdGubun=68&ncvContSeq=495").get();

                    Elements scripts = coronaUrl.getElementsByTag("script");
                    for(Element e : scripts){
                        if(e.data().contains("RSS_DATA")){
                            int idx_begin = e.data().indexOf("RSS_DATA");
                            String front = e.data().substring(idx_begin);

                            int idx_end = front.indexOf(";");

                            String cutStr = front.substring(0,idx_end+1);
                            cutStr = cutStr.replaceAll(" ","");
                            cutStr = cutStr.replace("\n","");

                            String[] temp = cutStr.split("\\{");

                            // index 0번은 "RSS_DATA = [" 이기 때문에 index 1번부터
                            for(int i=1; i<temp.length; ++i){
                                int be = temp[i].indexOf("-");
                                int end = temp[i].substring(be).indexOf("}");

                                String endStr = temp[i].substring(be, be+end);
                                endStr = endStr.replace("'","");
                                endStr = endStr.replaceAll("<br/>","");
                                distanceList.add(endStr);
                            }
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread() {
            @Override
            public void run() {
                Document doc = null;
                try {
                    doc = Jsoup.connect("https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query=%EC%BD%94%EB%A1%9C%EB%82%98").get();
                    Log.d("signal", "first");

                    Element contents = doc.select("div.status_info li.info_01").select(".info_num").first();
                    Log.d("확진자", contents.text());
                    bundle.putString("confirmed", contents.text());

                    contents = doc.select("div.status_info li.info_01").select("em.info_variation").first();
                    Log.d("▲", contents.text());
                    bundle.putString("confirmed_var", contents.text());

                    contents = doc.select("div.status_info li.info_02").select(".info_num").first();
                    Log.d("검사중", contents.text());
                    bundle.putString("inspection", contents.text());

                    contents = doc.select("div.status_info li.info_02").select("em.info_variation").first();
                    Log.d("▼", contents.text());
                    bundle.putString("inspection_var", contents.text());


                    contents = doc.select("div.status_info li.info_03").select(".info_num").first();
                    Log.d("격리해제", contents.text());
                    bundle.putString("release", contents.text());

                    contents = doc.select("div.status_info li.info_03").select("em.info_variation").first();
                    Log.d("▲", contents.text());
                    bundle.putString("release_var", contents.text());


                    contents = doc.select("div.status_info li.info_04").select(".info_num").first();
                    Log.d("사망자", contents.text());
                    bundle.putString("dead", contents.text());


                    contents = doc.select("div.status_info li.info_04").select("em.info_variation").first();
                    Log.d("▲", contents.text());
                    bundle.putString("dead_var", contents.text());

                    contents = doc.select("div.status_info.abroad_info li.info_01").select(".info_num").first();
                    Log.d("전 세계 확진자", contents.text());

                    contents = doc.select("div.status_info.abroad_info li.info_01").select("em.info_variation").first();
                    Log.d("▲", contents.text());

                    contents = doc.select("div.status_today li.info_02").select("em.info_num").first();
                    Log.d("▲", contents.text());
                    bundle.putString("today_domestic", contents.text());

                    contents = doc.select("div.status_today li.info_03").select("em.info_num").first();
                    Log.d("▲", contents.text());
                    bundle.putString("today_abroad", contents.text());

                    Elements elements = doc.select("div.csp_infoCheck_area._togglor_root a.info_text._trigger");
                    Log.d("time", elements.select("span._update_time").text());
                    bundle.putString("today_std_time", elements.select("span._update_time").text());

                    elements = doc.select("div.vaccine_status_item_inner");
                    Log.d("백신", elements.text());
                    bundle.putString("domestic_vaccine", elements.text());

                    Message msg = handler.obtainMessage();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    Log.e("error", e.getMessage());
                }
            }
        }.start();

    }

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            str = new StringBuilder("확진자 : " + msg.getData().getString("confirmed"));
            str.append("\n▲" + msg.getData().getString("confirmed_var"));
            str.append("\n검사중 : " + msg.getData().getString("inspection"));
            str.append("\n▼" + msg.getData().getString("inspection_var"));
            str.append("\n격리해제 : " + msg.getData().getString("release"));
            str.append("\n▲" + msg.getData().getString("release_var"));
            str.append("\n사망자 : " + msg.getData().getString("dead"));
            str.append("\n▲" + msg.getData().getString("dead_var"));
//            textView2.setText("▲"+msg.getData().getString("confirmed_var"));
//            textView3.setText("검사중 : "+msg.getData().getString("inspection"));
//            textView4.setText("▼"+msg.getData().getString("inspection_var"));
//            textView5.setText("격리해제 : "+msg.getData().getString("release"));
//            textView6.setText("▲"+msg.getData().getString("release_var"));
//            textView7.setText("사망자 : "+msg.getData().getString("dead"));
//            textView8.setText("▲"+msg.getData().getString("dead_var"));
            textView1.setText("확진자 →" + msg.getData().getString("confirmed") + " ▲" + msg.getData().getString("confirmed_var") + " (국내: " + msg.getData().getString("today_domestic") + " , 해외: " + msg.getData().getString("today_abroad") + ")");
            textView2.setText("격리해제 →" + msg.getData().getString("release") + " ▲" + msg.getData().getString("release_var"));
            textView3.setText("사망자 →" + msg.getData().getString("dead") + " ▲" + msg.getData().getString("dead_var"));
            textView4.setText("집계 기준 시간 → " + msg.getData().getString("today_std_time"));
            textView5.setText(msg.getData().getString("domestic_vaccine"));
            textView6.setText("거리두기" +distanceList.get(defaultRegionNumber));
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}