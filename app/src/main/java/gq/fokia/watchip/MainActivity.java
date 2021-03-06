package gq.fokia.watchip;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import static gq.fokia.watchip.GetIpStatus.intervalTime;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textView;
    private long lastTime;
    private long currentTime;
    private Button start;
    private Button stop;
    private String ip;
    private static String KEY = "ip";
    private IpUtils ipUtils;
    private EditText interval;
    public CheckBox voice;
    public CheckBox vibration;
    public Boolean bvoice = false;
    public Boolean bvibration = false;
    private Toolbar toolbar;
    private SharedPreferences pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //5.0 全透明实现
        //getWindow.setStatusBarColor(Color.TRANSPARENT)
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //4.4 全透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.tb);
        setSupportActionBar(toolbar);
        pref = getSharedPreferences("data", 0);
        textView = (TextView) findViewById(R.id.ip);
        textView.setOnClickListener(this);
        voice = (CheckBox) findViewById(R.id.voice);
        vibration = (CheckBox) findViewById(R.id.vibration);

        start = (Button) findViewById(R.id.start);
        start.setOnClickListener(this);
        stop = (Button) findViewById(R.id.stop);
        stop.setOnClickListener(this);

        interval = (EditText) findViewById(R.id.editText);

        bvoice = pref.getBoolean("voice", false);
        bvibration = pref.getBoolean("vibration", false);
        intervalTime = pref.getInt("intervalTime", 5);
        voice.setChecked(bvoice);
        vibration.setChecked(bvibration);

        interval.setText(intervalTime+"");
        //设置光标位置
        interval.setSelection((intervalTime+"").length());

        if(savedInstanceState != null){
            ip = savedInstanceState.getString(KEY,"Touch me");
            textView.setText(ip);
            intervalTime = savedInstanceState.getInt("intervalTime",1);
            interval.setText(intervalTime+"");

            bvoice = savedInstanceState.getBoolean("voice", false);
            bvibration = savedInstanceState.getBoolean("vibration", false);

            voice.setChecked(bvoice);
            vibration.setChecked(bvibration);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        saveData();
    }

    @Override
    public void onBackPressed(){
        currentTime = System.currentTimeMillis();
        if(currentTime - lastTime < 2 * 1000){
            super.onBackPressed();
        }else {
            Toast.makeText(this,"再按一次退出程序",Toast.LENGTH_SHORT).show();
            lastTime = currentTime;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(KEY, ip);
        savedInstanceState.putBoolean("voice", bvoice);
        savedInstanceState.putBoolean("vibration", bvibration);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ip:
                setText();
                break;
            case R.id.start:
                if(setText()) {

                    String interval_text = interval.getText().toString().trim();
                    if(!interval_text.isEmpty()) {
                        intervalTime = Integer.parseInt(interval.getText().toString());
                        Log.d("MainActivity", intervalTime + "");
                    }
                    SharedPreferences.Editor editor = getSharedPreferences("data",0).edit();
                    Log.d("voice.isChecked()",voice.isChecked()+"");
                    if(voice.isChecked()){
                        editor.putBoolean("voice", true);
                    }else {
                        editor.putBoolean("voice", false);
                    }
                    if(vibration.isChecked())
                    {
                        editor.putBoolean("vibration", true);
                    }else {
                        editor.putBoolean("vibration", false);
                    }
                    editor.commit();
                    Intent startIntent = new Intent(this, GetIpStatus.class);
                    startService(startIntent);
                }
                break;
            case R.id.stop:
                Intent stopIntent = new Intent(this, GetIpStatus.class);
                stopService(stopIntent);
                break;
            default:
                break;
        }
    }

    public void saveData(){
        SharedPreferences.Editor editor = getSharedPreferences("data",0).edit();
        editor.putString("ipAddress",ip);

        editor.putInt("intervalTime", intervalTime);
        if(voice.isChecked()){
            bvoice = true;
        }
        if(vibration.isChecked()){
            bvibration = true;
        }
        editor.putBoolean("voice", bvoice);
        editor.putBoolean("vibration", bvibration);
        editor.commit();
    }

    public Boolean setText(){
        if(!IpUtils.getIpAddress().equals("")) {
            ip = IpUtils.getIpAddress();
            textView.setText(ip);
            return true;
        }else {
            Toast.makeText(this,"网络不可用",Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
