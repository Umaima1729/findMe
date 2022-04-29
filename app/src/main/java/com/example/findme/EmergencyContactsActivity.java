package com.example.findme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EmergencyContactsActivity extends AppCompatActivity {
    private EditText emergencyContact1, emergencyContact2, emergencyContact3;
    private CountryCodePicker ccp1, ccp2, ccp3;
    private TextInputEditText messageField;
    private Button saveBtn;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        messageField = (TextInputEditText) findViewById(R.id.messagefield);
        emergencyContact1 = (EditText) findViewById(R.id.editTextPhone1);
        emergencyContact2 = (EditText) findViewById(R.id.editTextPhone2);
        emergencyContact3 = (EditText) findViewById(R.id.editTextPhone3);
        ccp1 = (CountryCodePicker) findViewById(R.id.ccp1);
        ccp2 = (CountryCodePicker) findViewById(R.id.ccp2);
        ccp3 = (CountryCodePicker) findViewById(R.id.ccp3);
        prefillFields();
        saveBtn = (Button) findViewById(R.id.saveContacts);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveEmergencyContacts();
            }
        });
    }

    private void prefillFields() {
        db.collection("users").document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        ArrayList<Map<String,String>> contacts = (ArrayList<Map<String, String>>) document.getData().get("contacts");
                        String message = (String) document.getData().get("message");
                        Log.d("Message",""+message);
                        if(message==null){
                            messageField.setText("I need your help. This is emergency situation!!!. Try to find me using the location.");
                        }else{
                            messageField.setText(message);
                        }
                        if(contacts.size()>0){
                            emergencyContact1.setText(getPhoneNumber(contacts.get(0)));
                            ccp1.setCountryForPhoneCode(Integer.parseInt(contacts.get(0).get("countryCode")));
                        }
                        if(contacts.size()>1){
                            emergencyContact2.setText(getPhoneNumber(contacts.get(1)));
                            ccp2.setCountryForPhoneCode(Integer.parseInt(contacts.get(1).get("countryCode")));
                        }
                        if(contacts.size()>2){
                            emergencyContact3.setText(getPhoneNumber(contacts.get(2)));
                            ccp3.setCountryForPhoneCode(Integer.parseInt(contacts.get(2).get("countryCode")));
                        }
                    }
                }else{
                    Log.d("FetchContactsFailed", "get failed with ", task.getException());
                    Toast.makeText(getApplicationContext(),"Problem Fetching Contacts",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private String getPhoneNumber(Map<String,String> contact) {
        return contact.get("contact");
    }

    private void saveEmergencyContacts() {
        String message = messageField.getText().toString().trim();
        String contact1 = emergencyContact1.getText().toString().trim();
        String contact2 = emergencyContact2.getText().toString().trim();
        String contact3 = emergencyContact3.getText().toString().trim();
        String countryCode1 = ccp1.getSelectedCountryCode().toString().trim();
        String countryCode2 = ccp2.getSelectedCountryCode().toString().trim();
        String countryCode3 = ccp3.getSelectedCountryCode().toString().trim();
        if(contact1.isEmpty() && contact2.isEmpty() && contact3.isEmpty()){
            Toast.makeText(EmergencyContactsActivity.this,"Please add atleast one emergencuy contact",Toast.LENGTH_LONG).show();
            emergencyContact1.setError("Email is required");
            emergencyContact1.requestFocus();
            return;
        }
        Map<String, Object> user = new HashMap<>();
        ArrayList<Map<String,String>> contacts = new ArrayList<>();
        if(!contact1.isEmpty()){
            Map<String, String> phoneNumber1 = new HashMap<>();
            phoneNumber1.put("countryCode", ccp1.getSelectedCountryCodeWithPlus());
            phoneNumber1.put("contact", contact1);
            contacts.add(phoneNumber1);
        }
        if(!contact2.isEmpty()){
            Map<String, String> phoneNumber2 = new HashMap<>();
            phoneNumber2.put("countryCode", ccp2.getSelectedCountryCodeWithPlus());
            phoneNumber2.put("contact", contact2);
            contacts.add(phoneNumber2);
        }
        if(!contact3.isEmpty()){
            Map<String, String> phoneNumber3 = new HashMap<>();
            phoneNumber3.put("countryCode", ccp3.getSelectedCountryCodeWithPlus());
            phoneNumber3.put("contact", contact3);
            contacts.add(phoneNumber3);
        }
        user.put("message",message);
        user.put("contacts",contacts);
        db.collection("users").document(mAuth.getCurrentUser().getUid()).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Intent intent = new Intent(getApplicationContext(),HomeScreenActivity.class);
                startActivity(intent);
                Toast.makeText(getApplicationContext(),"Emergency Contacts Saved Successfully",Toast.LENGTH_LONG).show();
                Log.d("Success", "DocumentSnapshot added with ID: ");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Failed to Save Emergency Contacts. Please try again",Toast.LENGTH_LONG).show();
                Log.w("Failure", "Error adding document", e);
            }
        });
    }
}