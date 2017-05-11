package com.example.c_gerasimovich.taleme.ui;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.c_gerasimovich.taleme.R;

import java.io.IOException;

public class Player extends Activity {

    private Button buttonPlayStop;
    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;

    private final Handler handler = new Handler();
    public String mTitle_of_tale;
    public TextView taleText;
    public MediaPlayer mPlayerBackground;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_player);

        Intent intent = getIntent();
        mTitle_of_tale = getIntent().getStringExtra(ListenListActivity.TITLE_OF_TALE);
        taleText = (TextView) findViewById(R.id.tale_text_in_player);


        setKaraokeText();


        initViews();
    }

        private void setKaraokeText() {

            /*taleText.setText(
    //        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(taleKey,"")
                    "When Osmo, the Bear, was left alone in the net, he thrashed about this way and that until he was exhausted. Then he fell asleep.\n" +
                            "\n" +
                            "While he slept a host of little Mice began playing all over his great body.\n" +
                            "\n" +
                            "Their tiny feet tickled him and he woke with a start. The Mice scampered off, all but one that Osmo caught under his paw.\n" +
                            "\n" +
                            "“Tweek! Tweek!” the frightened little Mouse cried. “Let me go! Let me go! Please let me go! If you do I’ll reward you some day! I promise I will!”\n" +
                            "\n" +
                            "[308] Osmo let out a great roar of laughter.\n" +
                            "\n" +
                            "“What, little one? You’ll reward me! Ha! Ha! That is good! The Mouse will reward the Bear! Well now, that is a joke! However, little one, I will let you go! You’re too weak and insignificant for me to kill and too small to eat. So run along!”\n" +
                            "\n" +
                            "With that the Bear lifted his paw and the little Mouse scampered off.\n" +
                            "\n" +
                            "“It will reward me for my kindness!” Osmo repeated, and in spite of the fact that he was fast caught in a net he shook again with laughter.\n" +
                            "\n" +
                            "He was still laughing when the little Mouse returned with a great army of his fellows. All the host at once began gnawing at the ropes of the net and in no time at all they had freed the big Bear.\n" +
                            "\n" +
                            "“You see,” the little Mouse said, “although we are weak and insignificant we can reward a kindness!”\n" +
                            "\n" +
                            "Osmo was so ashamed for having laughed at the Mice on account of their size that all he could say as he shambled off into the forest was:\n" +
                            "\n" +
                            "“Thanks!”"
            );*/
    //        taleText.setText();


            final Handler mHandler = new Handler();
    //        final TextView tv = new TextView(this);
            taleText.setText("Playing1... ");
    //        setContentView(tv);

            mPlayerBackground = MediaPlayer.create(this, R.raw.detskaya);

            try {
                mPlayerBackground.prepare();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mPlayerBackground.start();


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

            final long startEndTime[][] = {
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
            mHandler.post(new Runnable() {

                public void run() {
                    final long currentPos = mPlayerBackground.getCurrentPosition();

                    int x = 0;

                    while (x < 12) {
                        if (currentPos > startEndTime[0][x] && currentPos < startEndTime[1][x]) {//0
                            taleText.append(words[x]);
                            words[x] = "";
                        }
                        x++;
                    }

                    mHandler.postDelayed(this, 1);
                }
            });


        }

    private void initViews() {
        buttonPlayStop = (Button) findViewById(R.id.ButtonPlayStop);
        mediaPlayer = new MediaPlayer();

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                seekChange(v);
                return false;
            }
        });
    }

    private void seekChange(View v) {
        if (mediaPlayer.isPlaying()) {
            SeekBar sb = (SeekBar) v;
            mediaPlayer.seekTo(sb.getProgress());
        }
    }

    public void playAndStop(View v) {
        if (buttonPlayStop.getText().toString().toLowerCase().equals(getString(R.string.play_str).toLowerCase())) {
            Log.d("DEV: ", buttonPlayStop.getText().toString().toLowerCase());
            Log.d("DEV: ", getString(R.string.play_str).toLowerCase());
            try {
                mediaPlayer.setDataSource(Environment.getExternalStorageDirectory().getPath() + "/" + mTitle_of_tale);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            buttonPlayStop.setText(getString(R.string.pause_str));
            try {
                mediaPlayer.start();
                startPlayProgressUpdater();
            } catch (IllegalStateException e) {
                mediaPlayer.pause();
            }
        } else {
            buttonPlayStop.setText(getString(R.string.play_str));
            mediaPlayer.pause();
        }
    }

    public void startPlayProgressUpdater() {
        seekBar.setProgress(mediaPlayer.getCurrentPosition());

        if (mediaPlayer.isPlaying()) {
            Runnable notification = new Runnable() {
                public void run() {
                    startPlayProgressUpdater();
                }
            };
            handler.postDelayed(notification, 1000);
        } else {
            mediaPlayer.pause();
            buttonPlayStop.setText(getString(R.string.play_str));
            seekBar.setProgress(0);
        }
    }

    @Override
    public void onBackPressed() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }

        if (mPlayerBackground.isPlaying()) {
            mPlayerBackground.stop();
        }

        finish();
    }
}