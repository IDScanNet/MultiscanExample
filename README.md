# Multiscan Library

## Setup

1. Add **idscan-public** maven repository to the project **build.gradle** file.
```
allprojects {
    repositories {
        ...
        maven {
            url 'https://www.myget.org/F/idscan-public/maven/'
        }
        ...
    }
}
```

2. Add the following to the module **build.gradle** file:
```
dependencies {
    ...
    implementation 'net.idscan.components.android:multiscan:1.0.0'
    ...
}
```

3. Add one or more dependencies of components to the **build.gradle** file:
```
dependencies {
    ...
    // For MRZ recognition.
    implementation 'net.idscan.components.android:multiscan-mrz:1.0.0'

    // For PDF417 recognition
    implementation 'net.idscan.components.android:multiscan-pdf417:1.0.0'
    ...
}
```
**Note** you need component dependency only if you use the component in the project.

## Using

For scanning you need setup and call ```MultiScanActivity```:

```
MultiScanActivity.build(this)

    // For PDF417 component.
    .withComponent(PDF417Component.build()
      .withLicenseKey("** PDF417 LICENSE KEY **")
      .complete())

    // For MRZ component.
    .withComponent(MRZComponent.build()
      .withLicenseKey("** MRZ LICENSE KEY **")
      .complete())

    .start(SCAN_ACTIVITY_CODE);
```
**Note** need to replace ```** PDF417 LICENSE KEY **``` by your **License Key** for PDF417 component and ```** MRZ LICENSE KEY **``` by your **License Key** for MRZ component.

**Note** you can use one or more components at the same time. If you use the component you have to add the appropriate component dependency to the module **build.gradle** file.

To process the result you need to override ```onActivityResult()``` of your Activity.

```
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  super.onActivityResult(requestCode, resultCode, data);

  if (requestCode == SCAN_ACTIVITY_CODE) {
    switch (resultCode) {
      case MultiScanActivity.RESULT_OK:
        if (data != null) {
          DocumentData document = (DocumentData) data.getSerializableExtra(MultiScanActivity.DOCUMENT_DATA);
          if (document != null) {
            MRZData mrzData = MRZComponent.extractDataFromDocument(document);
            PDF417Data pdf417Data = PDF417Component.extractDataFromDocument(document);

            if (mrzData != null) {
              // TODO: Handle MRZ data.
            }

            if (pdf417Data != null) {
              // TODO: Handle PDF417 data.
            }
          }
        }
        break;

      case MultiScanActivity.ERROR_RECOGNITION:
      case MultiScanActivity.ERROR_INVALID_CAMERA_NUMBER:
      case MultiScanActivity.ERROR_CAMERA_NOT_AVAILABLE:
      case MultiScanActivity.ERROR_INVALID_CAMERA_ACCESS:
      case MultiScanActivity.RESULT_CANCELED:
        // TODO: Handle the error.
        break;
    }
  }
}
```

#### Error codes:

* ```ERROR_RECOGNITION``` internal error.

* ```ERROR_CAMERA_NOT_AVAILABLE``` device has no camera.

* ```ERROR_INVALID_CAMERA_NUMBER``` invalid camera number is selected.

* ```ERROR_INVALID_CAMERA_ACCESS``` application cannot access the camera. For example, camera can be captured by the other application or application has no permission to use the camera.

## Customization

For customization **scanning activity** you need to extend ```MultiScanActivity``` and override some methods. Also you need to set this activity during setup.

```
MultiScanActivity.build(this)
  ...
  .withCustomActivity(CustomScanActivity.class)
  ...
  .start(SCAN_ACTIVITY_CODE);
```
**Note** replace ```CustomScanActivity.class``` by your custom Scanning Activity.

#### Custom Viewfinder

The **scanning activity** has the following structure:
![Structure of Scanning View](/images/scan_view_structure.png)

By default, **Viewfinder** layer is a simple view with a frame. You can replace it with a custom view. For that you need to override ```getViewFinder(LayoutInflater inflater)``` method. Also, you can add any views to **Viewfinder** layer.
```
@Override
protected View getViewFinder(LayoutInflater inflater) {
  View v = inflater.inflate(R.layout.custom_viewfinder, null);

  // TODO: setup view.

  return v;
}
```

**Note** **Viewfinder** layer is drawn as an overlay above the **camera preview** layer, so it should has a transparent background color.

#### Select camera

You have two ways to select active camera in the **scanning activity**:

1. You can override ```selectCamera(int numberOfCameras)``` method and return the number of desired camera.
```
@Override
protected int selectCamera(int numberOfCameras) {
    // TODO: Return number of camera in range [0, numberOfCameras).
}
```
2. You can call ```setCamera(int id)``` method to change the current active camera.


#### Handle scanned data

By default, when document is recognized it returns via ```onActivityResult``` method. But you can change this behavior by overriding ```onData(DocumentData result)``` method. That is default implementation of this method:
```
protected void onData(@NonNull DocumentData data) {
  this.finish(data);
}
```
But you can process scanned data in a different way. For example, you can display the document data on **Viewfiender** layer. Also you don't have to return the result immediately. Instead of, you can return scanned data at any time in future by calling ```void finish(DocumentData data)``` method.

#### Flashlight

You have two ways to control the flashlight:

1. You can set the state of the flashlight during setup by calling ```setFlashState(boolean state)``` method:
```
MultiScanActivity.build(this)
  ...
  .setFlashState(true)
  ...
  .start(SCAN_ACTIVITY_CODE);
```

2. You can change the state of the flashlight by calling ```setFlashState(booelan state)```.
```
public class CustomScanActivity extends MultiScanActivity {
    ...
    void switchFlashlight() {
        setFlashState(!getFlashState());
    }
    ...
}
```
