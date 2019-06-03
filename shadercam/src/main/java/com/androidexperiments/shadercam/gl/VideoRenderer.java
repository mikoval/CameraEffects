package com.androidexperiments.shadercam.gl;

import com.androidexperiments.shadercam.fragments.VideoFragment;
import com.androidexperiments.shadercam.utils.ShaderUtils;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.uncorkedstudios.android.view.recordablesurfaceview.RecordableSurfaceView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Environment;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.Surface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

/**
 * Base camera rendering class. Responsible for rendering to proper window contexts, as well as
 * recording video with built-in media recorder.
 *
 * Subclass this and add any kind of fun stuff u want, new shaders, textures, uniforms - go to town!
 */

public class VideoRenderer implements RecordableSurfaceView.RendererCallbacks,
        SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG = VideoRenderer.class.getSimpleName();

    int mNeedsRefreshCount = 0;

    private boolean updateShaders;
    private boolean requestScreenshot = false;

    File imageFile;

    /**
     * if you create new files, just override these defaults in your subclass and
     * don't edit the {@link #vertexShaderCode} and {@link #fragmentShaderCode} variables
     */
    private String DEFAULT_FRAGMENT_SHADER = "vid.frag.glsl";

    private String DEFAULT_VERTEX_SHADER = "vid.vert.glsl";

    /**
     * Current context for use with utility methods
     */
    private WeakReference<Context> mContextWeakReference;

    protected int mSurfaceWidth, mSurfaceHeight;

    public SurfaceTexture mSurfaceTexture;

    /**
     * if you override these in ctor of subclass, loader will ignore the files listed above
     */
    private String vertexShaderCode;

    private String fragmentShaderCode;

    /**
     * Basic mesh rendering code
     */
    private static float squareSize = 1.0f;

    private static float squareCoords[] = {
            -squareSize, squareSize, // 0.0f,     // top left
            squareSize, squareSize, // 0.0f,   // top right
            -squareSize, -squareSize, // 0.0f,   // bottom left
            squareSize, -squareSize, // 0.0f,   // bottom right
    };

    private static short drawOrder[] = {0, 1, 2, 1, 3, 2};

    public FloatBuffer textureBuffer;

    private float textureCoords[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
    };

    protected Shader mShaderProgram;

    public FloatBuffer vertexBuffer;

    private ShortBuffer drawListBuffer;

    /**
     * "arbitrary" maximum number of textures. seems that most phones dont like more than 16
     */
    private static final int MAX_TEXTURES = 16;

    /**
     * for storing all texture ids from genTextures, and used when binding
     * after genTextures, id[0] is reserved for camera texture
     */
    public int[] mTexturesIds = new int[MAX_TEXTURES];

    /**
     * array of proper constants for use in creation,
     * updating, and drawing. most phones max out at 16
     * same number as {@link #MAX_TEXTURES}
     *
     * Used in our implementation of {@link #addTexture(Bitmap, String)}
     */
    private int[] mTextureConsts = {
            GLES20.GL_TEXTURE1,
            GLES20.GL_TEXTURE2,
            GLES20.GL_TEXTURE3,
            GLES20.GL_TEXTURE4,
            GLES20.GL_TEXTURE5,
            GLES20.GL_TEXTURE6,
            GLES20.GL_TEXTURE7,
            GLES20.GL_TEXTURE8,
            GLES20.GL_TEXTURE9,
            GLES20.GL_TEXTURE10,
            GLES20.GL_TEXTURE11,
            GLES20.GL_TEXTURE12,
            GLES20.GL_TEXTURE13,
            GLES20.GL_TEXTURE14,
            GLES20.GL_TEXTURE15,
            GLES20.GL_TEXTURE16,
    };

    /**
     * array of {@link Texture} objects used for looping through
     * during the render pass. created in {@link #addTexture(int, Bitmap, String, boolean)}
     * and looped in {@link #setExtraTextures()}
     */
    private ArrayList<Texture> mTextureArray;


    /**
     * matrix for transforming our camera texture, available immediately after {@link #}s
     * {@code updateTexImage()} is called in our main {@link #onDrawFrame()} loop.
     */
    public float[] mCameraTransformMatrix = new float[16];
    public float[] mOrthoMatrix = new float[16];

    private float mAspectRatio = 1.0f;

    /**
     * Interface listener for some callbacks to the UI thread when rendering is setup and finished.
     */
    private OnRendererReadyListener mOnRendererReadyListener;

    /**
     * Width and height storage of our viewport size, so we can properly accomodate any size View
     * used to display our preview on screen.
     */
    private int mViewportWidth, mViewportHeight;

    /**
     * Reference to our users VideoFragment to ease setting viewport size. Thought about decoupling but wasn't
     * worth the listener/callback hastle
     */
    private VideoFragment mVideoFragment;


    /**
     * Array of ints for use with screen orientation hint in our MediaRecorder.
     * See {@link #()} for more info on its usage.
     */
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String mFragmentShaderPath;

    private String mVertexShaderPath;

    /**
     * Simple ctor to use default shaders
     */
    public VideoRenderer(Context context) {
        Log.e(TAG, "CREATED VIDEO RENDERER");
        init(context, DEFAULT_FRAGMENT_SHADER, DEFAULT_VERTEX_SHADER);
    }

    /**
     * Main constructor for passing in shaders to override the default shader.
     * Context, texture, width, and height are passed in automatically by CameraTextureListener
     *
     * @param fragPath the file name of your fragment shader, ex: "lip_service.frag" if it is top-level /assets/ folder. Add subdirectories if needed
     * @param vertPath the file name of your vertex shader, ex: "lip_service.vert" if it is top-level /assets/ folder. Add subdirectories if needed
     */
    public VideoRenderer(Context context, String fragPath, String vertPath) {
        init(context, fragPath, vertPath);
    }

    private void init(Context context, String fragPath, String vertPath) {
        this.mContextWeakReference = new WeakReference<>(context);
        this.mFragmentShaderPath = fragPath;
        this.mVertexShaderPath = vertPath;

        mShaderProgram = new BasicShader(context, this);
    }

    protected void initGLComponents() {
        onPreSetupGLComponents();
        setupVertexBuffer();
        setupTextures();
        setupCameraTexture();
        mShaderProgram.init();
        onSetupComplete();
    }

    // ------------------------------------------------------------
    // deinit
    // ------------------------------------------------------------

    public void deinitGL() {
        deinitGLComponents();
    }

    protected void deinitGLComponents() {
        GLES20.glDeleteTextures(MAX_TEXTURES, mTexturesIds, 0);
        GLES20.glDeleteProgram(mShaderProgram.program);

    }

    // ------------------------------------------------------------
    // setup
    // ------------------------------------------------------------

    /**
     * override this method if there's anything else u want to accomplish before
     * the main camera setup gets underway
     */
    private void onPreSetupGLComponents() {

    }

    public void setAspectRatio(float aspect) {
        mAspectRatio = aspect;
    }


    protected void setupVertexBuffer() {
        // Draw list buffer
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        // Initialize the texture holder
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);
    }

    protected void setupTextures() {
        ByteBuffer texturebb = ByteBuffer.allocateDirect(textureCoords.length * 4);
        texturebb.order(ByteOrder.nativeOrder());

        textureBuffer = texturebb.asFloatBuffer();
        textureBuffer.put(textureCoords);
        textureBuffer.position(0);

        // Generate the max amount texture ids
        GLES20.glGenTextures(MAX_TEXTURES, mTexturesIds, 0);
        checkGlError("Texture generate");
    }

    /**
     * Remember that Android's camera api returns camera texture not as {@link GLES20#GL_TEXTURE_2D}
     * but rather as {@link GLES11Ext#GL_TEXTURE_EXTERNAL_OES}, which we bind here
     */
    protected void setupCameraTexture() {
        //set texture[0] to camera texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTexturesIds[0]);
        checkGlError("Texture bind");
    }

    public int getCameraTexture() {
        if (mTexturesIds != null && mTexturesIds.length > 0) {
            return mTexturesIds[0];
        } else {
            return -1;
        }
    }

    /**
     * called when all setup is complete on basic GL stuffs
     * override for adding textures and other shaders and make sure to call
     * super so that we can let them know we're done
     */
    protected void onSetupComplete() {
        if (mOnRendererReadyListener != null) {
            mOnRendererReadyListener.onRendererReady();
        }

    }


    /**
     * stop our thread, and make sure we kill a recording if its still happening
     *
     * this should only be called from our handler to ensure thread-safe
     */
    public void shutdown() {

        mOnRendererReadyListener.onRendererFinished();
    }



    /**
     * creates a new texture with specified resource id and returns the
     * tex id num upon completion
     */
    public int addTexture(int resource_id, String uniformName) {
        int texId = mTextureConsts[mTextureArray.size()];
        if (mTextureArray.size() + 1 >= MAX_TEXTURES) {
            throw new IllegalStateException("Too many textures! Please don't use so many :(");
        }

        Bitmap bmp = BitmapFactory
                .decodeResource(mContextWeakReference.get().getResources(), resource_id);

        return addTexture(texId, bmp, uniformName, true);
    }

    public int addTexture(Bitmap bitmap, String uniformName) {
        int texId = mTextureConsts[mTextureArray.size()];
        if (mTextureArray.size() + 1 >= MAX_TEXTURES) {
            throw new IllegalStateException("Too many textures! Please don't use so many :(");
        }

        return addTexture(texId, bitmap, uniformName, true);
    }

    public int addTexture(int texId, Bitmap bitmap, String uniformName, boolean recycle) {
        int num = mTextureArray.size() + 1;

        GLES20.glActiveTexture(texId);
        checkGlError("Texture generate");
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexturesIds[num]);
        checkGlError("Texture bind");
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_NEAREST);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        if (recycle) {
            bitmap.recycle();
        }

        Texture tex = new Texture(num, texId, uniformName);

        if (!mTextureArray.contains(tex)) {
            mTextureArray.add(tex);
            Log.d(TAG, "addedTexture() " + mTexturesIds[num] + " : " + tex);
        }

        return num;
    }

    /**
     * updates specific texture and recycles bitmap used for updating
     */
    public void updateTexture(int texNum, Bitmap drawingCache) {
        GLES20.glActiveTexture(mTextureConsts[texNum - 1]);
        checkGlError("Texture generate");
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexturesIds[texNum]);
        checkGlError("Texture bind");
        GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, drawingCache);
        checkGlError("Tex Sub Image");

        drawingCache.recycle();
    }

    /**
     * override this and copy if u want to add your own mTexturesIds
     * if u need different uv coordinates, refer to {@link #setupTextures()}
     * for how to create your own buffer
     */
    protected void setExtraTextures() {

        for (int i = 0; i < mTextureArray.size(); i++) {
            Texture tex = mTextureArray.get(i);
            int imageParamHandle = GLES20
                    .glGetUniformLocation(mShaderProgram.program, tex.uniformName);

            GLES20.glActiveTexture(tex.texId);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexturesIds[tex.texNum]);
            GLES20.glUniform1i(imageParamHandle, tex.texNum);
        }
    }

    protected void drawElements() {

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT,
                drawListBuffer);

        if(GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER) != GLES30.GL_FRAMEBUFFER_COMPLETE) {
            Log.d("FINDME: ", "INVALID FBO");

        } else {
        }
    }

    protected void onDrawCleanup() {
        //GLES20.glDisableVertexAttribArray(positionHandle);
        //GLES20.glDisableVertexAttribArray(textureCoordinateHandle);
    }

    /**
     * utility for checking GL errors
     */
    public void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("SurfaceTest", op + ": glError " + GLUtils.getEGLErrorString(error));
        }
    }


    public void setOnRendererReadyListener(OnRendererReadyListener listener) {
        mOnRendererReadyListener = listener;

    }

    public void setVideoFragment(VideoFragment videoFragment) {
        mVideoFragment = videoFragment;
        mVideoFragment.setPreviewTexture(getCameraTexture());
    }

    @Override
    public void onSurfaceCreated() {
        deinitGL();
        mTextureArray = new ArrayList<>();
        initGLComponents();
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        mSurfaceHeight = height;
        mSurfaceWidth = width;
        mViewportHeight = height;
        mViewportWidth = width;

        mAspectRatio = 1.0f * mViewportWidth / mViewportHeight;
    }

    @Override
    public void onSurfaceDestroyed() {
        deinitGL();
    }

    @Override
    public void onContextCreated() {

    }

    private boolean mFrameAvailableRegistered = false;

    @Override
    public void onPreDrawFrame() {
        if (mSurfaceTexture != null) {
            if (!mFrameAvailableRegistered) {
                mSurfaceTexture.setOnFrameAvailableListener(this);
                mFrameAvailableRegistered = true;
            }
        } else {
            if (mVideoFragment.getSurfaceTexture() != null) {
                mSurfaceTexture = mVideoFragment.getSurfaceTexture();
            } else {
                mVideoFragment.setPreviewTexture(getCameraTexture());
            }
        }
    }

    @Override
    public void onDrawFrame() {


        Matrix.orthoM(mOrthoMatrix, 0, -mAspectRatio, mAspectRatio,  -1,  1 ,-1, 1);

        if (mNeedsRefreshCount > 0) {
            for (int i = 0; i < mNeedsRefreshCount; i++) {
                mSurfaceTexture.updateTexImage();
                mSurfaceTexture.getTransformMatrix(mCameraTransformMatrix);
                mNeedsRefreshCount--;
            }

        }

        GLES20.glViewport(0, 0, mViewportWidth, mViewportHeight);

       //GLES20.glClearColor(1, 0, 0, 1.0f);
        //GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);


        mShaderProgram.update();

        mShaderProgram.draw();
        

        if(requestScreenshot) {
            screenshot();
            requestScreenshot = false;
        }

        if(updateShaders) {
            mShaderProgram.init();
            updateShaders = false;
        }

        //onDrawCleanup();
    }

    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        mSurfaceTexture = surfaceTexture;
        mSurfaceTexture.setOnFrameAvailableListener(this);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mNeedsRefreshCount++;
        if (mSurfaceTexture != null) {
        } else {
            if (mVideoFragment.getSurfaceTexture() != null) {
                mSurfaceTexture = mVideoFragment.getSurfaceTexture();
            }
        }
    }

    /**
     * Internal class for storing refs to mTexturesIds for rendering
     */
    private class Texture {

        public int texNum;

        public int texId;

        public String uniformName;

        private Texture(int texNum, int texId, String uniformName) {
            this.texNum = texNum;
            this.texId = texId;
            this.uniformName = uniformName;
        }

        @Override
        public String toString() {
            return "[Texture] num: " + texNum + " id: " + texId + ", uniformName: " + uniformName;
        }

    }

    /**
     * Interface for callbacks when render thread completes its setup
     */
    public interface OnRendererReadyListener {

        /**
         * Called when {@link #onSetupComplete()} is finished with its routine
         */
        void onRendererReady();

        /**
         * Called once the looper is killed and our {@link #run()} method completes
         */
        void onRendererFinished();
    }

    public int getWidth() {
        return mSurfaceWidth;
    }

    public int getHeight() {
        return mSurfaceHeight;
    }

    public void takeScreenshot(File image) {
        requestScreenshot = true;
        imageFile = image;
    }

    public enum Direction { VERTICAL, HORIZONTAL };

    public static Bitmap flip(Bitmap src, Direction type) {
        android.graphics.Matrix matrix = new android.graphics.Matrix();

        if(type == Direction.VERTICAL) {
            matrix.preScale(1.0f, -1.0f);
        }
        else if(type == Direction.HORIZONTAL) {
            matrix.preScale(-1.0f, 1.0f);
        } else {
            return src;
        }

        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    public class saveScreenshot implements Runnable {
        ByteBuffer buffer;
        public saveScreenshot(ByteBuffer buffer) {
            this.buffer = buffer;
        }

        public void run() {
            int width = mSurfaceWidth;
            int height = mSurfaceHeight;
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            bitmap = flip(bitmap, Direction.VERTICAL);

            FileOutputStream fos = null;
            File file = imageFile;
            try {
                fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.flush();
                    fos.close();
                    scanFile(file.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void screenshot () {
        int width = mSurfaceWidth;
        int height = mSurfaceHeight;

        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);

        Runnable r = new saveScreenshot(buffer);
        new Thread(r).start();
    }

    public void scanFile(String path) {
        MediaScannerConnection.scanFile(mContextWeakReference.get(),
                new String[] { path }, null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("FINDME", "Finished scanning" + path);
                    }
                });
    }

    public void setShader(Shader shader) {
        if(mShaderProgram != null){
            mShaderProgram.onDestroy();
        }
        mShaderProgram = shader;
        updateShaders = true;
    }

    public void setTouch(float x, float y) {
        mShaderProgram.setTouch(x, y);
    }

    public void setVideoImage(FirebaseVisionImage image, int width, int height){
        mShaderProgram.setVideoImage(image, width, height);
    }

}
