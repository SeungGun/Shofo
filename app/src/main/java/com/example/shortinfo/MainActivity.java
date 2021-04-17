package com.example.shortinfo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MainActivity extends AppCompatActivity {
    public String str = "";
    TextView textView1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Bundle bundle = new Bundle();

        textView1 = findViewById(R.id.value1);
        new Thread(){
            @Override
            public void run(){
                Document doc = null;
                try {
                    doc = Jsoup.connect("https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query=%EC%BD%94%EB%A1%9C%EB%82%98").get();
                    Log.d("signal","first");
                    Elements contents = doc.select(".info_num");

                    for(Element element : contents){
                        Log.d("result",element.text());
                    }
                    str += contents.text();
                    Log.d("signal",str);

                    bundle.putString("numbers",str);
                    Message msg = handler.obtainMessage();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
                catch (Exception e){
                    Log.e("error",e.getMessage());
                }
            }
        }.start();

    }
    Handler handler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(Message msg){
            textView1.setText(msg.getData().getString("numbers"));
        }
    };

}