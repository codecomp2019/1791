package com.canthackthis.memewhisperer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.FileNotFoundException;
import java.io.InputStream;




public class GalleryActivity extends AppCompatActivity {
    private static int RESULT_LOAD_IMG = 1;
    private Bitmap selectedImage = null;
    static{
        System.loadLibrary("opencv_java3");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        OpenCVLoader.initDebug();
        final Button buttonGallery = (Button) findViewById(R.id.gallery);
        final Button buttonRead = (Button) findViewById(R.id.read);
        final ImageView imageView = findViewById(R.id.imageView);

        // final RecyclerView recyclerView = findViewById(R.id.recyclerView);
        //recyclerView.getResources();
      //  recyclerView.draw();
        buttonGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImage();
                imageView.setImageBitmap(selectedImage);//set image to new bitmap
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Mat matOrg = new Mat();
                Mat matGry = new Mat();
                readImage();
            //    Utils.bitmapToMat(selectedImage,matOrg);//bitmap to mat original
            //    Imgproc.cvtColor(matOrg,matGry, Imgproc.COLOR_BGR2GRAY);//original to grayscale
            //    Core.bitwise_not(matGry,matGry);//inverse graytogray
            //    Imgproc.equalizeHist(matGry,matGry);//increasecontrast
            //    Bitmap selectedImageGry = selectedImage;//initialize new bitmap
            //    Utils.matToBitmap(matGry,selectedImageGry);//revert gray to new bitmap

            }
        });
        buttonRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readImage();
            }
        });
    }

    protected void readImage(){
        if(selectedImage!=null){
            TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();//build text recognizer
            Frame frame = new Frame.Builder().setBitmap(selectedImage).build();//create frame of bitmap
            Toast.makeText(getApplicationContext(), convertDetectToString(textRecognizer.detect(frame)),Toast.LENGTH_LONG).show();//generate toast
        }
        else{
            Toast.makeText(getApplicationContext(), "No meme has been selected.",Toast.LENGTH_LONG).show();
        }
    }

    protected void openImage(){
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
                final ImageView imageView = findViewById(R.id.imageView);
                imageView.setImageBitmap(selectedImage);


            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(getApplicationContext(), "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }
}
