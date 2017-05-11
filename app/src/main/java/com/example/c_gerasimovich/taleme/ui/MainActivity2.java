package com.example.c_gerasimovich.taleme.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.c_gerasimovich.taleme.R;

public class MainActivity2 extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    }
    public void activity_player(View view) {
        Intent intent = new Intent(this, Player.class);
        startActivity(intent);
    }
    public void activity_recorder(View view) {
        Intent intent = new Intent(this, Recorder.class);
        startActivity(intent);
    }
    public void activity_karaoke(View view) {
        Intent intent = new Intent(this, Karaoke.class);
        startActivity(intent);
    }

}
