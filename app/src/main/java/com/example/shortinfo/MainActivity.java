package com.example.shortinfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
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
    private TextView currentLocation;
    private ArrayList<String> distanceList;
    private static final int defaultRegionNumber = 8; //경기도
    private TextView worldConfirmedText;
    private TextView worldConfirmedVarText;
    private String[] vaccineFirst;
    private String[] vaccineSecond;
    private ProgressBar progressBar;
    public static final String[] weeks = {"일요일","월요일","화요일","수요일","목요일","금요일","토요일"};
    TextView testAddr;
    GpsTracker gpsTracker;
    String address;

    double la;
    double lo;

    private Workbook workbook;
    private Sheet sheet;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

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

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        } else {
            checkRunTimePermission();
        }
        testAddr = findViewById(R.id.test);
        gpsTracker = new GpsTracker(this);
        progressBar = findViewById(R.id.progressBar);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream inputStream = getBaseContext().getResources().getAssets().open("position_sheet.xls");
                    workbook = Workbook.getWorkbook(inputStream);
                    sheet = workbook.getSheet(0);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (BiffException e) {
                    e.printStackTrace();
                }

                Log.d("length ", sheet.getRows() + ""); //인덱스 row 2부터 3775까지 for(int i=1; i<3775; ++i)

                // 경도 : latitude (13) , 위도 : longitude (14)
                int min_index = 0;
                double min_value_a = 10000000;
                double min_value_b = 10000000;
                for (int i = 1; i < 3775; ++i) {
                    double a = Math.abs(Double.parseDouble(sheet.getCell(14, i).getContents()) - la);
                    double b = Math.abs(Double.parseDouble(sheet.getCell(13, i).getContents()) - lo);
                    if (min_value_a > a && min_value_b > b) {
                        min_value_a = a;
                        min_value_b = b;
                        min_index = i;
                    }
                }
                Log.d("min", min_value_a + " , " + min_value_b + " , " + sheet.getCell(4, min_index).getContents());

            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                la = gpsTracker.getLatitude();
                lo = gpsTracker.getLongitude();
                Log.d("la , lo", la + " , " + lo);
                address = getCurrentAddress(la, lo);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        currentLocation.setText(address);
                    }
                });
            }
        }).start();
        final Bundle bundle = new Bundle();
        currentTime = findViewById(R.id.cur_time);

        new Thread(){
            @Override
            public void run(){
                while(!isInterrupted()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Calendar calendar = Calendar.getInstance();
                            boolean isPM = false;
                            int year = calendar.get(Calendar.YEAR);
                            int month = calendar.get(Calendar.MONTH);
                            int day = calendar.get(Calendar.DAY_OF_MONTH);
                            int week = calendar.get(Calendar.DAY_OF_WEEK);
                            int hour = calendar.get(Calendar.HOUR_OF_DAY);
                            int minute = calendar.get(Calendar.MINUTE);
                            int second = calendar.get(Calendar.SECOND);
                            if(hour > 12){
                                isPM = true;
                            }
                            else{
                                isPM = false;
                            }
                            currentTime.setText(year+"년 "+(month+1)+"월 "+day+"일 "+weeks[week-1]+"\n"
                                    +(isPM ? "오후 " +(hour- 12) : " 오전 " +hour)+"시 "+minute+"분 "+second+"초");
                        }
                    });
                    try{
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        }.start();
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                Document vaccineUrl = null;
                try {
                    vaccineUrl = Jsoup.connect("https://search.naver.com/search.naver?sm=tab_sug.top&where=nexearch&query=%EC%BD%94%EB%A1%9C%EB%82%98+%EB%B0%B1%EC%8B%A0+%EC%A0%91%EC%A2%85+%ED%98%84%ED%99%A9&oquery=%EC%BD%94%EB%A1%9C%EB%82%98&tqi=hLI4RlprvxsssdKQRURssssss4C-138619&acq=%EC%BD%94%EB%A1%9C%EB%82%98+%EB%B0%B1&acr=4&qdt=0").get();
                    Elements elements;
                    elements = vaccineUrl.select("div.vaccine_status_item");
                    boolean flag = false;
                    for(Element e : elements){
                        if(!flag) {
                            vaccineFirst = e.text().split(" ");
                            flag = true;
                        }
                        else{
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
                    bundle.putString("domestic_vaccine_second",secondVacc);

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

                    contents = doc.select("div.status_info.abroad_info li.info_01").select(".info_num").first();
                    bundle.putString("world",contents.text());
//                    Log.d("전 세계 확진자", contents.text());
                    contents = doc.select("div.status_info.abroad_info li.info_01").select("em.info_variation").first();
                    bundle.putString("world_var",contents.text());
//                    Log.d("▲", contents.text());

                    contents = doc.select("div.status_today li.info_02").select("em.info_num").first();
                    bundle.putString("today_domestic", contents.text());

                    contents = doc.select("div.status_today li.info_03").select("em.info_num").first();
                    bundle.putString("today_abroad", contents.text());

                    Elements elements = doc.select("div.csp_infoCheck_area._togglor_root a.info_text._trigger");
                    bundle.putString("today_std_time", elements.select("span._update_time").text());

                    elements = doc.select("div.patients_info div.csp_infoCheck_area._togglor_root a.info_text._trigger");
                    int id = elements.text().indexOf("세계현황");
                    String s1 = elements.text().substring(id);
                    int id2 = s1.substring(4).indexOf("세계현황");

                    String sub = s1.substring(4 + id2);
                    Log.d("world_std_time", sub);

                    bundle.putString("world_std_time",sub);
                    Message msg = handler.obtainMessage();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    Log.e("error", e.getMessage());
                }
            }
        }.start();

    }

    public void onGetAddress(View view) {
        currentLocation.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                la = gpsTracker.getLatitude();
                lo = gpsTracker.getLongitude();
                Log.d("la , lo", la + " , " + lo);
                address = getCurrentAddress(la, lo);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        currentLocation.setText(address);
                        currentLocation.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    }
                });

            }
        }).start();
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
            confirmedText.setText("국내 확진자 → " + msg.getData().getString("confirmed"));
            confirmedVarText.setText( " ▲ " + msg.getData().getString("confirmed_var"));
            confirmedDetailText.setText("(국내: " + msg.getData().getString("today_domestic") + " , 해외: " + msg.getData().getString("today_abroad") + ")");
            releaseText.setText("국내 격리해제 → " + msg.getData().getString("release"));
            releaseVarText.setText(" ▲ " + msg.getData().getString("release_var"));
            deadText.setText("국내 사망자 → " + msg.getData().getString("dead"));
            deadVarText.setText( " ▲ " + msg.getData().getString("dead_var"));
            stdDateText.setText("※ 국내 집계 기준 시간 " + msg.getData().getString("today_std_time"));
            vaccineFirstText.setText(msg.getData().getString("domestic_vaccine_first"));
            vaccineSecondText.setText(msg.getData().getString("domestic_vaccine_second"));
            distancingText.setText("※ 거리두기 " +distanceList.get(defaultRegionNumber));
            worldConfirmedText.setText(" → "+msg.getData().getString("world"));
            worldConfirmedVarText.setText(" ▲ "+msg.getData().getString("world_var"));
            worldStdTime.setText("※ "+msg.getData().getString("world_std_time"));

            currentLocation.setText(address);

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
            case R.id.weather_location:
                FrameLayout container = new FrameLayout(this);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
                params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);

                EditText editText = new EditText(this);
                editText.setLayoutParams(params);
                container.addView(editText);
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setTitle("날씨 위치 변경")
                        .setMessage("보고싶은 날씨의 위치를 입력하세요. (시 또는 구 또는 동 단위로)")
                        .setView(container)
                        .setPositiveButton("저장", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //입력한 값을 cache에 저장 및 그 값대로 날씨 웹 URL의 parameter로 주고 파싱
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
        if(requestCode == PERMISSIONS_REQUEST_CODE && grantResults.length == REQUIRED_PERMISSIONS.length){
            boolean check_result = true;

            for(int result : grantResults){
                if(result != PackageManager.PERMISSION_GRANTED){
                    check_result = false;
                    break;
                }
            }

            if(check_result){

            }
            else{
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0]) || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])){
                    //퍼미션 거부 앱을 다시 실행하여 퍼미션 허용해주세요
                }
                else{
                    // 퍼미션 거부, 설정에서 퍼미션을 허용해야함
                }
            }
        }
    }
    public void checkRunTimePermission(){
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if(hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED){

        }
        else{
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])){
                // 이 앱을 실행하려면 위치 접근 권한 필요
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
            else{
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }
    }
    public String getCurrentAddress(double latitude, double longitude){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addressList;

        try{
            addressList = geocoder.getFromLocation(latitude,longitude, 7);
        }
        catch (IOException e){
            // 네트워크 문제
            return "서비스 사용불가";
        }
        catch (IllegalArgumentException e){
            return "잘못된 GPS 좌표";
        }
        if(addressList == null || addressList.size() == 0){
            return "주소 미발견";
        }

        Address address = addressList.get(0);
        return address.getAddressLine(0).toString() +"\n";
    }

    private void showDialogForLocationServiceSetting(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("위치 서비스 활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n위치 설정을 수정하시겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent callGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPS,GPS_ENABLE_REQUEST_CODE);
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

        switch (requestCode){
            case GPS_ENABLE_REQUEST_CODE:
                if(checkLocationServicesStatus()){
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
}