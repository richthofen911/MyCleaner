package net.callofdroidy.mycleaner;

import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class ActivityMain extends AppCompatActivity {

    private EditText et_targetDir;
    private TextView tv_status;
    private Spinner spinner;
    private SharedPreferences spAppGarbagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spAppGarbagePath = getApplication().getSharedPreferences("app_garbage_path_index", 0);
        checkPathIndexFile(spAppGarbagePath);
        et_targetDir = (EditText) findViewById(R.id.et_targetDir);
        tv_status = (TextView) findViewById(R.id.tv_status);
        spinner = (Spinner) findViewById(R.id.spinner);
        setUpSpinner();

        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String targetDir = et_targetDir.getText().toString();
                if (!targetDir.equals(""))
                    cleanTargetDir(et_targetDir.getText().toString());
                else
                    Toast.makeText(ActivityMain.this, "Target Directory shouldn't be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            String content = msg.getData().getString("content");
            switch (msg.what){
                case Constants.MESSAGE_STATUS:
                    tv_status.append("status: " + content + "\n");
                    break;
                case Constants.MESSAGE_FINISH:
                    tv_status.append("status: " + content + "\n");
                    Toast.makeText(ActivityMain.this, content, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_ERROR:
                    tv_status.append("status: " + content + "\n");
                    Toast.makeText(ActivityMain.this, content, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void cleanTargetDir(String dirPath){
        new Thread(new ThreadCleanAppGarbage(dirPath, mHandler)).start();
    }

    private void setUpSpinner(){
        ArrayList<String> appList = new ArrayList<>();
        for(Map.Entry<String, ?> entry: spAppGarbagePath.getAll().entrySet())
            appList.add(entry.getKey());
        for (String key: appList){
            if(key.equals("init")){
                appList.remove(key);
                break;
            }
        }
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, appList));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                decideFilePath((String) parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void decideFilePath(String appName){
        String targetFilePath = spAppGarbagePath.getString(appName, "not defined");
        if(!targetFilePath.equals("not defined") && !targetFilePath.endsWith(File.separator))
            targetFilePath = targetFilePath + File.separator;
        et_targetDir.setText(targetFilePath);
    }

    private void checkPathIndexFile(SharedPreferences mIndexFile){
        if(mIndexFile.getString("init", "no").equals("no")){
            SharedPreferences.Editor mIndexFileEditor = mIndexFile.edit();
            mIndexFileEditor.putString("init", "yes");
            mIndexFileEditor.putString("WeChat", "Not known yet");
            mIndexFileEditor.putString("QQ", Environment.getExternalStorageDirectory() + "/Android/data/com.tencent.mobileqq");
            mIndexFileEditor.putString("Zhihu", Environment.getExternalStorageDirectory() + "/Android/data/com.zhihu.android/cache");
            mIndexFileEditor.commit();
        }
    }

    private boolean isExternalStorageAvailable(){
        return Environment.getExternalStorageState().equals("mounted");
    }
}
