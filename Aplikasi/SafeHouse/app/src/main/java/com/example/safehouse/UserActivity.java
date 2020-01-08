package com.example.safehouse;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity {

    TextView jumlah, bedRoom, livingRoom;
    DatabaseReference dref;
    String status;
    Button  btNotify, btSignOut;

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList;

    DatabaseReference dbUsers;

    private static final  String CHANNEL_ID = "safe_house";
    private static final String CHANNEL_NAME = "Safe House";
    private static final String CHANNEL_DESC = "Safe House Notification";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        jumlah = (TextView) findViewById(R.id.jumlah);
        jumlah.setVisibility(View.GONE);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        adapter = new UserAdapter(this, userList);
        recyclerView.setAdapter(adapter);

        btNotify = (Button) findViewById(R.id.button_notify);
        btNotify.setVisibility(View.GONE);

        btSignOut = (Button) findViewById(R.id.bt_logout);

        bedRoom = (TextView) findViewById(R.id.bed_room);
        livingRoom = (TextView) findViewById(R.id.living_room);

        dref = FirebaseDatabase.getInstance().getReference();
        dref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Query query = FirebaseDatabase.getInstance().getReference("Users")
                        .orderByChild("status")
                        .equalTo(1);
                query.addListenerForSingleValueEvent(valueEventListener);

                if (dataSnapshot.child("bedRoom").getValue().toString().equals("1")){
                    bedRoom.setText("Bed Room is not Safe");
                    bedRoom.setTextColor(Color.RED);
                    displayNotification();
                    btNotify.setVisibility(View.VISIBLE);
                }
                if (dataSnapshot.child("livingRoom").getValue().toString().equals("1")){
                    livingRoom.setText("Living Room is not Safe");
                    livingRoom.setTextColor(Color.RED);
                    displayNotification();
                    btNotify.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference("bedRoom")
                        .setValue(0);
                FirebaseDatabase.getInstance().getReference("livingRoom")
                        .setValue(0);
                bedRoom.setText("Bed Room is Safe"); bedRoom.setTextColor(Color.BLACK);
                livingRoom.setText("Living Room is Safe"); livingRoom.setTextColor(Color.BLACK);
                btNotify.setVisibility(View.GONE);
                btNotify.setVisibility(View.GONE);
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
                startActivity(new Intent(UserActivity.this, LoginActivity.class));
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
}

