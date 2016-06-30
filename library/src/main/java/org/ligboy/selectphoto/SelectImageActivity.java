package org.ligboy.selectphoto;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Window;
import android.widget.ArrayAdapter;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageActivity;
import com.theartofdev.edmodo.cropper.CropImageOptions;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

/**
 * SelectImageActivity
 */
public class SelectImageActivity extends AppCompatActivity
        implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {

    public static final int REQUEST_CODE_SELECT_IMAGE = 1435;
    public static final int RESULT_ERROR = 404;

    public static final String EXTRA_ERROR = "error";
    /**
     * The file suffix type of image.
     */
    public static final String EXTRA_IMAGE_TYPE = "image_type";
    /**
     * The title of dialog
     */
    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_CROP = "crop";
    private static final String EXTRA_CROP_OPTIONS = "crop_options";

    private static final String SAVE_CAPTURE_URI = "capture_uri";
    private static final String SAVE_TITLE = "title";
    private static final String SAVE_IMAGE_TYPE = "image_type";
    private static final String SAVE_CROP = "crop";
    private static final String SAVE_CROP_OPTIONS = "crop_options";

    private static final int REQUEST_CODE_IMAGE_CAPTURE = 1675;
    private static final int REQUEST_CODE_IMAGE_PICK = 1676;
    private static final String CACHE_DIR = "_crop";

    private Uri mCaptureUri;
    private String mTitle;
    private CropImageOptions mOptions;
    private String mImageType = ImageTypeUtil.TYPE_UNKNOWN;
    private boolean mCrop;

    private String mAuthorities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuthorities = getString(R.string.sp_provider_authorities);

        if (TextUtils.isEmpty(mAuthorities)) {
            throw new Error("@string/sp_provider_authorities must be override.");
        }

        getTheme().applyStyle(R.style.SelectImageTransparent, true);
        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.getDecorView().setBackgroundColor(Color.TRANSPARENT);
        window.getDecorView().setWillNotDraw(true);

        if (savedInstanceState != null) {
            mCaptureUri = savedInstanceState.getParcelable(SAVE_CAPTURE_URI);
            mTitle = savedInstanceState.getString(SAVE_TITLE);
            mCrop = savedInstanceState.getBoolean(SAVE_CROP, false);
            mOptions = savedInstanceState.getParcelable(SAVE_CROP_OPTIONS);
        } else {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                mTitle = extras.getString(EXTRA_TITLE);
                mCrop = extras.getBoolean(EXTRA_CROP, false);
                mOptions = extras.getParcelable(EXTRA_CROP_OPTIONS);
            }
        }

        if (mCrop && mOptions == null) {
            throw new RuntimeException("mOptions can't be null.");
        }

        String[] strings = new String[]{getString(R.string.sp_tack_photo), getString(R.string.sp_albums)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.sp_list_item_choose_image, strings);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (TextUtils.isEmpty(mTitle)) {
            builder.setTitle(getString(R.string.sp_select_photo));
        } else {
            builder.setTitle(mTitle);
        }
        builder.setAdapter(adapter, this);
        builder.setOnCancelListener(this);
        AlertDialog mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case 0:
                capture();
                break;
            case 1:
                pickImage();
                break;
        }
        dialog.dismiss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_CAPTURE_URI, mCaptureUri);
        outState.putString(SAVE_TITLE, mTitle);
        outState.putString(SAVE_IMAGE_TYPE, mImageType);
        outState.putBoolean(SAVE_CROP, mCrop);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_IMAGE_PICK:
                    if (data.getData() != null) {
                        try {
                            mImageType = ImageTypeUtil.detectType(this, data.getData());
                        } catch (IOException ignored) {
                        }
                        if (mCrop) {
                            crop(data.getData());
                        } else {
                            setSuccess(data.getData());
                        }
                        return;
                    }
                    break;
                case REQUEST_CODE_IMAGE_CAPTURE:
                    if (mCaptureUri != null) {
                        try {
                            mImageType = ImageTypeUtil.detectType(this, mCaptureUri);
                        } catch (IOException ignored) {
                        }

                        if (mCrop) {
                            crop(mCaptureUri);
                        } else {
                            setSuccess(mCaptureUri);
                        }
                        return;
                    }
                    break;
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    setSuccess(result.getUri(), ImageTypeUtil.TYPE_JPG);
                    return;
            }
            setError();
        } else if (resultCode == Activity.RESULT_CANCELED) {
            setResult(RESULT_CANCELED);
            finish();
        } else if (CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE == resultCode){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            Intent intent = new Intent();
            //noinspection ThrowableResultOfMethodCallIgnored
            intent.putExtra(EXTRA_ERROR, result.getError());
            setResult(RESULT_ERROR, intent);
            finish();
        }
    }

    private void setError() {
        setResult(RESULT_ERROR);
        finish();
    }

    private void setSuccess(Uri output) {
        setSuccess(output, mImageType);
    }

    private void setSuccess(Uri output, String type) {
        Intent intent = new Intent();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
        intent.putExtra(EXTRA_IMAGE_TYPE, type);
        intent.setData(output);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private boolean capture() {
        File file = ContextUtil.createTempFile(this, "image-capture-", null, CACHE_DIR);
        if (file != null && file.canWrite()) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            mCaptureUri = FileProvider.getUriForFile(this, mAuthorities, file);
            ContextUtil.grantUriPermissionToIntent(this, intent, mCaptureUri);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, mCaptureUri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_CODE_IMAGE_CAPTURE);
            }
        } else {
            setError();
            return true;
        }
        return false;
    }

    private void crop(@NonNull Uri uri) {
        File file = ContextUtil.createTempFile(this, "photo", ".jpg", CACHE_DIR);
        Intent intent = new Intent();
        intent.setClass(this, CropImageActivity.class);
        intent.putExtra(CropImage.CROP_IMAGE_EXTRA_SOURCE, uri);
        mOptions.outputUri = Uri.fromFile(file);
        intent.putExtra(CropImage.CROP_IMAGE_EXTRA_OPTIONS, mOptions);
        startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);

    }

    private void pickImage() {
        Intent intent;
        //if version >= KITKAT && has NO read external storage permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        }
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        Intent i = Intent.createChooser(intent, null);
        startActivityForResult(i, REQUEST_CODE_IMAGE_PICK);
    }

    /**
     * Create a builder for SelectImageActivity
     * @return Builder
     */
    public static  Builder builder() {
        return new Builder();
    }

    /**
     * Create a builder for SelectImageActivity with a intent.
     * @param intent Intent which will be merged.
     * @return Builder
     */
    public static  Builder builder(Intent intent) {
        return new Builder(intent);
    }

    public static class Builder {

        private Intent mIntent;

        /**
         * Options for image crop UX
         */
        private final CropImageOptions mOptions;

        public Builder(Intent intent) {
            mIntent = new Intent(intent);
            mOptions = new CropImageOptions();
        }

        public Builder() {
            mIntent = new Intent();
            mOptions = new CropImageOptions();
        }

        /**
         * Set the title of the selector's dialog
         * @param title title
         */
        public Builder setTitle(String title) {
            mIntent.putExtra(EXTRA_TITLE, title);
            return this;
        }

        /**
         * Set the aspect ratio for cropping
         * @param aspectX aspect x
         * @param aspectY aspect y
         */
        public Builder withAspect(int aspectX, int aspectY) {
            setAspectRatio(aspectX, aspectY);
            return this;
        }

        /**
         * Crop picture with fixed 1:1 aspect ratio.
         */
        public Builder asSquare() {
            setAspectRatio(1, 1);
            return this;
        }

        /**
         * Set the maximum size.
         * @param width Max width
         * @param height Max height
         */
        public Builder withMaxSize(int width, int height) {
            setMaxCropResultSize(width, height);
            return this;
        }

        /**
         * Set enable/disable crop function.
         * <p>
         * @param enable Is enable the Crop function. Default false.
         */
        public Builder withCrop(boolean enable) {
            mIntent.putExtra(EXTRA_CROP, enable);
            return this;
        }



        /**
         * The shape of the cropping window.<br>
         * <i>Default: RECTANGLE</i>
         */
        public Builder setCropShape(@NonNull CropImageView.CropShape cropShape) {
            mOptions.cropShape = cropShape;
            return this;
        }

        /**
         * An edge of the crop window will snap to the corresponding edge of a specified bounding
         * box when the crop window edge is less than or equal to this distance (in pixels) away
         * from the bounding box edge (in pixels).<br>
         * <i>Default: 3dp</i>
         */
        public Builder setSnapRadius(float snapRadius) {
            mOptions.snapRadius = snapRadius;
            return this;
        }

        /**
         * The radius of the touchable area around the handle (in pixels).<br>
         * We are basing this value off of the recommended 48dp Rhythm.<br>
         * See: http://developer.android.com/design/style/metrics-grids.html#48dp-rhythm<br>
         * <i>Default: 48dp</i>
         */
        public Builder setTouchRadius(float touchRadius) {
            mOptions.touchRadius = touchRadius;
            return this;
        }

        /**
         * whether the guidelines should be on, off, or only showing when resizing.<br>
         * <i>Default: ON_TOUCH</i>
         */
        public Builder setGuidelines(@NonNull CropImageView.Guidelines guidelines) {
            mOptions.guidelines = guidelines;
            return this;
        }

        /**
         * The initial scale type of the image in the crop image view<br>
         * <i>Default: FIT_CENTER</i>
         */
        public Builder setScaleType(@NonNull CropImageView.ScaleType scaleType) {
            mOptions.scaleType = scaleType;
            return this;
        }

        /**
         * if to show crop overlay UI what contains the crop window UI surrounded by background
         * over the cropping image.<br>
         * <i>default: true, may disable for animation or frame transition.</i>
         */
        public Builder setShowCropOverlay(boolean showCropOverlay) {
            mOptions.showCropOverlay = showCropOverlay;
            return this;
        }

        /**
         * if auto-zoom functionality is enabled.<br>
         * default: true.
         */
        public Builder setAutoZoomEnabled(boolean autoZoomEnabled) {
            mOptions.autoZoomEnabled = autoZoomEnabled;
            return this;
        }

        /**
         * The max zoom allowed during cropping.<br>
         * <i>Default: 4</i>
         */
        public Builder setMaxZoom(int maxZoom) {
            mOptions.maxZoom = maxZoom;
            return this;
        }

        /**
         * The initial crop window padding from image borders in percentage of the cropping image
         * dimensions.<br>
         * <i>Default: 0.1</i>
         */
        public Builder setInitialCropWindowPaddingRatio(float initialCropWindowPaddingRatio) {
            mOptions.initialCropWindowPaddingRatio = initialCropWindowPaddingRatio;
            return this;
        }

        /**
         * whether the width to height aspect ratio should be maintained or free to change.<br>
         * <i>Default: false</i>
         */
        public Builder setFixAspectRatio(boolean fixAspectRatio) {
            mOptions.fixAspectRatio = fixAspectRatio;
            return this;
        }

        /**
         * the X,Y value of the aspect ratio.<br>
         * <i>Default: 1/1</i>
         */
        public Builder setAspectRatio(int aspectRatioX, int aspectRatioY) {
            mOptions.aspectRatioX = aspectRatioX;
            mOptions.aspectRatioY = aspectRatioY;
            return this;
        }

        /**
         * the thickness of the guidelines lines (in pixels).<br>
         * <i>Default: 3dp</i>
         */
        public Builder setBorderLineThickness(float borderLineThickness) {
            mOptions.borderLineThickness = borderLineThickness;
            return this;
        }

        /**
         * the color of the guidelines lines.<br>
         * <i>Default: Color.argb(170, 255, 255, 255)</i>
         */
        public Builder setBorderLineColor(int borderLineColor) {
            mOptions.borderLineColor = borderLineColor;
            return this;
        }

        /**
         * thickness of the corner line (in pixels).<br>
         * <i>Default: 2dp</i>
         */
        public Builder setBorderCornerThickness(float borderCornerThickness) {
            mOptions.borderCornerThickness = borderCornerThickness;
            return this;
        }

        /**
         * the offset of corner line from crop window border (in pixels).<br>
         * <i>Default: 5dp</i>
         */
        public Builder setBorderCornerOffset(float borderCornerOffset) {
            mOptions.borderCornerOffset = borderCornerOffset;
            return this;
        }

        /**
         * the length of the corner line away from the corner (in pixels).<br>
         * <i>Default: 14dp</i>
         */
        public Builder setBorderCornerLength(float borderCornerLength) {
            mOptions.borderCornerLength = borderCornerLength;
            return this;
        }

        /**
         * the color of the corner line.<br>
         * <i>Default: WHITE</i>
         */
        public Builder setBorderCornerColor(int borderCornerColor) {
            mOptions.borderCornerColor = borderCornerColor;
            return this;
        }

        /**
         * the thickness of the guidelines lines (in pixels).<br>
         * <i>Default: 1dp</i>
         */
        public Builder setGuidelinesThickness(float guidelinesThickness) {
            mOptions.guidelinesThickness = guidelinesThickness;
            return this;
        }

        /**
         * the color of the guidelines lines.<br>
         * <i>Default: Color.argb(170, 255, 255, 255)</i>
         */
        public Builder setGuidelinesColor(int guidelinesColor) {
            mOptions.guidelinesColor = guidelinesColor;
            return this;
        }

        /**
         * the color of the overlay background around the crop window cover the image parts not in
         * the crop window.<br>
         * <i>Default: Color.argb(119, 0, 0, 0)</i>
         */
        public Builder setBackgroundColor(int backgroundColor) {
            mOptions.backgroundColor = backgroundColor;
            return this;
        }

        /**
         * the min size the crop window is allowed to be (in pixels).<br>
         * <i>Default: 42dp, 42dp</i>
         */
        public Builder setMinCropWindowSize(int minCropWindowWidth, int minCropWindowHeight) {
            mOptions.minCropWindowWidth = minCropWindowWidth;
            mOptions.minCropWindowHeight = minCropWindowHeight;
            return this;
        }

        /**
         * the min size the resulting cropping image is allowed to be, affects the cropping window
         * limits (in pixels).<br>
         * <i>Default: 40px, 40px</i>
         */
        public Builder setMinCropResultSize(int minCropResultWidth, int minCropResultHeight) {
            mOptions.minCropResultWidth = minCropResultWidth;
            mOptions.minCropResultHeight = minCropResultHeight;
            return this;
        }

        /**
         * the max size the resulting cropping image is allowed to be, affects the cropping window
         * limits (in pixels).<br>
         * <i>Default: 99999, 99999</i>
         */
        public Builder setMaxCropResultSize(int maxCropResultWidth, int maxCropResultHeight) {
            mOptions.maxCropResultWidth = maxCropResultWidth;
            mOptions.maxCropResultHeight = maxCropResultHeight;
            return this;
        }

        /**
         * the title of the {@link CropImageActivity}.<br>
         * <i>Default: ""</i>
         */
        public Builder setActivityTitle(String activityTitle) {
            mOptions.activityTitle = activityTitle;
            return this;
        }

        /**
         * the color to use for action bar items icons.<br>
         * <i>Default: NONE</i>
         */
        public Builder setActivityMenuIconColor(int activityMenuIconColor) {
            mOptions.activityMenuIconColor = activityMenuIconColor;
            return this;
        }

        /**
         * the compression format to use when writting the image.<br>
         * <i>Default: JPEG</i>
         */
        public Builder setOutputCompressFormat(Bitmap.CompressFormat outputCompressFormat) {
            mOptions.outputCompressFormat = outputCompressFormat;
            return this;
        }

        /**
         * the quility (if applicable) to use when writting the image (0 - 100).<br>
         * <i>Default: 90</i>
         */
        public Builder setOutputCompressQuality(int outputCompressQuality) {
            mOptions.outputCompressQuality = outputCompressQuality;
            return this;
        }

        /**
         * the size to downsample the cropped image to.<br>
         * NOTE: resulting image will not be exactly (reqWidth, reqHeight)
         * see: <a href="http://developer.android.com/training/displaying-bitmaps/load-bitmap.html"
         * >Loading Large Bitmaps Efficiently</a><br>
         * <i>Default: 0, 0 - not set, will not downsample</i>
         */
        public Builder setRequestedSize(int reqWidth, int reqHeight) {
            mOptions.outputRequestWidth = reqWidth;
            mOptions.outputRequestHeight = reqHeight;
            return this;
        }

        /**
         * if the result of crop image activity should not save the cropped image bitmap.<br>
         * Used if you want to crop the image manually and need only the crop rectangle and rotati
         * on data.<br> <i>Default: false</i>
         */
        public Builder setNoOutputImage(boolean noOutputImage) {
            mOptions.noOutputImage = noOutputImage;
            return this;
        }

        /**
         * the initial rectangle to set on the cropping image after loading.<br>
         * <i>Default: NONE - will initialize using initial crop window padding ratio</i>
         */
        public Builder setInitialCropWindowRectangle(Rect initialCropWindowRectangle) {
            mOptions.initialCropWindowRectangle = initialCropWindowRectangle;
            return this;
        }

        /**
         * the initial rotation to set on the cropping image after loading (0-360 degrees clockwise
         * ).<br> <i>Default: NONE - will read image exif data</i>
         */
        public Builder setInitialRotation(int initialRotation) {
            mOptions.initialRotation = initialRotation;
            return this;
        }

        /**
         * if to allow rotation during cropping.<br>
         * <i>Default: true</i>
         */
        public Builder setAllowRotation(boolean allowRotation) {
            mOptions.allowRotation = allowRotation;
            return this;
        }

        /**
         * if to allow counter-clockwise rotation during cropping.<br>
         * Note: if rotation is disabled this option has no effect.<br>
         * <i>Default: false</i>
         */
        public Builder setAllowCounterRotation(boolean allowCounterRotation) {
            mOptions.allowCounterRotation = allowCounterRotation;
            return this;
        }

        /**
         * The amount of degreees to rotate clockwise or counter-clockwise (0-360).<br>
         * <i>Default: 90</i>
         */
        public Builder setRotationDegrees(int rotationDegrees) {
            mOptions.rotationDegrees = rotationDegrees;
            return this;
        }

        /**
         * Select a picture from a activity with a custom requestCode.
         * @param activity Activity to receive result
         * @param requestCode requestCode for result
         */
        public void start(Activity activity, int requestCode) {
            mOptions.validate();
            mIntent.setClass(activity, SelectImageActivity.class);
            mIntent.putExtra(EXTRA_CROP_OPTIONS, mOptions);
            activity.startActivityForResult(mIntent, requestCode);
        }

        /**
         * Select a picture from a activity with a custom requestCode.
         * @param activity Activity to receive result
         */
        public void start(Activity activity) {
            start(activity, REQUEST_CODE_SELECT_IMAGE);
        }

        /**
         * Select a picture from a {@link android.app.Fragment} with a custom requestCode.
         * @param context Context
         * @param fragment Fragment to receive result
         * @param requestCode requestCode for result
         */
        public void start(Context context, android.support.v4.app.Fragment fragment,
                          int requestCode) {
            mOptions.validate();
            mIntent.setClass(context, SelectImageActivity.class);
            mIntent.putExtra(EXTRA_CROP_OPTIONS, mOptions);
            fragment.startActivityForResult(mIntent, requestCode);
        }

        /**
         * Select a picture from a {@link android.support.v4.app.Fragment}
         * @param context Context
         * @param fragment Fragment to receive result
         */
        public void start(Context context, android.support.v4.app.Fragment fragment) {
            start(context, fragment, REQUEST_CODE_SELECT_IMAGE);
        }

        /**
         * Select a picture from a {@link android.support.v4.app.Fragment} with a custom
         * requestCode.
         * @param context Context
         * @param fragment Fragment to receive result
         * @param requestCode requestCode for result
         */
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public void start(Context context, android.app.Fragment fragment, int requestCode) {
            mOptions.validate();
            mIntent.setClass(context, SelectImageActivity.class);
            mIntent.putExtra(EXTRA_CROP_OPTIONS, mOptions);
            fragment.startActivityForResult(mIntent, requestCode);
        }

        /**
         * Select a picture from a {@link android.app.Fragment}
         * @param context Context
         * @param fragment Fragment to receive result
         */
        public void start(Context context, android.app.Fragment fragment) {
            start(context, fragment, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}
