package com.example.c_gerasimovich.taleme.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.example.c_gerasimovich.taleme.R;

import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO = 0;
    private static final String AUDIO_FILE_PATH =
            Environment.getExternalStorageDirectory().getPath() + "/recorded_audio.wav";

    private static final String ACCESS_TOKEN = "4K3tBFSlgH4AAAAAAAAACvRd44AF1k6TmAId9ddK7UKXRZER9D9iyHKAdhawGnmm";

    private static final String TAG = "DEV: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(
                    new ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryDark)));
        }

       /* Util.requestPermission(this, Manifest.permission.RECORD_AUDIO);
        Util.requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
*/
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

//        downloadFilesFromDropbox();

    }

    private void downloadFilesFromDropbox() {

        new Thread(new Runnable() {
            @Override
            public void run() {

//                mHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        showProgress();
//                    }
//                });


                try {
                    DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial", "en_US");
                    DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);

                    // Get files and folder metadata from Dropbox root directory
                    ListFolderResult result = client.files().listFolder("/TaleMe/");
                    while (true) {
                        for (Metadata metadata : result.getEntries()) {
                            Log.d(TAG, metadata.getPathLower());

                            DbxDownloader<FileMetadata> dl = client.files().download(metadata.getPathLower());
                            String localPath = Environment.getExternalStorageDirectory() + "/"
                                    + metadata.getPathLower().substring(metadata.getPathLower().lastIndexOf("/") + 1, metadata.getPathLower().length());


                            try {
                                FileOutputStream fout = new FileOutputStream(localPath);
                                dl.download(fout);
                                Log.d(TAG, "Download Complete");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        }

                        if (!result.getHasMore()) {
                            break;
                        }

                        result = client.files().listFolderContinue(result.getCursor());
                    }

/*

                    DbxDownloader<FileMetadata> dl = client.files().download("/remote-file-path");
                    long size = dl.getResult().size;

                    FileOutputStream fout = new FileOutputStream("local-file");
                    dl.download(new ProgressOutputStream(size, fout, new ProgressOutputStream.Listener() {
                        void progress(long completed, long totalSize) {
                            // update progress bar here ...
                        }
                    });
*/


                } catch (DbxException e) {
                    e.printStackTrace();
                }



                    /*File file = new File("/magnum-opus.txt");
                    FileOutputStream outputStream = new FileOutputStream(file);
                    FileMetadata metadata = mClient.files().uploadBuilder("/TaleMe/" + FILE_NAME).withMode(WriteMode.OVERWRITE)
                            .uploadAndFinish(in);
                    Log.d(TAG, "Upload complete " + metadata.getRev());

                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                }*/

              /*  mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        hideProgress();
                        selectAudio();
                    }
                });*/
            }
        }).start();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Audio recorded successfully!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Audio was not recorded", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void recordAudio(View v) {
        Intent intent = new Intent(this, TalesListActivity.class);
        startActivity(intent);

       /* AndroidAudioRecorder.with(this)
                // Required
                .setFilePath(AUDIO_FILE_PATH)
                .setColor(ContextCompat.getColor(this, R.color.recorder_bg))
                .setRequestCode(REQUEST_RECORD_AUDIO)

                // Optional
                .setSource(AudioSource.MIC)
                .setChannel(AudioChannel.STEREO)
                .setSampleRate(AudioSampleRate.HZ_48000)
                .setAutoStart(false)
                .setKeepDisplayOn(true)

                // Start recording
                .record();*/
    }

    public void listenAudio(View view) {

        Intent intent = new Intent(this, ListenListActivity.class);
        startActivity(intent);
    }
}