package pl.edu.agh.mobilne.ultrasound.android.lib.send;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SenderService extends Service {
    public static final String NOTIFICATION_ID
            = "pl.edu.agh.mobilne.ultrasound.android.lib.send.SenderService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
