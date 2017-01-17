package gq.fokia.lockip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;



/**
 * Created by fokia on 17-1-16.
 */

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
            Log.d("Receiver","on Executed");
            Intent i = new Intent(context, GetIpStatus.class);
            context.startService(i);
    }
}
