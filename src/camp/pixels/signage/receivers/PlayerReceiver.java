package camp.pixels.signage.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import static android.support.v4.content.WakefulBroadcastReceiver.startWakefulService;
import android.util.Log;
import camp.pixels.signage.FullScreenWebViewActivity;
import camp.pixels.signage.R;
import camp.pixels.signage.services.PlayerService;
import camp.pixels.signage.services.PollingService;
import static camp.pixels.signage.util.NetworkInterfaces.getIPAddress;
import static camp.pixels.signage.util.NetworkInterfaces.getMACAddress;


public class PlayerReceiver extends WakefulBroadcastReceiver {
    
    private static final String TAG = "PlayerReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, PlayerService.class);
        startWakefulService(context, service);
    }
}