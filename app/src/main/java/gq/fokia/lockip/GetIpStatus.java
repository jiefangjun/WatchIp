package gq.fokia.lockip;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;


/**
 * Created by fokia on 17-1-15.
 */

public class GetIpStatus extends Service {
    private static final String TAG = "GetIpStatus";
    private Notification notification;
    public String ipStatus;
    private AlarmReceiver alarmReceiver;
    private AlarmManager alarmManager;
    private PendingIntent pi;
    public GetIpBinder mBinder = new GetIpBinder();
    static class GetIpBinder extends Binder{
        public void startProgress(){
            Log.d("TAG","startProgress executed");
        }
        public String getIp(Boolean useIPv4){
            try {
                for (Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces(); nis.hasMoreElements(); ) {
                    NetworkInterface ni = nis.nextElement();
                    // 防止小米手机返回10.0.2.15
                    if (!ni.isUp()) continue;
                    for (Enumeration<InetAddress> addresses = ni.getInetAddresses(); addresses.hasMoreElements(); ) {
                        InetAddress inetAddress = addresses.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            String hostAddress = inetAddress.getHostAddress();
                            boolean isIPv4 = hostAddress.indexOf(':') < 0;
                            if (useIPv4) {
                                if (isIPv4) return hostAddress;
                            } else {
                                if (!isIPv4) {
                                    int index = hostAddress.indexOf('%');
                                    return index < 0 ? hostAddress.toUpperCase() : hostAddress.substring(0, index).toUpperCase();
                                }
                            }
                        }
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate(){
        super.onCreate();
        ipStatus = mBinder.getIp(true);
        Log.d(TAG,ipStatus);
        setNotification(ipStatus);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d(TAG,"onStartCommand executed");
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(!mBinder.getIp(true).equals(ipStatus)){
                    Log.d(TAG,"设置通知栏");
                    ipStatus = mBinder.getIp(true);
                    setNotification(mBinder.getIp(true));
                }

            }

        }).start();
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int oneSecond = 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + oneSecond;
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
                .setContentTitle(ip)
                .setPriority(2)
                .setWhen(System.currentTimeMillis());
        notification = builder.build();
        notification.defaults = Notification.DEFAULT_SOUND;
        startForeground(110, notification);
    }
}
