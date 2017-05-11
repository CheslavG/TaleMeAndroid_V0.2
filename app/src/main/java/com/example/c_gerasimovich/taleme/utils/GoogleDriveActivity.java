package com.example.c_gerasimovich.taleme.utils;

import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

/**
 * Created by anray on 23.03.2017.
 */

public class GoogleDriveActivity extends AppCompatActivity  {
//implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
    public static  String FILE_NAME = "";
    private Button button_create_file;
    private Button button_upload_to_google_drive;
//    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "DRIVE: ";
    protected static final int REQUEST_CODE_RESOLUTION = 1337;
    private String FOLDER_NAME = "TaleMe";
//    public ResultCallback<DriveFolder.DriveFileResult> mResultUploadCallback;

    /*//region    ========================== Lifecycle ==========================

*//*    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        

        //---- use this to get the fucking SHA1 for the fucking google project....
        Log.v(">SHA > ", getCertificateSHA1Fingerprint(this));

        button_create_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //WRITE A SIMPLE TEXT FILE IN SDCARD. BE CAREFUL TO GRANT PERMISSION IN ANDROID 6+
                writeToFile("tehfile", " fuck google...");
            }
        });

        button_upload_to_google_drive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mGoogleApiClient != null) {
                    upload_to_drive();
                } else {
                    Log.e(TAG, "Could not fucking connect to google drive manager");
                }
            }
        });
    }*//*

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER) // required for App Folder sample
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == RESULT_OK) {
            mGoogleApiClient.connect();
            showToast("onActivityResult");
        }
    }

    private void showToast(String text) {

        Toast.makeText(GoogleDriveActivity.this, text, Toast.LENGTH_SHORT).show();


    }

    //endregion ========================== Lifecycle ==========================

    private String getCertificateSHA1Fingerprint() {
        PackageManager pm = getPackageManager();
        String packageName = getPackageName();
        int flags = PackageManager.GET_SIGNATURES;
        PackageInfo packageInfo = null;
        try {
            packageInfo = pm.getPackageInfo(packageName, flags);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Signature[] signatures = packageInfo.signatures;
        byte[] cert = signatures[0].toByteArray();
        InputStream input = new ByteArrayInputStream(cert);
        CertificateFactory cf = null;
        try {
            cf = CertificateFactory.getInstance("X509");
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        X509Certificate c = null;
        try {
            c = (X509Certificate) cf.generateCertificate(input);
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        String hexString = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(c.getEncoded());
            hexString = byte2HexFormatted(publicKey);
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }
        return hexString;
    }

    public static String byte2HexFormatted(byte[] arr) {
        StringBuilder str = new StringBuilder(arr.length * 2);
        for (int i = 0; i < arr.length; i++) {
            String h = Integer.toHexString(arr[i]);
            int l = h.length();
            if (l == 1) h = "0" + h;
            if (l > 2) h = h.substring(l - 2, l);
            str.append(h.toUpperCase());
            if (i < (arr.length - 1)) str.append(':');
        }
        return str.toString();
    }

    protected void upload_to_drive() {

        //async check if folder exists... if not, create it. continue after with create_file_in_folder(driveId);
        check_folder_exists();
    }

    private void check_folder_exists() {
        Query query =
                new Query.Builder().addFilter(Filters.and(Filters.eq(SearchableField.TITLE, FOLDER_NAME), Filters.eq(SearchableField.TRASHED, false)))
                        .build();
        Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {

            @Override
            public void onResult(DriveApi.MetadataBufferResult result) {
                if (!result.getStatus().isSuccess()) {
                    Log.e(TAG, "Cannot create folder in the root.");
                } else {
                    boolean isFound = false;
                    for (Metadata m : result.getMetadataBuffer()) {
                        if (m.getTitle().equals(FOLDER_NAME)) {
                            Log.e(TAG, "Folder exists");
                            isFound = true;
                            DriveId driveId = m.getDriveId();
                            create_file_in_folder(driveId);
                            break;
                        }
                    }
                    if (!isFound) {
                        Log.i(TAG, "Folder not found; creating it.");
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(FOLDER_NAME).build();
                        Drive.DriveApi.getRootFolder(mGoogleApiClient)
                                .createFolder(mGoogleApiClient, changeSet)
                                .setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
                                    @Override
                                    public void onResult(DriveFolder.DriveFolderResult result) {
                                        if (!result.getStatus().isSuccess()) {
                                            Log.e(TAG, "U AR A MORON! Error while trying to create the folder");
                                        } else {
                                            Log.i(TAG, "Created a folder");
                                            DriveId driveId = result.getDriveFolder().getDriveId();
                                            create_file_in_folder(driveId);
                                        }
                                    }
                                });
                    }
                }
            }
        });
    }

    private void create_file_in_folder(final DriveId driveId) {

        Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                if (!driveContentsResult.getStatus().isSuccess()) {
                    Log.e(TAG, "U AR A MORON! Error while trying to create new file contents");
                    return;
                }

                OutputStream outputStream = driveContentsResult.getDriveContents().getOutputStream();

                //------ THIS IS AN EXAMPLE FOR PICTURE ------
                //ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                //image.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
                //try {
                //  outputStream.write(bitmapStream.toByteArray());
                //} catch (IOException e1) {
                //  Log.i(TAG, "Unable to write file contents.");
                //}
                //// Create the initial metadata - MIME type and title.
                //// Note that the user will be able to change the title later.
                //MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                //    .setMimeType("image/jpeg").setTitle("Android Photo.png").build();

                //------ THIS IS AN EXAMPLE FOR FILE --------
                final File theFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + FILE_NAME); //>>>>>> WHAT FILE ?
                try {
                    FileInputStream fileInputStream = new FileInputStream(theFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                } catch (IOException e1) {
                    Log.i(TAG, "U AR A MORON! Unable to write file contents.");
                }

                MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(theFile.getName()).setMimeType("audio").build();
                DriveFolder folder = driveId.asDriveFolder();

                mResultUploadCallback = new ResultCallback<DriveFolder.DriveFileResult>() {
                    @Override
                    public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
                        if (!driveFileResult.getStatus().isSuccess()) {
                            Log.e(TAG, "U AR A MORON!  Error while trying to create the file");
                            return;
                        }
                        Log.v(TAG, "Created a file: " + driveFileResult.getDriveFile().getDriveId());
                        Toast.makeText(GoogleDriveActivity.this, "Upload was done!", Toast.LENGTH_SHORT).show();


                    }
                };

                folder.createFile(mGoogleApiClient, changeSet, driveContentsResult.getDriveContents())
                        .setResultCallback(mResultUploadCallback);
            }
        });
    }



    public void writeToFile(String fileName, String body) {
        FileOutputStream fos = null;
        try {
            final File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/xtests/");
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.e("ALERT", "U AR A MORON!  could not create the directories. CHECK THE FUCKING PERMISSIONS SON!");
                }
            }
            final File myFile = new File(dir, fileName + "_" + String.valueOf(System.currentTimeMillis()) + ".txt");
            if (!myFile.exists()) {
                myFile.createNewFile();
            }

            fos = new FileOutputStream(myFile);
            fos.write(body.getBytes());
            fos.close();
            Toast.makeText(GoogleDriveActivity.this, "File created ok! Let me give you a fucking congratulations!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //region    ========================== Google Drive callbacks ==========================


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.v(TAG, "+++++++++++++++++++ onConnected +++++++++++++++++++");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "onConnectionSuspended [" + String.valueOf(i) + "]");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "U AR A MORON! Exception while starting resolution activity", e);
        }
    }


    //endregion ========================== Google Drive callbacks ==========================*/


}