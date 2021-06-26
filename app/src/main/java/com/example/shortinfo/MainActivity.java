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

public class MainActivity extends AppCompatActivity {
    public StringBuilder str;
    TextView textView1;
    TextView textView2;
    TextView textView3;
    TextView textView4;
    TextView textView5;
    TextView textView6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.activity_main);

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
                Document doc2 = null;
                try {
                    doc2 = Jsoup.connect("http://ncov.mohw.go.kr/").get();
                    Element ele = doc2.select("div#step_map_city1 p.rssd_descript").first();
                    Log.d("▲", ele.text());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //bundle.putString("vaccine_status", elements.text());
            }
        }).start();
        /*
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

                    elements = doc.select("div.social_distancing_map._patients_map");
//                    contents = doc.select("div.city_info_box").select("p.city_desc").first();
                    int j = 0;
                    for(Element e : elements){
                        Log.d("a"+(j++),e.text());
                    }



                    //bundle.putString("numbers",str.toString());
                    Message msg = handler.obtainMessage();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    Log.e("error", e.getMessage());
                }
            }
        }.start();

         */
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
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}