function CameraMultiple() {}

CameraMultiple.prototype.takePicture = function(successCallback, errorCallback) {
  var options = {};
  cordova.exec(successCallback, errorCallback, 'ToastyPlugin', 'show', [options]);
}

CameraMultiple.install = function() {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.cameraMultiple = new CameraMultiple();
  return window.plugins.cameraMultiple;
};
cordova.addConstructor(CameraMultiple.install);