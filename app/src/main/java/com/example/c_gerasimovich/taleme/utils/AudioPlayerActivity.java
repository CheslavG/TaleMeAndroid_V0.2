package com.example.c_gerasimovich.taleme.utils;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cleveroad.audiovisualization.DbmHandler;
import com.cleveroad.audiovisualization.GLAudioVisualizationView;
import com.example.c_gerasimovich.taleme.R;
import com.example.c_gerasimovich.taleme.model.AudioChannel;
import com.example.c_gerasimovich.taleme.model.AudioSampleRate;
import com.example.c_gerasimovich.taleme.model.AudioSource;
import com.example.c_gerasimovich.taleme.ui.ListenListActivity;
import com.example.c_gerasimovich.taleme.ui.MainActivity;
import com.example.c_gerasimovich.taleme.ui.PreferencesManager;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import omrecorder.AudioChunk;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.Recorder;

public class AudioPlayerActivity extends AppCompatActivity
        implements PullTransport.OnAudioChunkPulledListener, MediaPlayer.OnCompletionListener {

    private String filePath, mTitle_of_tale;
    private AudioSource source;
    private AudioChannel channel;
    private AudioSampleRate sampleRate;
    private int color = Color.parseColor("#F4511E");
    private boolean autoStart;
    private boolean keepDisplayOn;

    private Recorder recorder;
    private VisualizerHandler visualizerHandler;

    private Timer timer;
    private MenuItem saveMenuItem;
    private int recorderSecondsElapsed;
    private int playerSecondsElapsed;
    private boolean isRecording;

    private RelativeLayout contentLayout;
    private GLAudioVisualizationView visualizerView;
    private TextView statusView;
    private TextView timerView;
    private ImageButton restartView;
    private ImageButton recordView;
    private ImageButton playView;
    private String taleKey;
    public TextView taleText;
    public MediaPlayer mBckgPlayer, mMainPlayer;
    public Handler mHandler;
    public Runnable mRunnable;
    private NestedScrollView mNestedScrollView;
    public int mInt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aar_activity_audio_player);


        if (savedInstanceState != null) {
            filePath = savedInstanceState.getString(AndroidAudioRecorder.EXTRA_FILE_PATH);
            source = (AudioSource) savedInstanceState.getSerializable(AndroidAudioRecorder.EXTRA_SOURCE);
            channel = (AudioChannel) savedInstanceState.getSerializable(AndroidAudioRecorder.EXTRA_CHANNEL);
            sampleRate = (AudioSampleRate) savedInstanceState.getSerializable(AndroidAudioRecorder.EXTRA_SAMPLE_RATE);
//            color = savedInstanceState.getInt(AndroidAudioRecorder.EXTRA_COLOR);
            autoStart = savedInstanceState.getBoolean(AndroidAudioRecorder.EXTRA_AUTO_START);
            keepDisplayOn = savedInstanceState.getBoolean(AndroidAudioRecorder.EXTRA_KEEP_DISPLAY_ON);
            mTitle_of_tale = savedInstanceState.getString(ListenListActivity.TITLE_OF_TALE);

        } else {
            source = (AudioSource) getIntent().getSerializableExtra(AndroidAudioRecorder.EXTRA_SOURCE);
            channel = (AudioChannel) getIntent().getSerializableExtra(AndroidAudioRecorder.EXTRA_CHANNEL);
            sampleRate = (AudioSampleRate) getIntent().getSerializableExtra(AndroidAudioRecorder.EXTRA_SAMPLE_RATE);
//            color = getIntent().getIntExtra(AndroidAudioRecorder.EXTRA_COLOR, Color.BLACK);
            autoStart = getIntent().getBooleanExtra(AndroidAudioRecorder.EXTRA_AUTO_START, false);
            keepDisplayOn = getIntent().getBooleanExtra(AndroidAudioRecorder.EXTRA_KEEP_DISPLAY_ON, false);
            mTitle_of_tale = getIntent().getStringExtra(ListenListActivity.TITLE_OF_TALE);
            filePath = Environment.getExternalStorageDirectory().getPath() + "/" + mTitle_of_tale;

        }

        taleKey = getIntent().getStringExtra(AndroidAudioRecorder.EXTRA_TALE);


        if (keepDisplayOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setBackgroundDrawable(
                    new ColorDrawable(Util2.getDarkerColor(color)));
            getSupportActionBar().setHomeAsUpIndicator(
                    ContextCompat.getDrawable(this, R.drawable.aar_ic_clear));
        }

        visualizerView = new GLAudioVisualizationView.Builder(this)
                .setLayersCount(1)
                .setWavesCount(6)
                .setWavesHeight(R.dimen.aar_wave_height)
                .setWavesFooterHeight(R.dimen.aar_footer_height)
                .setBubblesPerLayer(20)
                .setBubblesSize(R.dimen.aar_bubble_size)
                .setBubblesRandomizeSize(true)
                .setBackgroundColor(Util2.getDarkerColor(color))
                .setLayerColors(new int[]{color})
                .build();

        contentLayout = (RelativeLayout) findViewById(R.id.content);
        statusView = (TextView) findViewById(R.id.status);
        timerView = (TextView) findViewById(R.id.timer);
        taleText = (TextView) findViewById(R.id.tale_text);
        restartView = (ImageButton) findViewById(R.id.restart);
        recordView = (ImageButton) findViewById(R.id.record);
        playView = (ImageButton) findViewById(R.id.play);
        mNestedScrollView = (NestedScrollView) findViewById(R.id.scroll);

        contentLayout.setBackgroundColor(Util2.getDarkerColor(color));
        contentLayout.addView(visualizerView, 0);
        restartView.setVisibility(View.INVISIBLE);
//        playView.setVisibility(View.INVISIBLE);

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

       /* if (Util2.isBrightColor(color)) {
            ContextCompat.getDrawable(this, R.drawable.aar_ic_clear)
                    .setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            ContextCompat.getDrawable(this, R.drawable.aar_ic_check)
                    .setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            statusView.setTextColor(Color.BLACK);
            timerView.setTextColor(Color.BLACK);
            restartView.setColorFilter(Color.BLACK);
            recordView.setColorFilter(Color.BLACK);
            playView.setColorFilter(Color.BLACK);
        }*/
    }

    private void startKaraokeText() {

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


        mHandler = new Handler();
//        final TextView tv = new TextView(this);
//        taleText.setText("Playing1... ");
//        setContentView(tv);
/*
        mBckgPlayer = MediaPlayer.create(this, R.raw.nicholas);

        try {
            mBckgPlayer.prepare();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mBckgPlayer.start();*/


        // //region    ========================== find song text ==========================


        String taletitle = mTitle_of_tale.replace("_", " ").replace(".wav", "");
        int res = R.string.TALE1;
        switch (taletitle) {
            case PreferencesManager.TALE1:
                res = R.string.TALE1;
                break;
            case PreferencesManager.TALE2:
                res = R.string.TALE2;
                break;

        }


        //endregion ========================== find song text ==========================

        final String words[] = getString(res).split(" "); /*{
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
        };*/

        Log.d("DEV: ", Arrays.toString(words));

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

        mRunnable = new Runnable() {

            public void run() {
                final long currentPos = mBckgPlayer.getCurrentPosition();

                int x = 0;

                /*while (x < 12) {
                    if (currentPos > startEndTime[0][x] && currentPos < startEndTime[1][x]) {//0
                        taleText.append(words[x]);
                        words[x] = "";
                    }
                    x++;
                }*/

                if (mInt < words.length) {
                    taleText.append(words[mInt] + " ");
                    mInt++;
                    mNestedScrollView.fullScroll(View.FOCUS_DOWN);
                    mHandler.postDelayed(this, 200);
                }


            }
        };
        mHandler.post(mRunnable);


    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (autoStart && !isRecording) {
            toggleRecording(null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            visualizerView.onResume();
        } catch (Exception e) {
        }
    }

    @Override
    protected void onPause() {
        restartRecording(null);
        try {
            visualizerView.onPause();
        } catch (Exception e) {
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        restartRecording(null);
        setResult(RESULT_CANCELED);
        try {
            visualizerView.release();
        } catch (Exception e) {
        }
        super.onDestroy();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(AndroidAudioRecorder.EXTRA_FILE_PATH, filePath);
        outState.putInt(AndroidAudioRecorder.EXTRA_COLOR, color);
        outState.putString(ListenListActivity.TITLE_OF_TALE, mTitle_of_tale);

        super.onSaveInstanceState(outState);
    }

    public void togglePlaying(View v) {
//        pauseRecording();
        Util2.wait(100, new Runnable() {
            @Override
            public void run() {
                if (isPlaying()) {
                    stopPlaying();
                } else {
                    startPlaying();
                }
            }
        });
    }

    private void startPlaying() {
        try {
//            stopRecording();

            mMainPlayer = new MediaPlayer();
            mBckgPlayer = MediaPlayer.create(this, R.raw.detskaya);
            try {
                mMainPlayer.setDataSource(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }


            mBckgPlayer.start();

            mMainPlayer.prepare();
            mMainPlayer.start();

            startKaraokeText();

            visualizerView.linkTo(DbmHandler.Factory.newVisualizerHandler(this, mMainPlayer));
            visualizerView.post(new Runnable() {
                @Override
                public void run() {
                    mMainPlayer.setOnCompletionListener(AudioPlayerActivity.this);
                }
            });

            timerView.setText("00:00:00");
            statusView.setText(R.string.aar_playing);
//            statusView.setVisibility(View.VISIBLE);
            playView.setImageResource(R.drawable.aar_ic_stop);

            playerSecondsElapsed = 0;
            startTimer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopPlaying() {
        statusView.setText("");
        mInt = 0;
//        statusView.setVisibility(View.INVISIBLE);
        playView.setImageResource(R.drawable.aar_ic_play);

        visualizerView.release();
        if (visualizerHandler != null) {
            visualizerHandler.stop();
        }

        mHandler.removeCallbacks(mRunnable);
        taleText.setText("");
        timerView.setText("00:00:00");

        if (mMainPlayer != null) {
            try {
                mMainPlayer.release();
//                mMainPlayer.reset();
            } catch (Exception e) {
            }
        }

        if (mBckgPlayer != null) {
            try {
                mBckgPlayer.release();
//                mBckgPlayer.reset();
            } catch (Exception e) {
            }
        }

        stopTimer();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.aar_audio_recorder, menu);
        saveMenuItem = menu.findItem(R.id.action_save);
        saveMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.aar_ic_check));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
        } else if (i == R.id.action_save) {
            selectAudio();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAudioChunkPulled(AudioChunk audioChunk) {
        float amplitude = isRecording ? (float) audioChunk.maxAmplitude() : 0f;
        visualizerHandler.onDataReceived(amplitude);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        stopPlaying();
    }

    private void selectAudio() {
        stopRecording();
        setResult(RESULT_OK);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void toggleRecording(View v) {
        stopPlaying();
        Util2.wait(100, new Runnable() {
            @Override
            public void run() {
                if (isRecording) {
                    pauseRecording();
                } else {
                    resumeRecording();
                }
            }
        });
    }


    public void restartRecording(View v) {
        if (isRecording) {
            stopRecording();
        } else if (isPlaying()) {
            stopPlaying();
        } else {
            visualizerHandler = new VisualizerHandler();
            visualizerView.linkTo(visualizerHandler);
            visualizerView.release();
            if (visualizerHandler != null) {
                visualizerHandler.stop();
            }
        }
        saveMenuItem.setVisible(false);
        statusView.setVisibility(View.INVISIBLE);
        restartView.setVisibility(View.INVISIBLE);
        playView.setVisibility(View.INVISIBLE);
        recordView.setImageResource(R.drawable.aar_ic_rec);
        timerView.setText("00:00:00");
        recorderSecondsElapsed = 0;
        playerSecondsElapsed = 0;
    }

    private void resumeRecording() {
        isRecording = true;
        saveMenuItem.setVisible(false);
        statusView.setText(R.string.aar_recording);
//        statusView.setVisibility(View.VISIBLE);
        restartView.setVisibility(View.INVISIBLE);
        playView.setVisibility(View.INVISIBLE);
        recordView.setImageResource(R.drawable.aar_ic_pause);
        playView.setImageResource(R.drawable.aar_ic_play);

        visualizerHandler = new VisualizerHandler();
        visualizerView.linkTo(visualizerHandler);

        if (recorder == null) {
            timerView.setText("00:00:00");

            recorder = OmRecorder.wav(
                    new PullTransport.Default(Util2.getMic(source, channel, sampleRate), AudioPlayerActivity.this),
                    new File(filePath));
        }
        recorder.resumeRecording();

        startTimer();
    }

    private void pauseRecording() {
        isRecording = false;
        if (!isFinishing()) {
//            saveMenuItem.setVisible(true);
        }
        statusView.setText(R.string.aar_paused);
        /*statusView.setVisibility(View.VISIBLE);
        restartView.setVisibility(View.VISIBLE);
        playView.setVisibility(View.VISIBLE);*/
        recordView.setImageResource(R.drawable.aar_ic_rec);
        playView.setImageResource(R.drawable.aar_ic_play);

        visualizerView.release();
        if (visualizerHandler != null) {
            visualizerHandler.stop();
        }

        if (recorder != null) {
            recorder.pauseRecording();
        }

        stopTimer();
    }

    private void stopRecording() {
        visualizerView.release();
        if (visualizerHandler != null) {
            visualizerHandler.stop();
        }

        recorderSecondsElapsed = 0;
        if (recorder != null) {
            recorder.stopRecording();
            recorder = null;
        }

        stopTimer();
    }


    private boolean isPlaying() {
        try {
            return mMainPlayer != null && mMainPlayer.isPlaying() && !isRecording;
        } catch (Exception e) {
            return false;
        }
    }

    private void startTimer() {
        stopTimer();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateTimer();
            }
        }, 0, 1000);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    private void updateTimer() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isRecording) {
                    recorderSecondsElapsed++;
                    timerView.setText(Util2.formatSeconds(recorderSecondsElapsed));
                } else if (isPlaying()) {
                    playerSecondsElapsed++;
                    timerView.setText(Util2.formatSeconds(playerSecondsElapsed));
                }
            }
        });
    }
}
