package gq.fokia.lockip;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import static gq.fokia.lockip.GetIpStatus.intervalTime;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView textView;
    private Button start;
    private Button stop;
    private String ip;
    private static String KEY = "ip";
    private IpUtils ipUtils;
    private EditText interval;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.ip);
        textView.setOnClickListener(this);
        start = (Button) findViewById(R.id.start);
        start.setOnClickListener(this);
        stop = (Button) findViewById(R.id.stop);
        stop.setOnClickListener(this);
        interval = (EditText) findViewById(R.id.editText);
        Log.d("MainActivity","on Executed");
        if(savedInstanceState != null){
            ip = savedInstanceState.getString(KEY,"Touch me");
            textView.setText(ip);
            intervalTime = savedInstanceState.getInt("intervalTime",1);
            interval.setText(intervalTime);
        }
        ipUtils = new IpUtils(this,true);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        saveData();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(KEY, ip);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ip:
                setText();
                break;
            case R.id.start:
                if(setText()) {
                    //intervalTime = interval.getTextDirection();
                    String interval_text = interval.getText().toString().trim();
                    if(!interval_text.isEmpty()) {
                        intervalTime = Integer.parseInt(interval.getText().toString());
                        Log.d("MainActivity", intervalTime + "");
                    }
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
        editor.putInt("intervalTime",intervalTime);
        editor.commit();
    }

    public Boolean setText(){
        if(!ipUtils.getIpAddress().equals("")) {
            ip = ipUtils.getIpAddress();
            textView.setText(ip);
            return true;
        }else {
            Toast.makeText(this,"网络不可用",Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
