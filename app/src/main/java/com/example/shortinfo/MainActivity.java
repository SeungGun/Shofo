package com.example.shortinfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private TextView confirmedText;
    private TextView confirmedVarText;
    private TextView confirmedDetailText;
    private TextView releaseText;
    private TextView releaseVarText;
    private TextView deadText;
    private TextView deadVarText;
    private TextView stdDateText;
    private TextView vaccineFirstText;
    private TextView distancingText;
    private TextView vaccineSecondText;
    private ArrayList<String> distanceList;
    private static final int defaultRegionNumber = 8; //경기도
    private String[] vaccineFirst;
    private String[] vaccineSecond;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("Short Information");
        distanceList = new ArrayList<>();

        Intent intent = new Intent(getApplicationContext(), ScreenService.class);
        startService(intent);

        final Bundle bundle = new Bundle();

        confirmedText = findViewById(R.id.corona_text_confirmed);
        confirmedVarText = findViewById(R.id.corona_text_confirmed_var);
        confirmedDetailText = findViewById(R.id.corona_text_confirmed_detail);
        releaseText = findViewById(R.id.corona_text_release);
        releaseVarText = findViewById(R.id.corona_text_release_var);
        deadText = findViewById(R.id.corona_text_dead);
        deadVarText = findViewById(R.id.corona_text_dead_var);
        stdDateText = findViewById(R.id.corona_std_date);
        vaccineFirstText = findViewById(R.id.corona_text_vaccine_first);
        vaccineSecondText = findViewById(R.id.corona_text_vaccine_second);
        distancingText = findViewById(R.id.corona_text_distancing);

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
                                int be = temp[i].indexOf("-"); // -부터
                                int end = temp[i].substring(be).indexOf("}"); // }까지 구분하기 위한 index 구하기

                                String endStr = temp[i].substring(be, be+end);
                                endStr = endStr.replace("'",""); // ' 제거
                                endStr = endStr.replaceAll("<br/>",""); // <br/> 제거

                                String first = endStr.substring(0,3);
                                String second = endStr.substring(3);
                                String complete = first + " " + second;
                                distanceList.add(complete);
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

                    Element contents = doc.select("div.status_info li.info_01").select(".info_num").first();
                    bundle.putString("confirmed", contents.text());

                    contents = doc.select("div.status_info li.info_01").select("em.info_variation").first();
                    bundle.putString("confirmed_var", contents.text());

                    contents = doc.select("div.status_info li.info_02").select(".info_num").first();
                    bundle.putString("inspection", contents.text());

                    contents = doc.select("div.status_info li.info_02").select("em.info_variation").first();
                    bundle.putString("inspection_var", contents.text());

                    contents = doc.select("div.status_info li.info_03").select(".info_num").first();
                    bundle.putString("release", contents.text());

                    contents = doc.select("div.status_info li.info_03").select("em.info_variation").first();
                    bundle.putString("release_var", contents.text());


                    contents = doc.select("div.status_info li.info_04").select(".info_num").first();
                    bundle.putString("dead", contents.text());


                    contents = doc.select("div.status_info li.info_04").select("em.info_variation").first();
                    bundle.putString("dead_var", contents.text());

//                    contents = doc.select("div.status_info.abroad_info li.info_01").select(".info_num").first();
//                    Log.d("전 세계 확진자", contents.text());
//                    contents = doc.select("div.status_info.abroad_info li.info_01").select("em.info_variation").first();
//                    Log.d("▲", contents.text());

                    contents = doc.select("div.status_today li.info_02").select("em.info_num").first();
                    bundle.putString("today_domestic", contents.text());

                    contents = doc.select("div.status_today li.info_03").select("em.info_num").first();
                    bundle.putString("today_abroad", contents.text());

                    Elements elements = doc.select("div.csp_infoCheck_area._togglor_root a.info_text._trigger");
                    bundle.putString("today_std_time", elements.select("span._update_time").text());

                    elements = doc.select("div.vaccine_status_item_inner");

                    boolean flag = false;
                    for(Element e : elements){
                        if(!flag) {
                            vaccineFirst = e.text().split(" ");
                            flag = true;
                        }
                        else{
                            vaccineSecond = e.text().split(" ");
                        }
                    }
                    //[0] ~ [2] : 전국 n차 접종
                    //[3] : n%
                    //[4] : 누적n + 명
                    //[5] : 신규n증가 - 증가 + 명
                    String firstVacc = processVaccineFirst(vaccineFirst);
                    String secondVacc = processVaccineFirst(vaccineSecond);

                    bundle.putString("domestic_vaccine_first", firstVacc);
                    bundle.putString("domestic_vaccine_second",secondVacc);

                    Message msg = handler.obtainMessage();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    Log.e("error", e.getMessage());
                }
            }
        }.start();

    }

    private String processVaccineFirst(String[] vaccineFirst) {
        String title = vaccineFirst[0] + " " + vaccineFirst[1] + " " + vaccineFirst[2] + " : ";
        String percent = vaccineFirst[3] + " , ";
        String cumul = vaccineFirst[4].substring(0,2) + " " + vaccineFirst[4].substring(2) + "명 / ";
        String newer = vaccineFirst[5].substring(0,2) + " " +vaccineFirst[5].substring(2, vaccineFirst[5].length() - 2) + "명";
        return title + percent +"\n " + cumul + newer;
    }

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            confirmedText.setText("확진자 → " + msg.getData().getString("confirmed"));
            confirmedVarText.setText( " ▲ " + msg.getData().getString("confirmed_var"));
            confirmedDetailText.setText("(국내: " + msg.getData().getString("today_domestic") + " , 해외: " + msg.getData().getString("today_abroad") + ")");
            releaseText.setText("격리해제 → " + msg.getData().getString("release"));
            releaseVarText.setText(" ▲ " + msg.getData().getString("release_var"));
            deadText.setText("사망자 → " + msg.getData().getString("dead"));
            deadVarText.setText( " ▲ " + msg.getData().getString("dead_var"));
            stdDateText.setText("※ 집계 기준 시간 " + msg.getData().getString("today_std_time"));
            vaccineFirstText.setText(msg.getData().getString("domestic_vaccine_first"));
            vaccineSecondText.setText(msg.getData().getString("domestic_vaccine_second"));
            distancingText.setText("※ 거리두기 " +distanceList.get(defaultRegionNumber));
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.distance_location:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("거리두기 지역 설정");
                builder.setItems(R.array.Region, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int[] indexItems = getResources().getIntArray(R.array.Region_index);
                        distancingText.setText("※ 거리두기 " +distanceList.get(indexItems[which]));
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}