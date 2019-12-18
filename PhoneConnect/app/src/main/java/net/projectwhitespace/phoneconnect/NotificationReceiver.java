package net.projectwhitespace.phoneconnect;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;

public class NotificationReceiver extends NotificationListenerService {

    private static final String TAG = "NotificationReceiver";


    Context context;

    private static Notification lastNotification = null;

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onCreate() {

        super.onCreate();
        context = getApplicationContext();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d(TAG, "Notification Received!");


        final PackageManager pm = getApplicationContext().getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(sbn.getPackageName(), 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");

        Bundle extras = sbn.getNotification().extras;


        final String title = extras.getString("android.title");
        final String text = extras.getCharSequence("android.text").toString();
        final String key = sbn.getKey();
        final Date time = Calendar.getInstance().getTime();

        Notification notification = new Notification();
        notification.title = title;
        notification.key = key;
        notification.message = text;
        notification.time = time;

        if(lastNotification != null){
            if(lastNotification.equals(notification)){
                return;
            }
            else{
                lastNotification = notification;
            }
        }

        if(lastNotification == null){
            lastNotification = notification;
        }

        MainActivity.txv_info.post(new Runnable() {
            @Override
            public void run() {
                MainActivity.txv_info.setText("Name: " + applicationName + "\nTitle: " + title + "\nText: " + text + "\nTime: " + time.getTime());
            }
        });


        new Thread(){
            @Override
            public void run() {
                Socket socket = null;
                BufferedWriter bw = null;

                try{
                    socket = new Socket("192.168.1.22", 5000);

                    bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    bw.write("Package: " + applicationName + "\n");
                    bw.write("Title: " + title + "\n");
                    bw.write("Text: " + text + "\n");

                }
                catch(Exception ex){
                    Log.e(TAG, ex.getMessage());
                }
                finally{
                    if(bw!=null){
                        try{
                            bw.close();
                        }
                        catch(Exception ex){
                            Log.d(TAG, ex.getMessage());
                        }
                    }
                    if(socket!=null){
                        try{
                            socket.close();
                        }
                        catch(Exception ex){
                            Log.d(TAG, ex.getMessage());
                        }
                    }
                }
            }
        }.start();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }
}

