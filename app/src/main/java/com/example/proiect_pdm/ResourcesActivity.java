package com.example.proiect_pdm;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ResourcesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ImageView pdfImageView;
    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;
    private ParcelFileDescriptor parcelFileDescriptor;
    private DrawerLayout drawerLayout;
    private Button prevButton, nextButton;
    private int currentPageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resources);

        pdfImageView = findViewById(R.id.pdf_image_view);
        drawerLayout = findViewById(R.id.drawer_layout);
        prevButton = findViewById(R.id.prev_button);
        nextButton = findViewById(R.id.next_button);

        // Set up navigation drawer
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            openPdfRenderer();
            showPage(currentPageIndex);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to open PDF", Toast.LENGTH_SHORT).show();
        }

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPage(currentPageIndex - 1);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPage(currentPageIndex + 1);
            }
        });

        updateButtonStates();
    }

    private void openPdfRenderer() throws IOException {
        File file = new File(getCacheDir(), "kmp.pdf");
        if (!file.exists()) {
            try (InputStream asset = getAssets().open("kmp.pdf");
                 FileOutputStream output = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = asset.read(buffer)) != -1) {
                    output.write(buffer, 0, length);
                }
            }
        }
        parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        if (parcelFileDescriptor != null) {
            pdfRenderer = new PdfRenderer(parcelFileDescriptor);
        }
    }

    private void showPage(int index) {
        if (pdfRenderer.getPageCount() <= index || index < 0) {
            return;
        }
        if (currentPage != null) {
            currentPage.close();
        }
        currentPage = pdfRenderer.openPage(index);
        Bitmap bitmap = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(), Bitmap.Config.ARGB_8888);
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        pdfImageView.setImageBitmap(bitmap);
        currentPageIndex = index;
        updateButtonStates();
    }

    private void updateButtonStates() {
        int pageCount = pdfRenderer.getPageCount();
        prevButton.setEnabled(currentPageIndex > 0);
        nextButton.setEnabled(currentPageIndex < pageCount - 1);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (currentPage != null) {
            currentPage.close();
        }
        if (pdfRenderer != null) {
            pdfRenderer.close();
        }
        try {
            if (parcelFileDescriptor != null) {
                parcelFileDescriptor.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            Intent intent = new Intent(ResourcesActivity.this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_login) {
            Intent intent = new Intent(ResourcesActivity.this, LoginActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_resources) {
            drawerLayout.closeDrawer(GravityCompat.START); // Close drawer without restarting the activity
        } else if (id == R.id.nav_help) {
            Intent intent = new Intent(ResourcesActivity.this, HelpActivity.class);
            startActivity(intent);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
