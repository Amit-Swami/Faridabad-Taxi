package com.example.faridabadtaxirider.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
//import android.support.annotation.RequiresApi;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
//import androidx.core.content.LocalBroadcastManager;
import android.widget.Toast;

import com.example.faridabadtaxirider.Common.Common;
import com.example.faridabadtaxirider.Helper.NotificationHelper;
import com.example.faridabadtaxirider.Model.Token;
import com.example.faridabadtaxirider.R;
import com.example.faridabadtaxirider.RateActivity;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        updateTokenServer(s); //when have refreshed token , we need update to our realtime database
    }

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {

        if (remoteMessage.getData() != null) {
            Map<String,String> data = remoteMessage.getData();
            String title = data.get("title");
            final String message = data.get("message");

            if (title.equals("Cancel")) {
                //because this is outside of main thread , so if you want to run Toast, +you need create Handler to do that
                //choose Handler from android.os
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MyFirebaseMessaging.this, message, Toast.LENGTH_SHORT).show();
                    }
                });

                LocalBroadcastManager.getInstance(MyFirebaseMessaging.this)
                        .sendBroadcast(new Intent(Common.CANCEL_BROADCAST_STRING));

            } else if (title.equals("Arrived")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    showArrivedNotificationAPI26(message);
                else
                    showArrivedNotification(message);
            } else if (title.equals("DropOff")) {
                openRateActivity(message);
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showArrivedNotificationAPI26(String body) {
        PendingIntent contentIntent =  PendingIntent.getActivity(getBaseContext(),
                0,new Intent(),PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationHelper notificationHelper=new NotificationHelper(getBaseContext());
        Notification.Builder builder = notificationHelper.getUberNotification("Arrived",body,contentIntent,defaultSound);

        notificationHelper.getManager().notify(1,builder.build());

    }

    private void updateTokenServer(final String refreshedToken) {
        FirebaseDatabase db=FirebaseDatabase.getInstance();
        final DatabaseReference tokens=db.getReference(Common.token_tb1);

        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
                Token token=new Token(refreshedToken);
                tokens.child(account.getId())
                        .setValue(token);
            }

            @Override
            public void onError(AccountKitError accountKitError) {

            }
        });


    }

    private void openRateActivity(String body) {

        LocalBroadcastManager.getInstance(MyFirebaseMessaging.this)
                .sendBroadcast(new Intent(Common.BROADCAST_DROP_OFF));

        Intent intent=new Intent(this, RateActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showArrivedNotification(String body) {
        //This code will work only for API 25 or below
        //for Android API 26 or higher , you need create Notification channel
        PendingIntent contentIntent =  PendingIntent.getActivity(getBaseContext(),
                0,new Intent(),PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(getBaseContext());

        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS|Notification.DEFAULT_SOUND)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_car)
                .setContentTitle("Arrived")
                .setContentText(body)
                .setContentIntent(contentIntent);
        NotificationManager manager= (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1,builder.build());

    }


}