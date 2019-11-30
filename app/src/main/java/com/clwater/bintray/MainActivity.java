package com.clwater.bintray;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.clwater.bintraylibrary.Test;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Test.test(this);
    }
}
