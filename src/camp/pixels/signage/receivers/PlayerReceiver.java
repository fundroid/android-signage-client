package camp.pixels.signage.receivers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import camp.pixels.signage.services.PlayerService;

import static android.support.v4.content.WakefulBroadcastReceiver.startWakefulService;


public class PlayerReceiver extends WakefulBroadcastReceiver {
    
    private static final String TAG = "PlayerReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, PlayerService.class);
        startWakefulService(context, service);
    }
}