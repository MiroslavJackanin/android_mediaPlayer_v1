package sk.it.android.myapplication_player1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerActivity extends AppCompatActivity {

    Button button;
    MediaPlayer mediaPlayer;
    SeekBar volumeSeekBar;
    AudioManager audioManager;
    SeekBar timeSeekBar;

    TextView titleTextView;
    TextView albumTextView;
    TextView artistTextView;

    Song song;
    Uri contentUri;
    boolean btn = true;

    String duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        button = findViewById(R.id.play_btn);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        final int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        intentInit();

        mpInit(contentUri);

        volSeekBarInit(maxVolume, currVolume);

        timeSeekBarInit(duration);

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeSeekBar.setProgress(mediaPlayer.getCurrentPosition());
            }
        }, 0, 200);
    }

    public void playBtnClick(View view) {
        if (btn) {
            mpStart();
        } else {
            mediaPlayer.pause();
            button.setBackgroundResource(R.drawable.play);
            btn = true;
        }
    }

    public void mpStart() {
        mediaPlayer.start();
        button.setBackgroundResource(R.drawable.stop);
        btn = false;
    }

    private void mpInit(Uri contentUri) {
        mediaPlayer = MediaPlayer.create(this, contentUri);
        mpStart();
    }

    private void volSeekBarInit(int maxVolume, int currVolume) {
        volumeSeekBar = findViewById(R.id.volume_seek_bar);
        volumeSeekBar.setMax(maxVolume);
        volumeSeekBar.setProgress(currVolume);
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void timeSeekBarInit(String duration) {
        timeSeekBar = findViewById(R.id.seek_bar);
        timeSeekBar.setMax(Integer.parseInt(duration));
        timeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
                timeSeekBar.setProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mediaPlayer.pause();
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.start();
            }
        });
    }

    private void intentInit() {
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        Intent intent = getIntent();
        song = intent.getParcelableExtra("song");
        metadataRetriever.setDataSource(song.getData());

        String title = song.getTitle();
        String album = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        String artist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        duration = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        initViews(title, album, artist);

        long id = song.getId();
        contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
    }

    private void initViews(String title, String album, String artist) {
        titleTextView = findViewById(R.id.title);
        albumTextView = findViewById(R.id.album);
        artistTextView = findViewById(R.id.artist);

        titleTextView.setText(title);
        albumTextView.setText(album);
        artistTextView.setText(artist);
    }
}