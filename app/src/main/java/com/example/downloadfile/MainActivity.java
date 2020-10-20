package com.example.downloadfile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.jetbrains.annotations.NotNull;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {
    private LinearLayout mLinearLayout;
    private TextView mTextView;
    private Button downloadSharhBUT;
    private ProgressBar mProgressBar;
    private String filePath;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 4567;
    private static String file_url = "https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initListeners();
    }



    private void initViews() {
        downloadSharhBUT = findViewById(R.id.sharing_audio_file_BUT);
        mProgressBar = findViewById(R.id.progressBar_downloadFile_PB);
        mLinearLayout = findViewById(R.id.LinearLayout_downloadFile_LL);
        mTextView = findViewById(R.id.text_downloadFile_TV);
    }

    private void initListeners() {
        downloadSharhBUT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (downloadSharhBUT.getText().equals("הורדה")){
                    DownloadFile();
                }else {
                    Intent intentShareFile = new Intent(Intent.ACTION_SEND);
                    File fileWithinMyDir = new File(filePath);

                    if(fileWithinMyDir.exists()) {
                        intentShareFile.setType("audio/*");
                        intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+ filePath));
                        startActivity(Intent.createChooser(intentShareFile, "Share File"));
                    }
                    downloadSharhBUT.setText("הורדה");
                }
            }
        });
    }


    private void DownloadFile() {
        askPermissions();
    }


    private void askPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            Log.d("TAG", "He has not Permissions, we ask now");
        } else {
            Log.d("TAG", "He has permissions");
            new DownloadFileFromURL().execute(file_url);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NotNull String permissions[], @NotNull int[] grantResults) {

        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new DownloadFileFromURL().execute(file_url);
                Log.d("TAG", "He approved the permissions request");

            } else {
                Log.d("TAG", "He did not approved the permissions request");
            }
        }
    }


    class DownloadFileFromURL extends AsyncTask<Object, Integer, Integer> {

        int count;
        int lenghtOfFile;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            downloadSharhBUT.setVisibility(View.GONE);
            mLinearLayout.setVisibility(View.VISIBLE);
        }


        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected Integer doInBackground(Object... params) {
            String name = "";
            try {
                URL url = new URL((String) params[0]);
                name = ((String) params[0]).substring(((String) params[0])
                        .lastIndexOf("/") + 1);
                URLConnection conection = url.openConnection();
                conection.connect();
                lenghtOfFile = conection.getContentLength();
                mProgressBar.setMax(lenghtOfFile);
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);
                File download = new File(Environment.getExternalStorageDirectory()
                        + "/download/");
                if (!download.exists()) {
                    download.mkdir();
                }
                filePath = download + "/" + name;
                Log.v("log_tag", " down url   " + filePath);
                FileOutputStream output = new FileOutputStream(filePath);
                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                  int r = (int)(total);
                    publishProgress(r);
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();

            }
                catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
           mProgressBar.setProgress(values[0]);
           mTextView.setText(findPercent(values[0])+"%");
        }

        private int findPercent(int progress) {
            return progress * 100 / lenghtOfFile;
        }

        @Override
        protected void onPostExecute(Integer result) {
//            add if there are file
            mLinearLayout.setVisibility(View.GONE);
            downloadSharhBUT.setText("שתף");
            downloadSharhBUT.setVisibility(View.VISIBLE);

        }
    }
}