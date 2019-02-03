package com.canthackthis.memewhisperer;

import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    TextToSpeech t1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
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
}
