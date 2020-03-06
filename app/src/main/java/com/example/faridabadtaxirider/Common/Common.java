package com.example.faridabadtaxirider.Common;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.example.faridabadtaxirider.Home;
import com.example.faridabadtaxirider.Model.DataMessage;
import com.example.faridabadtaxirider.Model.FCMResponse;
import com.example.faridabadtaxirider.Model.Rider;
import com.example.faridabadtaxirider.Model.Token;
import com.example.faridabadtaxirider.Remote.FCMClient;
import com.example.faridabadtaxirider.Remote.GoogleMapAPI;
import com.example.faridabadtaxirider.Remote.IFCMService;
import com.example.faridabadtaxirider.Remote.IGoogleAPI;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Common {

    public static final int PICK_IMAGE_REQUEST = 9999 ;
    public static boolean isDriverFound=false;
    public static String driverId="";

    public static Location mLastLocation;

    public static Rider currentUser = new Rider();

    public static final String BROADCAST_DROP_OFF = "arrived";

    public static final String CANCEL_BROADCAST_STRING = "cancel_pickup";

    public static final String driver_tb1="Drivers";
    public static final String user_driver_tb1="DriversInformation";
    public static final String user_rider_tb1="RidersInformation";
    public static final String pickup_request_tb1="PickupRequest";
    public static final String token_tb1="Tokens";
    public static final String rate_detail_tb1="RateDetails";
    public static final String user_field = "rider_usr";
    public static final String pwd_field = "rider_pwd";


    public static final String fcmURL="https://fcm.googleapis.com/";
    public static final String googleAPIUrl="https://maps.googlesapis.com";

    private static double base_fare=2.55;
    private static double time_rate=0.35;
    private static double distance_rate=1.75;

    public static double getPrice(double km,int min)
    {
        return (base_fare+(time_rate*min)+(distance_rate*km));
    }

    public static IFCMService getFCMService()
    {
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }

    public static IGoogleAPI getGoogleServices()
    {
        return GoogleMapAPI.getClient(googleAPIUrl).create(IGoogleAPI.class);
    }

    public static void sendRequestToDriver(String driverId, final IFCMService mService, final Context context, final Location currentLocation) {
        DatabaseReference tokens= FirebaseDatabase.getInstance().getReference(Common.token_tb1);

        tokens.orderByKey().equalTo(driverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapshot:dataSnapshot.getChildren())
                        {
                            Token token=postSnapshot.getValue(Token.class); //get Token object from database with key

                            //Make raw payload - convert LatLng to json
                            String riderToken= FirebaseInstanceId.getInstance().getToken();

                            Map<String,String> content = new HashMap<>();
                            content.put("customer",riderToken);
                            content.put("lat",String.valueOf(currentLocation.getLatitude()));
                            content.put("lng",String.valueOf(currentLocation.getLongitude()));
                            DataMessage dataMessage = new DataMessage(token.getToken(),content);

                            mService.sendMessage(dataMessage)
                                    .enqueue(new Callback<FCMResponse>() {
                                        @Override
                                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                            if (response.body().success==1)
                                                Toast.makeText(context, "Request sent!", Toast.LENGTH_SHORT).show();
                                            else
                                                Toast.makeText(context, "Failed!", Toast.LENGTH_SHORT).show();

                                        }

                                        @Override
                                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                                            Log.e("ERROR",t.getMessage());

                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}