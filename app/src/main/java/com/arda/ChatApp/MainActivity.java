package com.arda.ChatApp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.arda.ChatApp.Notifications.Token;
import com.arda.ChatApp.Fragments.ChatFragment;
import com.arda.ChatApp.Fragments.ContactFragment;
import com.arda.ChatApp.Fragments.GroupFragment;
import com.arda.aydin.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
{

    private Toolbar mToolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private TextView textView;

    private BottomNavigationView bottomNav;
    private NavigationView navigationView;

    private FragmentManager fragmentManager;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference rootRef;

    private String currentUserID;

    CircleImageView NavProfileImg;
    TextView NavUserName;

    CircleImageView retrieveImage;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        textView = findViewById(R.id.empty_view);

        if(mAuth.getCurrentUser()!=null)
        {
            currentUser = mAuth.getCurrentUser();
            currentUserID = mAuth.getCurrentUser().getUid();
        }
        else
        {
            Intent loginIntent=new Intent(MainActivity.this,LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(loginIntent);
            finish();
        }


        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        retrieveImage = findViewById(R.id.toolbar_profile);
        drawerLayout = findViewById(R.id.drawer);

        //Toolbar_Profile();

        navigationView = findViewById(R.id.navigation_view);

        NavProfileImg = navigationView.getHeaderView(0).findViewById(R.id.navigation_image);
        NavUserName = navigationView.getHeaderView(0).findViewById(R.id.navigation_userName);
        //NavigationHeader();


        bottomNav = findViewById(R.id.bottom_navigation_bar);

        navigationView.setNavigationItemSelectedListener(item ->
        {
            switch (item.getItemId())
            {

                case R.id.main_profile_option:
                    SendUserToSettingsActivity();
                    break;
                case R.id.main_sent_requests_option:
                    SendUserToSentRequestActivity();
                    break;
                case R.id.Chat_Requests_option:
                    SendUserToChatRequestActivity();
                    break;
                case R.id.invite_friends_option:
                    Toast.makeText(MainActivity.this, "Test", Toast.LENGTH_SHORT).show();
            }


            drawerLayout.closeDrawer((GravityCompat.START));
            return true;
        });


        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                ,new ChatFragment()).commit();


        bottomNav.setOnNavigationItemSelectedListener(menuItem ->
        {

            Fragment fragment = null;
            switch (menuItem.getItemId())
            {
                case R.id.chats:
                    fragment = new ChatFragment();
                    break;
                case R.id.groups:
                    fragment = new GroupFragment();
                    break;
                case R.id.contacts:
                    fragment = new ContactFragment();
                    break;
            }

            if (fragment != null) {
                fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }

            return true;
        });

        updateToken(FirebaseInstanceId.getInstance().getToken());

    }

    private void updateToken(String token)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);

        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser()!=null)
        {
            currentUser = mAuth.getCurrentUser();
            currentUserID = mAuth.getCurrentUser().getUid();
            reference.child(currentUserID).setValue(token1);
        }

    }


    @Override
    protected void onStart()
    {
        super.onStart();

        checkPermission();

        //currentUser = mAuth.getCurrentUser();

        if(currentUser==null)
        {
            SendUserToLoginActivity();
        }
        else
        {
            SharedPreferences();
            UpdateUserStatus("Online");
            VerifyUserExistence();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (currentUser != null) {
            UpdateUserStatus("Online");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (currentUser != null) {
            UpdateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        //currentUser = mAuth.getCurrentUser();

        if(currentUser !=null)
        {
            UpdateUserStatus("offline");
        }

    }

    //check whether user's data is present on database
    private void VerifyUserExistence()
    {

        String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        rootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if((dataSnapshot.child("name").exists()))
                {

                    Toolbar_Profile();
                    NavigationHeader();

                }
                else
                {
                    SendUserToLoginSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    @Override
    public void onBackPressed()
    {
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if(item.getItemId()==R.id.logout)
        {
            UpdateUserStatus("offline");
            mAuth.signOut();
            SendUserToLoginActivity();
        }
        if(item.getItemId()==R.id.main_setting_option)
        {
            SendUserToSettingsActivity();
        }
        if(item.getItemId()==R.id.main_create_group_option)
        {
            Toast.makeText(this, "group", Toast.LENGTH_SHORT).show();
        }
        if(item.getItemId()==R.id.main_find_friends_option)
        {
            SendUserToFindFriendActivity();
        }
        return true;
    }

    private void NavigationHeader()
    {
        DatabaseReference database = rootRef.child("Users").child(currentUserID);
        database.keepSynced(true);
        database.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists() && (dataSnapshot.hasChild("name")))
                {
                    if(dataSnapshot.hasChild("image"))
                    {
                        final String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                        Picasso.get().load(retrieveProfileImage)
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.profile)
                                .into(NavProfileImg, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                    }
                                    @Override
                                    public void onError(Exception e)
                                    {
                                        Picasso.get().load(retrieveProfileImage)
                                                .placeholder(R.drawable.profile)
                                                .into(NavProfileImg);
                                    }
                                });
                    }

                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();

                    NavUserName.setText(retrieveUserName);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void Toolbar_Profile()
    {
        DatabaseReference database = rootRef.child("Users").child(currentUserID);
        database.keepSynced(true);
        database.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("image"))
                    {
                        final String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                        Picasso.get().load(retrieveProfileImage)
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.profile)
                                .into(retrieveImage, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                    }
                                    @Override
                                    public void onError(Exception e)
                                    {
                                        Picasso.get().load(retrieveProfileImage)
                                                .placeholder(R.drawable.profile)
                                                .into(retrieveImage);
                                    }
                                });
                    }

                    retrieveImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            drawerLayout.openDrawer(GravityCompat.START);
                            // getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    static void UpdateUserStatus(String state)
    {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMM-yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> OnlineStateMap= new HashMap<>();
        OnlineStateMap.put("time", saveCurrentTime);
        OnlineStateMap.put("date", saveCurrentDate);
        OnlineStateMap.put("state", state);

       DatabaseReference rootRef =  FirebaseDatabase.getInstance().getReference();

       if(mAuth.getCurrentUser()!=null)
        {
            String currentUserID = mAuth.getCurrentUser().getUid();
            rootRef.child("Users").child(currentUserID).child("userState")
                    .updateChildren(OnlineStateMap);
        }

    }


    private void SendUserToLoginActivity()
    {
        Intent loginIntent=new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void SendUserToLoginSettingsActivity()
    {
        Intent loginProfileIntent=new Intent(MainActivity.this,LoginProfileActivity.class);
        loginProfileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(loginProfileIntent);
        finish();
    }

    private void SendUserToSentRequestActivity()
    {
        Intent sentReqIntent=new Intent(MainActivity.this,SentRequestActivity.class);
        startActivity(sentReqIntent);
    }

    private void SendUserToChatRequestActivity()
    {
        Intent chatReqIntent=new Intent(MainActivity.this,ReceivedRequestActivity.class);
        startActivity(chatReqIntent);
    }

    private void SendUserToSettingsActivity()
    {
        Intent settingsIntent=new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void SendUserToFindFriendActivity()
    {
        Intent FindFriendIntent=new Intent(MainActivity.this,FindFriendActivity.class);
        startActivity(FindFriendIntent);
    }

    public void checkPermission()
    {

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted()
            {

            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {

            }
        };

        TedPermission.with(MainActivity.this)
                .setPermissionListener(permissionListener)
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }

    private void SharedPreferences()
    {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null)
        {
            String currentUserID = mAuth.getCurrentUser().getUid();
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID",currentUserID);
            editor.apply();
        }
    }

}
