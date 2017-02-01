package gq.fokia.watchip;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


/**
 * Created by fokia on 17-1-15.
 */

public class GetIpStatus extends Service {
    private static final String TAG = "GetIpStatus";
    public static int intervalTime = 1;
    public static Boolean is_voice = false;
    public static Boolean is_vibration = false;
    private Notification notification;
    public String ipStatus = "";
    private AlarmManager alarmManager;
    private PendingIntent pi;
    private IpUtils ipUtils;
    private String ipError = "网络不可用";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate(){
        super.onCreate();
        ipUtils = new IpUtils(this, true);
        if(ipUtils.getIpAddress().equals(ipStatus)) {
            Log.d(TAG, ipStatus);
            ipStatus = ipUtils.getIpAddress();
            setNotification(ipStatus);
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d(TAG,"onStartCommand executed");
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(!ipUtils.getIpAddress().equals(ipStatus)){
                    if(ipUtils.getIpAddress().equals("")){
                        setNotification(ipError);
                        ipStatus = ipUtils.getIpAddress();

                    }
                    else {
                        Log.d(TAG, ipUtils.getIpAddress());
                        Log.d(ipStatus, ipStatus);
                        Log.d(TAG, "设置通知栏");
                        ipStatus = ipUtils.getIpAddress();
                        setNotification(ipStatus);
                    }
                }

            }

        }).start();
        Log.d("intervalTime",intervalTime+"");
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long triggerAtTime = SystemClock.elapsedRealtime() + intervalTime * 1000;
        Intent i = new Intent(this, AlarmReceiver.class);
        pi = PendingIntent.getBroadcast(this,0,i,0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy(){
        Log.d(TAG,"onDestroy executed");
        stopForeground(true);
        alarmManager.cancel(pi);
        super.onDestroy();
    }

    public void setNotification(String ip){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Intent notificationIntent = new Intent(this,MainActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(this,0,notificationIntent,0))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("WatchIp")
                .setContentText(ip)
                .setPriority(2)
                .setWhen(System.currentTimeMillis());
        notification = builder.build();
        SharedPreferences config = getSharedPreferences("data",0);
        is_voice = config.getBoolean("voice", false);
        is_vibration = config.getBoolean("vibration", false);
        if(is_voice){
            if(is_vibration){
                notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
            }else
            notification.defaults = Notification.DEFAULT_SOUND;
        } else if(is_vibration){
            notification.defaults = Notification.DEFAULT_VIBRATE;
        }
        startForeground(110, notification);
    }
}
