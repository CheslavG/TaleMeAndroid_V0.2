package com.example.c_gerasimovich.taleme.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.c_gerasimovich.taleme.utils.AudioPlayerActivity;

import java.io.File;
import java.util.ArrayList;


public class ListenListActivity extends ListActivity {

    public static final String TITLE_OF_TALE = "title";
    //    private String AUDIO_FILE_PATH = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString();
    private String AUDIO_FILE_PATH = Environment.getExternalStorageDirectory().getPath();
    private static final int REQUEST_RECORD_AUDIO = 0;
    public String[] mValues;
    public File file;
    public ArrayList<String> myList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_tales_list);


        File directory = Environment.getExternalStorageDirectory();
        file = directory;
        File list[] = file.listFiles();
        myList = new ArrayList<String>();

        for (int i = 0; i < list.length; i++) {
            if (list[i].getName().toLowerCase().contains(".wav"))
                myList.add(list[i].getName());
        }
       /* ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, myList);*/

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, myList);
        setListAdapter(adapter);


    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Intent intent = new Intent(this, AudioPlayerActivity.class);
        intent.putExtra(TITLE_OF_TALE, myList.get(position));
        startActivity(intent);


    }
}
