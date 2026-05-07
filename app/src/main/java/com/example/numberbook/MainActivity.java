package com.example.numberbook;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private Button btnLoadContacts;
    private Button btnSyncContacts;
    private Button btnSearch;
    private EditText etKeyword;
    private TextView tvStatus;
    private RecyclerView recyclerViewContacts;
    private ContactAdapter adapter;
    private final List<Contact> contactList = new ArrayList<>();
    private ContactApi contactApi;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    loadContacts();
                } else {
                    Toast.makeText(this, "Permission refusee", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLoadContacts = findViewById(R.id.btnLoadContacts);
        btnSyncContacts = findViewById(R.id.btnSyncContacts);
        btnSearch = findViewById(R.id.btnSearch);
        etKeyword = findViewById(R.id.etKeyword);
        tvStatus = findViewById(R.id.tvStatus);
        recyclerViewContacts = findViewById(R.id.recyclerViewContacts);

        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactAdapter(contactList);
        recyclerViewContacts.setAdapter(adapter);

        contactApi = RetrofitClient.getClient().create(ContactApi.class);

        btnLoadContacts.setOnClickListener(v -> checkPermissionAndLoadContacts());
        btnSyncContacts.setOnClickListener(v -> syncContactsToServer());
        btnSearch.setOnClickListener(v -> searchContacts());

        checkPermissionAndLoadContacts();
    }

    private void checkPermissionAndLoadContacts() {
        // Commentaire LEMGHILI Mohammed Amine: verification de la permission READ_CONTACTS avant la lecture.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            loadContacts();
        } else {
            tvStatus.setText("Autorisation contacts demandee");
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
        }
    }

    private void loadContacts() {
        // Commentaire LEMGHILI Mohammed Amine: lecture des contacts du mobile avec ContentResolver.
        contactList.clear();

        String[] projection = {
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        try (Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )) {
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndexOrThrow(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                );
                int phoneIndex = cursor.getColumnIndexOrThrow(
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                );

                while (cursor.moveToNext()) {
                    String name = cursor.getString(nameIndex);
                    String phone = cursor.getString(phoneIndex);
                    contactList.add(new Contact(name, phone));
                }
            }
        }

        adapter.updateData(new ArrayList<>(contactList));
        tvStatus.setText("Contacts charges depuis le telephone : " + contactList.size());
        Toast.makeText(this, "Contacts charges : " + contactList.size(), Toast.LENGTH_SHORT).show();
    }

    private void syncContactsToServer() {
        if (contactList.isEmpty()) {
            tvStatus.setText("Aucun contact local a synchroniser");
            Toast.makeText(this, "Aucun contact a synchroniser", Toast.LENGTH_SHORT).show();
            return;
        }

        // Commentaire LEMGHILI Mohammed Amine: chaque contact est envoye au backend PHP avec Retrofit.
        final int total = contactList.size();
        final int[] completed = {0};
        final int[] success = {0};

        for (Contact contact : contactList) {
            contactApi.insertContact(contact).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        success[0]++;
                    }
                    showSyncProgressIfFinished(total, completed, success);
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                    showSyncProgressIfFinished(total, completed, success);
                }
            });
        }

        Toast.makeText(this, "Synchronisation lancee", Toast.LENGTH_SHORT).show();
        tvStatus.setText("Synchronisation en cours vers le serveur");
    }

    private void showSyncProgressIfFinished(int total, int[] completed, int[] success) {
        completed[0]++;
        if (completed[0] == total) {
            tvStatus.setText("Synchronisation terminee : " + success[0] + "/" + total);
            Toast.makeText(
                    MainActivity.this,
                    "Synchronisation terminee : " + success[0] + "/" + total,
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void searchContacts() {
        String keyword = etKeyword.getText().toString().trim();

        if (keyword.isEmpty()) {
            tvStatus.setText("Recherche impossible : champ vide");
            Toast.makeText(this, "Saisir un nom ou un numero", Toast.LENGTH_SHORT).show();
            return;
        }

        // Commentaire LEMGHILI Mohammed Amine: recherche distante par nom ou par numero.
        tvStatus.setText("Recherche distante en cours");
        contactApi.searchContacts(keyword).enqueue(new Callback<List<Contact>>() {
            @Override
            public void onResponse(@NonNull Call<List<Contact>> call, @NonNull Response<List<Contact>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateData(response.body());
                    tvStatus.setText("Resultats trouves : " + response.body().size());
                } else {
                    tvStatus.setText("Aucun resultat trouve");
                    Toast.makeText(MainActivity.this, "Aucun resultat", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Contact>> call, @NonNull Throwable t) {
                tvStatus.setText("Erreur reseau pendant la recherche");
                Toast.makeText(MainActivity.this, "Erreur lors de la recherche", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
