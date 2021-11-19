package com.example.shortinfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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
    private TextView worldStdTime;
    private TextView currentTime;
    private TextView currentDate;
    private TextView currentWeeks;
    private TextView currentLocation;
    private TextView worldConfirmedText;
    private TextView worldConfirmedVarText;
    private TextView temperatureText;
    private TextView PM10Text;
    private TextView PM2_5Text;
    private TextView weatherLocation;
    private TextView currentWeatherStatus;
    private TextView ultravioletText;
    private TextView compareYesterday;
    private TextView issueKeywordStdTime;
    private TextView rainPercentText;
    private TextView humidityPercentText;
    private TextView windStateText;
    private TextView windStateValueText;
    private TextView sunsetValueText;
    private ImageView weatherImage;
    private Bundle bundle;
    private ImageButton currentLocationWeather;
    private Button layoutRefreshButton;
    private ProgressBar progressBar;
    private ProgressBar networkProgressBar;
    private GpsTracker gpsTracker;
    private ListView keywordListView;

    private LinearLayout backgroundScreen;
    private LinearLayout foregroundScreen;
    private String[] vaccineFirst;
    private String[] vaccineSecond;
    private String address;
    private String inputAddress;
    private String area1;
    private String area2;
    private String area3;
    private String detailName;
    private String detailNumber;
    private String building;
    private String groundNumber;
    private ArrayList<String> distanceList;
    private ArrayAdapter<String> adapter;
    private double latitude;
    private double longitude;
    private boolean useCurrentAddress = false;

    public static final int DEFAULT_REGION_NUMBER = 8; //경기도
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    public static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    public static final String[] WEEKS = {"일요일", "월요일", "화요일", "수요일", "목요일", "금요일", "토요일"};
    public SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.activity_main);
        initializeObjects(); // 오브젝트 초기화 작업

        int status = getNetworkConnectState();
        if (status == 1) { // 와이파이
        } else if (status == 2) { // 모바일
        } else { // 연결안됨
            foregroundScreen.setVisibility(View.GONE);
            backgroundScreen.setVisibility(View.VISIBLE);
        }

        layoutRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (backgroundScreen.getVisibility() == View.VISIBLE) {
                    layoutRefreshButton.setBackgroundResource(R.drawable.ic_baseline_refresh_24);
                    int status = getNetworkConnectState();
                    layoutRefreshButton.setVisibility(View.GONE);
                    networkProgressBar.setVisibility(View.VISIBLE);
                    if (status == 1 || status == 2) {

                        layoutRefreshButton.setVisibility(View.GONE);
                        networkProgressBar.setVisibility(View.VISIBLE);

                        backgroundScreen.setVisibility(View.GONE);
                        foregroundScreen.setVisibility(View.VISIBLE);
                        getInitialLocation(); // 초기 위도, 경도값을 구해 주소정보 가져오기 +날씨 정보 가져오기
                        getRegionDistanceInfo(); // 지역별 거리두기 정보
                        getTodayOccurrence(); // 국내 발생 현황(국내발생 및 해외유입 정보)
                        executeTimeClock(); // 시계 기능
                        getVaccineInfo(); // 백신 접종 현황 정보
                        getCoronaInfo(); // 코로나 현황 정보
                        getLiveIssuesKeywords(); // 실시간 이슈 키워드 정보
                    } else {
                        layoutRefreshButton.setVisibility(View.VISIBLE);
                        networkProgressBar.setVisibility(View.GONE);
                        layoutRefreshButton.setBackgroundResource(R.drawable.ic_baseline_close_24);
                    }
                }
            }
        });

        getSupportActionBar().setTitle("Short Information");
        final Intent intent = new Intent(getApplicationContext(), ScreenService.class);
        startService(intent);

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        } else {
            checkRunTimePermission();
        }

        currentLocationWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (useCurrentAddress) {
                    useCurrentAddress = false;
                    currentLocationWeather.setBackgroundColor(Color.parseColor("#ffffff"));
                } else {
                    useCurrentAddress = true;
                    currentLocationWeather.setBackgroundColor(Color.parseColor("#46BEFF"));
                    getCurrentLocationWeather();
                }
                editor.putBoolean("isCurrent", useCurrentAddress);
                editor.apply();
            }
        });

        getInitialLocation(); // 초기 위도, 경도값을 구해 주소정보 가져오기 +날씨 정보 가져오기
        getRegionDistanceInfo(); // 지역별 거리두기 정보
        getTodayOccurrence(); // 국내 발생 현황(국내발생 및 해외유입 정보)
        executeTimeClock(); // 시계 기능
        getVaccineInfo(); // 백신 접종 현황 정보
        getCoronaInfo(); // 코로나 현황 정보
        getLiveIssuesKeywords(); // 실시간 이슈 키워드 가져오기
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            Toast.makeText(getApplicationContext(), "위치 정보를 갱신 중입니다...", Toast.LENGTH_SHORT).show();
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            getAddressUsingNaverAPI();
        }
    };

    public int getNetworkConnectState() {
        int TYPE_WIFI = 1;
        int TYPE_MOBILE = 2;
        int TYPE_NOT_CONNECTED = 3;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && connectivityManager.getActiveNetworkInfo().isConnected()) {
            int type = networkInfo.getType();
            if (type == ConnectivityManager.TYPE_MOBILE) {
                return TYPE_MOBILE;
            } else if (type == ConnectivityManager.TYPE_WIFI) {
                return TYPE_WIFI;
            }
        }
        return TYPE_NOT_CONNECTED;
    }

    public void getLiveIssuesKeywords() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection;
                try {
                    URL url = new URL("https://news.nate.com/today/keywordList");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Accept-Charset", "UTF-8");
                    urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                    if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));

                        String line;
                        String page = "";
                        while ((line = reader.readLine()) != null) {
                            page += line;
                        }
                        JSONObject jsonObject = new JSONObject(page);
                        String stdTime = jsonObject.getString("service_dtm");
                        JSONObject data = jsonObject.getJSONObject("data");
                        ArrayList<String> keywordList = new ArrayList<>();
                        for (int i = 0; i <= 9; ++i) {
                            keywordList.add((i + 1) + ". " + data.getJSONObject(i + "").optString("keyword_service").replace("<br />", " "));
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                issueKeywordStdTime.setText("※기준 시간 " + stdTime);
                                adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, keywordList);
                                keywordListView.setAdapter(adapter);
                                setListViewHeightBasedOnChildren(keywordListView);
                            }
                        });
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            //listItem.measure(0, 0);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();

        params.height = totalHeight;
        listView.setLayoutParams(params);

        listView.requestLayout();
    }


    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            try {
                confirmedText.setText("국내 확진자 → " + (msg.getData().getString("confirmed") == null ? "" : msg.getData().getString("confirmed")));
                confirmedVarText.setText(" ▲ " + (msg.getData().getString("confirmed_var") == null ? "" : msg.getData().getString("confirmed_var")));
                releaseText.setText("국내 격리해제 → " + (msg.getData().getString("release") == null ? "" : msg.getData().getString("release")));
                releaseVarText.setText(" ▲ " + (msg.getData().getString("release_var") == null ? "" : msg.getData().getString("release_var")));
                deadText.setText("국내 사망자 → " + (msg.getData().getString("dead") == null ? "" : msg.getData().getString("dead")));
                deadVarText.setText(" ▲ " + (msg.getData().getString("dead_var") == null ? "" : msg.getData().getString("dead_var")));
                stdDateText.setText("※ 국내 집계 기준 시간 " + (msg.getData().getString("today_std_time") == null ? "" : msg.getData().getString("today_std_time")));
                vaccineFirstText.setText(msg.getData().getString("domestic_vaccine_first") == null ? "" : msg.getData().getString("domestic_vaccine_first"));
                vaccineSecondText.setText(msg.getData().getString("domestic_vaccine_second") == null ? "" : msg.getData().getString("domestic_vaccine_second"));
                worldConfirmedText.setText(" → " + (msg.getData().getString("world") == null ? "" : msg.getData().getString("world")));
                worldConfirmedVarText.setText(" ▲ " + (msg.getData().getString("world_var") == null ? "" : msg.getData().getString("world_var")));
                worldStdTime.setText("※ " + (msg.getData().getString("world_std_time") == null ? "" : msg.getData().getString("world_std_time")));
                DecimalFormat formatter = new DecimalFormat("###,###");
                confirmedDetailText.setText("(국내 발생: " + (formatter.format(msg.getData().getInt("today_domestic") - msg.getData().getInt("today_abroad")))
                        + " , 해외 유입: " + (formatter.format(msg.getData().getInt("today_abroad"))) + ")");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    private void initializeObjects() {
        distanceList = new ArrayList<>();
        sharedPreferences = getSharedPreferences("useCurLoc", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        bundle = new Bundle();
        gpsTracker = new GpsTracker(this);
        backgroundScreen = findViewById(R.id.background_screen);
        foregroundScreen = findViewById(R.id.foreground_screen);
        networkProgressBar = findViewById(R.id.network_progressbar);
        layoutRefreshButton = findViewById(R.id.layout_refresh);
        progressBar = findViewById(R.id.progressBar);
        currentLocation = findViewById(R.id.cur_location);
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
        worldConfirmedText = findViewById(R.id.corona_text_world);
        worldConfirmedVarText = findViewById(R.id.corona_text_world_var);
        worldStdTime = findViewById(R.id.corona_text_world_std_time);
        currentTime = findViewById(R.id.cur_time);
        currentDate = findViewById(R.id.cur_date);
        currentWeeks = findViewById(R.id.weeks);
        temperatureText = findViewById(R.id.temperature);
        PM10Text = findViewById(R.id.PM10_text);
        PM2_5Text = findViewById(R.id.PM2_5_text);
        weatherLocation = findViewById(R.id.location);
        currentLocationWeather = findViewById(R.id.curloc_wt_button);
        currentWeatherStatus = findViewById(R.id.status);
        ultravioletText = findViewById(R.id.ultraviolet_text);
        compareYesterday = findViewById(R.id.cmp_yesterday);
        weatherImage = findViewById(R.id.weather_image);
        rainPercentText = findViewById(R.id.rain_percent_value);
        humidityPercentText = findViewById(R.id.humidity_percent_value);
        windStateText = findViewById(R.id.wind_state);
        windStateValueText = findViewById(R.id.wind_state_value);
        sunsetValueText = findViewById(R.id.sunset_value);
        issueKeywordStdTime = findViewById(R.id.issue_std_time);
        keywordListView = findViewById(R.id.keyword_list);
    }

    public void getCurrentLocationWeather() {
        if (address != null) {
            String[] divide = address.split(" ");
            if (area3 != null) {
                inputAddress = divide[1] + " " + divide[2] + " " + area3;
            } else {
                inputAddress = divide[1] + " " + divide[2];
            }
            weatherLocation.setText(inputAddress);
            getWeatherOfLocation();
        } else {
            Log.d("nullll", "dwdw");
        }
    }

    public void getInitialLocation() {
        if (gpsTracker == null) {
            gpsTracker = new GpsTracker(this);
        }
        gpsTracker.getLocation(locationListener);
        new Thread(new Runnable() {
            @Override
            public void run() {
                latitude = gpsTracker.getLatitude(); //위도
                longitude = gpsTracker.getLongitude(); // 경도
                Log.d("lon, lati", longitude + " " + latitude + " ");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getAddressUsingNaverAPI();
                    }
                });
            }
        }).start();
    }

    public void getTodayOccurrence() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Document document = null;
                try {
                    document = Jsoup.connect("http://ncov.mohw.go.kr/bdBoardList_Real.do?brdId=1&brdGubun=11&ncvContSeq=&contSeq=&board_id=&gubun=").get();
                    Element element = document.select("div.data_table.mgt16").select("tr.sumline td").first();
                    int abroad = Integer.parseInt(element.text().replaceAll(",", "").trim());
                    bundle.putInt("today_abroad", abroad);
                    Elements elements = document.select("div.caseTable ul.ca_body").select("dd.ca_value");
                    int i = 0;
                    for (Element e : elements) {
                        if (i == 6) {
                            int domestic = Integer.parseInt(e.text().replaceAll(",", "").trim());
                            bundle.putInt("today_domestic", domestic);
                            break;
                        }
                        i++;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    Message msg = handler.obtainMessage();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }
    /* ----------------onCreate()-----------------------------------------------------------*/

    private void getCoronaInfo() {
        new Thread() {
            @Override
            public void run() {
                Document doc = null;
                try {
                    doc = Jsoup.connect("https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query=%EC%BD%94%EB%A1%9C%EB%82%98").get();
                    // 코로나
                    Element contents = doc.select("div.status_info li.info_01").select(".info_num").first();
                    bundle.putString("confirmed", contents.text() == null ? "데이터 에러" : contents.text());

                    contents = doc.select("div.status_info li.info_01").select("em.info_variation").first();
                    bundle.putString("confirmed_var", contents.text() == null ? "데이터 에러" : contents.text());

                    contents = doc.select("div.status_info li.info_03").select(".info_num").first();
                    bundle.putString("release", contents.text() == null ? "데이터 에러" : contents.text());

                    contents = doc.select("div.status_info li.info_03").select("em.info_variation").first();
                    bundle.putString("release_var", contents.text() == null ? "데이터 에러" : contents.text());

                    contents = doc.select("div.status_info li.info_04").select(".info_num").first();
                    bundle.putString("dead", contents.text() == null ? "데이터 에러" : contents.text());

                    contents = doc.select("div.status_info li.info_04").select("em.info_variation").first();
                    bundle.putString("dead_var", contents.text() == null ? "데이터 에러" : contents.text());

                    contents = doc.select("div.status_info.abroad_info li.info_01").select(".info_num").first();
                    bundle.putString("world", contents.text() == null ? "데이터 에러" : contents.text());

                    contents = doc.select("div.status_info.abroad_info li.info_01").select("em.info_variation").first();
                    bundle.putString("world_var", contents.text() == null ? "데이터 에러" : contents.text());

                    Elements elements = doc.select("div.csp_infoCheck_area._togglor_root a.info_text._trigger span._update_time");
                    bundle.putString("today_std_time", elements.text() == null ? "데이터 에러" : elements.text());

                    elements = doc.select("div.patients_info div.csp_infoCheck_area._togglor_root a.info_text._trigger");

                    if (elements.text() == null) {
                        bundle.putString("world_std_time", "데이터 에러");
                    } else {
                        int id = elements.text().indexOf("세계현황");
                        String s1 = elements.text().substring(id);
                        int id2 = s1.substring(4).indexOf("세계현황");
                        String sub = s1.substring(4 + id2);
                        bundle.putString("world_std_time", sub);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Message msg = handler.obtainMessage();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            }
        }.start();
    }

    public void getVaccineInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Document vaccineUrl = null;
                try {
                    vaccineUrl = Jsoup.connect("https://search.naver.com/search.naver?sm=tab_sug.top&where=nexearch&query=%EC%BD%94%EB%A1%9C%EB%82%98+%EB%B0%B1%EC%8B%A0+%EC%A0%91%EC%A2%85+%ED%98%84%ED%99%A9&oquery=%EC%BD%94%EB%A1%9C%EB%82%98&tqi=hLI4RlprvxsssdKQRURssssss4C-138619&acq=%EC%BD%94%EB%A1%9C%EB%82%98+%EB%B0%B1&acr=4&qdt=0").get();
                    // 코로나 백신
                    Elements elements;
                    elements = vaccineUrl.select("div.vaccine_status_item");
                    boolean flag = false;
                    if (elements.text() == null) {
                        bundle.putString("domestic_vaccine_first", "데이터 에러");
                        bundle.putString("domestic_vaccine_second", "데이터 에러");
                        return;
                    }
                    for (Element e : elements) {
                        if (!flag) {
                            vaccineFirst = e.text().split(" ");
                            flag = true;
                        } else {
                            vaccineSecond = e.text().split(" ");
                            break;
                        }
                    }
                    //[0] ~ [2] : 전국 n차 접종
                    //[3] : n%
                    //[4] : 누적n + 명
                    //[5] : 신규n증가 - 증가 + 명
                    String firstVacc = processVaccineFirst(vaccineFirst);
                    String secondVacc = processVaccineFirst(vaccineSecond);

                    bundle.putString("domestic_vaccine_first", firstVacc);
                    bundle.putString("domestic_vaccine_second", secondVacc);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    Message msg = handler.obtainMessage();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }

            }
        }).start();
    }

    public void executeTimeClock() {
        new Thread() {
            @Override
            public void run() {
                while (!isInterrupted()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Calendar calendar = Calendar.getInstance();
                            boolean isPM = false;
                            int year = calendar.get(Calendar.YEAR);
                            int month = calendar.get(Calendar.MONTH); // 1월 : 0
                            int day = calendar.get(Calendar.DAY_OF_MONTH);
                            int week = calendar.get(Calendar.DAY_OF_WEEK);
                            int hour = calendar.get(Calendar.HOUR_OF_DAY);
                            int minute = calendar.get(Calendar.MINUTE);
                            int second = calendar.get(Calendar.SECOND);
                            if (hour >= 12) {
                                isPM = true;
                            } else {
                                isPM = false;
                            }
                            currentDate.setText(year + "년 " + (month + 1) + "월 " + day + "일");
                            String colors;
                            if (WEEKS[week - 1].equals("일요일")) {
                                colors = "#FF0000";
                            } else if (WEEKS[week - 1].equals("토요일")) {
                                colors = "#3CA0E1";
                            } else {
                                colors = "#000000";
                            }
                            currentWeeks.setTextColor(Color.parseColor(colors));
                            currentWeeks.setText(WEEKS[week - 1]);
                            currentTime.setText((isPM ? "오후 " + (hour == 12 ? hour : (hour - 12)) : " 오전 " + (hour == 0 ? 12 : hour)) + ":"
                                    + (minute < 10 ? "0" + minute : minute) + ":" + (second < 10 ? "0" + second : second));
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public void getRegionDistanceInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Document coronaUrl = null;
                try {
                    coronaUrl = Jsoup.connect("http://ncov.mohw.go.kr/regSocdisBoardView.do?brdId=6&brdGubun=68&ncvContSeq=495").get();

                    Elements scripts = coronaUrl.getElementsByTag("script");
                    if (scripts.text() == null) {
                        distanceList.add("데이터 에러");
                        return;
                    }
                    for (Element e : scripts) {
                        if (e.data().contains("RSS_DATA")) {
                            int idx_begin = e.data().indexOf("RSS_DATA");
                            String front = e.data().substring(idx_begin);

                            int idx_end = front.indexOf(";");

                            String cutStr = front.substring(0, idx_end + 1);
                            cutStr = cutStr.replaceAll(" ", "");
                            cutStr = cutStr.replace("\n", "");

                            String[] temp = cutStr.split("\\{");

                            // index 0번은 "RSS_DATA = [" 이기 때문에 index 1번부터
                            for (int i = 1; i < temp.length; ++i) {
                                int be = temp[i].indexOf("-"); // -부터
                                int end = temp[i].substring(be).indexOf("}"); // }까지 구분하기 위한 index 구하기

                                String endStr = temp[i].substring(be, be + end);
                                endStr = endStr.replace("'", ""); // ' 제거
                                endStr = endStr.replaceAll("<br/>", ""); // <br/> 제거

                                String first = endStr.substring(0, 3);
                                String second = endStr.substring(3);
                                String complete = first + " " + second;
                                distanceList.add(complete);
                            }
                            break;
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            distancingText.setText("※ 거리두기 " + distanceList.get(DEFAULT_REGION_NUMBER));
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void getAddressUsingNaverAPI() {
        new Thread() {
            @Override
            public void run() {
                HttpURLConnection urlConnection;
                try {
                    Log.d("longitude & latitude", longitude + ", " + latitude);
                    URL url = new URL("https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc?coords=" + longitude + "," + latitude + "&orders=legalcode,admcode,addr,roadaddr&output=json");

                    //여러 개의 값을 입력할 수 있으며, orders 요청순으로 결과가 표시됩니다.
                    //예) orders=legalcode
                    //orders=addr,admcode
                    //orders=addr,admcode,roadaddr
                    //orders=legalcode,addr,admcode,roadaddr
                    // legalcode : 법정동
                    // admcode : 행정동
                    // addr : 지번 주소
                    // roadaddr: 도로명 주소

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Accept-Charset", "UTF-8");
                    urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                    urlConnection.setRequestProperty("X-NCP-APIGW-API-KEY-ID", "78obj02dm7");
                    urlConnection.setRequestProperty("X-NCP-APIGW-API-KEY", "26ZH2x2dbDxREayPjwWOziWD1ZcJOMp0aRmUiW8K");

                    if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));

                        String line;
                        StringBuilder page = new StringBuilder();
                        while ((line = reader.readLine()) != null) {
                            page.append(line); // 주소 정보가 담긴 json String
                        }

                        JSONObject json = new JSONObject(page.toString()); // convert string to json
                        int statusCode = json.optJSONObject("status").optInt("code");
                        if (statusCode == 0) {
                            int orderJsonArrLength = json.optJSONArray("results").length();
                            Log.d("Length of order Json", orderJsonArrLength + " ");
                            switch (orderJsonArrLength) {
                                case 0: // result 없음
                                    break;
                                case 1: // legalcode
                                    JSONObject legal = json.optJSONArray("results").getJSONObject(0).getJSONObject("region");
                                    area1 = legal.optJSONObject("area1").optString("name");
                                    area2 = legal.optJSONObject("area2").optString("name");
                                    area3 = legal.optJSONObject("area3").optString("name");
                                    address = area1 + " " + area2 + " " + area3;
                                    break;
                                case 2: // legalcode, admcode
                                    JSONObject admcode = json.optJSONArray("results").getJSONObject(1).getJSONObject("region");
                                    area1 = admcode.getJSONObject("area1").optString("name");
                                    area2 = admcode.getJSONObject("area2").optString("name");
                                    area3 = admcode.getJSONObject("area3").optString("name");
                                    address = area1 + " " + area2 + " " + area3;
                                    break;
                                case 3: // legalcode, admcode, addr
                                    JSONObject addr = json.optJSONArray("results").getJSONObject(2).getJSONObject("region");
                                    area1 = addr.getJSONObject("area1").optString("name");
                                    area2 = addr.getJSONObject("area2").optString("name");
                                    area3 = addr.getJSONObject("area3").optString("name");
                                    groundNumber = json.optJSONArray("results").getJSONObject(2).getJSONObject("land").optString("number1");
                                    address = area1 + " " + area2 + " " + area3 + " " + groundNumber;
                                    break;
                                case 4: // legalcode, admcode, addr, roadaddr
                                    JSONObject roadaddr = json.optJSONArray("results").getJSONObject(3).getJSONObject("region");
                                    area1 = roadaddr.getJSONObject("area1").optString("name");
                                    area2 = roadaddr.getJSONObject("area2").optString("name");
                                    area3 = roadaddr.getJSONObject("area3").optString("name");
                                    detailName = json.optJSONArray("results").getJSONObject(3).getJSONObject("land").optString("name");
                                    detailNumber = json.optJSONArray("results").getJSONObject(3).getJSONObject("land").optString("number1");
                                    building = json.optJSONArray("results").getJSONObject(3).getJSONObject("land").getJSONObject("addition0").optString("value");
                                    address = area1 + " " + area2 + " " + detailName + " " + detailNumber + " (" + area3 + (building.equals("") ? building : ", " + building) + ")";
                                    break;
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (sharedPreferences.getBoolean("isCurrent", false)) {
                                        useCurrentAddress = sharedPreferences.getBoolean("isCurrent", false);
                                        currentLocationWeather.setBackgroundColor(Color.parseColor("#46BEFF"));
                                        getCurrentLocationWeather();
                                    } else {
                                        compareYesterday.setText("설정한 위치 및 날씨가 없습니다.");
                                    }
                                    currentLocation.setText(address);
                                    currentLocation.setVisibility(View.VISIBLE);
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                            //
                            // 도 : results[0] -> region -> area1 -> name
                            // 시 & 구 : results[0] -> region -> area2 -> name
                            // 동 : results[0] -> region -> area3 -> name
                            // 상세 주소 도로명 : results[0] -> land /-> number1 : 상세주소(번호)
                            // name : 상세 명칭(도로명 이름)
                            // addition0 -> value : 건물
                            // addition1 -> value : 우편번호
                            // addition2 -> value : 도로코드
                            // addition3 -> value : ???
                            // addition4 -> value : ???
                            // 도 시 구 도로명 도로번호 (동 건물)
                            //Reference : https://api.ncloud-docs.com/docs/ai-naver-mapsreversegeocoding-gc

                            // 1차 : 도, 시
                            // 2차 : 시, 군, 구
                            // 3차 : 시, 구, 동, 읍, 면
                        }
                    } else {
                        currentLocation.setText("http error");
                    }
                } catch (MalformedURLException e) {
                    currentLocation.setText("Malformed");
                    e.printStackTrace();
                } catch (IOException e) {
                    currentLocation.setText("네트워크 연결이 되어있지 않습니다.");
                    e.printStackTrace();
                } catch (JSONException e) {
                    currentLocation.setText("위치 서비스가 활성화 되어 있지 않습니다.");
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * Button Click event for Refresh Button of current location
     *
     * @param view
     */
    public void onGetAddress(View view) {
        if (gpsTracker == null) {
            gpsTracker = new GpsTracker(this);
        }
        if (gpsTracker.getLocation(locationListener) != null) {
            currentLocation.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    latitude = gpsTracker.getLatitude(); // 위도
                    longitude = gpsTracker.getLongitude(); // 경도
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getAddressUsingNaverAPI();
                        }
                    });
                }
            }).start();
        } else {
            currentLocation.setText("위치 서비스를 사용할 수 없습니다. 재시도 바랍니다.");
        }
    }

    private String processVaccineFirst(String[] vaccineFirst) {
        String title = vaccineFirst[0] + " " + vaccineFirst[1] + " " + vaccineFirst[2] + " : ";
        String percent = vaccineFirst[3] + " , ";
        String cumul = vaccineFirst[4].substring(0, 2) + " " + vaccineFirst[4].substring(2) + "명 / ";
        String newer = vaccineFirst[5].substring(0, 2) + " " + vaccineFirst[5].substring(2, vaccineFirst[5].length() - 2) + "명";
        return title + percent + "\n " + cumul + newer;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.distance_location:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("거리두기 지역 설정");
                builder.setItems(R.array.Region, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int[] indexItems = getResources().getIntArray(R.array.Region_index);
                        distancingText.setText("※ 거리두기 " + distanceList.get(indexItems[which]));
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            case R.id.weather_location:
                FrameLayout container = new FrameLayout(this);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
                params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);

                final EditText editText = new EditText(this);
                editText.setLayoutParams(params);
                container.addView(editText);
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setTitle("날씨 지역명 변경")
                        .setMessage("보고싶은 날씨의 지역명을 입력하세요. (시 또는 구 또는 동 단위로 지역명만 입력)")
                        .setView(container)
                        .setPositiveButton("저장", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //입력한 값을 cache에 저장 및 그 값대로 날씨 웹 URL의 parameter로 주고 파싱
                                inputAddress = editText.getText().toString();
                                getWeatherOfLocation();
                                weatherLocation.setText(inputAddress);
                                currentLocationWeather.setBackgroundColor(Color.parseColor("#ffffff"));
                                useCurrentAddress = false;
                            }
                        });
                AlertDialog alertDialog1 = builder2.create();
                alertDialog1.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE && grantResults.length == REQUIRED_PERMISSIONS.length) {
            boolean check_result = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {

            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0]) || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    //퍼미션 거부 앱을 다시 실행하여 퍼미션 허용해주세요
                } else {
                    // 퍼미션 거부, 설정에서 퍼미션을 허용해야함
                }
            }
        }
    }

    public void checkRunTimePermission() {
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                // 이 앱을 실행하려면 위치 접근 권한 필요
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    public String getCurrentAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addressList;

        try {
            addressList = geocoder.getFromLocation(latitude, longitude, 5);
        } catch (IOException e) {
            // 네트워크 문제
            return "서비스 사용불가";
        } catch (IllegalArgumentException e) {
            return "잘못된 GPS 좌표";
        }
        if (addressList == null || addressList.size() == 0) {
            return "주소 미발견";
        }

        Address address = addressList.get(0);
        return address.getAddressLine(0) + "\n";
    }

    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("위치 서비스 활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n위치 설정을 수정하시겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent callGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPS, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                if (checkLocationServicesStatus()) {
                    // GPS 활성화 되있음
                    checkRunTimePermission();
                    return;
                }
                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void getWeatherOfLocation() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Document weatherDoc = null;
                try {
                    inputAddress = inputAddress.replace(' ', '+');

                    getWeatherImageAccordingToWeather();

                    weatherDoc = Jsoup.connect("https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query=" + inputAddress + "+날씨").get();
                    final String temperature = weatherDoc.select("div.temperature_text").first().text().replace("현재 온도", ""); // 온도 정보
                    final String tempInfo = weatherDoc.select("div.temperature_info").first().text(); // 이외 정보
                    final String airInfo = weatherDoc.select("div.report_card_wrap").first().text(); // 대기 정보

                    /**
                     * "temperature_info" text split information (updated 2021-11-11)
                     * 0 : 어제보다
                     * 1 : {n}˚
                     * 2 : 낮아요 or 높아요
                     * 3 : 날씨 상태(맑음, 흐림등)
                     * 4 : 강수확률
                     * 5 : {n}%
                     * 6 : 습도
                     * 7 : {n}%
                     * 8 : 바람(##풍)
                     * 9 : {n}m/s
                     */
                    String[] temps = tempInfo.split(" ");
                    String[] airs = airInfo.split(" ");

                    String cmp = temps[0] + " " + temps[1] + " " + temps[2];
                    String stateText = temps[3];

                    String rainValue = temps[5];
                    String humidityValue = temps[7];

                    String windText = temps[8];
                    String windValue = temps[9];

                    // index 1, 3, 5로 수준 값 가져오기
                    PM10Text.setTextColor(Color.parseColor(getColorAccordingStd(airs[1])));
                    PM2_5Text.setTextColor(Color.parseColor(getColorAccordingStd(airs[3])));
                    ultravioletText.setTextColor(Color.parseColor(getColorAccordingStd(airs[5])));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            PM10Text.setText(airs[1]);
                            PM2_5Text.setText(airs[3]);
                            ultravioletText.setText(airs[5]);
                            temperatureText.setText(temperature);
                            currentWeatherStatus.setText(stateText);
                            compareYesterday.setText(cmp);
                            rainPercentText.setText(rainValue);
                            humidityPercentText.setText(humidityValue);
                            windStateText.setText(windText + " ");
                            windStateValueText.setText(windValue);
                            sunsetValueText.setText(airs[7]);
                        }
                    });
                } catch (Exception e) {
                    String[] reTryInputAddressSplit = inputAddress.split("\\+");

                    if(reTryInputAddressSplit.length == 1){
                        compareYesterday.setText("날씨 정보를 가져올 수 없는 지역입니다.");
                        return;
                    }
                    StringBuilder newAddress = new StringBuilder();
                    for (int i = 0; i < reTryInputAddressSplit.length - 1; ++i) {
                        newAddress.append(reTryInputAddressSplit[i]);
                        if (i == reTryInputAddressSplit.length - 2) {
                            break;
                        }
                        newAddress.append("+");
                    }
                    inputAddress = newAddress.toString();
                    getWeatherOfLocation();
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void getWeatherImageAccordingToWeather() {
        new Thread() {
            @Override
            public void run() {
                try {
                    /**
                     * Expecting value of @imageClassName : wt_icon icon_wt{number}
                     * Image format for URL : [URL]icon_wt_{number}.svg
                     */
                    Document doc = Jsoup.connect("https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query=" + inputAddress + "+날씨").get();
                    String imageClassName = doc.select("div.weather_graphic div.weather_main").select("i").attr("class");
                    String state = imageClassName.split(" ")[1];
                    int num = Integer.parseInt(state.split("_")[1].substring(2)); // get integer value

                    final String param = num > 9 ? String.valueOf(num) : "0" + num; // int to String

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                GlideToVectorYou.justLoadImage(MainActivity.this, Uri.parse("https://ssl.pstatic.net/sstatic/keypage/outside/scui/weather_new/img/weather_svg/icon_wt_" + param + ".svg"), weatherImage);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public String getColorAccordingStd(String std) {
        switch (std) {
            case "좋음":
                return "#32a1ff";
            case "보통":
                return "#03c75a";
            case "나쁨":
                return "#fd9b5a";
            case "매우나쁨":
                return "#ff5959";
        }
        return "#000000";
    }
}