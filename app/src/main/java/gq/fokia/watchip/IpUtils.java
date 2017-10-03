package gq.fokia.watchip;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * Created by fokia on 17-1-20.
 */

public class IpUtils {

    public static String getIpAddress(){
        Enumeration e = null;
        String ipv4 = "";
        try {
            e = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e1) {
            e1.printStackTrace();
        }
        while(e.hasMoreElements())
        {
            NetworkInterface n = (NetworkInterface) e.nextElement();
            //去除其他干扰接口
            if (!n.getName().equals("tun0") && !n.getName().equals("lo") && !n.getName().equals("dummy0")){
                Enumeration ee = n.getInetAddresses();
                while (ee.hasMoreElements())
                {
                    InetAddress i = (InetAddress) ee.nextElement();
                    //根据长度判断，仅获取ipv4地址
                    if (i.getHostAddress().length() <= 15)
                        ipv4 = i.getHostAddress();
                    Log.i(n.getName(), i.getHostAddress());
                }
            }

        }
        return ipv4;
    }

}
