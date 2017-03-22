package com.pixeldp.prototype;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.pixeldp.prototype.device_control.BluetoothControl;
import com.pixeldp.util.ImageUtil;
import com.pixeldp.prototype.R;

import org.opencv.core.Rect;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FindingPupilActivity extends Activity {
    @Bind(R.id.imageView_findingPupil)
    FindingPupilImageView imageView_findingPupil;

    private BluetoothControl bluetoothControl;
    private Matrix translatedMatrix;
    private Bitmap picture;
    private float viewAngle;
    public static float eye_camera_distance; // meter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finding_pupil);
        ButterKnife.bind(this);

        bluetoothControl = BluetoothControl.getInstance(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Toast.makeText(getApplicationContext(), "Cannot connect to raspberry pi using bluetooth", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        ImageReceiver imageReceiver = new ImageReceiver(FindingPupilActivity.this);
        imageReceiver.execute();
        viewAngle = 50.764152f;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if ( picture != null  && !picture.isRecycled()) {
            picture.recycle();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        imageView_findingPupil.setDrawing(true);
    }

    @OnClick(R.id.button_designation)
    public void onClickButtonDesignation() {
        imageView_findingPupil.setDrawing(false);

        float[] values = new float[9];
        translatedMatrix.getValues(values);

        float absoluteX = (imageView_findingPupil.getWidth() / 2 / values[Matrix.MSCALE_X]) - (values[Matrix.MTRANS_X] / values[Matrix.MSCALE_X]);
        float absoluteY = (imageView_findingPupil.getHeight() / 2 / values[Matrix.MSCALE_Y]) - (values[Matrix.MTRANS_Y] / values[Matrix.MSCALE_Y]);
        float accumulatedScaleRatio = values[Matrix.MSCALE_X];
        float absoluteDiameterPixelNum = 200 / accumulatedScaleRatio;

        float tan = (float)Math.tan(Math.toRadians(viewAngle/2.0f));
        float totalLength = tan * (eye_camera_distance*1000) * 2.0f;
        float ratio = absoluteDiameterPixelNum / picture.getHeight();
        float pupilDiameter = totalLength * ratio; // mm
        double estimatedViewAngle = Math.toDegrees(Math.atan(tan*(10.0f/pupilDiameter)))*2.0;

        Log.d("debugging_findingPupil", "estimatedViewAngle : " + estimatedViewAngle);

        Bitmap drawingCache = getDrawingCache(imageView_findingPupil);
        Rect pupilRect = new Rect(drawingCache.getWidth()/2 - 100, drawingCache.getHeight()/2 -100 , 200, 200);
        Bitmap pupilBitmap = Bitmap.createBitmap(drawingCache, pupilRect.x, pupilRect.y, pupilRect.width, pupilRect.height);

        Intent intent = new Intent(getApplicationContext(), AnalyzingCrescentActivity.class);
        intent.putExtra("pupilBitmap", pupilBitmap);
        intent.putExtra("pupilDiameter", pupilDiameter);
        intent.putExtra("eye_camera_distance", eye_camera_distance);
        startActivity(intent);
    }

    private class ImageReceiver extends AsyncTask<Void, Integer, Bitmap> {
        private ProgressDialog progressDialog;

        ImageReceiver(Context context) {
            progressDialog = new ProgressDialog(context);

            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setTitle("receiving");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(final DialogInterface dialog) {
                    bluetoothControl.sendMessage("cancel", null);
                    bluetoothControl.setReceivingImage(false);
                    cancel(true);
                    dialog.dismiss();

                    finish();
                }
            });
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            bluetoothControl.setOnProgressListener(new BluetoothControl.OnProgressListener() {
                @Override
                public void onProgress(int progress) {
                    publishProgress(progress);
                }
            });

            return bluetoothControl.receiveImage();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            progressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            progressDialog.dismiss();
            bluetoothControl.setReceivingImage(false);

            if ( bitmap == null ) {
                Toast.makeText(getApplicationContext(), "기기와 연결되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            if ( isCancelled() ) {
                Log.d("debugging_showPicture", "canceled");
                finish();
                return;
            }

            picture = ImageUtil.getRotatedBitmap(bitmap, -90);
            imageView_findingPupil.setImageBitmap(picture);
            zoomToCenter(picture);
            imageView_findingPupil.setOnTouchListener(new FindingPupilOnTouchListener());
        }
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

            return (float)Math.sqrt(x * x + y * y);
        }

        private void middlePointBetweenTwoFingers(PointF point, MotionEvent event) {
            float x = event.getX(0) + event.getX(1);
            float y = event.getY(0) + event.getY(1);
            point.set(x / 2, y / 2);
        }
    }

    public static class FindingPupilImageView extends ImageView {
        Paint paint = new Paint();
        private static boolean isDrawing;

        public FindingPupilImageView(Context context) {
            this(context, null);
        }
        public FindingPupilImageView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }
        public FindingPupilImageView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if( isDrawing ) {
                paint.setColor(Color.WHITE);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(getWidth() / 2, getHeight() / 2, 100, paint);
            }
        }

        public void setDrawing(boolean _isDrawing) {
            isDrawing = _isDrawing;
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