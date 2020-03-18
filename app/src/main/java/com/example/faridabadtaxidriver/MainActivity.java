package com.example.faridabadtaxidriver;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.faridabadtaxidriver.Common.Common;
import com.example.faridabadtaxidriver.Model.FbdTaxiDriver;
import com.example.faridabadtaxidriver.Model.Token;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {


    private static final int REQUEST_CODE = 1000;
    Button btnContinue;

    FirebaseDatabase db;
    DatabaseReference users;
    MaterialEditText edtEmail,edtPwd,edtName,edtPh;
    RelativeLayout rootLayout;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void updateTokenToServer(){
        FirebaseDatabase db=FirebaseDatabase.getInstance();
        final DatabaseReference tokens=db.getReference(Common.token_tb1);



        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(final Account account) {
                FirebaseInstanceId.getInstance()
                        .getInstanceId()
                        .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                            @Override
                            public void onSuccess(InstanceIdResult instanceIdResult) {
                                Token token=new Token(instanceIdResult.getToken());
                                tokens.child(account.getId())
                                        .setValue(token);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("ERROR_TOKEN",e.getMessage());
                                Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onError(AccountKitError accountKitError) {
                Log.d("ERROR_ACCOUNTKIT",accountKitError.getUserFacingMessage());
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        printKeyHash();

        //Init paperdb
        Paper.init(this);

        //init firebase
            db = FirebaseDatabase.getInstance();
            users = db.getReference(Common.user_driver_tb1);

        btnContinue=findViewById(R.id.btnContinue);
        rootLayout=findViewById(R.id.rootLayout);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithPhone();
            }
        });


        //Auto login with facebook account kit for second time
        if (AccountKit.getCurrentAccessToken() != null)
        {
            final AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(MainActivity.this).build();
            waitingDialog.show();
            waitingDialog.setMessage("Please waiting....");
            waitingDialog.setCancelable(false);

            AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                @Override
                public void onSuccess(Account account) {
                    users.child(account.getId())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Common.currentFbdTaxiDriver = dataSnapshot.getValue(FbdTaxiDriver.class);

                                    updateTokenToServer();

                                    Intent homeIntent = new Intent(MainActivity.this,DriverHome.class);
                                    startActivity(homeIntent);

                                    waitingDialog.dismiss();
                                    finish();
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

    }

    private void signInWithPhone() {
        Intent intent = new Intent(MainActivity.this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder = new AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE, AccountKitActivity.ResponseType.TOKEN);
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,configurationBuilder.build());
        startActivityForResult(intent,REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE)
        {
            AccountKitLoginResult result = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            if (result.getError() != null)
            {
                Toast.makeText(this, ""+result.getError().getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            else if (result.wasCancelled())
            {
                Toast.makeText(this, "Cancel login", Toast.LENGTH_SHORT).show();
                return;
            }
            else {
                if (result.getAccessToken() != null)
                {
                    //show dialog
                    final AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(MainActivity.this).build();
                    waitingDialog.show();
                    waitingDialog.setMessage("Please waiting....");
                    waitingDialog.setCancelable(false);

                    //Get current phone
                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(final Account account) {
                            final String userId = account.getId();

                            //Check if exists on Firebase
                            users.orderByKey().equalTo(account.getId())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (!dataSnapshot.child(account.getId()).exists())//if not exists
                                            {
                                                //we will create new fbdTaxiDriver and login
                                                FbdTaxiDriver fbdTaxiDriver = new FbdTaxiDriver();
                                                fbdTaxiDriver.setPhone(account.getPhoneNumber().toString());
                                                fbdTaxiDriver.setName(account.getPhoneNumber().toString());
                                                fbdTaxiDriver.setAvatarUrl("");
                                                fbdTaxiDriver.setRates("0.0");
                                                fbdTaxiDriver.setCarType("UberX");//Default car type is UberX

                                                //Register to Firebase
                                                users.child(account.getId())
                                                        .setValue(fbdTaxiDriver)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                                //Login
                                                                users.child(account.getId())
                                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                Common.currentFbdTaxiDriver = dataSnapshot.getValue(FbdTaxiDriver.class);

                                                                                updateTokenToServer();

                                                                                Intent homeIntent = new Intent(MainActivity.this,DriverHome.class);
                                                                                startActivity(homeIntent);

                                                                                waitingDialog.dismiss();
                                                                                finish();
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }

                                            else // if user exist , just login
                                            {
                                                //Login
                                                users.child(account.getId())
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                Common.currentFbdTaxiDriver = dataSnapshot.getValue(FbdTaxiDriver.class);

                                                                updateTokenToServer();

                                                                Intent homeIntent = new Intent(MainActivity.this,DriverHome.class);
                                                                startActivity(homeIntent);

                                                                waitingDialog.dismiss();
                                                                finish();
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                            }

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }

                        @Override
                        public void onError(AccountKitError accountKitError) {
                            Toast.makeText(MainActivity.this, ""+accountKitError.getErrorType().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    }

    private void printKeyHash() {
        try{
            PackageInfo info = getPackageManager().getPackageInfo("com.example.faridabadtaxidriver",
                    PackageManager.GET_SIGNATURES);

            for (Signature signature:info.signatures)
            {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KEYHASH", Base64.encodeToString(md.digest(),Base64.DEFAULT));
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


}