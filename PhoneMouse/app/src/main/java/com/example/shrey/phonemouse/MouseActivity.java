package com.example.shrey.phonemouse;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.widget.Button;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.opencv.highgui.Highgui.IMREAD_GRAYSCALE;

public class MouseActivity extends AppCompatActivity {


    public static final int LEFT_PRESS = 1;
    public static final int LEFT_RELEASE = 3;
    public static final int RIGHT_PRESS = 2;
    public static final int RIGHT_RELEASE = 4;

    private double[] mVelocity;
    private Queue<Integer> mActionQueue;

    private SocketTask mSocketTask;
    private BluetoothDevice mBluetoothDevice;

    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private ImageReader mImageReader;


    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest mPreviewRequest;

    private CanvasView mCanvasView;

    private Button mLeftMouseButton;
    private Button mRightMouseButton;

    private Mat mLastImageMat;

    private long mLastTime;

    private Range<Integer> mMaxFps;

    // A Semaphore to prevent the app from exiting before closing the camera.
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);


    //init opencv
    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d("OPENCV", "FAILURE");
        } else {
            Log.d("OPENCV", "SUCCESS");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mouse);

        Intent intent = getIntent();

        mBluetoothDevice = (BluetoothDevice) intent.getParcelableExtra("BLUETOOTH_DEVICE");

        mVelocity = new double[2];

        mLeftMouseButton = (Button) findViewById(R.id.left_mouse_button);
        mRightMouseButton = (Button) findViewById(R.id.right_mouse_button);
        mCanvasView = (CanvasView) findViewById(R.id.canvas_view);

        mLeftMouseButton.setOnTouchListener(leftMouseButtonTouch);
        mRightMouseButton.setOnTouchListener(rightMouseButtonTouch);

        mActionQueue = new LinkedList<>();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Arrays.fill(mVelocity, 0.0);
        mActionQueue.clear();

        //setup socket
        mSocketTask = new SocketTask(mVelocity, mActionQueue, mBluetoothDevice);
        mSocketTask.execute();

        startBackgroundThread();
        openCamera();
    }

    @Override
    protected void onPause() {
        closeCamera();
        mSocketTask.cancel(true);
        stopBackgroundThread();
        super.onPause();
    }

    //https://github.com/googlesamples/android-Camera2Basic
    private void openCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager
                        .getCameraCharacteristics(cameraId);
                Integer facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);

                //camera is facing backwards
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    StreamConfigurationMap map = cameraCharacteristics
                            .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                    //get smallest size
                    Size minSize = Collections.min(
                            Arrays.asList(map.getOutputSizes(ImageReader.class)),
                            new Comparator<Size>() {
                                @Override
                                public int compare(Size o1, Size o2) {
                                    return Long.signum((long) o1.getWidth() * o1.getHeight() -
                                            (long) o2.getWidth() * o2.getHeight());
                                }
                            });

                    Range<Integer>[] fpsRanges = cameraCharacteristics
                            .get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);


                    //get fastest preview fps
                    mMaxFps = Collections.max(Arrays.asList(fpsRanges),
                            new Comparator<Range<Integer>>() {
                                @Override
                                public int compare(Range<Integer> o1, Range<Integer> o2) {
                                    return Integer.compare(o1.getLower(), o2.getLower());
                                }
                            });

                    //make sure you have permissions
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                            PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                                this,
                                new String[]{Manifest.permission.CAMERA},
                                200);
                        return;
                    }

                    mImageReader = ImageReader.newInstance(minSize.getWidth(), minSize.getHeight(),
                            ImageFormat.JPEG, 2);

                    mImageReader.setOnImageAvailableListener(imageAvailable, mBackgroundHandler);

                    if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                        throw new RuntimeException("Time out waiting to lock camera opening.");
                    }
                    cameraManager.openCamera(cameraId, cameraStateCallback, mBackgroundHandler);
                    break;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void createCameraSession() {
        try {
            mPreviewRequestBuilder
                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            Surface surface = mImageReader.getSurface();
            mPreviewRequestBuilder.addTarget(surface);

            mCameraDevice.createCaptureSession(Arrays.asList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            mCaptureSession = session;

                            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                    CaptureRequest.CONTROL_MODE_OFF);
                            mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE,
                                    CaptureRequest.FLASH_MODE_TORCH);
                            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,
                                    mMaxFps);

                            // Finally, we start displaying the camera preview.
                            mPreviewRequest = mPreviewRequestBuilder.build();
                            try {
                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        null, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                        }
                    }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    ImageReader.OnImageAvailableListener imageAvailable =
            new ImageReader.OnImageAvailableListener() {

                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = reader.acquireLatestImage();
                    if (image == null) {
                        return;
                    }

                    long time = System.nanoTime();
                    Log.d("FPS", 1000000000.0 / (time - mLastTime) + "");

                    Mat buf = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC1);

                    //convert image to mat for phase correlate
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    buf.put(0, 0, bytes);

                    Mat mat = Highgui.imdecode(buf, IMREAD_GRAYSCALE);
                    Mat floatMat = new Mat(mat.rows(), mat.cols(), CvType.CV_32FC1);
                    mat.convertTo(floatMat, CvType.CV_32FC1);


                    if (mLastImageMat != null) {
                        //get how far image moved
                        Point point = Imgproc.phaseCorrelate(floatMat, mLastImageMat);

                        //switching vals because matricies are y then x
                        mVelocity[0] = -point.y * ((time - mLastTime) / 1000000000.0);
                        mVelocity[1] = point.x * ((time - mLastTime) / 1000000000.0);
                        mCanvasView.updatePos(mVelocity[0], mVelocity[1]);
                    }

                    mLastImageMat = floatMat;

                    image.close();
                    mLastTime = time;
                }
            };

    private CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraOpenCloseLock.release();
            mCameraDevice = camera;
            createCameraSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mCameraOpenCloseLock.release();
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            mCameraOpenCloseLock.release();
            camera.close();
            mCameraDevice = null;

        }
    };

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    private View.OnTouchListener leftMouseButtonTouch = new View.OnTouchListener() {

        //listen for button up and down
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mActionQueue.add(LEFT_PRESS);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                mActionQueue.add(LEFT_RELEASE);
            }
            return true;
        }
    };

    private View.OnTouchListener rightMouseButtonTouch = new View.OnTouchListener() {

        //listen for button up and down
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mActionQueue.add(RIGHT_PRESS);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                mActionQueue.add(RIGHT_RELEASE);
            }
            return true;
        }
    };

}
