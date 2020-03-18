package com.example.faridabadtaxidriver.Service;

import android.content.Intent;
import android.util.Log;

import com.example.faridabadtaxidriver.Common.Common;
import com.example.faridabadtaxidriver.CustomerCall;
import com.example.faridabadtaxidriver.Model.Token;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        updateTokenServer(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData() != null) {

            Map<String,String> data = remoteMessage.getData(); //get data from notification
            String customer = data.get("customer");
            String lat = data.get("lat");
            String lng = data.get("lng");


            Intent intent = new Intent(getBaseContext(), CustomerCall.class);
            intent.putExtra("lat", lat);
            intent.putExtra("lng", lng);
            intent.putExtra("customer", customer);

            startActivity(intent);
        }
    }

    private void updateTokenServer(String refreshedToken) {
        FirebaseDatabase db=FirebaseDatabase.getInstance();
        final DatabaseReference tokens=db.getReference(Common.token_tb1);

        final Token token=new Token(refreshedToken);

        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
                tokens.child(account.getId())
                        .setValue(token);
            }

            @Override
            public void onError(AccountKitError accountKitError) {
                Log.d("ERROR_ACCOUNTKIT", ""+accountKitError.getUserFacingMessage());
            }
        });
    }
}