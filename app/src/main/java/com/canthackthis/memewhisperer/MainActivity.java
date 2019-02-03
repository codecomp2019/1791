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
    private TextView result;
    private static int RESULT_LOAD_IMG = 1;
    private Bitmap selectedImage = null;
    private ImageView currentImageView;
   // private Button
    static{
        System.loadLibrary("opencv_java3");

    }
    //First Page Buttons
    Button choose;
    //Second Page Buttons
    Button read;
    Button gallery;
    Button next;
    //Third Page Buttons
    Button read1;
    Button newMeme;

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
               /* this.setContentView(R.layout.next);
                read =  findViewById(R.id.read);
                gallery =  findViewById(R.id.gallery);
                next =  findViewById(R.id.next);
                read.setOnClickListener(this);
                gallery.setOnClickListener(this);
                next.setOnClickListener(this);*/
                this.setContentView(R.layout.image_area);
                read1 =  findViewById(R.id.read1);
                newMeme =  findViewById(R.id.newMeme);
                read1.setOnClickListener(this);
                newMeme.setOnClickListener(this);
                ImageView imageView1 = findViewById(R.id.imageView1);
                openImage(imageView1);
                break;
            case R.id.read1:
                //read the image in selected
                readImage();
                if (memeMatched)getContext();
                break;
            case R.id.gallery:
                //openGallery
                ImageView imageViewNext = findViewById(R.id.imageView);
                openImage(imageViewNext);
                break;
            case R.id.next:
                this.setContentView(R.layout.image_area);
                read1 =  findViewById(R.id.read1);
                newMeme =  findViewById(R.id.newMeme);
                read1.setOnClickListener(this);
                newMeme.setOnClickListener(this);
                break;
            case R.id.newMeme:
                //openGallery
                imageView1 = findViewById(R.id.imageView1);
                openImage(imageView1);
                break;

        }
    }

    //onCreate override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_area);
        setContentView(R.layout.next);
        setContentView(R.layout.start_buttons);
        temp = (Button) findViewById(R.id.temp);
        choose = (Button) findViewById(R.id.choose);
        temp.setOnClickListener(this);
        choose.setOnClickListener(this);



        
        if (OpenCVLoader.initDebug()){
            Toast.makeText(getApplicationContext(), "YO", Toast.LENGTH_SHORT).show();
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

                    // builder.append("\n").append("About : ").append(about.attr("p")).append(about.text());

                } catch (IOException e) {
                    builder.append("error : ").append(e.getMessage()).append("\n");
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        memeAbout = builder.toString();
                        t1.speak(memeAbout, TextToSpeech.QUEUE_ADD, null);
                        Toast.makeText(getApplicationContext(), memeAbout, Toast.LENGTH_SHORT).show();
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
            Frame frame = new Frame.Builder().setBitmap(selectedImage).build();//create frame of bitmap
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
                t1.speak(memeName+". . . . . . . . "+convertDetectToString(textRecognizer.detect(frame))+". . . . . . . . ", TextToSpeech.QUEUE_FLUSH, null);

            }
            else{
                memeMatched = false;
                t1.speak("There was no match found for this meme. . . . . . . . ", TextToSpeech.QUEUE_FLUSH, null);
                t1.speak(convertDetectToString(textRecognizer.detect(frame)), TextToSpeech.QUEUE_ADD, null);


            }
            Toast.makeText(getApplicationContext(), convertDetectToString(textRecognizer.detect(frame)),Toast.LENGTH_LONG).show();//generate toast
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
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent,RESULT_LOAD_IMG);
    }

    protected String convertDetectToString(SparseArray<TextBlock> text){


        StringBuilder stringBuilder = new StringBuilder();

        for (int i=0; i<text.size(); i++){
            // Toast.makeText(getApplicationContext(),text.valueAt(i).getValue(),Toast.LENGTH_LONG).show();;
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


    public void onPause() {
        if (t1 != null) {
            t1.stop();
            t1.shutdown();
        }
        super.onPause();
    }

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
