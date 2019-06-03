package com.ShaderProjects.shadercam.ShaderPrograms;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ShaderProjects.shadercam.Shapes.glLines;
import com.ShaderProjects.shadercam.Shapes.glRect;
import com.androidexperiments.shadercam.example.R;
import com.androidexperiments.shadercam.gl.BasicShader;
import com.androidexperiments.shadercam.gl.InverseShader;
import com.androidexperiments.shadercam.gl.Shader;
import com.androidexperiments.shadercam.gl.VideoRenderer;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import org.joml.Matrix4f;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;

import static com.androidexperiments.shadercam.gl.GlUtil.loadTexture;

public class ZeldaMaskShaderProgram extends Shader {
    final static String frag = "edge.frag.glsl";
    final static String vert = "basic.vert.glsl";

    final static float SCALE = 1.0f;

    private glRect rect;
    private glLines lines;

    private Context context;
    private ImageView imageView;
    private RelativeLayout layout;
    private Activity activity;
    private  boolean complete = true;

    private int count1 = 0;
    private  int count2 = 0;
    private  long time;

    private int maskTexture;


    private BasicShader basicShader;
    private InverseShader inverseShader;

    private FirebaseVisionImage textImage;
    private FirebaseVisionFaceDetector faceDetector;
    private long millis_startTime;


    private Hashtable<Integer,FaceWrapper> map;

    private class FaceWrapper {
        public Rect position;
        public FirebaseVisionFace target;
        private int  count;
        private float interpolate;

        public FaceWrapper(FirebaseVisionFace face) {
            this.target = face;
            this.position = face.getBoundingBox();
            count = 3;
        }

        public void updateCount() {
            count--;
        }

        public void update(FirebaseVisionFace face) {
            this.target = face;

            count = 3;
            this.interpolate = 0.1f;
        }
        public void updateInt() {
            if(this.interpolate < 1.0) {
                this.interpolate = this.interpolate += .1;
            }
        }

        public Rect getFace() {
            Rect rect = new Rect();
            Rect target = this.target.getBoundingBox();
            Rect prev = this.position;
            float inter = interpolate;
            double left = target.left * inter + prev.left * (1.0 - inter);
            double right = target.right * inter + prev.right * (1.0 - inter);
            double top = target.top * inter + prev.top * (1.0 - inter);
            double bottom = target.bottom * inter + prev.bottom * (1.0 - inter);
            rect.set((int) left, (int)top, (int)right, (int)bottom);

            this.position = rect;

            return position;
        }

        private float[] getCenter(List<FirebaseVisionPoint> points) {
            float eye[] = new float[2];

            for(FirebaseVisionPoint point: points){
                eye[0] += point.getX();
                eye[1] += point.getY();
            }
            eye[0] /= points.size();
            eye[1] /= points.size();
            return eye;
        }

        public double getAngle() {
            float[] left = getCenter(target.getContour(FirebaseVisionFaceContour.LEFT_EYE).getPoints());
            float[] right = getCenter(target.getContour(FirebaseVisionFaceContour.RIGHT_EYE).getPoints());
            float delta_x = left[0] - right[0];
            float delta_y = left[1] - right[1];

            double angle =  Math.atan2(delta_y, delta_x);
            return angle;
        }

    }

    public ZeldaMaskShaderProgram(Context context, VideoRenderer renderer) {
        super(context, renderer, frag, vert);
        this.context = context;

        FirebaseApp.initializeApp(context);


        FirebaseVisionFaceDetectorOptions options = new FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                .enableTracking()
                .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
          .build();

        map = new Hashtable<Integer,FaceWrapper>();

        faceDetector = FirebaseVision.getInstance().getVisionFaceDetector(options);

        activity = (Activity) context;
        imageView = new ImageView(context);
        layout = new RelativeLayout(context);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ALIGN_TOP);
        params.addRule(RelativeLayout.ALIGN_LEFT);
        layout.setLayoutParams(params);

        imageView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setBackgroundColor(Color.RED);
        layout.addView(imageView);
        layout.setVisibility(View.INVISIBLE);

        activity.runOnUiThread(new addImageView(layout, activity));

    }

    @Override
    public void init(){
        super.init();

        basicShader = new BasicShader(context, renderer);
        basicShader.init();
        inverseShader = new InverseShader(context, renderer);
        inverseShader.init();
        rect = new glRect(context);
        rect.init();
        lines = new glLines(context);
        lines.init();

        maskTexture = loadTexture(context, R.drawable.zelda_mask);
        rect.setTexture(maskTexture);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //layout.setVisibility(View.VISIBLE);
            }
        });


        time = System.currentTimeMillis();
    }

    public class addImageView implements Runnable {
        private RelativeLayout layout;
        private Activity activity;
        public addImageView(RelativeLayout data, Activity activity) {
            this.layout = data;
            this.activity = activity;
        }

        @Override
        public void run() {
            FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);
            content.addView(layout);
        }
    }
    public class updateImageView implements Runnable {
        private Bitmap img;
        private ImageView imgView;
        public updateImageView(Bitmap img, ImageView imgView) {
            this.img = img;
            this.imgView = imgView;
        }

        @Override
        public void run() {
            imageView.setImageBitmap(img);
            imageView.setAlpha(1.0f);
        }
    }

    private void FPS(){
        count1++;

        FPS();
        long newTime = System.currentTimeMillis();
        double seconds = ((double)(newTime - time )) / 1000.0;
        if(seconds > 5.0) {
            time = newTime;
            double fps = count1 / seconds;
            double fps2 = count2 / seconds;

            Log.d("FINDME", "FPS = " + fps + ", ML FPS = " + fps2 + " , ");
            count1 = 0;
            count2 = 0;
        }
    }

    private Bitmap createBitmap() {
        int h = renderer.getHeight()/(int)SCALE;
        int w = renderer.getWidth()/(int)SCALE;

        GLES20.glViewport(0, 0, w, h);
        inverseShader.draw();


        ByteBuffer buffer = ByteBuffer.allocate(w * h * 4);
        GLES20.glReadPixels(0, 0, w, h,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);

        final Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(buffer);
        return bmp;
    }

    private void drawBackground() {
        int h = renderer.getHeight();
        int w = renderer.getWidth();

        GLES20.glViewport(0, 0, w, h);
        basicShader.draw();
    }

    private void drawMasks() {
        Set<Integer> keys = map.keySet();
        for(int i: keys) {
            FaceWrapper fWrapper= map.get(i);
            Rect face = fWrapper.getFace();
            fWrapper.updateInt();

            //float rotation = fWrapper.target.getHeadEulerAngleZ();

            //Log.d("FINDME: " , "ROTATIONS: " + fWrapper.target.getHeadEulerAngleY() + ", " + fWrapper.target.getHeadEulerAngleZ());

            float screenAspect = renderer.getHeight() * 1.0f/ renderer.getWidth();
            float previewAspect = imageHeight * 1.0f/ imageWidth;

            float screenToTextureAspectRatio = screenAspect / previewAspect;



            float width = screenToTextureAspectRatio * (float)face.width()/(float)imageWidth;
            float height =  (float)face.height()/(float)imageHeight;
            float x = convertX(face.centerX());
            float y = convertY(face.centerY());

            Matrix4f mat = new Matrix4f();
            float aspect = screenAspect;
            mat.translate(x, y + 0.1f, 0).scale(width, height, 0.0f).scale(2.5f, 2.0f, 0.0f).rotateZ((float)fWrapper.getAngle() + 3.14f);

            //Log.d("FINDME", "x = " + x + ", MATRIX : " + mat.toString());

            Bitmap map = image.getBitmap();
            rect.setTransform(mat);
            rect.draw();

            ArrayList<glLines.Point> points = new ArrayList<glLines.Point>();
            //FirebaseVisionFaceContour contour = fWrapper.target.getContour(FirebaseVisionFaceContour.FACE);
            //FirebaseVisionFaceContour contourOld = fWrapper.prev.getContour(FirebaseVisionFaceContour.FACE);
/*
            for(int j = 0; j < contour.getPoints().size(); j++) {
                FirebaseVisionPoint point1 = contour.getPoints().get(j);
                FirebaseVisionPoint point2 = contourOld.getPoints().get(j);
                float inter = fWrapper.interpolate;
                fWrapper.updateInt();
                x = convertX(point1.getX()) * inter + convertX(point2.getX()) * (1.0f - inter);
                y = convertY(point1.getY()) * inter + convertY(point2.getY()) * (1.0f - inter);
                points.add(new glLines.Point(x,y));
            }
            lines.setVertices(points);
            lines.setTransform(mat);
            lines.draw();

 */

        }
    }

    private float convertX (float x) {
        float screenAspect = renderer.getHeight() * 1.0f/ renderer.getWidth();
        float previewAspect = imageHeight * 1.0f/ imageWidth;

        float screenToTextureAspectRatio = screenAspect / previewAspect;

        return   screenToTextureAspectRatio  * -2.0f * ((x /  imageWidth) - 0.5f);
    }
    private float convertY (float y) {
        return  -2.0f * (( y/  imageHeight) - 0.5f);
    }

    @Override
    public void draw() {


        //Log.d("FINDME:", " RENDERER DIMENSIONS: " +  renderer.getWidth() + ", " + renderer.getHeight());


        if(basicShader == null) {
            return;
        }


        drawBackground();

        drawMasks();

        if(image == null){
            return;
        }



        if(!complete){
            return;
        }

        count2++;

        complete = false;

        millis_startTime = System.currentTimeMillis();


        faceDetector.detectInImage(image).addOnSuccessListener(
                new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionFace> faces) {


                        Bitmap markedBitmap = image.getBitmap().copy(Bitmap.Config.ARGB_8888, true);

                        Canvas canvas = new Canvas(markedBitmap);
                        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                        paint.setColor(Color.parseColor("#99003399"));

                        //Log.d("FINDME:" , "FOUND FACES : "  + faces.size());


                        for(FirebaseVisionFace face: faces) {
                            FaceWrapper existing = map.get(face.getTrackingId());
                            if(existing != null) {
                                existing.update(face);
                            } else {
                                map.put(face.getTrackingId(), new FaceWrapper(face));
                            }

                            canvas.drawRect(face.getBoundingBox(), paint);



                        }
                        for(Integer i: map.keySet()) {
                            FaceWrapper obj = map.get(i);
                            obj.updateCount();
                            if(obj.count < 0) {
                                map.remove(i);
                            }
                        }


                        activity.runOnUiThread(new updateImageView(markedBitmap, imageView));


                        complete = true;
                        long millis_endTime = System.currentTimeMillis();

                        Log.d("FINDME:" , "TOTAL ELLAPSED TIME: " + ((float)(millis_endTime - millis_startTime)/100.0f));

                    }
                }).addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        complete = true;
                    }
                });
    }

    @Override
    public void onDestroy() {
        final Activity activity = (Activity) context;
        FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);
        content.removeView(layout);
        layout.setVisibility(View.GONE);
    }
}

