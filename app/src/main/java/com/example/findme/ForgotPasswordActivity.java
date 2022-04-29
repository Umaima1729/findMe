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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText emailField;
    private Button resetPasswordButton;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        mAuth = FirebaseAuth.getInstance();
        emailField = (EditText) findViewById(R.id.email);
        resetPasswordButton = (Button) findViewById(R.id.resetpasswordbutton);
        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword();
            }
        });
    }

    private void resetPassword() {
        String email = emailField.getText().toString().trim();
        if(email.isEmpty()){
            emailField.setError("Email is required");
            emailField.requestFocus();
            return;
        }
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                    startActivity(intent);
                    Toast.makeText(ForgotPasswordActivity.this,"Password reset link has been successfully sent to your email",Toast.LENGTH_LONG).show();
                }else{
                    Log.d("PasswordResetError",task.getException().toString());
                    Toast.makeText(ForgotPasswordActivity.this,task.getException().getMessage().toString()+". Please try again",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}