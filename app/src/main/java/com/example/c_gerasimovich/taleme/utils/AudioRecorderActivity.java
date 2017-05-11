package com.example.c_gerasimovich.taleme.utils;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cleveroad.audiovisualization.DbmHandler;
import com.cleveroad.audiovisualization.GLAudioVisualizationView;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.users.FullAccount;
import com.example.c_gerasimovich.taleme.R;
import com.example.c_gerasimovich.taleme.model.AudioChannel;
import com.example.c_gerasimovich.taleme.model.AudioSampleRate;
import com.example.c_gerasimovich.taleme.model.AudioSource;
import com.example.c_gerasimovich.taleme.ui.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import omrecorder.AudioChunk;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.Recorder;

public class AudioRecorderActivity extends GoogleDriveActivity
        implements PullTransport.OnAudioChunkPulledListener, MediaPlayer.OnCompletionListener {

    private static final String ACCESS_TOKEN = "4K3tBFSlgH4AAAAAAAAACvRd44AF1k6TmAId9ddK7UKXRZER9D9iyHKAdhawGnmm";

    private static final String TAG = "DEV: ";
    private String filePath;
    private AudioSource source;
    private AudioChannel channel;
    private AudioSampleRate sampleRate;
    private int color;
    private boolean autoStart;
    private boolean keepDisplayOn;

    private MediaPlayer player;
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
    public DbxClientV2 mClient;
    private ProgressDialog mProgressDialog;
    private Handler mHandler = new Handler();

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            // permission was granted, yay! Do the
            // contacts-related task you need to do.



        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aar_activity_audio_recorder);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
        }

        DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial", "en_US");
        mClient = new DbxClientV2(config, ACCESS_TOKEN);
        // Get current account info

        new Thread(new Runnable() {
            @Override
            public void run() {
                FullAccount account = null;
                try {
                    account = mClient.users().getCurrentAccount();
                } catch (DbxException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, account.getName().getDisplayName());
            }
        }).start();


        if (savedInstanceState != null) {
            filePath = savedInstanceState.getString(AndroidAudioRecorder.EXTRA_FILE_PATH);
            source = (AudioSource) savedInstanceState.getSerializable(AndroidAudioRecorder.EXTRA_SOURCE);
            channel = (AudioChannel) savedInstanceState.getSerializable(AndroidAudioRecorder.EXTRA_CHANNEL);
            sampleRate = (AudioSampleRate) savedInstanceState.getSerializable(AndroidAudioRecorder.EXTRA_SAMPLE_RATE);
            color = savedInstanceState.getInt(AndroidAudioRecorder.EXTRA_COLOR);
            autoStart = savedInstanceState.getBoolean(AndroidAudioRecorder.EXTRA_AUTO_START);
            keepDisplayOn = savedInstanceState.getBoolean(AndroidAudioRecorder.EXTRA_KEEP_DISPLAY_ON);
        } else {
            filePath = getIntent().getStringExtra(AndroidAudioRecorder.EXTRA_FILE_PATH);
            source = (AudioSource) getIntent().getSerializableExtra(AndroidAudioRecorder.EXTRA_SOURCE);
            channel = (AudioChannel) getIntent().getSerializableExtra(AndroidAudioRecorder.EXTRA_CHANNEL);
            sampleRate = (AudioSampleRate) getIntent().getSerializableExtra(AndroidAudioRecorder.EXTRA_SAMPLE_RATE);
            color = getIntent().getIntExtra(AndroidAudioRecorder.EXTRA_COLOR, Color.BLACK);
            autoStart = getIntent().getBooleanExtra(AndroidAudioRecorder.EXTRA_AUTO_START, false);
            keepDisplayOn = getIntent().getBooleanExtra(AndroidAudioRecorder.EXTRA_KEEP_DISPLAY_ON, false);
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

        contentLayout.setBackgroundColor(Util2.getDarkerColor(color));
        contentLayout.addView(visualizerView, 0);
        restartView.setVisibility(View.INVISIBLE);
        playView.setVisibility(View.INVISIBLE);

        taleText.setText(
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
        );
//        taleText.setText();

        if (Util2.isBrightColor(color)) {
            ContextCompat.getDrawable(this, R.drawable.aar_ic_clear)
                    .setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            ContextCompat.getDrawable(this, R.drawable.aar_ic_check)
                    .setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            statusView.setTextColor(Color.BLACK);
            timerView.setTextColor(Color.BLACK);
            restartView.setColorFilter(Color.BLACK);
            recordView.setColorFilter(Color.BLACK);
            playView.setColorFilter(Color.BLACK);
        }
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
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.aar_audio_recorder, menu);
        saveMenuItem = menu.findItem(R.id.action_save);
        saveMenuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.aar_ic_check));
        return super.onCreateOptionsMenu(menu);
    }

    public void showProgress() {
        if (mProgressDialog == null) {
          /*  mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("Uploading...");
            mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mProgressDialog.show();*/

            mProgressDialog = ProgressDialog.show(this, "Uploading...", "Uploading...");
//            mProgressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mProgressDialog.setContentView(new ProgressBar(this));

//            mProgressDialog.setContentView(R.layout.progress_splash);


        } else {
            mProgressDialog.show();
//            mProgressDialog.setContentView(R.layout.progress_splash);

        }

    }

    public void hideProgress() {
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.hide();
                mProgressDialog.dismiss();

            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
        } else if (i == R.id.action_save) {

//            uploadToGoogleDrive();
            Log.d(TAG, filePath);
            FILE_NAME = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
            Log.d(TAG, FILE_NAME);
            // Upload "test.txt" to Dropbox


            new Thread(new Runnable() {
                @Override
                public void run() {

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            showProgress();
                        }
                    });


                    try {
                        InputStream in = new FileInputStream(filePath);
                        FileMetadata metadata = mClient.files().uploadBuilder("/TaleMe/" + FILE_NAME).withMode(WriteMode.OVERWRITE)
                                .uploadAndFinish(in);
                        Log.d(TAG, "Upload complete " + metadata.getRev());

                    } catch (Exception e) {
                        Log.d(TAG, e.toString());
                    }

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            hideProgress();
                            selectAudio();
                        }
                    });
                }
            }).start();





            /*File flacFile = new File(filePath);
            IConvertCallback callback = new IConvertCallback() {
                @Override
                public void onSuccess(final File convertedFile) {
                    // So fast? Love it!




                }
                @Override
                public void onFailure(Exception error) {
                    // Oops! Something went wrong
                    Log.d(TAG, error.toString());
                }
            };
            AndroidAudioConverter.with(this)
                    // Your current audio file
                    .setFile(flacFile)

                    // Your desired audio format
                    .setFormat(AudioFormat.MP3)

                    // An callback to know when conversion is finished
                    .setCallback(callback)

                    // Start conversion
                    .convert();*/


        }
        return super.onOptionsItemSelected(item);
    }

    private void uploadToGoogleDrive() {

        FILE_NAME = filePath.substring(filePath.indexOf("/") + 1, filePath.length());
        /*mResultUploadCallback = new ResultCallback<DriveFolder.DriveFileResult>() {
            @Override
            public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
                if (!driveFileResult.getStatus().isSuccess()) {
                    Log.e(TAG, "U AR A MORON!  Error while trying to create the file");
                    return;
                }
                Log.v(TAG, "Created a file: " + driveFileResult.getDriveFile().getDriveId());
                Toast.makeText(AudioRecorderActivity.this, "Upload was done!", Toast.LENGTH_SHORT).show();

                selectAudio();

            }
        };*/
//        upload_to_drive();


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

    public void togglePlaying(View v) {
        pauseRecording();
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
        statusView.setVisibility(View.VISIBLE);
        restartView.setVisibility(View.INVISIBLE);
        playView.setVisibility(View.INVISIBLE);
        recordView.setImageResource(R.drawable.aar_ic_pause);
        playView.setImageResource(R.drawable.aar_ic_play);

        visualizerHandler = new VisualizerHandler();
        visualizerView.linkTo(visualizerHandler);

        if (recorder == null) {
            timerView.setText("00:00:00");

            recorder = OmRecorder.wav(
                    new PullTransport.Default(Util2.getMic(source, channel, sampleRate), AudioRecorderActivity.this),
                    new File(filePath));
        }
        recorder.resumeRecording();

        startTimer();
    }

    private void pauseRecording() {
        isRecording = false;
        if (!isFinishing()) {
            saveMenuItem.setVisible(true);
        }
        statusView.setText(R.string.aar_paused);
        statusView.setVisibility(View.VISIBLE);
        restartView.setVisibility(View.VISIBLE);
        playView.setVisibility(View.VISIBLE);
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

    private void startPlaying() {
        try {
            stopRecording();
            player = new MediaPlayer();
            player.setDataSource(filePath);
            player.prepare();
            player.start();

            visualizerView.linkTo(DbmHandler.Factory.newVisualizerHandler(this, player));
            visualizerView.post(new Runnable() {
                @Override
                public void run() {
                    player.setOnCompletionListener(AudioRecorderActivity.this);
                }
            });

            timerView.setText("00:00:00");
            statusView.setText(R.string.aar_playing);
            statusView.setVisibility(View.VISIBLE);
            playView.setImageResource(R.drawable.aar_ic_stop);

            playerSecondsElapsed = 0;
            startTimer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopPlaying() {
        statusView.setText("");
        statusView.setVisibility(View.INVISIBLE);
        playView.setImageResource(R.drawable.aar_ic_play);

        visualizerView.release();
        if (visualizerHandler != null) {
            visualizerHandler.stop();
        }

        if (player != null) {
            try {
                player.stop();
                player.reset();
            } catch (Exception e) {
            }
        }

        stopTimer();
    }

    private boolean isPlaying() {
        try {
            return player != null && player.isPlaying() && !isRecording;
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
