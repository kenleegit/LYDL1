package net.agnusvox.lydl1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by ken on 17/9/2017.
 */

public class DownloadReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //ToDo Update on-screen message
        //Is it triggered on all download done? even those not submitted by this App?
        Toast.makeText(context, R.string.one_downloaded ,Toast.LENGTH_SHORT).show();
    }
}
