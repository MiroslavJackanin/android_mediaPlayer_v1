package sk.it.android.myapplication_player1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MyAdapter.OnItemListener {

    private static final int PERMISSION_CODE = 1;
    private static final String PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;

    RecyclerView recyclerView;
    ArrayList<Song> arrayList;
    MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkMyPermission()) {
            renderList();
        } else {
            requestMyPermissions();
        }
    }

    private void requestMyPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{PERMISSION}, PERMISSION_CODE);
    }
    private boolean checkMyPermission() {
        return ActivityCompat.checkSelfPermission(this, PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                renderList();
            } else {
                Snackbar.make(findViewById(android.R.id.content), "Permission denied", Snackbar.LENGTH_INDEFINITE).setAction("Go to settings", v -> {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                    intent.setData(uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }).show();
            }
        }
    }

    public void renderList() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        getMusic();
        adapter = new MyAdapter(arrayList, this);
        recyclerView.setAdapter(adapter);
    }

    public void getMusic() {
        arrayList = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri, null, null, null, null);

        if (songCursor != null && songCursor.moveToFirst()) {
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            do {
                String currentTitle = songCursor.getString(songTitle);
                String currentData = songCursor.getString(songData);
                long id = songCursor.getLong(songCursor.getColumnIndex(MediaStore.Audio.Media._ID));

                Song song = new Song(currentTitle, id, currentData);
                arrayList.add(song);
            } while (songCursor.moveToNext());
            songCursor.close();
        }
    }

    @Override
    public void onItemClick(int position) {
        Song song = arrayList.get(position);
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("song", song);
        startActivity(intent);
    }
}