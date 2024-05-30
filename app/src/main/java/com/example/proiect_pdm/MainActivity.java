package com.example.proiect_pdm;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private EditText mainStringEditText;
    private EditText patternStringEditText;
    private Button searchButton;
    private TextView resultTextView;
    private CheckBox caseSensitiveCheckbox;
    private TableLayout lpsTableLayout;
    private DrawerLayout drawerLayout;
    private Button resourceButton;
    private Button helpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainStringEditText = findViewById(R.id.main_string);
        patternStringEditText = findViewById(R.id.pattern_string);
        searchButton = findViewById(R.id.search_button);
        resultTextView = findViewById(R.id.result_text);
        caseSensitiveCheckbox = findViewById(R.id.case_sensitive_checkbox);
        lpsTableLayout = findViewById(R.id.lps_table);
        drawerLayout = findViewById(R.id.drawer_layout);
        resourceButton = findViewById(R.id.resource_button);
        helpButton = findViewById(R.id.help_button);

        // Set up navigation drawer
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSearch();
            }
        });

        resourceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openResourcesPage();
            }
        });

        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHelpPage();
            }
        });
    }

    private void performSearch() {
        String mainString = mainStringEditText.getText().toString();
        String patternString = patternStringEditText.getText().toString();
        boolean isCaseSensitive = caseSensitiveCheckbox.isChecked();

        if (mainString.isEmpty() || patternString.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please enter both strings.", Toast.LENGTH_SHORT).show();
        } else {
            List<Integer> occurrences = isCaseSensitive ? kmpSearch(mainString, patternString) : kmpSearchIgnoreCase(mainString, patternString);
            int[] lps = computeLPSArray(patternString, isCaseSensitive);

            if (occurrences.isEmpty()) {
                resultTextView.setText("Pattern not found.");
            } else {
                StringBuilder resultText = new StringBuilder("Pattern found at positions: ");
                for (int pos : occurrences) {
                    resultText.append(pos).append(", ");
                }
                resultTextView.setText(resultText.toString());
            }

            displayColoredStrings(mainString, occurrences, patternString);
            displayLPSTable(patternString, lps);
        }
    }

    private void openResourcesPage() {
        Intent intent = new Intent(MainActivity.this, ResourcesActivity.class);
        startActivity(intent);
    }

    private void openHelpPage() {
        Intent intent = new Intent(MainActivity.this, HelpActivity.class);
        startActivity(intent);
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
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_login) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_resources) {
            Intent intent = new Intent(MainActivity.this, ResourcesActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_help) {
            Intent intent = new Intent(MainActivity.this, HelpActivity.class);
            startActivity(intent);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private List<Integer> kmpSearch(String text, String pattern) {
        List<Integer> occurrences = new ArrayList<>();
        boolean isCaseSensitive = caseSensitiveCheckbox.isChecked();

        int[] lps = computeLPSArray(pattern, isCaseSensitive);
        int textLength = text.length();
        int patternLength = pattern.length();
        int i = 0; // Index for text[]
        int j = 0; // Index for pattern[]

        while (i < textLength) {
            if (pattern.charAt(j) == text.charAt(i)) {
                i++;
                j++;
            }

            if (j == patternLength) {
                occurrences.add(i - j);
                j = lps[j - 1];
            } else if (i < textLength && pattern.charAt(j) != text.charAt(i)) {
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }

        return occurrences;
    }

    private List<Integer> kmpSearchIgnoreCase(String text, String pattern) {
        List<Integer> occurrences = new ArrayList<>();
        boolean isCaseSensitive = caseSensitiveCheckbox.isChecked();

        int[] lps = computeLPSArray(pattern, isCaseSensitive);
        int textLength = text.length();
        int patternLength = pattern.length();
        int i = 0; // Index for text[]
        int j = 0; // Index for pattern[]

        while (i < textLength) {
            if (Character.toLowerCase(pattern.charAt(j)) == Character.toLowerCase(text.charAt(i))) {
                i++;
                j++;
            }

            if (j == patternLength) {
                occurrences.add(i - j);
                j = lps[j - 1];
            } else if (i < textLength && Character.toLowerCase(pattern.charAt(j)) != Character.toLowerCase(text.charAt(i))) {
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }

        return occurrences;
    }

    private int[] computeLPSArray(String pattern, boolean isCaseSensitive) {
        int patternLength = pattern.length();
        int[] lps = new int[patternLength];
        int len = 0; // Length of the previous longest prefix suffix
        int i = 1;

        lps[0] = 0; // lps[0] is always 0

        while (i < patternLength) {
            char currentChar = pattern.charAt(i);
            char previousChar = pattern.charAt(len);

            // Compare characters based on case sensitivity
            boolean charsEqual = isCaseSensitive ? currentChar == previousChar :
                    Character.toLowerCase(currentChar) == Character.toLowerCase(previousChar);

            if (charsEqual) {
                len++;
                lps[i] = len;
                i++;
            } else {
                if (len != 0) {
                    len = lps[len - 1];
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }

        return lps;
    }

    private void displayLPSArray(int[] lps) {
        lpsTableLayout.removeAllViews();
        TableRow headerRow = new TableRow(this);
        headerRow.setGravity(Gravity.CENTER_HORIZONTAL);

        // Add headers for the table
        TextView header1 = new TextView(this);
        header1.setText("Index");
        header1.setPadding(16, 16, 16, 16);
        header1.setBackgroundColor(Color.LTGRAY);
        header1.setTextColor(Color.BLACK);

        TextView header2 = new TextView(this);
        header2.setText("LPS Value");
        header2.setPadding(16, 16, 16, 16);
        header2.setBackgroundColor(Color.LTGRAY);
        header2.setTextColor(Color.BLACK);

        headerRow.addView(header1);
        headerRow.addView(header2);
        lpsTableLayout.addView(headerRow);

        for (int i = 0; i < lps.length; i++) {
            TableRow row = new TableRow(this);
            row.setGravity(Gravity.CENTER_HORIZONTAL);

            TextView indexText = new TextView(this);
            indexText.setText(String.valueOf(i));
            indexText.setPadding(16, 16, 16, 16);
            indexText.setTextColor(Color.BLACK);

            TextView valueText = new TextView(this);
            valueText.setText(String.valueOf(lps[i]));
            valueText.setPadding(16, 16, 16, 16);
            valueText.setTextColor(Color.BLACK);

            row.addView(indexText);
            row.addView(valueText);
            lpsTableLayout.addView(row);
        }
    }

    private void displayColoredStrings(String mainString, List<Integer> occurrences, String patternString) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        int occurrenceCount = 1;

        for (int pos : occurrences) {
            if (pos >= mainString.length()) {
                break; // If pos is beyond the length of mainString, exit the loop
            }

            // Append occurrence number
            builder.append(occurrenceCount + ". ");
            occurrenceCount++;

            // Append text before the current occurrence
            builder.append(getColoredString(mainString.substring(0, pos), Color.RED));
            // Append matched text with entire pattern colored green
            builder.append(getColoredString(mainString.substring(pos, pos + patternString.length()), Color.GREEN));
            // Append the remaining text after the matched substring
            builder.append(getColoredString(mainString.substring(pos + patternString.length()), Color.RED));
            builder.append("\n"); // Add a new line for each occurrence
        }

        resultTextView.setText(builder);
    }

    private void displayLPSTable(String pattern, int[] lps) {
        lpsTableLayout.removeAllViews(); // Clear existing table rows

        // Create header row
        TableRow headerRow = new TableRow(this);
        TextView indexHeader = createTableHeaderTextView("Index");
        TextView valueHeader = createTableHeaderTextView("Value");
        headerRow.addView(indexHeader);
        headerRow.addView(valueHeader);
        lpsTableLayout.addView(headerRow);

        // Create rows for each index-value pair in the LPS array
        for (int i = 0; i < lps.length; i++) {
            TableRow row = new TableRow(this);
            TextView indexTextView = createTableDataTextView(String.valueOf(i));
            TextView valueTextView = createTableDataTextView(String.valueOf(lps[i]));
            row.addView(indexTextView);
            row.addView(valueTextView);
            lpsTableLayout.addView(row);
        }
    }

    private TextView createTableHeaderTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(Color.BLACK);
        textView.setPadding(16, 8, 16, 8);
        return textView;
    }

    private TextView createTableDataTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(Color.BLACK);
        textView.setPadding(16, 8, 16, 8);
        return textView;
    }

    private SpannableString getColoredString(String str, int color) {
        SpannableString spannable = new SpannableString(str);
        spannable.setSpan(new ForegroundColorSpan(color), 0, str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }
}
