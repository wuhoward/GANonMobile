package com.example.pytorchandroid;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;

public class Result extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String latency = getIntent().getStringExtra("latency");
        Bitmap pred = (Bitmap) getIntent().getParcelableExtra("pred");

        ImageView imageView = findViewById(R.id.image);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setAdjustViewBounds(true);
        imageView.setImageBitmap(pred);

        TextView textView = findViewById(R.id.label);
        textView.setText("Latency: " + latency + "s");
    }
}