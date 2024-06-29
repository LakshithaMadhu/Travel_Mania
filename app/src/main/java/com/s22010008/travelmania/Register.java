/*package com.s22010008.travelmania;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
public class Register extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText signUpEmail,signUPPassword;
    private TextView logginRedirected,resetRedirect;
    private Button regbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        auth = FirebaseAuth.getInstance();
        signUpEmail = findViewById(R.id.editTextTextPassword1);
        signUPPassword = findViewById(R.id.editTextTextPassword2);
        regbtn = findViewById(R.id.button2);
        logginRedirected = findViewById(R.id.textView7);
        resetRedirect = findViewById(R.id.textView10);

        regbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String user = signUpEmail.getText().toString().trim();
                String pass = signUPPassword.getText().toString().trim();

                if(user.isEmpty()){
                    signUpEmail.setError("Email cannot be empty");
                }
                if(pass.isEmpty()){
                    signUPPassword.setError("Password cannot be empty");
                }else {
                    auth.createUserWithEmailAndPassword(user,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task){
                            if(task.isSuccessful()){
                                Toast.makeText(Register.this,"Signup Successfully",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(Register.this, MainActivity.class));
                            }else{
                                Toast.makeText(Register.this,"Signup Failed"+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        logginRedirected.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                startActivity(new Intent(Register.this,MainActivity.class));
            }
        });

        resetRedirect.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                startActivity(new Intent(Register.this, ResetPasswordActivity.class));
            }
        });





    }
}*/

// RegistrationActivity.java
package com.s22010008.travelmania;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class Register extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText registerEmail, registerPassword, registerName;
    private Button registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        registerEmail = findViewById(R.id.editTextTextPassword1);
        registerPassword = findViewById(R.id.editTextTextPassword2);
        registerName = findViewById(R.id.name);
        registerBtn = findViewById(R.id.button2);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = registerEmail.getText().toString().trim();
                String password = registerPassword.getText().toString().trim();
                String name = registerName.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    registerEmail.setError("Email is required");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    registerPassword.setError("Password is required");
                    return;
                }
                if (TextUtils.isEmpty(name)) {
                    registerName.setError("Name is required");
                    return;
                }

                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = auth.getCurrentUser();
                                    if (user != null) {
                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(name)
                                                .build();

                                        user.updateProfile(profileUpdates)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(Register.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                                            startActivity(new Intent(Register.this, MainActivity.class));
                                                            finish();
                                                            // Navigate to another activity or close this one
                                                        }
                                                    }
                                                });
                                    }
                                } else {
                                    Toast.makeText(Register.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}



