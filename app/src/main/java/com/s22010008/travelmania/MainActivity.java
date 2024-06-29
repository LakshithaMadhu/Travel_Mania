package com.s22010008.travelmania;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText loginEmail,loginPassword;
    private TextView signupRedirected,resetRedirect;
    private Button logbtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        loginEmail = findViewById(R.id.editTextTextEmailAddress);
        loginPassword = findViewById(R.id.editTextTextPassword);
        logbtn = findViewById(R.id.button);
        signupRedirected = findViewById(R.id.textView7);
        resetRedirect = findViewById(R.id.textView10);

        logbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String email = loginEmail.getText().toString();
                String pass = loginPassword.getText().toString();


                if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    if(!pass.isEmpty()){
                        auth.signInWithEmailAndPassword(email,pass)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        Toast.makeText(MainActivity.this,"Login Successful",Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(MainActivity.this, Dashboard.class));
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this,"Login Failed",Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }else {
                        loginPassword.setError("password cannot be empty");
                    }
                }
                else if(email.isEmpty()){
                    loginEmail.setError("Email cannot be empty");
                }else {
                    loginEmail.setError("Please enter valid email");
                }

            }
        });

        signupRedirected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,Register.class));
            }
        });

        resetRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ResetPasswordActivity.class));
            }
        });


    }





}