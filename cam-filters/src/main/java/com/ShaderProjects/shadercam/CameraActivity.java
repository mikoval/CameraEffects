package com.ShaderProjects.shadercam;

import com.ShaderProjects.shadercam.ShaderPrograms.EdgeShaderProgram;
import com.ShaderProjects.shadercam.ShaderPrograms.MaskShaderProgram;
import com.ShaderProjects.shadercam.ShaderPrograms.RainShaderProgram;
import com.ShaderProjects.shadercam.ShaderPrograms.ZeldaMaskShaderProgram;
import com.androidexperiments.shadercam.example.R;
import com.ShaderProjects.shadercam.ShaderPrograms.CartoonShaderProgram;
import com.ShaderProjects.shadercam.ShaderPrograms.RippleShaderProgram;
import com.androidexperiments.shadercam.fragments.PermissionsHelper;
import com.androidexperiments.shadercam.fragments.VideoFragment;
import com.androidexperiments.shadercam.gl.BasicShader;
import com.androidexperiments.shadercam.gl.Shader;
import com.androidexperiments.shadercam.gl.VideoRenderer;
import com.androidexperiments.shadercam.utils.ShaderUtils;
import com.uncorkedstudios.android.view.recordablesurfaceview.RecordableSurfaceView;

import android.Manifest;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Written by Anthony Tripaldi - modified by someone else
 *
 * Very basic implemention of shader camera.
 */
public class CameraActivity extends FragmentActivity
        implements PermissionsHelper.PermissionsListener {


    private static final String TAG = "FOO";

    private static final String TAG_CAMERA_FRAGMENT = "tag_camera_frag";

    /**
     * filename for our test video output
     */
    private static final String TEST_VIDEO_FILE_NAME = "test_video.mp4";

    /**
     * We inject our views from our layout xml here using {@link ButterKnife}
     */
    @Bind(R.id.surface_view)
    RecordableSurfaceView mRecordableSurfaceView;

    @Bind(R.id.btn_record)
    ImageButton mRecordBtn;

    @Bind(R.id.btn_record_activate)
    ImageButton mRecordBtnActivate;

    @Bind(R.id.btn_picture)
    ImageButton mPictureBtn;

    @Bind(R.id.btn_picture_activate)
    ImageButton mPictureBtnActivate;

    @Bind(R.id.my_recycler_view)
    CustomScroller effectsScroller;

    @Bind(R.id.filter_name)
    TextView filterName;

    /**
     * Custom fragment used for encapsulating all the {@link android.hardware.camera2} apis.
     */
    private VideoFragment mVideoFragment;


    protected VideoRenderer mVideoRenderer;

    /**
     * boolean for triggering restart of camera after completed rendering
     */

    private PermissionsHelper mPermissionsHelper;

    private boolean mPermissionsSatisfied = false;

    private File mOutputFile;

    private boolean mIsRecording = false;

    private CustomScroller recyclerView;
    private RecyclerView.Adapter mAdapter;

    private boolean showingEffectsSlider = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rsv);


        ButterKnife.bind(this);

        //setup permissions for M or start normally
        if (PermissionsHelper.isMorHigher()) {
            setupPermissions();
        }

        recyclerView = (CustomScroller) findViewById(R.id.my_recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        ArrayList<ShaderHandle> arr = new ArrayList();

        {
            arr.add(new ShaderHandle("Cartoon", new CartoonShaderProgram(this, null),  R.drawable.cartoon_img));
            arr.add(new ShaderHandle("Ripple", new RippleShaderProgram(this, null),  R.drawable.ripple_img));
            arr.add(new ShaderHandle("None", new BasicShader(this, null),  R.drawable.basic_img));
            arr.add(new ShaderHandle("Edge", new EdgeShaderProgram(this, null),  R.drawable.edge_img));
            arr.add(new ShaderHandle("Rain", new RainShaderProgram(this, null),  R.drawable.rain_img));
            arr.add(new ShaderHandle("Mask", new MaskShaderProgram(this, null),  R.drawable.mask));
            arr.add(new ShaderHandle("Zelda Mask", new ZeldaMaskShaderProgram(this, null),  R.drawable.zelda_mask));


        }


        mAdapter = new MyAdapter(this, arr, layoutManager);
        recyclerView.setAdapter(mAdapter);

        recyclerView.setChangeHandler(new CustomScroller.ChangeHandler() {
            @Override
            public void onChanged(View v) {
                ShaderWidget view = (ShaderWidget) v;
                filterName.setText(view.getText());
                Shader shader = view.getShader();

                shader.setRenderer(mVideoRenderer);
               

                mVideoRenderer.setShader(shader);


            }
        });

    }


    /**
     * create the camera fragment responsible for handling camera state and add it to our activity
     */
    private void setupVideoFragment(VideoRenderer renderer) {

        mVideoFragment = VideoFragment.getInstance();
        mVideoFragment.setRecordableSurfaceView(mRecordableSurfaceView);
        mVideoFragment.setVideoRenderer(renderer);
        mVideoFragment.setCameraToUse(VideoFragment.CAMERA_PRIMARY);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(mVideoFragment, TAG_CAMERA_FRAGMENT);
        transaction.commit();
    }


    private void setupPermissions() {
        mPermissionsHelper = PermissionsHelper.attach(this);
        mPermissionsHelper.setRequestedPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE

        );
    }


    /**
     * add a listener for touch on our surface view that will pass raw values to our renderer for
     * use in our shader to control color channels.
     */
    private void setupInteraction() {

        mRecordableSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    mVideoRenderer.setTouch(-100, -100);
                } else {
                    mVideoRenderer.setTouch(event.getX(), event.getY());
                }
                return true;
            }
        });
     }

    /**
     * Things are good to go and we can continue on as normal. If this is called after a user
     * sees a dialog, then onResume will be called next, allowing the app to continue as normal.
     */
    @Override
    public void onPermissionsSatisfied() {
        Log.d(TAG, "onPermissionsSatisfied()");
        mPermissionsSatisfied = true;
    }

    /**
     * User did not grant the permissions needed for out app, so we show a quick toast and kill the
     * activity before it can continue onward.
     *
     * @param failedPermissions string array of which permissions were denied
     */
    @Override
    public void onPermissionsFailed(String[] failedPermissions) {
        Log.e(TAG, "onPermissionsFailed()" + Arrays.toString(failedPermissions));
        mPermissionsSatisfied = false;
        Toast.makeText(this, "shadercam needs all permissions to function, please try again.",
                Toast.LENGTH_LONG).show();
        this.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume()");
        setupInteraction();

        ShaderUtils.goFullscreen(this.getWindow());

        /**
         * if we're on M and not satisfied, check for permissions needed
         * {@link PermissionsHelper#checkPermissions()} will also instantly return true if we've
         * checked prior and we have all the correct permissions, allowing us to continue, but if its
         * false, we want to {@code return} here so that the popup will trigger without {@link #setReady(SurfaceTexture, int, int)}
         * being called prematurely
         */

        if (PermissionsHelper.isMorHigher()) {
            if (!mPermissionsHelper.checkPermissions()) {
                return;
            } else {
                if (mVideoRenderer == null) {
                    mVideoRenderer = new VideoRenderer(this);
                }
                setupVideoFragment(mVideoRenderer);
                mRecordableSurfaceView.resume();

                mOutputFile = getVideoFile();
                android.graphics.Point size = new android.graphics.Point();
                getWindowManager().getDefaultDisplay().getRealSize(size);
                try {
                    mRecordableSurfaceView.initRecorder(mOutputFile, size.x, size.y, null, null);
                } catch (IOException ioex) {
                    Log.e(TAG, "Couldn't re-init recording", ioex);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        shutdownCamera();
        mRecordableSurfaceView.pause();
        finish();
    }


    @OnClick(R.id.btn_picture)
    public void onClickPicture() {
        takePicture();
    }


    @OnClick(R.id.btn_record)
    public void onClickRecord() {
        ImageButton btn = (ImageButton)findViewById(R.id.btn_record);
        if (mIsRecording) {
            btn.setColorFilter(Color.argb(255, 136, 136, 136));
            stopRecording();
        } else {
            btn.setColorFilter(Color.argb(255, 255, 0, 0));

            startRecording();
        }
    }

    @OnClick(R.id.btn_swap_camera)
    public void onClickSwapCamera() {
        mVideoFragment.swapCamera();
    }

    @OnClick(R.id.btn_record_activate)
    public void onClickActivateRecord() {
        mRecordBtn.setVisibility(View.VISIBLE);
        mPictureBtn.setVisibility(View.GONE);
        mRecordBtnActivate.setVisibility(View.GONE);
        mPictureBtnActivate.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.btn_picture_activate)
    public void onClickActivatePicture() {
        mPictureBtn.setVisibility(View.VISIBLE);
        mRecordBtn.setVisibility(View.GONE);
        mPictureBtnActivate.setVisibility(View.GONE);
        mRecordBtnActivate.setVisibility(View.VISIBLE);
    }

    /*
    @OnClick(R.id.btn_shader_1)
    public void onClickShader1() {
        mVideoRenderer.setShader(new BasicShader(this, mVideoRenderer));
    }

    @OnClick(R.id.btn_shader_2)
    public void onClickShader2() {
        mVideoRenderer.setShader(new EdgeShaderProgram(this, mVideoRenderer));
    }


    @OnClick(R.id.btn_shader_3)
    public void onClickShader3() {
        mVideoRenderer.setShader(new CartoonShaderProgram(this, mVideoRenderer));
    }

    @OnClick(R.id.btn_shader_4)
    public void onClickShader4() {
        mVideoRenderer.setShader(new RippleShaderProgram(this, mVideoRenderer));
    }

*/

    @OnClick(R.id.btn_effects)
    public void onClickShader4() {
        if(showingEffectsSlider) {
            effectsScroller.setVisibility(View.GONE);
        } else {
            effectsScroller.setVisibility(View.VISIBLE);
        }
        showingEffectsSlider = !showingEffectsSlider;
    }

    private void takePicture() {
        mVideoRenderer.takeScreenshot(getImageFile());
    }
    private void scanFile(String path) {

        MediaScannerConnection.scanFile(this,
                new String[] { path }, null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {

                    }
                });
    }

    private void startRecording() {
        mRecordableSurfaceView.startRecording();
        mIsRecording = true;

        //mRecordBtn.setText("Stop");

    }

    private void stopRecording() {
        //mRecordBtn.setText("Record");
        Log.e(TAG, "Stopping");

        mRecordableSurfaceView.stopRecording();

        scanFile(mOutputFile.getAbsolutePath());


        try {
            mOutputFile = getVideoFile();
            android.graphics.Point size = new android.graphics.Point();
            getWindowManager().getDefaultDisplay().getRealSize(size);
            mRecordableSurfaceView.initRecorder(mOutputFile, size.x, size.y, null, null);
        } catch (IOException ioex) {
            Log.e(TAG, "Couldn't re-init recording", ioex);
        }

        mIsRecording = false;
        Toast.makeText(this, "File recording complete: " + getVideoFile().getAbsolutePath(),
                Toast.LENGTH_LONG).show();



    }

    private File getImageFile() {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root + "/saved_images_1");

        Log.d(TAG, root);

        myDir.mkdirs();

        String fname = System.currentTimeMillis() + "_" + "Image.png";
        File file = new File(myDir, fname);

        return file;
    }

    private File getVideoFile() {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root + "/saved_images_1");

        Log.d(TAG, root);

        myDir.mkdirs();

        String fname = System.currentTimeMillis() + "_" + "Video.mp4";
        File file = new File(myDir, fname);

        return file;
    }

    /**
     * kills the camera in camera fragment and shutsdown render thread
     */
    private void shutdownCamera() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.remove(mVideoFragment);
        ft.commit();
        mVideoFragment = null;
    }
}
