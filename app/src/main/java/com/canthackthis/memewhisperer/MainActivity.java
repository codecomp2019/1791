package com.canthackthis.memewhisperer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    TextToSpeech t1;
    private static int RESULT_LOAD_IMG = 1;
    private Bitmap selectedImage = null;
    private ImageView currentImageView;

    static{
        System.loadLibrary("opencv_java3");

    }

    //First Page Buttons
    Button choose;
    //Second Page Buttons
    Button about;
    Button newMeme;
    ImageView imageView;
    TextView textView;

    //Meme
    String memeURL = "";
    String memeName = "";
    String memeAbout = "";
    boolean memeMatched = false;

    //Database
    HashMap<String, Mat> map = new HashMap<String, Mat>();

    //onClick implementation
    public void onClick(View v) {
        // Perform action on click
        switch(v.getId()) {
            case R.id.choose:
                //Proceed to the next layout
                this.setContentView(R.layout.image_area);
                about =  findViewById(R.id.about);
                newMeme =  findViewById(R.id.newMeme);
                about.setOnClickListener(this);
                newMeme.setOnClickListener(this);
                imageView = findViewById(R.id.imageView);
                imageView.setOnClickListener(this);
                openImage(imageView);
                textView = findViewById(R.id.textView);
                textView.setVisibility(textView.VISIBLE);
                break;
            case R.id.about:
                //read the image in selected
                if (memeMatched)getContext();
                else {
                    memeMatched = false;
                    t1.speak("There was no match found for this meme. . . . . . . . ", TextToSpeech.QUEUE_FLUSH, null);
                }
                break;
            case R.id.newMeme:
                //openGallery
                imageView = findViewById(R.id.imageView);
                openImage(imageView);
                break;
            case R.id.imageView:
                readImage();
                break;

        }
    }

    //onCreate override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_area);
        setContentView(R.layout.start_buttons);
        choose =  findViewById(R.id.choose);
        choose.setOnClickListener(this);
        
        if (OpenCVLoader.initDebug()){
            Toast.makeText(getApplicationContext(), "   YO\nAnd welcome!", Toast.LENGTH_SHORT).show();
        }
        // This is the part that does speaking
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.ENGLISH);
                    t1.setSpeechRate((float)1);
                }
            }
        });
        t1.speak("Put your words right here", TextToSpeech.QUEUE_FLUSH, null);
        initializeMap(map);

    }

    private void getContext(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final StringBuilder builder = new StringBuilder();

                try {
                    Document doc = Jsoup.connect("https://knowyourmeme.com/memes/" + memeURL).get();
                    String title = doc.title();
                    Element content = doc.getElementById("entry_body");
                    Element about = content.getElementsByTag("p").first();

                    String context = ("About the meme: " + about.text());

                    builder.append(title).append("\n");
                    builder.append("\n").append(context);

                } catch (IOException e) {
                    builder.append("error : ").append(e.getMessage()).append("\n");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        memeAbout = builder.toString();
                        t1.speak(memeAbout, TextToSpeech.QUEUE_ADD, null);
                    }
                });
            }
        }).start();
    }

    private void initializeMap(HashMap<String, Mat> map){
        //very bad initialization of templates HashMap until scaleable alternative is fixed
        Bitmap adv = BitmapFactory.decodeResource(getResources(), R.drawable.t_advice_dog);
        Bitmap pen = BitmapFactory.decodeResource(getResources(), R.drawable.t_socially_awkward_penguin);
        Bitmap blb = BitmapFactory.decodeResource(getResources(), R.drawable.t_bad_luck_brian);
        Bitmap ggg = BitmapFactory.decodeResource(getResources(), R.drawable.t_good_guy_greg);
        Bitmap twbg = BitmapFactory.decodeResource(getResources(), R.drawable.t_that_would_be_great);
        Mat im1 = new Mat();
        Mat im2 = new Mat();
        Mat im3 = new Mat();
        Mat im4 = new Mat();
        Mat im5 = new Mat();
        Utils.bitmapToMat(adv, im1);
        Utils.bitmapToMat(pen, im2);
        Utils.bitmapToMat(blb, im3);
        Utils.bitmapToMat(ggg, im4);
        Utils.bitmapToMat(twbg, im5);
        map.put("Advice Dog", im1);
        map.put("Socially Awkward Penguin", im2);
        map.put("Bad Luck Brian", im3);
        map.put("Good Guy Greg", im4);
        map.put("That Would Be Great", im5);

    }

    //Calculates the histogram of an image and compares it against template database;
    //Returns entry that is a best match and has over a .50 similarity.
    private Map.Entry<String, Mat> findMatch(Mat meme, HashMap<String, Mat> templates) {
        double res = 0;
        double max = 0;
        Map.Entry<String, Mat> match = null;
        for (Map.Entry<String, Mat> entry : templates.entrySet()) {
            Mat hist0 = new Mat();
            Mat hist1 = new Mat();

            int hist_bins = 30;
            int hist_range[] = {0, 180};

            MatOfFloat ranges = new MatOfFloat(0f, 256f);
            MatOfInt histSize = new MatOfInt(25);

            Imgproc.calcHist(Arrays.asList(meme), new MatOfInt(0), new Mat(), hist0, histSize, ranges);
            Imgproc.calcHist(Arrays.asList(entry.getValue()), new MatOfInt(0), new Mat(), hist1, histSize, ranges);

            res = Imgproc.compareHist(hist0, hist1, Imgproc.CV_COMP_CORREL);
            if (max < res && res > .50) {
                match = entry;
                max = res;
            }
        }
        return match;
    }

    protected void readImage(){
        if(selectedImage!=null){
            TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();//build text recognizer
            Bitmap top = Bitmap.createBitmap(selectedImage, 0, 0, selectedImage.getWidth(), selectedImage.getHeight()/2);
            Bitmap bot = Bitmap.createBitmap(selectedImage, 0, selectedImage.getHeight()/2, selectedImage.getWidth(), selectedImage.getHeight()/2);
            Frame frameTop = new Frame.Builder().setBitmap(top).build();//create frame of bitmap
            Frame frameBot = new Frame.Builder().setBitmap(bot).build();//create frame of bitmap
            Mat img = new Mat();
            Utils.bitmapToMat(selectedImage, img);
            Map.Entry<String, Mat> meme = findMatch(img, map);
            if (meme != null){
                memeMatched = true;
                memeName = meme.getKey();
                memeURL = memeName;
                String[] s = memeURL.split(" ");
                memeURL = "";
                for (int x = 0; x < s.length; x++){
                    memeURL += s[x] + "-";
                }
                memeURL = memeURL.substring(0, memeURL.length()-1);
                memeURL = memeURL.toLowerCase();
                t1.speak(memeName, TextToSpeech.QUEUE_FLUSH, null);
                t1.playSilentUtterance(300, TextToSpeech.QUEUE_ADD, null);
                t1.speak(convertDetectToString(textRecognizer.detect(frameTop)), TextToSpeech.QUEUE_ADD, null);
                t1.playSilentUtterance(500, TextToSpeech.QUEUE_ADD, null);
                t1.speak(convertDetectToString(textRecognizer.detect(frameBot)), TextToSpeech.QUEUE_ADD, null);
                t1.playSilentUtterance(300, TextToSpeech.QUEUE_ADD, null);
            }
            else{
                memeMatched = false;
                t1.speak("There was no match found for this meme. . . . . . . . ", TextToSpeech.QUEUE_FLUSH, null);
                t1.playSilentUtterance(300, TextToSpeech.QUEUE_ADD, null);
                t1.speak(convertDetectToString(textRecognizer.detect(frameTop)), TextToSpeech.QUEUE_ADD, null);
                t1.playSilentUtterance(500, TextToSpeech.QUEUE_ADD, null);
                t1.speak(convertDetectToString(textRecognizer.detect(frameBot)), TextToSpeech.QUEUE_ADD, null);
                t1.playSilentUtterance(300, TextToSpeech.QUEUE_ADD, null);

            }
        }
        else{
            Toast.makeText(getApplicationContext(), "No meme has been selected.",Toast.LENGTH_LONG).show();
        }
    }

    protected void openImage(ImageView imageView){
        currentImageView = imageView;
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,RESULT_LOAD_IMG);
    }

    protected String convertDetectToString(SparseArray<TextBlock> text){


        StringBuilder stringBuilder = new StringBuilder();

        for (int i=0; i<text.size(); i++){
            String str = text.valueAt(i).getValue();
            stringBuilder.append(str + " ");


        }
        return stringBuilder.toString();
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                selectedImage = BitmapFactory.decodeStream(imageStream);//Image bitmap
                currentImageView.setImageBitmap(selectedImage);


            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(getApplicationContext(), "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }


    //On Pause implementation
    public void onPause() {
        if (t1 != null) {
            t1.stop();
            t1.shutdown();
        }
        super.onPause();
    }

    //On Resume Override
    @Override
    public void onResume(){
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.ENGLISH);
                    t1.setSpeechRate((float)1);
                }
            }
            });
        super.onResume();
    }
}
