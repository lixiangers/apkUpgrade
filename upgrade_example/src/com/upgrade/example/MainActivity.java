package com.upgrade.example;

import java.io.File;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends Activity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private final String testUrl = "http://bs.baidu.com/appstore/apk_0D991094E5CE38957811C1FA3726B748.apk";
    private String mDirPath;
    public static final String UPGRADE = "upgrade";
    private DownLoadFileService loadFileService;
    private TextView messageTextView;

    private boolean isRegisteredService;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageTextView = (TextView) findViewById(R.id.tv_message);
        mDirPath = createDownLoadUpgradeDir(getApplicationContext());
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_download:
                Intent intent = new Intent(getApplicationContext(), DownLoadFileService.class);
                intent.putExtra("down_load_path", mDirPath);
                intent.putExtra("down_load_url", testUrl);
                bindService(intent, serviceConnection, BIND_AUTO_CREATE);
                break;
            case R.id.stop_download:
                if (isRegisteredService) {
                    unbindService(serviceConnection);
                    isRegisteredService = false;
                }
                break;
            case R.id.delete_download_file:
                if (isRegisteredService) {
                    unbindService(serviceConnection);
                    isRegisteredService = false;
                }
                File dir = new File(mDirPath);
                UpgradeUtil.deleteContentsOfDir(dir);
                break;
        }
    }

    private static String createDownLoadUpgradeDir(Context context) {
        String dir = null;
        final String dirName = UPGRADE;
        File root = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            root = context.getExternalFilesDir(null);
        } else {
            root = context.getFilesDir();
        }
        File file = new File(root, dirName);
        file.mkdirs();
        dir = file.getAbsolutePath();
        return dir;
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            loadFileService = ((DownLoadFileService.LocalBinder) service).getService();
            isRegisteredService = true;

            loadFileService.setOnProgressChangeListener(new DownLoadFileService.onProgressChangeListener() {
                @Override
                public void onProgressChange(final int progress, final String message) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            messageTextView.setText(message + ",进度" + progress + "%");
                        }
                    });
                }
            });
            Log.i(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "onServiceDisconnected");
            loadFileService = null;
            isRegisteredService = false;
        }
    };
}
