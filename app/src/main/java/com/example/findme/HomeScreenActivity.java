package com.example.findme;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.findme.customview.SpreadView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.BiConsumer;

public class HomeScreenActivity extends AppCompatActivity {
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private SpreadView sosButton;
    private ImageView emergencyContacts, logoutBtn;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private int i=0;
    private static final int REQUEST_CODE = 101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchCurrentLocation();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        if(mAuth.getCurrentUser()==null){
            Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(intent);
        }
        emergencyContacts = (ImageView) findViewById(R.id.emergencycontacts);
        logoutBtn = (ImageView) findViewById(R.id.logout);
        sosButton = (SpreadView) findViewById(R.id.sosbutton);
        sosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                i++;
                Handler handler = new Handler();
                Runnable run = new Runnable() {
                    @Override
                    public void run() {
                        i=0;
                    }
                };
                if(i==3){
                    handleEmergency();
                    i=0;
                }else{
                    handler.postDelayed(run, 500);
                }
            }
        });
        emergencyContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),EmergencyContactsActivity.class);
                startActivity(intent);
            }
        });
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent intent = new Intent(getApplicationContext(),SplashActivity.class);
                startActivity(intent);
            }
        });
    }

    private void fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.SEND_SMS}, REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location!=null){
                    currentLocation = location;
                    Toast.makeText(getApplicationContext(),currentLocation.getLatitude() + " " + currentLocation.getLongitude(),Toast.LENGTH_SHORT ).show();
                }
            }
        });
    }

    private void handleEmergency() {
        db.collection("users").document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList <Map<String,String>> contacts = (ArrayList<Map<String,String>>) document.getData().get("contacts");
                        String message = (String) document.getData().get("message");
                        for(Map<String,String> contact:contacts){
                            shareLocationViaSMS(contact.get("countryCode")+contact.get("contact"), message, currentLocation);
                        }
                        Log.d("FetchContactsSuccess", "DocumentSnapshot data: " + document.getData().get("contacts"));
                    } else {
                        Log.d("FetchContactsNotFound", "No such document");
                        Toast.makeText(getApplicationContext(),"Please add contacts to share your location",Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d("FetchContactsFailed", "get failed with ", task.getException());
                    Toast.makeText(getApplicationContext(),"Problem Fetching Contacts",Toast.LENGTH_LONG).show();
                }
            }
        });
        Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
        startActivity(intent);
    }

    public void shareLocationViaSMS(String phoneNumber, String messageText, Location currentLocation) {
        try{
            SmsManager sms = SmsManager.getDefault();
            StringBuffer message = new StringBuffer();
            message.append(messageText+" \n");
            message.append("http://maps.google.com?q=");
            message.append(currentLocation.getLatitude());
            message.append(",");
            message.append(currentLocation.getLongitude());
            sms.sendTextMessage(phoneNumber, null, message.toString(), null, null);
            Toast.makeText(getApplicationContext(),"Alerted "+phoneNumber,Toast.LENGTH_LONG).show();
        }catch(Exception e){
            Log.d("smsfailed",e.getMessage());
            Toast.makeText(getApplicationContext(),e.getMessage().toString(),Toast.LENGTH_LONG).show();
            Toast.makeText(getApplicationContext(),"Please allow app to send sms",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_CODE:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    fetchCurrentLocation();
                }
                break;
        }
    }
}