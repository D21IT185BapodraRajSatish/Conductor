package com.example.proto_type_12;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class First_Activity extends AppCompatActivity {

    static TextInputLayout TIL_cardID;
    boolean validcard = false;
    Toolbar toolbar;
    double CurrentBalance;
    double tempBel;

    GetCardID cardID1;

    String Currunt_User_email;

    String Conductor_Name;

    FirebaseAuth auth;

    Spinner sp1, sp2, sp3;
    ArrayAdapter<String> adapter1, adapter2, adapter3;
    ArrayList<String> arrayList1, arrayList2, arrayList3;
    GetandSetRout getRout, getSource, getDestination;
    Button btn_pg2_send, btngetcardid;
    TextView PrizeShow, retry;

    DatabaseReference reference1, reference2, reference3, reference4, reference5;

    String RECIPENT_EMail, CARDID;

    double getsourcekm, getdestinationkm, distance = 0, pay;
    ProgressDialog progressDialog;

    Bundle bundle;

    @Override
    protected void onStart() {
        super.onStart();
        if (!isConnected()) {
            TIL_cardID.setEnabled(false);
            sp1.setEnabled(false);
            sp2.setEnabled(false);
            sp3.setEnabled(false);
            Toast.makeText(getApplicationContext(), "Please Connect To Internet", Toast.LENGTH_SHORT).show();
            AlertDialogBox();
        } else {
            TIL_cardID.setEnabled(true);
            sp1.setEnabled(true);
            sp2.setEnabled(true);
            sp3.setEnabled(true);
            ConductorInfo();
        }

    }

    private void ConductorInfo() {
        DatabaseReference CheckDB = FirebaseDatabase.getInstance().getReference("Conductor");

        Currunt_User_email = auth.getCurrentUser().getEmail();

        CheckDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.hasChildren()) {
                        if (Currunt_User_email.equals(ds.getValue(RegisterUser.class).getEmail())) {
                            Conductor_Name = ds.getValue(RegisterUser.class).getfName() + " " + ds.getValue(RegisterUser.class).getlName();
                            break;
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_activity);

        bundle = new Bundle();
        bundle = savedInstanceState;

        initialize();

        btngetcardid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnected()) {
                    progressDialog.setTitle("Scanning");
                    progressDialog.setMessage("Waiting for Card ID");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    //new doit().execute();
                    GETCARDID();
                } else
                    AlertDialogBox();
            }
        });

        btn_pg2_send.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                if (isConnected()) {
                    String cmp1 = sp2.getSelectedItem().toString();
                    String cmp2 = sp3.getSelectedItem().toString();
                    if (cmp1.equals(cmp2)) {
                        showError();
                    } else {
                        DatabaseReference CardIDdb = FirebaseDatabase.getInstance().getReference("Customer");
                        CardIDdb.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot cardidDB : dataSnapshot.getChildren()) {
                                    if (TIL_cardID.getEditText().getText().toString().equals(cardidDB.getValue(RegisterUser.class).getCardID())) {
                                        RECIPENT_EMail = cardidDB.getValue(RegisterUser.class).getEmail();
                                        CARDID = cardidDB.getValue(RegisterUser.class).getCardID();
                                        CurrentBalance = Double.valueOf(cardidDB.getValue(RegisterUser.class).getBalance());
                                        validcard = true;
                                        break;
                                    } else {
                                        validcard = false;
                                    }
                                }
                                if (!validcard) {
                                    Toast.makeText(First_Activity.this, "Invalid Card", Toast.LENGTH_SHORT).show();
                                } else {
                                    final androidx.appcompat.app.AlertDialog.Builder getEmail = new androidx.appcompat.app.AlertDialog.Builder(First_Activity.this);
                                    getEmail.setCancelable(false);
                                    getEmail.setTitle("Send E-Ticket");
                                    getEmail.setMessage("Send E-Ticket to " + RECIPENT_EMail);
                                    getEmail.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            DatabaseReference UpdataeBlance = FirebaseDatabase.getInstance().getReference("Customer").child(TIL_cardID.getEditText().getText().toString());
                                            RegisterUser bel = new RegisterUser();
                                            if (CurrentBalance <= 25) {
                                                Toast.makeText(First_Activity.this, "Not enough Balance", Toast.LENGTH_SHORT).show();
                                            } else {
                                                tempBel = CurrentBalance - pay;
                                                if (tempBel <= 25) {
                                                    bel.setBalance(String.valueOf(tempBel));
                                                    UpdataeBlance.child("balance").setValue(bel.getBalance());
                                                    sendMail(1);
                                                } else {
                                                    bel.setBalance(String.valueOf(tempBel));
                                                    UpdataeBlance.child("balance").setValue(bel.getBalance());
                                                    sendMail(0);
                                                }
                                            }

                                        }
                                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    });
                                    getEmail.show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                } else {
                    AlertDialogBox();
                }
            }
        });

        reference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                getRout = dataSnapshot.getValue(GetandSetRout.class);
                arrayList1.add(String.valueOf(getRout.getRout_Number()));
                sp1.setAdapter(adapter1);
                progressDialog.dismiss();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        sp1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position,
                                       long id) {

                adapter2.clear();
                adapter3.clear();

                String T2, T3;
                T2 = T3 = sp1.getItemAtPosition(position).toString();
                reference2 = FirebaseDatabase.getInstance().getReference("stops").child(T2);
                reference3 = FirebaseDatabase.getInstance().getReference("stops").child(T3);


                itemAddInSP2();
                itemAddInSP3();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sp2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position,
                                       long id) {
                String Spinner2 = sp2.getItemAtPosition(position).toString();
                Spinner2 = Spinner2.replace(".", "_");
                Spinner2 = Spinner2.replace("/", "--");
                reference4 = FirebaseDatabase.getInstance().getReference("stops").child(sp1.getSelectedItem().toString()).child(Spinner2);
                reference4.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        getsourcekm = dataSnapshot.getValue(GetandSetRout.class).getKilometre();
                        payment();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        sp3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position,
                                       long id) {
                String Spinner3 = sp3.getItemAtPosition(position).toString();
                Spinner3 = Spinner3.replace(".", "_");
                Spinner3 = Spinner3.replace("/", "--");
                reference5 = FirebaseDatabase.getInstance().getReference("stops").child(sp1.getSelectedItem().toString()).child(Spinner3);
                reference5.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        getdestinationkm = dataSnapshot.getValue(GetandSetRout.class).getKilometre();
                        payment();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void GETCARDID() {
        DatabaseReference getcardid = FirebaseDatabase.getInstance().getReference("CardID");
        getcardid.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    cardID1 = snapshot.getValue(GetCardID.class);
                    TIL_cardID.getEditText().setText(cardID1.getValue());
                } else {
                    Toast.makeText(First_Activity.this, "Please Scan Card Again", Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void payment() {
        String cmp1 = sp2.getSelectedItem().toString();
        String cmp2 = sp3.getSelectedItem().toString();
        if (cmp1.equals(cmp2)) {
            pay = 0.0;
        } else {
            distance = getsourcekm - getdestinationkm;
            double distance1 = (Math.abs(distance));
            if (distance1 <= 5) {
                pay = 3.0;
            } else {
                pay = 5.0;
            }
        }
        PrizeShow.setText("₹ " + pay);
    }

    private void sendMail(int RequestCode) {
        String mail = RECIPENT_EMail;
        String message = "Dear Passenger,\n" +
                "            Your payment of ₹ " + pay + "  is debited from the card number " + CARDID + " for the city bus Route " + sp1.getSelectedItem().toString() + " from " + sp2.getSelectedItem().toString() + " to " + sp3.getSelectedItem().toString() + " is accepted by conductor " + Conductor_Name + " and your ticket is booked. And your current balance is ₹ " + tempBel + "\n" +
                "Happy Journey,\n" +
                "Bus Services.";
        String subject = "Ticket issue";

        JavaMailAPI javaMailAPI = new JavaMailAPI(this, mail, subject, message);
        javaMailAPI.SetToastMessage("E-Ticket Send");
        javaMailAPI.execute();
        TIL_cardID.getEditText().setText("");
        if (RequestCode == 1) {
            sendAlertMail();
        }
    }

    private void sendAlertMail() {
        //Dear Passenger,\n" +
        //                "            Your payment of ₹ "+pay+" for the city bus Rout "+sp1.getSelectedItem()+" from "+sp2.getSelectedItem()+" to "+sp3.getSelectedItem()+" is accepted by conductor "+ConductorName+" and your ticket is booked.\n" +
        //                "Happy Journey,\n" +
        //                "BusServices

        String mail = RECIPENT_EMail;
        String message = "Dear Passenger,\n" +
                "\t\tYou have reached to minimum balance in the card number " + CARDID + ". Kindly recharge your card from the respected bus service office. Otherwise, you will not be able to use your card for payment is city bus services\n" +
                "Thank You,\n" +
                "Bus Services.";
        String subject = "Card Balance Alert";

        JavaMailAPI javaMailAPI = new JavaMailAPI(this, mail, subject, message);
        javaMailAPI.SetToastMessage("Alert Balance Mail Send");
        javaMailAPI.execute();
        TIL_cardID.getEditText().setText("");
    }

    private void showError() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(First_Activity.this);
        builder.setCancelable(false);
        builder.setTitle("ERROR");
        builder.setMessage("You can't Select Source and Destination Stop Same");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sp2.setFocusable(true);
            }
        });
        builder.create().show();
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

    private void itemAddInSP3() {
        reference3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    if (item.hasChildren()) {
                        getDestination = item.getValue(GetandSetRout.class);
                        String Temp = getDestination.getStp_Name();
                        Temp = Temp.replace("_", ".");
                        Temp = Temp.replaceAll("--", "/");
                        arrayList3.add(Temp);
                    }
                }
                sp3.setAdapter(adapter3);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void itemAddInSP2() {
        reference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    if (item.hasChildren()) {
                        getSource = item.getValue(GetandSetRout.class);
                        String Temp = getSource.getStp_Name();
                        Temp = Temp.replaceAll("_", ".");
                        Temp = Temp.replaceAll("--", "/");
                        arrayList2.add(Temp);
                        //Toast.makeText(Issue_Ticket.this, item.getValue(GetandSetRout.class).getStp_Name(), Toast.LENGTH_SHORT).show();
                    }
                }
                sp2.setAdapter(adapter2);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initialize() {

        auth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TIL_cardID = findViewById(R.id.CARD_ID);

        progressDialog = new ProgressDialog(First_Activity.this);
        sp1 = findViewById(R.id.SP1);
        sp2 = findViewById(R.id.SP2);
        sp3 = findViewById(R.id.SP3);

        PrizeShow = findViewById(R.id.Pay_ISSUE_TICKET1);

        arrayList1 = new ArrayList<>();
        adapter1 = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, arrayList1);

        arrayList2 = new ArrayList<>();
        adapter2 = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, arrayList2);

        arrayList3 = new ArrayList<>();
        adapter3 = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, arrayList3);

        /*auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();
        s=user.getEmail();*/

        btn_pg2_send = findViewById(R.id.SUBMIT_BTN);
        btngetcardid = findViewById(R.id.GET_ID_BTN);


        getRout = new GetandSetRout();
        getSource = new GetandSetRout();
        getDestination = new GetandSetRout();

        reference1 = FirebaseDatabase.getInstance().getReference("stops");

        progressDialog.setCancelable(false);
        progressDialog.setTitle("Fetching details");
        progressDialog.setMessage("Processing...");
        progressDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.log_out, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.log_out_menu:
                auth.signOut();
                startActivity(new Intent(this, Login.class));
                finish();
                Toast.makeText(this, "Log Out Successfully", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void AlertDialogBox() {
        Dialog dialog = new Dialog(First_Activity.this, R.style.NO_INTERNET_DIALOG);
        dialog.setContentView(R.layout.no_internet_dilog);
        dialog.setCancelable(false);
        dialog.show();
        retry = dialog.findViewById(R.id.BTN_RETRY);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                progressDialog.setCancelable(false);
                progressDialog.setTitle("Waiting For Connection..");
                progressDialog.show();
                startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!isConnected()) {
                            progressDialog.dismiss();
                            AlertDialogBox();
                        } else {
                            progressDialog.dismiss();
                        }
                    }
                }, 5000);
            }
        });
    }
}