package com.example.songs;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {

    Button btnplay,btnnext,btnprev,btnff,btnfr;
    TextView txtsname,txtsstart,txtsstop;
    SeekBar seekMusic;
    BarVisualizer visualizer;
    ImageView imageview;

    String Sname;
    public static final String EXTRA_NAME="song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;
    Thread UpdateSeekbar;

    @Override
    protected void onDestroy() {

        if(visualizer==null){
            visualizer.release();
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getSupportActionBar().setTitle("Now Playing Music");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btnprev=findViewById(R.id.btnpreview);
        btnff=findViewById(R.id.btnff);
        btnplay=findViewById(R.id.playbutton);
        btnfr=findViewById(R.id.btnfr);
        btnnext=findViewById(R.id.btnnext);


        txtsname=findViewById(R.id.txtsn);
        txtsstart=findViewById(R.id.txtsstart);
        txtsstop=findViewById(R.id.txtsstop);

        seekMusic=findViewById(R.id.seekbar);
        visualizer=findViewById(R.id.bar);
        imageview=findViewById(R.id.imageview);

        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Intent i= getIntent();
        Bundle bundle=i.getExtras();

        mySongs=(ArrayList)bundle.getParcelableArrayList("songs");
        String songName= i.getStringExtra("songname");
        position=bundle.getInt("position",0);

        txtsname.setSelected(true);
        Uri uri= Uri.parse(mySongs.get(position).toString());
        Sname=mySongs.get(position).getName();

        txtsname.setText(Sname);

        mediaPlayer=mediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();

        UpdateSeekbar=new Thread(){
            @Override
            public void run(){
                int totalDuration= mediaPlayer.getDuration();
                int currentPosition=0;

                while(currentPosition<totalDuration){
                    try{
                        sleep(500);
                        currentPosition=mediaPlayer.getCurrentPosition();
                        seekMusic.setProgress(currentPosition);

                    }catch(InterruptedException | IllegalStateException e){
                        e.printStackTrace();
                    }
                }
            }
        };
        seekMusic.setMax(mediaPlayer.getDuration());
        UpdateSeekbar.start();
        seekMusic.getProgressDrawable().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.MULTIPLY);
        seekMusic.getThumb().setColorFilter(getResources().getColor(R.color.white),PorterDuff.Mode.SRC_IN);

        seekMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String Endtime =CreateTime(mediaPlayer.getDuration());
        txtsstop.setText(Endtime);

        final Handler handler= new Handler();
        final int delay=1000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currenttime=CreateTime(mediaPlayer.getCurrentPosition());
                txtsstart.setText(currenttime);
                handler.postDelayed(this, delay);

            }
        },delay);


        btnplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    btnplay.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
                    mediaPlayer.pause();
                }else{
                    btnplay.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                    mediaPlayer.start();
                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnnext.performClick();
            }
        });

        int audioSessionId=mediaPlayer.getAudioSessionId();
        if(audioSessionId!=-1){
            visualizer.setAudioSessionId(audioSessionId);
        }

        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position=((position+1)%mySongs.size());
                Uri uri=Uri.parse(mySongs.get(position).toString());
                mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
                Sname=mySongs.get(position).getName();
                txtsname.setText(Sname);
                mediaPlayer.start();
                btnplay.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                StartAnimation(imageview);

                int audioSessionId=mediaPlayer.getAudioSessionId();
                if(audioSessionId!=-1){
                    visualizer.setAudioSessionId(audioSessionId);
                }
            }
        });


        btnprev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position=((position-1)<0)?(mySongs.size()-1):(position-1);
                Uri uri = Uri.parse(mySongs.get(position).toString());
                mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
                Sname=mySongs.get(position).getName();
                txtsname.setText(Sname);
                mediaPlayer.start();
                btnplay.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                StartAnimation(imageview);

                int audioSessionId=mediaPlayer.getAudioSessionId();
                if(audioSessionId!=-1){
                    visualizer.setAudioSessionId(audioSessionId);
                }
            }
        });

        btnff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }

            }
        });

        btnfr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
                }

            }
        });




    }

    public void StartAnimation(View view ){
        ObjectAnimator animator=ObjectAnimator.ofFloat(imageview,"rotation", 0f,360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet=new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }

    public String CreateTime(int duration){
        String time="";
        int min=duration/1000/60;
        int sec=duration/1000%60;

        time+=min+":";

        if(sec<=10){
            time+="0";
        }
        time+=sec;
        return time;
    }
}