package com.example.miljanamilena.pathfinder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent map = new Intent("android.intent.action.MAP");
        startActivity(map);
        finish();
    }
}
