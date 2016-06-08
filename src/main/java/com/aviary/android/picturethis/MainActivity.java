package com.aviary.android.picturethis;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.googlecode.javacv.FFmpegFrameRecorder;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_highgui;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends Activity {

    GridView gridGallery;
    Handler handler;
    GalleryAdapter adapter;
    protected static int MAX_IMAGE_SELECTION_LENGTH;

    ImageView imgSinglePick;
    ImageButton btnGalleryPickMul;
    ImageButton btnVideoCreate;
    ImageButton btnHelp;

    ViewSwitcher viewSwitcher;
    ImageLoader imageLoader;
    ProgressDialog progressDialog;
    GestureDetector gestureDetector;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_multi);

        MAX_IMAGE_SELECTION_LENGTH = getResources().getInteger(R.integer.photo_limit);
        gestureDetector = new GestureDetector(getApplicationContext(), new GestureListener());
        initImageLoader();
        init();
    }

    private void initImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc().imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .bitmapConfig(Bitmap.Config.RGB_565).build();
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
                this).defaultDisplayImageOptions(defaultOptions).memoryCache(
                new WeakMemoryCache());

        ImageLoaderConfiguration config = builder.build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
    }

    private void init() {

        handler = new Handler();
        gridGallery = (GridView) findViewById(R.id.gridGallery);
        gridGallery.setFastScrollEnabled(true);
        gridGallery.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int position, long arg3) {
                Intent i = new Intent(MainActivity.this, MainImageEditActivity.class);
                String d = adapter.getItem(position).sdcardPath;
                i.putExtra("uri", d);
                i.putExtra("position", position);
                startActivityForResult(i, 300);
                overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
                return true;
            }
        });

        gridGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    final int position, long arg3) {
                AlertDialog.Builder alert = new AlertDialog.Builder(
                        MainActivity.this);
                alert.setTitle("Alert!!");
                alert.setMessage("Are you sure to delete record");
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.remove(position);
                        if(adapter.getCount() == 0)
                        {
                            btnVideoCreate.setEnabled(false);
                        }
                        dialog.dismiss();

                    }
                });
                alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });

                alert.show();
            }

        });


        adapter = new GalleryAdapter(getApplicationContext(), imageLoader);
        adapter.setMultiplePick(false);
        gridGallery.setAdapter(adapter);

        viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
        viewSwitcher.setDisplayedChild(1);

        btnGalleryPickMul = (ImageButton) findViewById(R.id.btnGalleryPickMul);
        btnGalleryPickMul.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(Action.ACTION_MULTIPLE_PICK);
                startActivityForResult(i, 200);
                overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
            }
        });

        btnVideoCreate = (ImageButton) findViewById(R.id.btnVideoCreate);
        btnVideoCreate.setEnabled(false);
        btnVideoCreate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                record();
            }
        });

        btnHelp = (ImageButton) findViewById(R.id.btnHelp);
        btnHelp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent appHelp = new Intent(MainActivity.this, Instructions.class);
                startActivity(appHelp);
                overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
            }
        });

    }

    private void record() {

        progressDialog = new ProgressDialog(MainActivity.this);
        // Set progressbar title
        progressDialog.setTitle("Video Creating");
        // Set progressbar message
        progressDialog.setMessage("Loading...");

        progressDialog.setCancelable(false);
        // Show progressbar
        progressDialog.show();

        Thread mThread = new Thread() {

            @Override
            public void run() {
                String path = Environment.getExternalStorageDirectory().getPath() + "/Picture_This_Videos";

                File folder = new File(path);
                ArrayList<String> listOfFiles;
                if (folder.exists()) {
                    listOfFiles = adapter.getAllPaths();
                } else {
                    if (folder.mkdir()) {
                        listOfFiles = adapter.getAllPaths();
                    } else {
                        Log.d("Record", "Error creating the folder  : " + folder.toString());
                        return;
                    }
                }

                opencv_core.IplImage[] iplimage = null;
                if (listOfFiles.size() > 0) {

                    iplimage = new opencv_core.IplImage[listOfFiles.size()];

                    for (int j = 0; j < listOfFiles.size(); j++) {
                        iplimage[j] = opencv_highgui.cvLoadImage(listOfFiles.get(j));
                    }

                }

                String videoDetails = path + System.currentTimeMillis() + ".mp4";
                FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(videoDetails, 200, 150);


                try {
                    recorder.setVideoCodec(13); // CODEC_ID_MPEG4 //CODEC_ID_MPEG1VIDEO
                    // //http://stackoverflow.com/questions/14125758/javacv-ffmpegframerecorder-properties-explanation-needed

                    recorder.setVideoQuality(1.0D);
                    recorder.setFrameRate(1.0D); // This is the frame rate for video. If
                    // you really want to have good video
                    // quality you need to provide large set
                    // of images.
                    recorder.setPixelFormat(0); // PIX_FMT_YUV420P
                    recorder.setVideoBitrate(40000);
                    recorder.start();

                    for (int i = 0; i < iplimage.length; i++) {

                        recorder.record(iplimage[i]);

                    }
                    recorder.stop();


                    progressDialog.dismiss();
                    Intent videoIntent = new Intent(MainActivity.this, VideoPlaying.class);
                    videoIntent.putExtra("uri", videoDetails);
                    startActivity(videoIntent);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        mThread.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            adapter.clear();

            viewSwitcher.setDisplayedChild(1);
            String single_path = data.getStringExtra("single_path");
            imageLoader.displayImage("file://" + single_path, imgSinglePick);

        } else if (requestCode == 200 && resultCode == Activity.RESULT_OK && null != data) {
            String[] all_path = data.getStringArrayExtra("all_path");

            ArrayList<CustomGallery> dataT = new ArrayList<CustomGallery>();
            btnVideoCreate.setEnabled(true);
            for (String string : all_path) {
                CustomGallery item = new CustomGallery();
                item.sdcardPath = string;

                dataT.add(item);

                MAX_IMAGE_SELECTION_LENGTH--;
            }

            viewSwitcher.setDisplayedChild(0);
            adapter.addAll(dataT);
        } else if (requestCode == 300 && resultCode == Activity.RESULT_OK && null != data) {

            String uri = data.getStringExtra("uri");
            int position = data.getIntExtra("position", 0);

            CustomGallery item = new CustomGallery();
            item.sdcardPath = uri;
            item.isSeleted = true;

            viewSwitcher.setDisplayedChild(0);
            adapter.replace(position, item);
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        // event when double tap occurs
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }


    }

}
