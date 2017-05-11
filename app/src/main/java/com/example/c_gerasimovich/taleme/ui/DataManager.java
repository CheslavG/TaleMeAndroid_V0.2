package com.example.c_gerasimovich.taleme.ui;

import android.content.Context;

import com.example.c_gerasimovich.taleme.TeleApp;

/**
 * Created by anray on 29.06.2016.
 */
public class DataManager {

    private static final String TAG = "DataManager";
    private static DataManager INSTANCE = null;

    private Context mContext;
    private PreferencesManager mPreferencesManager;




    private DataManager() {
        this.mPreferencesManager = new PreferencesManager();
        this.mContext = TeleApp.getContext();


    }

    public static DataManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DataManager();

        }
        return INSTANCE;
    }

    public PreferencesManager getPreferencesManager() {
        return mPreferencesManager;
    }

    public Context getContext() {
        return mContext;
    }



    //region =====================Database===================



//    public List<User> getUserListFromDb() {
//        List<User> userList = new ArrayList<>();
//
//        try {
//
//            userList = mDaoSession.queryBuilder(User.class)
//                    .where(UserDao.Properties.CodeLines.gt(0))
//                    .orderDesc(UserDao.Properties.CodeLines)
//                    .build()
//                    .list();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return userList;
//    }


    //endregion

}
