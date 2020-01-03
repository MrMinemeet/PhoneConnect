package net.projectwhitespace.phoneconnect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
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

    private static final String LIGHT_THEME = "LightTheme";
    private static final String DARK_THEME = "DarkTheme";
    private static final String SETBYSYSTEM_THEME = "SetBySystemTheme";


    // BROADCAST PATTERN:
    /*
     * {Server ID (SHA256)}
     * {PUBLIC KEY}
     *
     *
     */

    // NOTIFICATION PATTERN:
    /*
     * {Appname}
     * {Title}
     * {Message}
     * {Date/Time}
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Load App Theme
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreference", Context.MODE_PRIVATE);
        SetMatchingTheme(sharedPreferences.getString("Theme", LIGHT_THEME));



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
                    Settings settings = Settings.getInstance();


                    ds = new DatagramSocket(settings.BROADCAST_PORT);

                    while(true)
                    {
                        ds.receive(dp);

                        String message = new String(dp.getData()).trim();
                        String broadcast_sender_ip = dp.getAddress().getHostAddress();

                        Log.d(APP_TAG, "Message from " + broadcast_sender_ip + " which was: " + message);

                        settings.SERVER_IP = broadcast_sender_ip;

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
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreference", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();


        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_open_notification_permission:
                startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                return true;


                // THEMES
            case R.id.dark_theme:
                editor.putString("Theme", DARK_THEME);
                editor.apply();

                // Create toast to inform User (even if he will see the change)
                Toast.makeText(this, R.string.Theme_set,Toast.LENGTH_SHORT).show();
                recreate();
                return true;

            case R.id.light_theme:
                editor.putString("Theme", LIGHT_THEME);
                editor.apply();

                // Create toast to inform User (even if he will see the change)
                Toast.makeText(this, R.string.Theme_set,Toast.LENGTH_SHORT).show();
                recreate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    // Set Theme
    public void SetMatchingTheme(String theme){
        switch(theme) {
            case LIGHT_THEME:
                setTheme(R.style.AppTheme);
                break;

            case DARK_THEME:
                setTheme(R.style.AppTheme_Dark);
                break;

            case SETBYSYSTEM_THEME:
                //TODO: Implement changing theme according to Systemsettings
                break;
        }
    }
}
