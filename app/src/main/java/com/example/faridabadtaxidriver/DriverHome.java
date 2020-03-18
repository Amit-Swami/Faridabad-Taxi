package com.example.faridabadtaxidriver;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.core.view.GravityCompat;

import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.faridabadtaxidriver.Common.Common;
import com.example.faridabadtaxidriver.Model.FbdTaxiDriver;
import com.example.faridabadtaxidriver.Model.Token;
import com.example.faridabadtaxidriver.Remote.IGoogleAPI;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.maps.android.SphericalUtil;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import androidx.drawerlayout.widget.DrawerLayout;
import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverHome extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback{

    PlacesClient placesClient;
    List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS);
    AutocompleteSupportFragment places_Fragment;

    private GoogleMap mMap;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;


    //play services
    private static final int MY_PERMISSION_REQUEST_CODE=7000;
    private static final int PLAY_SERVICE_RES_REQUEST=7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    private static int UPDATE_INTTERVAL=5000;
    private static int FASTEST_INTTERVAL=3000;
    private static int DISPLACEMENT=10;

    DatabaseReference drivers;
    GeoFire geoFire;

    Marker mCurrent;

    MaterialAnimatedSwitch location_switch;

    SupportMapFragment mapFragment;

    //Car animation
    private List<LatLng> polyLineList;
    private Marker carMarker;
    private  float v;
    private double lat,lng;
    private Handler handler;
    private LatLng startPosition,endPosition,currentPosition;
    private int index,next;
    //private Button btnGo;
  //  private PlaceAutocompleteFragment places;
    AutocompleteFilter typeFilter;
    private String destination;
    private PolylineOptions polylineOptions,blackPolylineOptions;
    private Polyline blackPolyline,greyPolyline;

    private IGoogleAPI mService;

    //presence system
    DatabaseReference onlineRef,currentUserRef;

    //Firebase Storage
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    Runnable drawPathRunnable=new Runnable() {
        @Override
        public void run() {
            if (index<polyLineList.size()-1)
            {
                index++;
                next=index+1;
            }
            if (index<polyLineList.size()-1)
            {
                startPosition=polyLineList.get(index);
                endPosition=polyLineList.get(next);
            }

            ValueAnimator valueAnimator=ValueAnimator.ofFloat(0,1);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    v=valueAnimator.getAnimatedFraction();
                    lng=v*endPosition.longitude+(1-v)*startPosition.longitude;
                    lat=v*endPosition.latitude+(1-v)*startPosition.latitude;
                    LatLng newPos=new LatLng(lat,lng);
                    carMarker.setPosition(newPos);
                    carMarker.setAnchor(0.5f,0.5f);
                    carMarker.setRotation(getBearing(startPosition,newPos));
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(newPos)
                                    .zoom(15.5f)
                                    .build()
                    ));
                }
            });
            valueAnimator.start();
            handler.postDelayed(this,3000);
        }
    };

    private float getBearing(LatLng startPosition, LatLng endPosition) {
        double lat=Math.abs(startPosition.latitude-endPosition.latitude);
        double lng=Math.abs(startPosition.longitude-endPosition.longitude);

        if (startPosition.latitude < endPosition.latitude && startPosition.longitude < endPosition.longitude)
            return (float) (Math.toDegrees(Math.atan(lng/lat)));
        else if (startPosition.latitude >= endPosition.latitude && startPosition.longitude < endPosition.longitude)
            return (float) ((90-Math.toDegrees(Math.atan(lng/lat)))+90);
        else if (startPosition.latitude >= endPosition.latitude && startPosition.longitude >= endPosition.longitude)
            return (float) (Math.toDegrees(Math.atan(lng/lat))+180);
        else if (startPosition.latitude < endPosition.latitude && startPosition.longitude >= endPosition.longitude)
            return (float) ((90-Math.toDegrees(Math.atan(lng/lat)))+270);
        return -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        initPlaces();

        setUpPlaceAutoComplete();

        //Init frebase storage
        firebaseStorage=FirebaseStorage.getInstance();
        storageReference=firebaseStorage.getReference();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navigationHeaderView = navigationView.getHeaderView(0);
        TextView txtName = navigationHeaderView.findViewById(R.id.txtDriverName);
        TextView txtStars = navigationHeaderView.findViewById(R.id.txtStars);
        CircleImageView imageAvatar = navigationHeaderView.findViewById(R.id.image_avatar);

        txtName.setText(Common.currentFbdTaxiDriver.getName());
        txtStars.setText(Common.currentFbdTaxiDriver.getRates());

        if (Common.currentFbdTaxiDriver.getAvatarUrl() != null && !TextUtils.isEmpty(Common.currentFbdTaxiDriver.getAvatarUrl())) {
            Picasso.with(this)
                    .load(Common.currentFbdTaxiDriver.getAvatarUrl())
                    .into(imageAvatar);
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //Init View

        location_switch=findViewById(R.id.location_switch);
        location_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isOnline) {

                if (isOnline)
                {
                    FirebaseDatabase.getInstance().goOnline();//set connected when switch to on

                    buildLocationCallBack();
                    buildLocationRequest();
                    fusedLocationProviderClient.requestLocationUpdates(mLocationRequest,locationCallback,Looper.myLooper());

                    //Geo Fire
                    drivers= FirebaseDatabase.getInstance().getReference(Common.driver_tb1).child(Common.currentFbdTaxiDriver.getCarType());
                    geoFire=new GeoFire(drivers);
                    displayLocation();
                    Snackbar.make(mapFragment.getView(),"You are online",Snackbar.LENGTH_SHORT)
                            .show();
                }
                else
                {
                    FirebaseDatabase.getInstance().goOffline();//set disconnected when switch to off

                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);

                    mCurrent.remove();
                    mMap.clear();
                    if (handler!=null)
                        handler.removeCallbacks(drawPathRunnable);
                    Snackbar.make(mapFragment.getView(),"You are offline",Snackbar.LENGTH_SHORT)
                            .show();
                }

            }
        });

        polyLineList=new ArrayList<>();

       /* //Places Api
        typeFilter=new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .setTypeFilter(3)
                .build();
        places= (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if (location_switch.isChecked())
                {
                    destination=place.getAddress().toString();
                    destination=destination.replace(" ","+");

                    getDirection();
                }
                else {
                    Toast.makeText(DriverHome.this, "Please change your status to ONLINE", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(DriverHome.this, ""+status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });*/



        setUpLocation();

        mService= Common.getGoogleAPI();

        updateFirebaseToken();
    }

    private void setUpPlaceAutoComplete() {

        places_Fragment= (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        places_Fragment.setPlaceFields(placeFields);
        places_Fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                if (location_switch.isChecked())
                {
                    destination=place.getAddress().toString();
                    destination=destination.replace(" ","+");

                    getDirection();
                }
                else {
                    Toast.makeText(DriverHome.this, "Please change your status to ONLINE", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(DriverHome.this, ""+status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initPlaces() {
        Places.initialize(this,getString(R.string.google_direction_api));
        placesClient=Places.createClient(this);
    }


    @Override
    protected void onResume() {
        super.onResume();

        //presence system
        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
                onlineRef= FirebaseDatabase.getInstance().getReference().child(".info/connected");
                currentUserRef=FirebaseDatabase.getInstance().getReference(Common.driver_tb1)
                        .child(Common.currentFbdTaxiDriver.getCarType())
                        .child(account.getId());
                onlineRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //we will remove value from Driver tb1 when driver disconnected
                        currentUserRef.onDisconnect().removeValue();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onError(AccountKitError accountKitError) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        FirebaseDatabase.getInstance().goOffline();//set disconnected when switch to off

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

        if (mCurrent != null && mMap != null) {
            mCurrent.remove();
            mMap.clear();
        }
        if (handler!=null)
            handler.removeCallbacks(drawPathRunnable);
        super.onDestroy();
    }

    private void updateFirebaseToken() {

        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
                FirebaseDatabase db=FirebaseDatabase.getInstance();
                DatabaseReference tokens=db.getReference(Common.token_tb1);
                Token token=new Token(FirebaseInstanceId.getInstance().getToken());
                tokens.child(account.getId())
                        .setValue(token);
            }

            @Override
            public void onError(AccountKitError accountKitError) {

            }
        });
    }

    private void getDirection() {
        currentPosition=new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude());

        String requestApi=null;
        try {
            requestApi="https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+currentPosition.latitude+","+currentPosition.longitude+"&"+
                    "destination="+destination+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);
            Log.d("FBDTAXI",requestApi);//print url for debug
            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {

                            try {
                                JSONObject jsonObject=new JSONObject(response.body().toString());
                                JSONArray jsonArray=jsonObject.getJSONArray("routes");
                                for (int i=0;i<jsonArray.length();i++)
                                {
                                    JSONObject route=jsonArray.getJSONObject(i);
                                    JSONObject poly=route.getJSONObject("overview_polyline");
                                    String polyline=poly.getString("points");
                                    polyLineList=decodePoly(polyline);
                                }
                                //Adjusting bounds
                                if (!polyLineList.isEmpty()) {
                                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                    for (LatLng latLng : polyLineList)
                                        builder.include(latLng);
                                    LatLngBounds bounds = builder.build();
                                    CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);
                                    mMap.animateCamera(mCameraUpdate);
                                }

                                polylineOptions=new PolylineOptions();
                                polylineOptions.color(Color.GRAY);
                                polylineOptions.width(5);
                                polylineOptions.startCap(new SquareCap());
                                polylineOptions.endCap(new SquareCap());
                                polylineOptions.jointType(JointType.ROUND);
                                polylineOptions.addAll(polyLineList);
                                greyPolyline=mMap.addPolyline(polylineOptions);

                                blackPolylineOptions=new PolylineOptions();
                                blackPolylineOptions.color(Color.BLACK);
                                blackPolylineOptions.width(5);
                                blackPolylineOptions.startCap(new SquareCap());
                                blackPolylineOptions.endCap(new SquareCap());
                                blackPolylineOptions.jointType(JointType.ROUND);
                                blackPolyline=mMap.addPolyline(blackPolylineOptions);

                                mMap.addMarker(new MarkerOptions()
                                        .position(polyLineList.get(polyLineList.size()-1))
                                        .title("Pickup Location"));

                                //Animation
                                ValueAnimator polyLineAnimator=ValueAnimator.ofInt(0,100);
                                polyLineAnimator.setDuration(2000);
                                polyLineAnimator.setInterpolator(new LinearInterpolator());
                                polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                        List<LatLng> points=greyPolyline.getPoints();
                                        int percentValue=(int)valueAnimator.getAnimatedValue();
                                        int size=points.size();
                                        int newPoints=(int)(size *(percentValue/100.0f));
                                        List<LatLng> p=points.subList(0,newPoints);
                                        blackPolyline.setPoints(p);
                                    }
                                });
                                polyLineAnimator.start();

                                carMarker=mMap.addMarker(new MarkerOptions().position(currentPosition)
                                        .flat(true)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.drivermarker)));

                                handler=new Handler();
                                index=-1;
                                next=1;
                                handler.postDelayed(drawPathRunnable,3000);



                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(DriverHome.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            poly.add(new LatLng((double) lat / 1E5, (double) lng / 1E5));
        }

        return poly;
    }

    //reuest runtime permission, we need to override onRequestPermissionResult method


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                        buildLocationCallBack();
                        buildLocationRequest();
                        if (location_switch.isChecked())
                            displayLocation();
            }
        }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED )
        {
            //Respond runtime permission
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            },MY_PERMISSION_REQUEST_CODE);
        }
        else
        {
                buildLocationRequest();
                buildLocationCallBack();
                if (location_switch.isChecked()) {
                    drivers = FirebaseDatabase.getInstance().getReference(Common.driver_tb1).child(Common.currentFbdTaxiDriver.getCarType());
                    geoFire = new GeoFire(drivers);
                    displayLocation();
                }
            }
    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location:locationResult.getLocations())
                {
                    Common.mLastLocation = location;
                }
                displayLocation();
            }
        };
    }

    private void buildLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }


    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED )
        {
            return;
        }

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Common.mLastLocation = location;

                        if (Common.mLastLocation != null)
                        {
                            if (location_switch.isChecked())
                            {
                                final double latitude=Common.mLastLocation.getLatitude();
                                final double longitude=Common.mLastLocation.getLongitude();

                                LatLng center = new LatLng(latitude,longitude);
                                LatLng northSide = SphericalUtil.computeOffset(center,100000,0);
                                LatLng southSide = SphericalUtil.computeOffset(center,100000,180);

                                LatLngBounds bounds=LatLngBounds.builder()
                                        .include(northSide)
                                        .include(southSide)
                                        .build();

                              //  places_Fragment.setBoundsBias(bounds);
                              //  places.setFilter(typeFilter);


                                //update to firebase
                                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                                    @Override
                                    public void onSuccess(Account account) {
                                        geoFire.setLocation(account.getId(), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                                            @Override
                                            public void onComplete(String key, DatabaseError error) {
                                                //Add marker
                                                if (mCurrent != null)
                                                    mCurrent.remove();//remove already marker
                                                mCurrent=mMap.addMarker(new MarkerOptions()
                                                        .position(new LatLng(latitude,longitude))
                                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                                                        .title("Your Location"));

                                                //Move camera to this position
                                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),15.0f));

                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(AccountKitError accountKitError) {

                                    }
                                });
                            }
                        }
                        else
                        {
                            Log.d("ERROR","Cannot get your location");
                        }
                    }
                });

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.driver_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_trip_history) {
            // Handle the camera action
        } else if (id == R.id.nav_car_type) {
            showDialogUpdateCarType();

        } else if (id == R.id.nav_sign_out) {

            signOut();

        } else if (id == R.id.nav_update_info) {

            showDialogUpdateInfo();

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showDialogUpdateCarType() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("UPDATE VEHICLE TYPE");
        alertDialog.setMessage("Plese fill all information");

        LayoutInflater inflater=this.getLayoutInflater();
        View layout_upd_cartype=inflater.inflate(R.layout.layout_update_car_type,null);

        final RadioButton rdi_uberX = layout_upd_cartype.findViewById(R.id.rdi_uberX);
        final RadioButton rdi_UberBlack=layout_upd_cartype.findViewById(R.id.rdi_uber_black);

        //Load default data from user information
       if(Common.currentFbdTaxiDriver.getCarType().equals("UberX"))
           rdi_uberX.setChecked(true);
       else if (Common.currentFbdTaxiDriver.getCarType().equals("Uber Black"))
           rdi_UberBlack.setChecked(true);


        alertDialog.setView(layout_upd_cartype);

        //set Button
        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                final android.app.AlertDialog waitingDialog=new SpotsDialog.Builder().setContext(DriverHome.this).build();
                waitingDialog.show();


                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(final Account account) {

                        Map<String,Object> updateInfo = new HashMap<>();
                        if (rdi_uberX.isChecked())
                            updateInfo.put("carType",rdi_uberX.getText().toString());
                        else if (rdi_UberBlack.isChecked())
                            updateInfo.put("carType",rdi_UberBlack.getText().toString());

                        DatabaseReference driverInformation = FirebaseDatabase.getInstance().getReference(Common.user_driver_tb1);
                        driverInformation.child(account.getId())
                                .updateChildren(updateInfo)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            currentUserRef=FirebaseDatabase.getInstance().getReference(Common.driver_tb1)
                                                    .child(Common.currentFbdTaxiDriver.getCarType())
                                                    .child(account.getId());

                                            Toast.makeText(DriverHome.this, "Vehicle Type Updated !", Toast.LENGTH_SHORT).show();
                                        }
                                        else
                                            Toast.makeText(DriverHome.this, "Vehicle Type Update Failed !", Toast.LENGTH_SHORT).show();

                                        waitingDialog.dismiss();
                                    }
                                });

                        //Refresh driver data
                        driverInformation.child(account.getId())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Common.currentFbdTaxiDriver = dataSnapshot.getValue(FbdTaxiDriver.class);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {

                    }
                });
            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

            }
        });

        alertDialog.show();

    }

    private void showDialogUpdateInfo() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("UPDATE INFORMATION");
        alertDialog.setMessage("Plese fill all information");

        LayoutInflater inflater=this.getLayoutInflater();
        View layout_upd_info=inflater.inflate(R.layout.layout_update_information,null);

        final MaterialEditText edtName=layout_upd_info.findViewById(R.id.edtName);
        final MaterialEditText edtPhone=layout_upd_info.findViewById(R.id.edtPhone);
        final ImageView image_upload=layout_upd_info.findViewById(R.id.image_upload);
        image_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        alertDialog.setView(layout_upd_info);

        //set Button
        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
              dialogInterface.dismiss();
                final android.app.AlertDialog waitingDialog=new SpotsDialog.Builder().setContext(DriverHome.this).build();
                waitingDialog.show();


               AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                   @Override
                   public void onSuccess(Account account) {
                       String name = edtName.getText().toString();
                       String phone = edtPhone.getText().toString();

                       Map<String,Object> updateInfo = new HashMap<>();
                       if (!TextUtils.isEmpty(name))
                           updateInfo.put("name",name);
                       if (!TextUtils.isEmpty(phone))
                           updateInfo.put("phone",phone);

                       DatabaseReference driverInformation = FirebaseDatabase.getInstance().getReference(Common.user_driver_tb1);
                       driverInformation.child(account.getId())
                               .updateChildren(updateInfo)
                               .addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task) {
                                       if (task.isSuccessful())
                                           Toast.makeText(DriverHome.this, "Information Updated !", Toast.LENGTH_SHORT).show();
                                       else
                                           Toast.makeText(DriverHome.this, "Information Update Failed !", Toast.LENGTH_SHORT).show();

                                       waitingDialog.dismiss();
                                   }
                               });
                   }

                   @Override
                   public void onError(AccountKitError accountKitError) {

                   }
               });
            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
             dialogInterface.dismiss();

            }
        });

        alertDialog.show();
    }

    private void chooseImage() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Piture: "),Common.PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null)
        {
            Uri saveUri= data.getData();
            if (saveUri != null)
            {
                final ProgressDialog mDialog = new ProgressDialog(this);
                mDialog.setMessage("Uploading...");
                mDialog.show();

                String imageName = UUID.randomUUID().toString(); // Random name image upload
                final StorageReference imageFolder=storageReference.child("image/"+imageName);
                imageFolder.putFile(saveUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                               mDialog.dismiss();

                                imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(final Uri uri) {
                                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                                        @Override
                                        public void onSuccess(Account account) {
                                            //Update this url to avatar property of FbdTaxiDriver
                                            //First , You need  add avatar property on user model
                                            Map<String,Object> avatarUpdate = new HashMap<>();
                                            avatarUpdate.put("avatarUrl",uri.toString());

                                            DatabaseReference driverInformation = FirebaseDatabase.getInstance().getReference(Common.user_driver_tb1);
                                            driverInformation.child(account.getId())
                                                    .updateChildren(avatarUpdate)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful())
                                                                Toast.makeText(DriverHome.this, "Uploaded !", Toast.LENGTH_SHORT).show();
                                                            else
                                                                Toast.makeText(DriverHome.this, "Upload error !", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }

                                        @Override
                                        public void onError(AccountKitError accountKitError) {

                                        }
                                    });
                                    }
                                });
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                             double progress = (100.0*taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                             mDialog.setMessage("Uploaded "+progress+"%");
                            }
                        });

            }
        }
    }


    private void signOut() {

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = new AlertDialog.Builder(this,android.R.style.Theme_Material_Dialog_Alert);
        else
            builder = new AlertDialog.Builder(this);

        builder.setMessage("Do you want to logout ?")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AccountKit.logOut();
                        Intent intent=new Intent(DriverHome.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.show();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        try{
            boolean isSuccess=googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this,R.raw.uber_style)
            );

            if (! isSuccess)
                Log.e("ERROR","Map style load failed !!!");
        }
        catch (Resources.NotFoundException ex){
            ex.printStackTrace();
        }

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        //Update location
        buildLocationRequest();
        buildLocationCallBack();
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest,locationCallback, Looper.myLooper());
    }
}
