package com.example.faridabadtaxidriver;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.faridabadtaxidriver.Common.Common;
import com.example.faridabadtaxidriver.Model.DataMessage;
import com.example.faridabadtaxidriver.Model.FCMResponse;
import com.example.faridabadtaxidriver.Model.Token;
import com.example.faridabadtaxidriver.Remote.IFCMService;
import com.example.faridabadtaxidriver.Remote.IGoogleAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerCall extends AppCompatActivity {

  TextView txtTime,txtAddress,txtDistance,txtCountDown;
  MediaPlayer mediaPlayer;
  IGoogleAPI mService;
  Button btnAccept,btnDecline;
  String customerId;
  IFCMService mFCMService;
  String lat,lng;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_customer_call);

    mService=Common.getGoogleAPI();
    mFCMService=Common.getFCMService();

    txtTime=findViewById(R.id.txtTime);
    txtAddress=findViewById(R.id.txtAddress);
    txtDistance=findViewById(R.id.txtDistance);
    txtCountDown=findViewById(R.id.txt_count_down);

    btnAccept=findViewById(R.id.btnAccept);
    btnDecline=findViewById(R.id.btnDecline);

    btnAccept.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent=new Intent(CustomerCall.this,DriverTracking.class);
        //send customer location to new activity
        intent.putExtra("lat",lat);
        intent.putExtra("lng",lng);
        intent.putExtra("customerId",customerId);

        startActivity(intent);
        finish();
      }
    });

    btnDecline.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (!TextUtils.isEmpty(customerId))
          cancelBooking(customerId);
      }
    });

    mediaPlayer=MediaPlayer.create(this,R.raw.ringtone);
    mediaPlayer.setLooping(true);
    mediaPlayer.start();

    if (getIntent() != null)
    {
      lat=getIntent().getStringExtra("lat");
      lng=getIntent().getStringExtra("lng");
      customerId=getIntent().getStringExtra("customer");

      getDirection(lat,lng);
    }

    startTimer();
  }

  private void startTimer() {
    CountDownTimer countDownTimer=new CountDownTimer(30000,1000) {
      @Override
      public void onTick(long l) {
        txtCountDown.setText(String.valueOf(l / 1000));
      }

      @Override
      public void onFinish() {
        if (!TextUtils.isEmpty(customerId))
          cancelBooking(customerId);
        else
          Toast.makeText(CustomerCall.this, "Custome Id must be not null", Toast.LENGTH_SHORT).show();
      }
    }.start();
  }

  private void cancelBooking(String customerId) {
    Token token=new Token(customerId);

    // Notification notification=new Notification("Cancel","Driver has cancelled your request");
    // Sender sender=new Sender(token.getToken(),notification);
    Map<String,String> content = new HashMap<>();
    content.put("title","Cancel");
    content.put("message","Driver has cancelled your request");
    DataMessage dataMessage = new DataMessage(token.getToken(),content);

    mFCMService.sendMessage(dataMessage)
            .enqueue(new Callback<FCMResponse>() {
              @Override
              public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if (response.body().success==1)
                {
                  Toast.makeText(CustomerCall.this, "Cancelled", Toast.LENGTH_SHORT).show();
                  finish();
                }
              }

              @Override
              public void onFailure(Call<FCMResponse> call, Throwable t) {

              }
            });
  }

  private void getDirection(String lat,String lng) {


    String requestApi=null;
    try {
      requestApi="https://maps.googleapis.com/maps/api/directions/json?"+
              "mode=driving&"+
              "transit_routing_preference=less_driving&"+
              "origin="+ Common.mLastLocation.getLatitude() +","+Common.mLastLocation.getLongitude()+"&"+
              "destination="+lat+","+lng+"&"+
              "key="+getResources().getString(R.string.google_direction_api);
      Log.d("FBDTAXI",requestApi);//print url for debug
      mService.getPath(requestApi)
              .enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {

                  try {
                    JSONObject jsonObject=new JSONObject(response.body().toString());

                    JSONArray routes=jsonObject.getJSONArray("routes");

                    //after get routes , just get first elements of routes
                    JSONObject object=routes.getJSONObject(0);

                    //after get first element , we need array with name legs
                    JSONArray legs=object.getJSONArray("legs");

                    //and get first element of leg array
                    JSONObject legsObject=legs.getJSONObject(0);

                    //Now,get distance
                    JSONObject distance=legsObject.getJSONObject("distance");
                    txtDistance.setText(distance.getString("text"));

                    //get Time
                    JSONObject time=legsObject.getJSONObject("duration");
                    txtTime.setText(time.getString("text"));

                    //get address
                    String address=legsObject.getString("end_address");
                    txtAddress.setText(address);


                  } catch (JSONException e) {
                    e.printStackTrace();
                  }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                  Toast.makeText(CustomerCall.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                }
              });
    }catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  @Override
  protected void onStop() {
      mediaPlayer.release();
      super.onStop();
   // if (mediaPlayer.isPlaying())
      //  mediaPlayer.release();
  }

  @Override
  protected void onPause() {
      mediaPlayer.release();
      super.onPause();
   // if (mediaPlayer.isPlaying())
     //   mediaPlayer.release();
  }

  @Override
  protected void onResume() {
    super.onResume();
    //if (mediaPlayer != null && !mediaPlayer.isPlaying())
      mediaPlayer.start();
  }
}