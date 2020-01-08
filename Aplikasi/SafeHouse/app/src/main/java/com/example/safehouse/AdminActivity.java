package com.example.safehouse;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.renderscript.RenderScript;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.nio.channels.Channel;
import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity{

    TextView jumlah, bedRoom, livingRoom, nameCurrent, statusCurrent;
    DatabaseReference dref;
    String email;
    Button btRegister, btNotify ,btUserList, btSignOut;

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList;

    private static final  String CHANNEL_ID = "safe_house";
    private static final String CHANNEL_NAME = "Safe House";
    private static final String CHANNEL_DESC = "Safe House Notification";

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        jumlah = (TextView) findViewById(R.id.jumlah);
        jumlah.setVisibility(View.GONE);

        btRegister = (Button) findViewById(R.id.admin_register);
        btRegister.setVisibility(View.VISIBLE);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        adapter = new UserAdapter(this, userList);
        recyclerView.setAdapter(adapter);

        btNotify = (Button) findViewById(R.id.button_notify);
        btNotify.setVisibility(View.GONE);
        btUserList = (Button) findViewById(R.id.user_list);
        btUserList.setVisibility(View.VISIBLE);
        btSignOut = (Button) findViewById(R.id.bt_logout);

        bedRoom = (TextView) findViewById(R.id.bed_room);
        bedRoom.setText("Safe"); bedRoom.setTextColor(Color.rgb(0,133,119));
        livingRoom = (TextView) findViewById(R.id.living_room);
        livingRoom.setText("Safe"); livingRoom.setTextColor(Color.rgb(0,133,119));

        nameCurrent = (TextView) findViewById(R.id.text_view_name_current);
        nameCurrent.setText("Safe House");
        statusCurrent = (TextView) findViewById(R.id.text_view_status);
        statusCurrent.setText("at Home Now"); statusCurrent.setTextColor(Color.rgb(0,133,119));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            email = user.getEmail();
        }

        dref = FirebaseDatabase.getInstance().getReference();
        dref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (email.equals("cris@gmail.com")){
                    btRegister.setVisibility(View.VISIBLE);
                    btUserList.setVisibility(View.VISIBLE);
                }else{
                    btRegister.setVisibility(View.GONE);
                    btUserList.setVisibility(View.GONE);
                }

                Query query = FirebaseDatabase.getInstance().getReference("Users")
                        .orderByChild("status")
                        .equalTo(1);
                query.addListenerForSingleValueEvent(valueEventListener);

                if (dataSnapshot.child("bedRoom").getValue().toString().equals("1")){
                    bedRoom.setText("Not Safe");
                    bedRoom.setTextColor(Color.RED);
                    displayNotification();
                    btNotify.setVisibility(View.VISIBLE);
                }

                if (dataSnapshot.child("livingRoom").getValue().toString().equals("1")){
                    livingRoom.setText("Not Safe");
                    livingRoom.setTextColor(Color.RED);
                    displayNotification();
                    btNotify.setVisibility(View.VISIBLE);
                }

                Query query1 = FirebaseDatabase.getInstance().getReference("Users");
                query1.addListenerForSingleValueEvent(valueEventListenerr);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminActivity.this, RegisterActivity.class));
            }
        });

        btNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference("bedRoom")
                        .setValue(0);
                bedRoom.setText("Safe");bedRoom.setTextColor(Color.rgb(0,133,119));
                FirebaseDatabase.getInstance().getReference("livingRoom")
                        .setValue(0);
                livingRoom.setText("Safe");livingRoom.setTextColor(Color.rgb(0,133,119));
                btNotify.setVisibility(View.GONE);
            }
        });

        btUserList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminActivity.this, UserListActivity.class));
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        btSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminActivity.this, LoginActivity.class));
                finish();
            }
        });

    }

    private void displayNotification(){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.alert)
                    .setContentTitle("Notification")
                    .setContentText("Someone is not known to enter the house!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat mNotificationMgr = NotificationManagerCompat.from(this);
        mNotificationMgr.notify(1, mBuilder.build());
    }

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            userList.clear();
            if (dataSnapshot.exists()) {
                FirebaseDatabase.getInstance().getReference("rumahKosong")
                        .setValue(0);
                recyclerView.setVisibility(View.VISIBLE);
                jumlah.setVisibility(View.GONE);
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    userList.add(user);
                }
                adapter.notifyDataSetChanged();
            }else{
                recyclerView.setVisibility(View.GONE);
                jumlah.setVisibility(View.VISIBLE);
                jumlah.setText("No one at home");
                FirebaseDatabase.getInstance().getReference("rumahKosong")
                        .setValue(1);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    ValueEventListener valueEventListenerr = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user.email.equals(email)){
                        nameCurrent.setText(user.name);
                    }
                    if (user.email.equals(email) && user.status == 1){
                        statusCurrent.setText("at Home Now"); statusCurrent.setTextColor(Color.rgb(0,133,119));
                    }else if(user.email.equals(email) && user.status == 0){
                        statusCurrent.setText("not at Home Now");statusCurrent.setTextColor(Color.RED);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
}

