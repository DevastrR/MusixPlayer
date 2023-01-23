package com.example.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gauravk.audiovisualizer.visualizer.BlobVisualizer;
import com.gauravk.audiovisualizer.visualizer.CircleLineVisualizer;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

public class Player extends AppCompatActivity {

    ImageButton b1,b2,b3,b4,b5;
    TextView t1, t2, t3;
    SeekBar seekBar;
    CircleLineVisualizer blobVisualizer;

    String sname;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;
    Thread updateSeekBar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home) {

            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if(blobVisualizer !=null ) {

            blobVisualizer.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getSupportActionBar().setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        b1 = findViewById(R.id.playbtn);
        b2 = findViewById(R.id.prev);
        b3 = findViewById(R.id.skip);
        b4 = findViewById(R.id.frwd);
        b5 = findViewById(R.id.rew);
        t1 = findViewById(R.id.txtsn);
        t2 = findViewById(R.id.txtstart);
        t3 = findViewById(R.id.txtstop);
        seekBar = findViewById(R.id.seekBar);
        blobVisualizer = findViewById(R.id.blob);

        if(mediaPlayer != null){

            mediaPlayer.stop();
            mediaPlayer.release();
        }
        Intent i = getIntent();
        Bundle bundle = i.getExtras();
        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        String songName = i.getStringExtra("Song Name");
        position = bundle.getInt("pos", 0);
        t1.setSelected(true);
        Uri uri = Uri.parse(mySongs.get(position).toString());
        sname = mySongs.get(position).getName();
        t1.setText(sname);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();

        updateSeekBar = new Thread() {

            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition = 0;

                while(currentPosition < totalDuration) {

                    try {
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                    }
                    catch (InterruptedException | IllegalStateException e){
                        e.printStackTrace();
                    }
                }
             }
        };
        seekBar.setMax(mediaPlayer.getDuration());
        updateSeekBar.start();
        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.design_default_color_primary),PorterDuff.Mode.MULTIPLY);
        seekBar.getThumb().setColorFilter(getResources().getColor(R.color.design_default_color_primary), PorterDuff.Mode.SRC_IN);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String endTime = createTime(mediaPlayer.getDuration());
        t3.setText(endTime);

        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                t2.setText(currentTime);
                handler.postDelayed(this, delay);
            }
        },delay);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()) {

                    b1.setBackgroundResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                }
                else {

                    b1.setBackgroundResource(R.drawable.ic_pause);
                    mediaPlayer.start();
                }

            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                b1.performClick();
            }
        });

        int audioSessionId = mediaPlayer.getAudioSessionId();
        if(audioSessionId != -1) {
            blobVisualizer.setAudioSessionId(audioSessionId);
        }

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position-1)<0)?(mySongs.size()-1):(position-1);
                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                sname = mySongs.get(position).getName();
                t1.setText(sname);
                mediaPlayer.start();
                b1.setBackgroundResource(R.drawable.ic_pause);
                int audioSessionId = mediaPlayer.getAudioSessionId();
                if(audioSessionId != -1) {
                    blobVisualizer.setAudioSessionId(audioSessionId);
                }
            }
        });
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position+1)%mySongs.size());
                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                sname = mySongs.get(position).getName();
                t1.setText(sname);
                mediaPlayer.start();
                b1.setBackgroundResource(R.drawable.ic_pause);
                int audioSessionId = mediaPlayer.getAudioSessionId();
                if(audioSessionId != -1) {
                    blobVisualizer.setAudioSessionId(audioSessionId);
                }
            }
        });

        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()) {

                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }
            }
        });

        b5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()) {

                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
                }
            }
        });
    }

    public String createTime(int duration) {

        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;

        time += min ;

        if (sec<10) {
            time +="0";
        }

         time += sec;

        return time;
    }
}