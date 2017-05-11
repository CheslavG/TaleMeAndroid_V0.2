package com.example.c_gerasimovich.taleme.ui;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.c_gerasimovich.taleme.R;
import com.example.c_gerasimovich.taleme.model.AudioChannel;
import com.example.c_gerasimovich.taleme.model.AudioSampleRate;
import com.example.c_gerasimovich.taleme.model.AudioSource;
import com.example.c_gerasimovich.taleme.utils.AndroidAudioRecorder;


public class TalesListActivity extends ListActivity {

//    private String AUDIO_FILE_PATH = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
    private String AUDIO_FILE_PATH = Environment.getExternalStorageDirectory().getPath();
    private static final int REQUEST_RECORD_AUDIO = 0;
    public String[] mValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_tales_list);

        mValues = new String[]{PreferencesManager.TALE1,PreferencesManager.TALE2};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mValues);
        setListAdapter(adapter);


    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        AUDIO_FILE_PATH += "/"+mValues[position].replace(" ","_") +".wav";

        AndroidAudioRecorder.with(this)
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
                .setTaleKey(mValues[position])

                // Start recording
                .record();


    }
}
