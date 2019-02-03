package com.canthackthis.memewhisperer;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("opencv_java3");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OpenCVLoader.initDebug();

        //set up database
        HashMap<String, Mat> templates = new HashMap<String, Mat>();
        initializeMap(templates);

        //input file
        Bitmap memeIm = BitmapFactory.decodeResource(getResources(), R.drawable.adv3);
        Mat meme = new Mat();
        Utils.bitmapToMat(memeIm, meme); //convert meme bitmap into a Mat object
        Map.Entry<String, Mat> match = findMatch(meme, templates);

        //output results
        Toast.makeText(this, "Name: " + match.getKey(), Toast.LENGTH_LONG).show();


        //ignore below until findMatch is debugged
        /*String name = "";
        double max = 0;
        double res = 0;
        for (Map.Entry<String, Bitmap> entry : templates.entrySet()) {
            Mat img1 = new Mat();
            Mat img2 = new Mat();
            Mat hist0 = new Mat();
            Mat hist1 = new Mat();
            Utils.bitmapToMat(meme, img1);
            Utils.bitmapToMat(entry.getValue(), img2);
            int hist_bins = 30;
            int hist_range[] = {0, 180};
            MatOfFloat ranges = new MatOfFloat(0f, 256f);
            MatOfInt histSize = new MatOfInt(25);

            Imgproc.calcHist(Arrays.asList(img1), new MatOfInt(0), new Mat(), hist0, histSize, ranges);
            Imgproc.calcHist(Arrays.asList(img2), new MatOfInt(0), new Mat(), hist1, histSize, ranges);

            res = Imgproc.compareHist(hist0, hist1, Imgproc.CV_COMP_CORREL);
            if (max < res && res > .50) {
                name = entry.getKey();
                max = res;
            }
        }*/

    }
    //Calculates the histogram of an image and compares it against template database;
    //Returns entry that is a best match and has over a .50 similarity.
    private Map.Entry<String, Mat> findMatch(Mat meme, HashMap<String, Mat> templates){
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
    private void initializeMap(HashMap<String, Mat> map){
        //very bad initialization of templates HashMap until scaleable alternative is fixed
        Bitmap adv = BitmapFactory.decodeResource(getResources(), R.drawable.t_advice_dog);
        Bitmap pen = BitmapFactory.decodeResource(getResources(), R.drawable.t_socially_awkward_penguin);
        Bitmap blb = BitmapFactory.decodeResource(getResources(), R.drawable.t_bad_luck_brian);
        Mat im1 = new Mat();
        Mat im2 = new Mat();
        Mat im3 = new Mat();
        Utils.bitmapToMat(adv, im1);
        Utils.bitmapToMat(pen, im2);
        Utils.bitmapToMat(blb, im3);
        map.put("Advice Dog", im1);
        map.put("Socially Awkward Penguin", im2);
        map.put("Bad Luck Brian", im3);
        //ignore below until relative paths are figured out
        /*File dir = new File("../../res/drawable/");
        Log.v("Files", dir.getAbsolutePath());
        String path = "";
        File [] files = dir.listFiles();
        Log.v("Files",dir.exists()+"");
        Log.v("Files",dir.isDirectory()+"");
        Log.v("Files",dir.listFiles()+"");
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().substring(0, 1) == "t_") { //"t_" signifies a template to include in the HashMap
                Bitmap image = BitmapFactory.decodeFile("../../res/drawable/" + files[i].getName());
                Mat img = new Mat();
                Utils.bitmapToMat(image, img); //Convert image to Mat for processing
                String[] filename = files[i].getName().split("_"); //Format name for display
                String name = "";
                for (int j = 1; j < filename.length; j++) {
                    name += filename[j] + " ";
                }
                name = name.substring(0, name.length() - 4);
                map.put(name, img); //Add template to map
            }
        }*/
    }



}
