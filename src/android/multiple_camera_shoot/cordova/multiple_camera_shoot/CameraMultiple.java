package multiple_camera_shoot.cordova.multiple_camera_shoot;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.widget.FrameLayout;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

public class CameraMultiple extends CordovaPlugin implements CamActivity.CameraPreviewListener{
    private int containerViewId = 505;
    private CamActivity camActivity;
    private CallbackContext camCallback;
    private PluginResult r;
    public static int ORIENTATION = 0;
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        camCallback = callbackContext;
        if(r == null)
            r = new PluginResult(PluginResult.Status.NO_RESULT, "camera stared...");
        try {
            if(action.equals("takePicture")) {
                Log.d("titi", "titi");
                this.openCamActivity();
            }
        }catch (Exception ex){
            callbackContext.error("une erreur est survenue : " + ex.getMessage());
            return true;
        }
        Log.d("toto", "toto");
        r.setKeepCallback(true);
        callbackContext.sendPluginResult(r);
        return true;
    }

    private void openCamActivity() {
        camActivity = new CamActivity();
        Bundle bundle = new Bundle();
        Display display = cordova.getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        bundle.putInt("image_width",size.x );
        switch (display.getRotation()) {
            case Surface.ROTATION_0: ORIENTATION = 0; break;
            case Surface.ROTATION_90: ORIENTATION = 90; break;
            case Surface.ROTATION_180: ORIENTATION = 180; break;
            case Surface.ROTATION_270: ORIENTATION = 270; break;
        }
        camActivity.setArguments(bundle);
        camActivity.setEventListener(this);
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FrameLayout containerView = (FrameLayout)cordova.getActivity().findViewById(containerViewId);
                if(containerView == null){
                    containerView = new FrameLayout(cordova.getActivity().getApplicationContext());
                    containerView.setId(containerViewId);
                    FrameLayout.LayoutParams containerLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                    cordova.getActivity().addContentView(containerView, containerLayoutParams);
                    //add the fragment to the container
                }
                FragmentManager fragmentManager = cordova.getActivity().getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction = fragmentTransaction.add(containerViewId,camActivity);
                fragmentTransaction.commit();
            }
        });
    }

    private boolean closeCamera() {
        camActivity.mCamera.release();
        camActivity.mCamera = null;
        FragmentManager fragmentManager = cordova.getActivity().getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(camActivity);
        fragmentTransaction.commit();
        return true;
    }

    @Override
    public void onUserFinish() {
        r = new PluginResult(PluginResult.Status.OK, camActivity.pictures);
        camCallback.sendPluginResult(r);
        closeCamera();
    }
}