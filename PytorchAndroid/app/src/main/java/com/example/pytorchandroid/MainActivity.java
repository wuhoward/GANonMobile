package com.example.pytorchandroid;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.view.View;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    int cameraRequestCode = 001;
    int PICK_IMAGE = 002;

    Classifier classifier;
    File file;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Context context = getApplicationContext();
        file = new File(context.getExternalCacheDir() + "/tmp.png");
        imageUri = FileProvider.getUriForFile(MainActivity.this, getApplicationContext().getPackageName() + ".provider", file);
        Button capture = findViewById(R.id.capture);
        Button capture2 = findViewById(R.id.capture2);

        String[] models = { "gan_full", "gan_compressed" };
        Spinner spin = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, models);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);

        capture.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view){

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(cameraIntent,cameraRequestCode);
                String model = spin.getSelectedItem().toString();
                classifier = new Classifier(Utils.assetFilePath(MainActivity.this, model+".pt"));

            }


        });

        capture2.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view){

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                String model = spin.getSelectedItem().toString();
                classifier = new Classifier(Utils.assetFilePath(MainActivity.this, model+".pt"));

            }


        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == cameraRequestCode && resultCode == RESULT_OK){

            Intent resultView = new Intent(this,Result.class);

            Bitmap rotatedBitmap = null;
            try {
                rotatedBitmap = Utils.handleSamplingAndRotationBitmap(this, imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            resultView.putExtra("imageUri", imageUri.toString());
            long startTime = System.nanoTime();
            Bitmap pred = classifier.predict(rotatedBitmap);
            double difference = (double) System.nanoTime() - startTime;
            resultView.putExtra("latency", String.format("%.3f", difference/1000000000));
            resultView.putExtra("pred",pred);

            startActivity(resultView);

        } else if (requestCode == PICK_IMAGE) {
            Intent resultView = new Intent(this,Result.class);
            Uri selectedImageUri = data.getData();
            resultView.putExtra("imageUri", selectedImageUri.toString());
            Bitmap rotatedBitmap = null;
            try {
                rotatedBitmap = Utils.handleSamplingAndRotationBitmap(this, selectedImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            long startTime = System.nanoTime();
            Bitmap pred = classifier.predict(rotatedBitmap);
            double difference = (double) System.nanoTime() - startTime;
            resultView.putExtra("latency", String.format("%.3f", difference/1000000000));
            resultView.putExtra("pred",pred);

            startActivity(resultView);
        }

    }

}

