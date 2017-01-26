package gq.fokia.lockip;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by fokia on 17-1-20.
 */

public class IpUtils {
    public Boolean useIPv4;
    public Context context;

    public IpUtils(Context context,Boolean useIPv4){
        this.context = context;
        this.useIPv4 = useIPv4;
    }

    public String getIpAddress(){
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isAvailable()){
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
            return "";
        }
        else {
            return "";
        }
    }
}
