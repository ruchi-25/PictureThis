package com.aviary.android.picturethis;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;


public class VideoPlaying extends Activity{
    private VideoView vidView;
    MediaController vidControl;
    DisplayMetrics dm;
    Button btnPlayVid;
    Uri vidUri;
    String vidAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_playing);
        vidView = (VideoView)findViewById(R.id.myVideo);
        vidControl = new MediaController(VideoPlaying.this);
        btnPlayVid = (Button)findViewById(R.id.btnPlayVid);
        dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;
        int width = dm.widthPixels;
        vidView.setMinimumWidth(width);
        vidView.setMinimumHeight(height);

        Intent intent = getIntent();

        vidAddress = intent.getStringExtra("uri");
        vidUri = Uri.parse(vidAddress);
        if (vidUri.toString() == "") {
            Toast.makeText(VideoPlaying.this, "Video Not found",
                    Toast.LENGTH_LONG).show();
        }

        vidView.requestFocus();
        vidView.setVideoURI(vidUri);
        vidControl.setAnchorView(vidView);
        vidView.setMediaController(vidControl);

    }

    public void playVideo(View v){
        vidView.start();
    }

    public void onClickShare(View view){
        ContentValues content = new ContentValues(4);
        content.put(MediaStore.Video.VideoColumns.DATE_ADDED,
                System.currentTimeMillis() / 1000);
        content.put(MediaStore.Video.Media.MIME_TYPE, "video/*");
        content.put(MediaStore.Video.Media.DATA, vidAddress);
        ContentResolver resolver = getBaseContext().getContentResolver();
        Uri uri = resolver.insert(MediaStore.Video.Media.INTERNAL_CONTENT_URI, content);

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("video/*");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Picture This Video!");

        sharingIntent.putExtra(android.content.Intent.EXTRA_STREAM,uri);

        startActivity(Intent.createChooser(sharingIntent,"Share Video Using"));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
    }
}
