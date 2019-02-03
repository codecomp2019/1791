package com.canthackthis.memewhisperer;

import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
<<<<<<< Updated upstream

public class MainActivity extends AppCompatActivity {
=======
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;
import android.view.View;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Locale;


import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {
    TextToSpeech t1;
    private Button getBtn;
    private TextView result;
>>>>>>> Stashed changes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
<<<<<<< Updated upstream
=======
        result = findViewById(R.id.result);
        getBtn = findViewById(R.id.getBtn);
        getBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                getContext();
            }
        });

        if (OpenCVLoader.initDebug()){
            Toast.makeText(getApplicationContext(), "YO", Toast.LENGTH_SHORT).show();
        }
        // This is the part that does speaking
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.JAPANESE);
                    t1.setSpeechRate((float).5);
                }
            }
        });
        t1.speak("Put your words right here", TextToSpeech.QUEUE_FLUSH, null);

    }
    public void onPause(){
        if(t1 !=null){
            t1.stop();
            t1.shutdown();
        }
        super.onPause();
    }

    private void getContext(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();

                try {
                    Document doc = Jsoup.connect("https://knowyourmeme.com/memes/bad-luck-brian").get();
                    String title = doc.title();
                    Element content = doc.getElementById("entry_body");
                    Element about = content.getElementsByTag("p").first();

                    String context = ("About the meme: " + about.text());

                    builder.append(title).append("\n");
                    builder.append("\n").append(context);

                    // builder.append("\n").append("About : ").append(about.attr("p")).append(about.text());

                } catch (IOException e) {
                    builder.append("error : ").append(e.getMessage()).append("\n");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        result.setText(builder.toString());
                    }
                });
            }
        }).start();
>>>>>>> Stashed changes
    }
}
