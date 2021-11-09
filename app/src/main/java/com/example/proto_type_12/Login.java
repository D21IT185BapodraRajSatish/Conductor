package com.example.proto_type_12;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Login extends AppCompatActivity {

    CardView C_view;

    TextInputLayout Email, Password;
    Button Log_in, BTN_FORGOT;
    FirebaseAuth auth;
    FirebaseUser user;

    ProgressDialog progressDialog;
    TextView retry;

    @Override
    protected void onStart() {
        super.onStart();
        if (isConnected()) {
            user = auth.getCurrentUser();
            if (user != null) {
                user.reload();
                if (auth.getCurrentUser().isEmailVerified()) {
                    startActivity(new Intent(Login.this, First_Activity.class));
                    finish();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                    builder.setTitle("Waring");
                    builder.setMessage("Please Verify Your Email First");
                    builder.setPositiveButton("Verify", (dialog, which) -> {
                        auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(Login.this, "Please Check Email :- \n" + auth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Login.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).setNegativeButton("Cancel", ((dialog, which) -> {
                        dialog.dismiss();
                    }));
                    builder.create();
                    builder.show();
                }
            }
        } else {
            AlertDialogBox();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Initialize();

        Log_in.setOnClickListener(v -> {
            if (isConnected()) {
                if (validate()) {
                    Log_IN();
                }
            } else {
                AlertDialogBox();
            }
        });

        BTN_FORGOT.setOnClickListener(V -> {
            auth.sendPasswordResetEmail(Email.getEditText().getText().toString())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(Login.this, "Check Your E-mail \n" +
                                    "Password Reset Link Send", Toast.LENGTH_LONG).show();
                            BTN_FORGOT.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(Login.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

    }

    private void Log_IN() {
        auth.signInWithEmailAndPassword(Email.getEditText().getText().toString(), Password.getEditText().getText().toString())
                .addOnSuccessListener(authResult -> {
                    if (auth.getCurrentUser().isEmailVerified()) {
                        startActivity(new Intent(Login.this, First_Activity.class));
                        finish();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                        builder.setTitle("Waring");
                        builder.setMessage("Please Verify Your Email First");
                        builder.setPositiveButton("Verify", (dialog, which) -> {
                            auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(Login.this, "Please Check Email :- \n" + auth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Login.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }).setNegativeButton("Cancel", ((dialog, which) -> dialog.dismiss()));
                        builder.create();
                        builder.show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    BTN_FORGOT.setVisibility(View.VISIBLE);
                });

    }

    private boolean validate() {
        boolean validAllFilds = true;
        if (Email.getEditText().getText().toString().isEmpty() & Password.getEditText().getText().toString().isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
            builder.setCancelable(false);
            builder.setTitle(Html.fromHtml("<font color='#ff0f0f'>Error</font>"));
            builder.setMessage("All Field are Required");
            builder.setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.cancel());
            builder.show();
            validAllFilds = false;
        } else {
            if (validAllFilds) {
                if (Email.getEditText().getText().toString().isEmpty()) {
                    Email.setErrorEnabled(true);
                    Email.setError("Please Enter This Field");
                    validAllFilds = false;
                } else {
                    Email.setErrorEnabled(false);
                    validAllFilds = true;
                }
            }
            if (validAllFilds) {
                if (Password.getEditText().getText().toString().isEmpty()) {
                    Password.setErrorEnabled(true);
                    Password.setError("Please Enter This Field");
                    validAllFilds = false;
                } else {
                    Pattern ps = Pattern.compile("^[a-zA-Z0-9]+$");
                    Matcher ms = ps.matcher(Password.getEditText().getText().toString());
                    if (!ms.matches()) {
                        Password.setErrorEnabled(true);
                        Password.setError("Please Enter Password In Digit & NUmber  Only");
                        validAllFilds = false;
                    } else {
                        if (Password.getEditText().getText().toString().length() < 8 | Password.getEditText().getText().toString().length() > 8) {
                            Password.setErrorEnabled(true);
                            Password.setError("Please Enter Password In 8 Digit Only");
                            validAllFilds = false;
                        } else {
                            Password.setErrorEnabled(false);
                            validAllFilds = true;
                        }
                    }
                }
            }
        }

        return validAllFilds;
    }

    private void Initialize() {

        C_view = findViewById(R.id.CARD_LAYOUT);

        Email = findViewById(R.id.Email);
        Password = findViewById(R.id.Password);

        Log_in = findViewById(R.id.Log_IN);
        BTN_FORGOT = findViewById(R.id.BTN_Forgot_Password);

        progressDialog = new ProgressDialog(Login.this);

        auth = FirebaseAuth.getInstance();
    }

    private boolean isConnected() {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isConnected();
            return connected;
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        }
        return connected;
    }

    private void AlertDialogBox() {
        Dialog dialog = new Dialog(Login.this, R.style.NO_INTERNET_DIALOG);
        dialog.setContentView(R.layout.no_internet_dilog);
        dialog.setCancelable(false);
        dialog.show();
        retry = dialog.findViewById(R.id.BTN_RETRY);
        retry.setOnClickListener(v -> {
            dialog.dismiss();
            progressDialog.setCancelable(false);
            progressDialog.setTitle("Waiting For Connection..");
            progressDialog.show();
            startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
            new Handler().postDelayed(() -> {
                if (!isConnected()) {
                    progressDialog.dismiss();
                    AlertDialogBox();
                } else {
                    progressDialog.dismiss();
                }
            }, 5000);
        });
    }

}