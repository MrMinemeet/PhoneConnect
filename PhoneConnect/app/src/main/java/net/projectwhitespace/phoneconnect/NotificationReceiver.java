package net.projectwhitespace.phoneconnect;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NotificationReceiver extends NotificationListenerService {

    private static final String TAG = "NotificationReceiver";


    Context context;
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

        // Gather information
        final PackageManager pm = getApplicationContext().getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(sbn.getPackageName(), 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");

        Bundle extras = sbn.getNotification().extras;

        // Put information into finals to show them in UI using post
        final String title = extras.getString("android.title");
        final String text = extras.getCharSequence("android.text").toString();
        final String key = sbn.getKey();
        final Date time = Calendar.getInstance().getTime();

        // Update Text in UI using post
        MainActivity.txv_info.post(new Runnable() {
            @Override
            public void run() {
                StringBuilder sb = new StringBuilder();
                sb.append("Name: ");
                sb.append(applicationName);

                sb.append("\nTitle: ");
                sb.append(title);

                sb.append("\nText: ");
                sb.append(text);

                sb.append("\nDatum/Zeit: ");
                DateFormat dateFormat = new SimpleDateFormat("dd.MM hh:mm:ss", Locale.GERMAN);
                String strDate = dateFormat.format(time);
                sb.append(strDate);

                MainActivity.txv_info.setText(sb.toString());
            }
        });


        // Thread to send notification information to PC
        new Thread(){
            @Override
            public void run() {
                Socket socket = null;
                BufferedWriter bw = null;

                Settings settings = Settings.getInstance();


                try{
                    socket = new Socket(settings.SERVER_IP,settings.SERVER_PORT);

                    bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));


                    StringBuilder textToSendSB = new StringBuilder();

                    textToSendSB.append("Package: ");
                    textToSendSB.append(applicationName);
                    textToSendSB.append("\nTitle: ");
                    textToSendSB.append(title);
                    textToSendSB.append("\nText: ");
                    textToSendSB.append(text);



                    /*
                    // Encrypt data using CryptLib (WIP)
                    String EncryptedData = "-1";
                    try{
                        CryptLib _crypt = new CryptLib();
                        String plainText = textToSendSB.toString();
                        String key = CryptLib.SHA256(settings.KEY, 32); //32 bytes = 256 bit
                        String iv = CryptLib.generateRandomIV(16); //16 bytes = 128 bit
                        EncryptedData = _crypt.encrypt(plainText, key, iv); //encrypt
                        Log.d(TAG,"encrypted text=" + EncryptedData);

                    }catch(Exception e){
                        Log.e(TAG, e.getMessage());
                    }

                    // Send Encrypted data
                    if(EncryptedData != "-1") {
                        bw.write(EncryptedData);
                    }
                    else {
                        Log.e(TAG, "Encryption failed!");
                    }
                    */

                    // Send Plain Text

                    bw.write(textToSendSB.toString());


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

