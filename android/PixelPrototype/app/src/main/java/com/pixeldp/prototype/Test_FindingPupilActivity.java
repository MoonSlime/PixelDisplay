package com.pixeldp.prototype;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import org.opencv.core.Rect;

import java.io.IOException;
import java.io.InputStream;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Test_FindingPupilActivity extends Activity {
    @Bind(R.id.imageView_test_findingPupil)
    FindingPupilActivity.FindingPupilImageView imageView_findingPupil;

    private Bitmap picture;
    private Matrix translatedMatrix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_finding_pupil);
        ButterKnife.bind(this);

        try {
            InputStream is = getAssets().open("test.jpg");
            picture = BitmapFactory.decodeStream(is);
            imageView_findingPupil.setImageBitmap(picture);
            zoomToCenter(picture);
            imageView_findingPupil.setOnTouchListener(new FindingPupilOnTouchListener());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (picture != null && !picture.isRecycled()) {
            picture.recycle();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        imageView_findingPupil.setDrawing(true);
    }

    @OnClick(R.id.button_test_designation)
    public void onClickButtonDesignation() {
        imageView_findingPupil.setDrawing(false);

        Bitmap drawingCache = getDrawingCache(imageView_findingPupil);
        Rect pupilRect = new Rect(drawingCache.getWidth() / 2 - 100, drawingCache.getHeight() / 2 - 100, 200, 200);
        Bitmap pupilBitmap = Bitmap.createBitmap(drawingCache, pupilRect.x, pupilRect.y, pupilRect.width, pupilRect.height);

        Intent intent = new Intent(getApplicationContext(), Test_AnalyzingCrescentActivity.class);
        intent.putExtra("pupilBitmap", pupilBitmap);
        startActivity(intent);
    }

    public Bitmap getDrawingCache(View view) {
        view.setDrawingCacheEnabled(true);
        Bitmap drawingCache = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return drawingCache;
    }

    private class FindingPupilOnTouchListener implements View.OnTouchListener {
        private static final int NONE = 0;
        private static final int DRAG = 1;
        private static final int ZOOM = 2;
        private int mode = NONE;

        // These matrices will be used to move and zoom image
        private Matrix previousMatrix = new Matrix();

        // Remember some things for zooming
        private PointF startPoint = new PointF();
        private PointF middlePoint = new PointF();
        private float previousDistance = 1f;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageView view = (ImageView) v;

            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    previousMatrix.set(translatedMatrix);
                    startPoint.set(event.getX(), event.getY());
                    mode = DRAG;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    previousDistance = distanceBetweenTwoFingers(event);
                    if (previousDistance > 10f) {
                        previousMatrix.set(translatedMatrix);
                        middlePointBetweenTwoFingers(middlePoint, event);
                        mode = ZOOM;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    switch (mode) {
                        case DRAG:
                            translatedMatrix.set(previousMatrix);
                            translatedMatrix.postTranslate(event.getX() - startPoint.x, event.getY() - startPoint.y);
                            break;
                        case ZOOM:
                            float nextDistance = distanceBetweenTwoFingers(event);
                            if (nextDistance > 10f) {
                                translatedMatrix.set(previousMatrix);
                                float scale = nextDistance / previousDistance;
                                translatedMatrix.postScale(scale, scale, middlePoint.x, middlePoint.y);
                            }
                            break;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    break;
            }

            view.setImageMatrix(translatedMatrix);
            return true;
        }

        private float distanceBetweenTwoFingers(MotionEvent event) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);

            return (float) Math.sqrt(x * x + y * y);
        }

        private void middlePointBetweenTwoFingers(PointF point, MotionEvent event) {
            float x = event.getX(0) + event.getX(1);
            float y = event.getY(0) + event.getY(1);
            point.set(x / 2, y / 2);
        }
    }

    private void zoomToCenter(Bitmap image) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Point displaySize = new Point(metrics.widthPixels, metrics.heightPixels);

        translatedMatrix = imageView_findingPupil.getImageMatrix();
        float scaleRatio = ((float) displaySize.y / image.getHeight());
        translatedMatrix.postTranslate((displaySize.x - image.getWidth()) / 2, (displaySize.y - image.getHeight()) / 2);
        translatedMatrix.postScale(scaleRatio, scaleRatio, displaySize.x / 2, displaySize.y / 2);
        imageView_findingPupil.setImageMatrix(translatedMatrix);
    }
}