package com.example.faridabadtaxirider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
//import android.support.annotation.NonNull;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
//import androidx.core.content.LocalBroadcastManager;
///import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
//import android.support.design.widget.NavigationView;
import androidx.core.view.GravityCompat;
//import androidx.core.widget.DrawerLayout;
//import android.support.v7.app.ActionBarDrawerToggle;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arsy.maps_library.MapRipple;
import com.example.faridabadtaxirider.Common.Common;
import com.example.faridabadtaxirider.Helper.CustomInfoWindow;
import com.example.faridabadtaxirider.Model.Rider;
import com.example.faridabadtaxirider.Model.Token;
import com.example.faridabadtaxirider.Remote.IFCMService;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.maps.android.SphericalUtil;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener, ValueEventListener {


    PlacesClient placesClient;
    List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG);
    AutocompleteSupportFragment place_location,place_destination;

    SupportMapFragment mapFragment;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;

    //Location
    private GoogleMap mMap;
    private static final int MY_PERMISSION_REQUEST_CODE=7000;
    private static final int PLAY_SERVICE_RES_REQUEST=7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTTERVAL=5000;
    private static int FASTEST_INTTERVAL=3000;
    private static int DISPLACEMENT=10;

    DatabaseReference ref;
    GeoFire geoFire;

    Marker mUserMarker,markerDestination;

    //Bottomsheet
    ImageView imgExpandable;
    BottomSheetRiderFragment mBottomSheet;
    Button btnPickupRequest;

    int radius=1; // 1km
    int distance=1;//3km
    private static final int LIMIT=3;
    
    //send alert
    IFCMService mService;

    //presence system
    DatabaseReference driversAvailable;

    //PlaceAutocompleteFragment place_location,place_destination;
    AutocompleteFilter typeFilter;

    String mPlaceLocation,mPlaceDestination;

    //New Update Information
    CircleImageView imageAvatar;
    TextView txtRiderName,txtStars;

    //Declare FireStorage to upload avatar
    FirebaseStorage storage;
    StorageReference storageReference;

    //Vehicle type
    ImageView carUberX,carUberBlack;
    boolean isUberX=true;

    //Map animation
    MapRipple mapRipple;


    private BroadcastReceiver mCancelBroadCast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Common.driverId="";
            Common.isDriverFound=false;

            btnPickupRequest.setText("PICKUP REQUEST");
            btnPickupRequest.setEnabled(true);

            if (mapRipple.isAnimationRunning())
                mapRipple.stopRippleMapAnimation();

            mUserMarker.hideInfoWindow();
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        initPlaces();

        setUpPlaceAutoComplete();

        //Register for cancel request
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mCancelBroadCast,new IntentFilter(Common.CANCEL_BROADCAST_STRING));

        //Register for arrived request
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mCancelBroadCast,new IntentFilter(Common.BROADCAST_DROP_OFF));
        
        mService=Common.getFCMService();

        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this);

        //Init storage
        storage = FirebaseStorage.getInstance();
        storageReference=storage.getReference();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navigationHeaderView = navigationView.getHeaderView(0);
        txtRiderName = navigationHeaderView.findViewById(R.id.txtRiderName);
        txtRiderName.setText(String.format("%s",Common.currentUser.getName()));
        txtStars=navigationHeaderView.findViewById(R.id.txtStars);
        txtStars.setText(String.format("%s",Common.currentUser.getRates()));
        imageAvatar=navigationHeaderView.findViewById(R.id.imageAvatar);

        carUberX=findViewById(R.id.select_uber_x);
        carUberBlack=findViewById(R.id.select_uber_black);

        //Event
        carUberX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isUberX = true;
                if (isUberX)
                {
                    carUberX.setImageResource(R.drawable.car_cui_select);
                    carUberBlack.setImageResource(R.drawable.car_vip);
                }
                else
                {
                    carUberX.setImageResource(R.drawable.car_cui);
                    carUberBlack.setImageResource(R.drawable.car_vip_select);
                }
                mMap.clear();
                if (driversAvailable != null)
                    driversAvailable.removeEventListener(Home.this);
                driversAvailable=FirebaseDatabase.getInstance().getReference(Common.driver_tb1).child(isUberX?"UberX":"Uber Black");
                driversAvailable.addValueEventListener(Home.this);
                loadAllAvailableDriver(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
            }
        });

        carUberBlack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isUberX=false;
                if (isUberX)
                {
                    carUberX.setImageResource(R.drawable.car_cui_select);
                    carUberBlack.setImageResource(R.drawable.car_vip);
                }
                else
                {
                    carUberX.setImageResource(R.drawable.car_cui);
                    carUberBlack.setImageResource(R.drawable.car_vip_select);
                }
                mMap.clear();
                if (driversAvailable != null)
                    driversAvailable.removeEventListener(Home.this);
                driversAvailable=FirebaseDatabase.getInstance().getReference(Common.driver_tb1).child(isUberX?"UberX":"Uber Black");
                driversAvailable.addValueEventListener(Home.this);
                loadAllAvailableDriver(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
            }
        });

        //Load Avatar
        if (Common.currentUser.getAvatarUrl() != null && !TextUtils.isEmpty(Common.currentUser.getAvatarUrl()))
        {
            Picasso.with(this)
                    .load(Common.currentUser.getAvatarUrl())
                    .into(imageAvatar);
        }


        //Maps
        mapFragment= (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        btnPickupRequest=findViewById(R.id.btnPickupRequest);
        btnPickupRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!Common.isDriverFound)
                {
                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(Account account) {
                            requestPickupHere(account.getId());

                        }

                        @Override
                        public void onError(AccountKitError accountKitError) {

                        }
                    });
                }
                else {
                    btnPickupRequest.setEnabled(false);
                    Common.sendRequestToDriver(Common.driverId, mService, getBaseContext(), Common.mLastLocation);
                }
            }
        });


        setUpLocation();

        updateFirebaseToken();
    }

    private void setUpPlaceAutoComplete() {

        place_location= (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.place_location);
        place_location.setPlaceFields(placeFields);
        place_destination= (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.place_destination);
        place_destination.setPlaceFields(placeFields);
        place_location.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                final String latLng=place.getAddress();
                // Log.i("Tag", "onPlaceSelcted" + latLng.latitude + "\n" + latLng.longitude);
                mPlaceLocation= Objects.requireNonNull(place.getAddress()).toString();
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(place.getLatLng())
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker))
                        .title("Pickup Here"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),15.0f));
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(Home.this, ""+status.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        place_destination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                final String latLng=place.getAddress().toString();
                //Log.i("Tag","onPlaceSelcted"+latLng.latitude+"\n"+latLng.longitude);
                mPlaceDestination= place.getAddress().toString();
                mMap.addMarker(new MarkerOptions().position(place.getLatLng())
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.destination_marker))
                        .title("Destination"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),15.0f));

                //show information in button
                BottomSheetRiderFragment mBottomsheet = BottomSheetRiderFragment.newInstance(mPlaceLocation,mPlaceDestination,false);
                mBottomsheet.show(getSupportFragmentManager(),mBottomsheet.getTag());
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(Home.this, ""+status.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initPlaces() {
        Places.initialize(this,getString(R.string.places_api_key));
        placesClient=Places.createClient(this);
    }

    private void updateFirebaseToken() {
        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(final Account account) {
                FirebaseDatabase db=FirebaseDatabase.getInstance();
                final DatabaseReference tokens=db.getReference(Common.token_tb1);

                FirebaseInstanceId.getInstance()
                        .getInstanceId()
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Home.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                            @Override
                            public void onSuccess(InstanceIdResult instanceIdResult) {
                                Token token=new Token(instanceIdResult.getToken());
                                tokens.child(account.getId())
                                        .setValue(token);     
                            }
                        });
            }

            @Override
            public void onError(AccountKitError accountKitError) {

            }
        });
    }


    private void requestPickupHere(String uid) {
        DatabaseReference dbRequest=FirebaseDatabase.getInstance().getReference(Common.pickup_request_tb1);
        GeoFire mGeoFire=new GeoFire(dbRequest);
        mGeoFire.setLocation(uid, new GeoLocation(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()),
                new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        //Fix crash
                    }
                });

        if (mUserMarker.isVisible())
            mUserMarker.remove();
        //Add new Marker
       mUserMarker = mMap.addMarker(new MarkerOptions()
                             .title("Pickup Here")
                             .snippet("")
                             .position(new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude()))
                             .icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker)));
       mUserMarker.showInfoWindow();


       //Animation
        mapRipple = new MapRipple(mMap,new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude()),this);
        mapRipple.withNumberOfRipples(1);
        mapRipple.withDistance(500);
        mapRipple.withRippleDuration(1000);
        mapRipple.withTransparency(0.5f);

        mapRipple.startRippleMapAnimation();

       btnPickupRequest.setText("Getting your DRIVER...");

       findDriver();
    }

    private void findDriver() {
        DatabaseReference driverLocation;
        if (isUberX)
            driverLocation=FirebaseDatabase.getInstance().getReference(Common.driver_tb1).child("UberX");
        else
            driverLocation=FirebaseDatabase.getInstance().getReference(Common.driver_tb1).child("Uber Black");
        final GeoFire gf=new GeoFire(driverLocation);

        final GeoQuery geoQuery=gf.queryAtLocation(new GeoLocation(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude()),radius);

        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                //if found
                if (!Common.isDriverFound)
                {
                    Common.isDriverFound=true;
                    Common.driverId=key;
                    btnPickupRequest.setText("CALL DRIVER");
                   // Toast.makeText(Home.this, ""+key, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                //if still not found driver increase distance
                if (!Common.isDriverFound && radius < LIMIT)
                {
                    radius++;
                    findDriver();
                }
                else {
                    if (!Common.isDriverFound) {
                        Toast.makeText(Home.this, "No any driver available near you", Toast.LENGTH_SHORT).show();
                        btnPickupRequest.setText("REQUEST PICKUP");
                        geoQuery.removeAllListeners();
                    }
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
            {
                case MY_PERMISSION_REQUEST_CODE:
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    {
                        setUpLocation();
                           //createLocationRequest();
                            //displayLocation();
                    }
                    break;
        }
    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
        {
            //Respond runtime permission
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CALL_PHONE
            },MY_PERMISSION_REQUEST_CODE);
        }
        else
        {
                buildLocationCallBack();
                createLocationRequest();
                displayLocation();
            }
    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                mLastLocation = locationResult.getLastLocation();
                Common.mLastLocation = locationResult.getLocations().get(locationResult.getLocations().size()-1); // get last location
                displayLocation();
            }
        };
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED )
        {
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
              Common.mLastLocation = location;
                if (Common.mLastLocation != null)
                {

                    //create LatLng from mLastLocation and this is center point
                    LatLng center = new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude());
                    //distance in meteres
                    //heading 0  is northSide ,90 is east, 180 is south and 270 is west
                    //base on compact
                    LatLng northSide = SphericalUtil.computeOffset(center,100000,0);
                    LatLng southSide = SphericalUtil.computeOffset(center,100000,180);

                    LatLngBounds bounds = LatLngBounds.builder()
                            .include(northSide)
                            .include(southSide)
                            .build();

                /*    place_location.setBoundsBias(bounds);
                    place_location.setFilter(typeFilter);

                    place_destination.setBoundsBias(bounds);
                    place_location.setFilter(typeFilter);*/

                    //presence system
                    driversAvailable=FirebaseDatabase.getInstance().getReference(Common.driver_tb1).child(isUberX?"UberX":"Uber Black");
                    driversAvailable.addValueEventListener(Home.this);

                    final double latitude=Common.mLastLocation.getLatitude();
                    final double longitude=Common.mLastLocation.getLongitude();


                    loadAllAvailableDriver(new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude()));
                }
                else
                {
                    Log.d("ERROR","Cannot get your location");
                }
            }
        });
    }

    private void loadAllAvailableDriver(final LatLng location) {

        //Add marker
        //here we will clear all map to delete old position of driver
        mMap.clear();
        if (mUserMarker != null)
            mUserMarker.remove();//remove already marker
        mUserMarker=mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker))
                .position(location)
                .title(String.format("You")));


        //Move camera to this position
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,15.0f));


        //Load all available driver in distance 3 km
        DatabaseReference driverLocation;
        if (isUberX)
            driverLocation=FirebaseDatabase.getInstance().getReference(Common.driver_tb1).child("UberX");
        else
            driverLocation=FirebaseDatabase.getInstance().getReference(Common.driver_tb1).child("Uber Black");
        GeoFire gf=new GeoFire(driverLocation);

        GeoQuery geoQuery=gf.queryAtLocation(new GeoLocation(location.latitude,location.longitude),distance);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {
                //use key to get email from table users
                FirebaseDatabase.getInstance().getReference(Common.user_driver_tb1)
                        .child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //Because rider and user model has same properties
                                //so we can use rider model to get user here
                                Rider rider=dataSnapshot.getValue(Rider.class);

                                if (isUberX)
                                {
                                    if (rider.getCarType().equals("UberX"))
                                    {
                                        //Add driver to map
                                        mMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(location.latitude,location.longitude))
                                                .flat(true)
                                                .title(rider.getName())
                                                .snippet("Driver ID : "+dataSnapshot.getKey())
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.drivermarker)));
                                    }
                                }
                                else
                                {
                                    if (rider.getCarType().equals("Uber Black"))
                                    {
                                        //Add driver to map
                                        mMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(location.latitude,location.longitude))
                                                .flat(true)
                                                .title(rider.getName())
                                                .snippet("Driver ID : "+dataSnapshot.getKey())
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.drivermarker)));
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (distance <= LIMIT)//distance just find for 3 km
                {
                    distance++;
                    loadAllAvailableDriver(location);
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void createLocationRequest() {
        mLocationRequest=new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
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
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_signOut) {
            signOut();
        }
        else if (id == R.id.nav_updateInformation)
        {
            showUpdateInformation();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showUpdateInformation() {

        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setTitle("Update Information");
        dialog.setMessage("Please fill full information");

        LayoutInflater inflater=LayoutInflater.from(this);
        View update_info_layout=inflater.inflate(R.layout.layout_update_information,null);

        final MaterialEditText edtName=update_info_layout.findViewById(R.id.edtName);
        final MaterialEditText edtPhone=update_info_layout.findViewById(R.id.edtPhone);
        final ImageView imgAvatar = update_info_layout.findViewById(R.id.imgAvatar);
        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImageAndUpload();
            }
        });

        dialog.setView(update_info_layout);

        //setButton
        dialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
              dialogInterface.dismiss();

                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        final android.app.AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(Home.this).build();
                        waitingDialog.show();

                        String name = edtName.getText().toString();
                        String phone = edtPhone.getText().toString();

                        Map<String,Object> update = new HashMap<>();
                        if (!TextUtils.isEmpty(name))
                            update.put("name",name);
                        if (!TextUtils.isEmpty(phone))
                            update.put("phone",phone);

                        //Update
                        DatabaseReference riderInformation =FirebaseDatabase.getInstance().getReference(Common.user_rider_tb1);
                        riderInformation.child(account.getId())
                                .updateChildren(update).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                waitingDialog.dismiss();
                                if (task.isSuccessful())
                                    Toast.makeText(Home.this, "Information updated!", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(Home.this, "Information wasn't update", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {

                    }
                });

            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
             dialogInterface.dismiss();
            }
        });


        dialog.show();
    }

    private void chooseImageAndUpload() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),Common.PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            Uri saveUri = data.getData();
            if (saveUri != null)
            {
                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Uploading...");
                progressDialog.show();

                String imageName = UUID.randomUUID().toString();
                final StorageReference imageFolder = storageReference.child("images/"+imageName);
                imageFolder.putFile(saveUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();

                                imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(final Uri uri) {
                                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                                        @Override
                                        public void onSuccess(Account account) {
                                            //Save url to User Information Table
                                            Map<String,Object> update = new HashMap<>();
                                            update.put("avatarUrl",uri.toString());

                                            //Made Update
                                            DatabaseReference riderInformation =FirebaseDatabase.getInstance().getReference(Common.user_rider_tb1);
                                            riderInformation.child(account.getId())
                                                    .updateChildren(update).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful())
                                                        Toast.makeText(Home.this, "Avatar was uploaded", Toast.LENGTH_SHORT).show();
                                                    else
                                                        Toast.makeText(Home.this, "Avatar wasn't uploaded", Toast.LENGTH_SHORT).show();

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        progressDialog.setMessage("Uploaded"+progress+"%");

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
                        Intent intent=new Intent(Home.this,MainActivity.class);
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

       mMap=googleMap;
       mMap.getUiSettings().setZoomControlsEnabled(true);
       mMap.getUiSettings().setZoomGesturesEnabled(true);
       mMap.setInfoWindowAdapter(new CustomInfoWindow(this));

       mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                   @Override
                   public void onMapClick(LatLng latLng) {
                       //first, check marker destination
                       //If is not null just remove available marker
                       if (markerDestination != null)
                           markerDestination.remove();
                       markerDestination=mMap.addMarker(new MarkerOptions()
                               .icon(BitmapDescriptorFactory.fromResource(R.mipmap.destination_marker))
                       .position(latLng)
                       .title("Destination"));
                       mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15.0f));

                       //show bottom sheet
                       BottomSheetRiderFragment mBottomsheet = BottomSheetRiderFragment.newInstance(String.format("%f,%f",mLastLocation.getLatitude(),mLastLocation.getLongitude()),
                               String.format("%f,%f",latLng.latitude,latLng.longitude),
                               true);
                       mBottomsheet.show(getSupportFragmentManager(),mBottomsheet.getTag());
                   }
               });

       mMap.setOnInfoWindowClickListener(this);

       fusedLocationProviderClient.requestLocationUpdates(mLocationRequest,locationCallback, Looper.myLooper());

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        //If marker info window is your location , don't apply this event
        if (!marker.getTitle().equals("You"))
        {

            Intent intent = new Intent(Home.this,CallDriver.class);
            //send information to new activity
            intent.putExtra("driverId",marker.getSnippet().replaceAll("\\D+",""));
            intent.putExtra("lat",Common.mLastLocation.getLatitude());
            intent.putExtra("lng",Common.mLastLocation.getLongitude());
            startActivity(intent);
        }
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        loadAllAvailableDriver(new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude()));
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mCancelBroadCast);
        super.onDestroy();
    }
}