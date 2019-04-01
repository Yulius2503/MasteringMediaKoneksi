    package com.project.masteringmediakoneksi;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

        private  List<Track> mListItems;
        private SCTrackAdapter mAdapter;
        private TextView mSelectedTrackTitile;
        private ImageView mSelectedTrackImage;
        private MediaPlayer mMediaPlayer;
        private ImageView mPlayerControl;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            if(mMediaPlayer.isPlaying()){
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mListItems =new ArrayList<Track>();
        ListView listView =findViewById(R.id.track_list_view);
        mAdapter =new SCTrackAdapter(this, mListItems);
        listView.setAdapter(mAdapter);

        mSelectedTrackTitile =(TextView)findViewById(R.id.selected_track_title);
        mSelectedTrackImage = (ImageView) findViewById(R.id.selected_track_image);
        mPlayerControl = (ImageView)findViewById(R.id.player_control);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                togglePlayPause();
            }
        });
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mPlayerControl.setImageResource(R.drawable.ic_play);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Track track = mListItems.get(position);
                mSelectedTrackTitile.setText(track.getmTitle());
                Picasso.with(MainActivity.this).load(track.getmArtworkURL()).into(mSelectedTrackImage);

                if (mMediaPlayer.isPlaying()){
                    mMediaPlayer.stop();
                    mMediaPlayer.reset();
                }
                try{
                    mMediaPlayer.setDataSource(track.getmStreamURL()+"?client id= "+ Config.CLIENT_ID);
                    mMediaPlayer.prepareAsync();
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }

        });
        mPlayerControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayPause();
            }
        });

        SCService scService = SoundCloud.getService();
        scService.getRecentTracks("last_week").enqueue(new Callback<List<Track>>() {
            @Override
            public void onResponse(Call<List<Track>> call, Response<List<Track>> response) {
                if (response.isSuccessful()){
                    List<Track> tracks = response.body();
                    loadTracks(tracks);
                }
                else {
                    showMessage("Error code"+ response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Track>> call, Throwable t) {
                showMessage("Network Error: " + t.getMessage());

            }
        });
    }
    private void togglePlayPause(){
        if (mMediaPlayer.isPlaying()){
            mMediaPlayer.pause();
            mPlayerControl.setImageResource(R.drawable.ic_play);
        }else {
            mMediaPlayer.start();
            mPlayerControl.setImageResource(R.drawable.ic_pause);
        }
    }
    private void showMessage (String message){
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }
    private void loadTracks (List<Track> tracks){
        mListItems.clear();
        mListItems.addAll(tracks);
        mAdapter.notifyDataSetChanged();
    }
}
