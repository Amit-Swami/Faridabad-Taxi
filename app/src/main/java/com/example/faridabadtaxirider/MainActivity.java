package com.example.faridabadtaxirider;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
//import android.support.annotation.NonNull;
//import android.support.design.widget.Snackbar;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.faridabadtaxirider.Common.Common;
import com.example.faridabadtaxirider.Model.Rider;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    Button btnContinue;

    FirebaseDatabase db;
    DatabaseReference users;
    MaterialEditText edtEmail,edtPwd,edtName,edtPh;
    RelativeLayout rootLayout;

    private final static int REQUEST_CODE=1000;

    TextView txt_forgot_pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        //init paper
        Paper.init(this);


        //init firebase
        db=FirebaseDatabase.getInstance();
        users=db.getReference(Common.user_rider_tb1);

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
                                    Common.currentUser = dataSnapshot.getValue(Rider.class);

                                    Intent homeIntent = new Intent(MainActivity.this,Home.class);
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
                                                //we will create new user and login
                                                Rider user = new Rider();
                                                user.setPhone(account.getPhoneNumber().toString());
                                                user.setName(account.getPhoneNumber().toString());
                                                user.setAvatarUrl("");
                                                user.setRates("0.0");

                                                //Register to Firebase
                                                users.child(account.getId())
                                                        .setValue(user)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                                //Login
                                                                users.child(account.getId())
                                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                Common.currentUser = dataSnapshot.getValue(Rider.class);

                                                                                Intent homeIntent = new Intent(MainActivity.this,Home.class);
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
                                                                Common.currentUser = dataSnapshot.getValue(Rider.class);

                                                                Intent homeIntent = new Intent(MainActivity.this,Home.class);
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
}