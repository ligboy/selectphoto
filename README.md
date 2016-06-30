# selectphoto
[![Build Status](https://travis-ci.org/ligboy/selectphoto.svg?branch=master)](https://travis-ci.org/ligboy/selectphoto)
[![Download](https://api.bintray.com/packages/ligboy/maven/select-photo/images/download.svg)](https://bintray.com/ligboy/maven/select-photo/_latestVersion)
Select a photo activity for android by stock camera or photos

### Download
This library already included in JCenter & MavenCentral
#### Gradle:
```groovy
compile 'org.ligboy.android:selectphoto:1.0.0'
```

### Usage

1. Define String res: `@string/sp_provider_authorities`, This is the authorities of the FileProvider for capture picture.

2. Define a Theme:
```xml
    <style name="AppTheme.Transparent">
        <item name="android:windowIsTranslucent">true</item>
    </style>
```
3. Add `SelectImageActivity` to `AndroidManifest.xml`
```xml
    <activity android:name="org.ligboy.selectphoto.SelectImageActivity"
        android:theme="@style/AppTheme.Transparent"/>
```
4. Let's start it

```java
    SelectImageActivity.builder()
            .asSquare() // with aspect ratio: 1:1
            .withCrop(true) //with crop function
            .start(MainActivity.this);
```
OnActivityResult:
```java
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SelectImageActivity.REQUEST_CODE_SELECT_IMAGE
                && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData(); //The image uri.
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
```

