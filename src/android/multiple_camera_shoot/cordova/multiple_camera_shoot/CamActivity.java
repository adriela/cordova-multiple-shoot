package multiple_camera_shoot.cordova.multiple_camera_shoot;

import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import org.json.JSONArray;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ubitik.scoplan.app.R;

public class CamActivity extends Fragment {
    public interface CameraPreviewListener {
        void onUserFinish();
    }
    public static int ORIENTATION = -1;
    public JSONArray pictures;
    private View cameraPreview;
    private static final String TAG = "cameraPreview";
    private static int image_width;
    public Camera mCamera;
    private CameraPreview mPreview;
    private OrientationEventListener orientationEventListener;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                addPhoto(pictureFile.getAbsolutePath());
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };
    private CameraPreviewListener eventListener;
    public void setEventListener(CameraPreviewListener listener){
        eventListener = listener;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        pictures = new JSONArray();
        image_width = getArguments().getInt("image_width");
        // Inflate the layout for this fragment
        cameraPreview =  inflater.inflate(R.layout.activity_cam, container, false);
        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(cameraPreview.getContext(), mCamera);
        FrameLayout preview = (FrameLayout) cameraPreview.findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        Button captureButton = (Button) cameraPreview.findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        mCamera.takePicture(null, null, mPicture);
                    }
                }
        );
        ImageButton closeBtn = (ImageButton) cameraPreview.findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        mCamera.stopPreview();
                        eventListener.onUserFinish();
                    }
                }
        );
        orientationEventListener = new OrientationEventListener(cameraPreview.getContext()) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation == ORIENTATION_UNKNOWN) return;
                android.hardware.Camera.CameraInfo info =
                        new android.hardware.Camera.CameraInfo();
                android.hardware.Camera.getCameraInfo(0, info);
                orientation = (orientation + 45) / 90 * 90;
                int rotation = 0;
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    rotation = (info.orientation - orientation + 360) % 360;
                } else {  // back-facing camera
                    rotation = (info.orientation + orientation) % 360;
                }
                Camera.Parameters mParameters = mCamera.getParameters();
                mParameters.setRotation(rotation);
                mCamera.setParameters(mParameters);
                mCamera.u
            }
        };
        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable();
        }
        return cameraPreview;
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(0); // attempt to get a Camera instance
            Camera.Parameters params = c.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            List<Camera.Size> camSizes = params.getSupportedPreviewSizes();
            Camera.Size optimumSize = camSizes.get(0);
            for (int i = 1; i < camSizes.size(); i++) {
                if(optimumSize.width*optimumSize.height < camSizes.get(i).width*camSizes.get(i).height)
                    optimumSize = camSizes.get(i);
            }
            params.setPreviewSize(optimumSize.width,optimumSize.height);
            c.setParameters(params);
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.d(TAG, e.getMessage());
        }
        return c; // returns null if camera is unavailable
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;


    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    private void addPhoto(String url){
        pictures.put(url);
        final LinearLayout lin = (LinearLayout)cameraPreview.findViewById(R.id.image_bar);
        LayoutInflater inflater = LayoutInflater.from(cameraPreview.getContext());
        View imageFrame = inflater.inflate(R.layout.image_layout, null);
        ImageButton im = imageFrame.findViewById(R.id.image_souche);
        im.getLayoutParams().width = image_width/3;
        im.getLayoutParams().height = image_width/3;
        im.requestLayout();
        Bitmap myBitmap = BitmapFactory.decodeFile(url);
        im.setImageBitmap(myBitmap);
        lin.addView(imageFrame);
        ViewTreeObserver vto = lin.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                ///getLocationOnScreen here
                HorizontalScrollView hs = (HorizontalScrollView)cameraPreview.findViewById(R.id.hs);
                hs.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                ViewTreeObserver obs = lin.getViewTreeObserver();
                obs.removeGlobalOnLayoutListener(this);
            }
        });
        mCamera.startPreview();
    }
}
