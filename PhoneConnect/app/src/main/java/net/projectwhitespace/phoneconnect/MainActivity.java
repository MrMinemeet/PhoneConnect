package net.projectwhitespace.phoneconnect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import static android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS;

public class MainActivity extends AppCompatActivity {

    public static TextView txv_info;

    static final String APP_TAG = "PhoneConnect";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Check if Notification Listener Service Permissions have been Granted
        if (!isNotificationServiceEnabled()) {
            // If not Granted then make Alert Dialog to request it
            final AlertDialog enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        }

        txv_info = findViewById(R.id.txv_info);

        // Start notificaiton listener service (Mainfest doesn't always work)
        startService(new Intent(this, NotificationReceiver.class));




        // Thread to receive Broadcast information for IP update
        new Thread(){
            @Override
            public void run(){
                byte[] lMsg = new byte[4096];
                DatagramPacket dp = new DatagramPacket(lMsg, lMsg.length);
                DatagramSocket ds = null;

                try
                {
                    ds = new DatagramSocket(25001);

                    while(true)
                    {
                        ds.receive(dp);

                        Log.d(APP_TAG, new String(lMsg, 0, dp.getLength()));
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    if (ds != null)
                    {
                        ds.close();
                    }
                }
            }
        }.start();

    }

    private AlertDialog buildNotificationServiceAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Notification Permission needed");
        alertDialogBuilder.setMessage("To work properly this app requires permissions to get incoming notifications.");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("Allow",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                });
        alertDialogBuilder.setNegativeButton("Deny",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // If you choose to not enable the notification listener
                        // the app. will not work as expected
                        Toast.makeText(MainActivity.this , "App won't work properly without this permission!", Toast.LENGTH_LONG).show();
                    }
                });
        return(alertDialogBuilder.create());
    }

    private boolean isNotificationServiceEnabled() {
        return NotificationManagerCompat.getEnabledListenerPackages(this).contains(getPackageName());
    }

    // Set Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    // Set menu actions
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_open_notification_permission:
                startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
