package com.example.proiect_pdm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import java.io.FileOutputStream;
import java.io.IOException;

public class CreateAccountActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button createAccountButton;
    private DrawerLayout drawerLayout;

    private static final String FILE_NAME = "account_information.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        createAccountButton = findViewById(R.id.create_account_button);
        drawerLayout = findViewById(R.id.drawer_layout);

        // Set up navigation drawer
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (saveAccountInformation(username, password)) {
                    Toast.makeText(CreateAccountActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CreateAccountActivity.this, "Failed to create account. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean saveAccountInformation(String username, String password) {
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(FILE_NAME, Context.MODE_APPEND);
            String accountInfo = username + ":" + password + "\n";
            fos.write(accountInfo.getBytes());
            Log.d("CreateAccountActivity", "Account information saved: " + accountInfo);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("CreateAccountActivity", "Error saving account information", e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            Intent intent = new Intent(CreateAccountActivity.this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_login) {
            Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_resources) {
            Intent intent = new Intent(CreateAccountActivity.this, ResourcesActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_help) {
            Intent intent = new Intent(CreateAccountActivity.this, HelpActivity.class);
            startActivity(intent);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
