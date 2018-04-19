package multiple_camera_shoot.cordova.multiple_camera_shoot;

import android.content.Context;
import android.content.Intent;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CameraMultiple extends CordovaPlugin {

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Context context = cordova.getActivity().getApplicationContext();
        if(action.equals("takePicture")) {
            this.openCamActivity(context);
            return true;
        }
        return false;
    }

    private void openCamActivity(Context context) {
        Intent intent = new Intent(context, CamActivity.class);
        this.cordova.getActivity().startActivity(intent);
    }
}