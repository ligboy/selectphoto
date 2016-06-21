package org.ligboy.selectphoto;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Window;
import android.widget.ArrayAdapter;

import com.soundcloud.android.crop.Crop;

import java.io.File;

/**
 * SelectImageActivity
 */
public class SelectImageActivity extends AppCompatActivity
        implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {
    private static final String SAVE_CAPTURE_URI = "capture_uri";
    private static final String SAVE_TITLE = "title";
    private static final String SAVE_ASPECT_X = "aspect_x";
    private static final String SAVE_ASPECT_Y = "aspect_y";
    private static final String SAVE_MAX_X = "max_x";
    private static final String SAVE_MAX_Y = "max_y";
    private static final String SAVE_IMAGE_TYPE = "image_type";

    private static final int REQUEST_CODE_IMAGE_CAPTURE = 1675;

    public static final int REQUEST_CODE_SELECT_IMAGE = 1435;
    public static final int RESULT_ERROR = 404;

    public static final String EXTRA_ERROR = "error";
    /**
     * The title of dialog
     */
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_ASPECT_X = "aspect_x";
    public static final String EXTRA_ASPECT_Y = "aspect_y";
    public static final String EXTRA_MAX_X = "max_x";
    public static final String EXTRA_MAX_Y = "max_y";

    /**
     * The file suffix type of image.
     */
    public static final String EXTRA_IMAGE_TYPE = "image_type";

    private Uri mCaptureUri;
    private String mTitle;
    private int mAspectX;
    private int mAspectY;
    private int mMaxX;
    private int mMaxY;
    private String mImageType = ImageUtil.TYPE_UNKNOWN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTheme().applyStyle(R.style.SelectImageTransparent, true);
        Window window = getWindow();
//        window.addFlags(Window.FEATURE_NO_TITLE);
//        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.getDecorView().setBackgroundColor(Color.TRANSPARENT);
        window.getDecorView().setWillNotDraw(true);
//        window.getDecorView().setVisibility(View.INVISIBLE);
        if (savedInstanceState != null) {
            mCaptureUri = savedInstanceState.getParcelable(SAVE_CAPTURE_URI);
            mTitle = savedInstanceState.getString(SAVE_TITLE);
            mAspectX = savedInstanceState.getInt(SAVE_ASPECT_X);
            mAspectY = savedInstanceState.getInt(SAVE_ASPECT_Y);
            mMaxX = savedInstanceState.getInt(SAVE_MAX_X);
            mMaxY = savedInstanceState.getInt(SAVE_MAX_Y);
        } else {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                mTitle = extras.getString(EXTRA_TITLE);
                mAspectX = extras.getInt(EXTRA_ASPECT_X);
                mAspectY = extras.getInt(EXTRA_ASPECT_Y);
                mMaxX = extras.getInt(EXTRA_MAX_X);
                mMaxY = extras.getInt(EXTRA_MAX_Y);
                mImageType = extras.getString(SAVE_IMAGE_TYPE);
                if (mImageType == null) {
                    mImageType = ImageUtil.TYPE_UNKNOWN;
                }
            }
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
                File file = ContextUtil.createTempFile(this, "image-capture-", null, "_crop");
                if (file != null && file.canWrite()) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    mCaptureUri = Uri.fromFile(file);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mCaptureUri);
                    startActivityForResult(intent, REQUEST_CODE_IMAGE_CAPTURE);
                } else {
                    setResult(RESULT_ERROR);
                    finish();
                    return;
                }
                break;
            case 1:
                Crop.pickImage(this);
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
        outState.putInt(SAVE_ASPECT_X, mAspectX);
        outState.putInt(SAVE_ASPECT_Y, mAspectY);
        outState.putInt(SAVE_MAX_X, mMaxX);
        outState.putInt(SAVE_MAX_Y, mMaxY);
        outState.putString(SAVE_IMAGE_TYPE, mImageType);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Crop.REQUEST_PICK:
                    if (data.getData() != null) {
                        mImageType = ImageUtil.detectType(this, data.getData());
                        File file1 = ContextUtil.createTempFile(this, "photo", ".jpg", "_crop");
                        if (file1 != null) {
                            crop(data.getData(), file1);
                        }
                        return;
                    }
                    break;
                case REQUEST_CODE_IMAGE_CAPTURE:
                    if (mCaptureUri != null) {
                        File file2 = ContextUtil.createTempFile(this, "photo", ".jpg", "_crop");
                        if (file2 != null) {
                            crop(mCaptureUri, file2);
                        }
                        return;
                    }
                    break;
                case Crop.REQUEST_CROP:
                    Uri output = data.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
//                    mImageType = ImageUtil.detectType(this, output);
                    Intent intent = new Intent();
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, output);
                    intent.putExtra(EXTRA_IMAGE_TYPE, ImageUtil.TYPE_JPG);
                    intent.setData(output);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                    return;
            }
            setResult(RESULT_ERROR);
            finish();
        } else if (resultCode == Activity.RESULT_CANCELED) {
            setResult(RESULT_CANCELED);
            finish();
        } else if (Crop.RESULT_ERROR == resultCode){
            Intent intent = new Intent();
            //noinspection ThrowableResultOfMethodCallIgnored
            Throwable error = Crop.getError(data);
            intent.putExtra(EXTRA_ERROR, error);
            setResult(RESULT_ERROR, intent);
            finish();
        }
    }

    private void crop(Uri uri, File file) {
        Crop crop = Crop.of(uri, Uri.fromFile(file));
        if (mAspectX > 0 && mAspectY > 0) {
            crop.withAspect(mAspectX, mAspectY);
        } else {
            crop.asSquare();
        }
        if (mMaxX > 0 && mMaxY > 0) {
            crop.withMaxSize(mMaxX, mMaxY);
        }
        crop.start(this);
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

        public Builder(Intent intent) {
            mIntent = new Intent(intent);
        }

        public Builder() {
            mIntent = new Intent();
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
            mIntent.putExtra(EXTRA_ASPECT_X, aspectX);
            mIntent.putExtra(EXTRA_ASPECT_Y, aspectY);
            return this;
        }

        /**
         * Crop picture with fixed 1:1 aspect ratio.
         */
        public Builder asSquare() {
            mIntent.putExtra(EXTRA_ASPECT_X, 1);
            mIntent.putExtra(EXTRA_ASPECT_Y, 1);
            return this;
        }

        /**
         * Set the maximum size.
         * @param width Max width
         * @param height Max height
         */
        public Builder withMaxSize(int width, int height) {
            mIntent.putExtra(EXTRA_MAX_X, width);
            mIntent.putExtra(EXTRA_MAX_Y, height);
            return this;
        }

        /**
         * Select a picture from a activity with a custom requestCode.
         * @param activity Activity to receive result
         * @param requestCode requestCode for result
         */
        public void start(Activity activity, int requestCode) {
            mIntent.setClass(activity, SelectImageActivity.class);
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
            mIntent.setClass(context, SelectImageActivity.class);
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
            mIntent.setClass(context, SelectImageActivity.class);
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
