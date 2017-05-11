package com.example.c_gerasimovich.taleme.ui;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.c_gerasimovich.taleme.R;

import java.io.IOException;

public class Karaoke extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_karaoke);

        final Handler mHandler = new Handler();
        final TextView tv = new TextView(this);
        tv.setText("Playing1... ");
        setContentView(tv);

        final MediaPlayer mPlayer = MediaPlayer.create(this, R.raw.detskaya);

        try {
            mPlayer.prepare();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mPlayer.start();


        final String words[] = {
                "Nicholas ",// 0
                "was ", // 1
                "older ",// 2
                "than ",// 3
                "sin ",// 4
                "and ",// 5
                "his ",// 6
                "beard ",// 7
                "could ",// 8
                "go ",// 9
                "no ",// 10
                "whiter. "// 11
        };

        final long startEndTime[][]={
                {   //start time
                        1148,// 0,0
                        1826, // 0,1
                        2766,// 0,2
                        3079,// 0,3
                        3549,// 0,4
                        4540,// 0,5
                        4697,// 0,6
                        4801,// 0,7
                        5114,// 0,8
                        5323,// 0,9
                        5532,// 0,10
                        5845// 0,11
                },
                {   //end time
                        1357,// 1,0
                        2192, // 1,1
                        3027,// 1,2
                        3183,// 1,3
                        3966,// 1,4
                        4645,// 1,5
                        4749,// 1,6
                        4958,// 1,7
                        5219,// 1,8
                        5427,// 1,9
                        5740,// 1,10
                        6210// 1,11
                }

        };
        mHandler.post(new Runnable(){

            public void run(){
                final long currentPos = mPlayer.getCurrentPosition();

                int x = 0;

                while( x < 12){
                    if( currentPos > startEndTime[0][x] && currentPos < startEndTime[1][x] ){//0
                        tv.append(words[x]);
                        words[x]="";
                    }
                    x++;
                }

                mHandler.postDelayed(this, 1);
            }
        });


    }

}
