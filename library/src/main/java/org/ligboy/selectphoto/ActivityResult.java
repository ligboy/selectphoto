package org.ligboy.selectphoto;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.theartofdev.edmodo.cropper.*;

/**
 * Result data of Crop Image Activity.
 */
final class ActivityResult implements Parcelable {

    public static final Creator<ActivityResult> CREATOR = new Creator<ActivityResult>() {
        @Override
        public ActivityResult createFromParcel(Parcel in) {
            return new ActivityResult(in);
        }

        @Override
        public ActivityResult[] newArray(int size) {
            return new ActivityResult[size];
        }
    };

    /**
     * The Android uri of the saved cropped image result
     */
    private final Uri mUri;

    /**
     * The error that failed the loading/cropping (null if successful)
     */
    private final Exception mError;

    /**
     * The 4 points of the cropping window in the source image
     */
    private final float[] mCropPoints;

    /**
     * The rectangle of the cropping window in the source image
     */
    private final Rect mCropRect;

    /**
     * The final rotation of the cropped image relative to source
     */
    private final int mRotation;

    ActivityResult(Uri uri, Exception error, float[] cropPoints, Rect cropRect, int rotation) {
        mUri = uri;
        mError = error;
        mCropPoints = cropPoints;
        mCropRect = cropRect;
        mRotation = rotation;
    }

    protected ActivityResult(Parcel in) {
        mUri = in.readParcelable(Uri.class.getClassLoader());
        mError = (Exception) in.readSerializable();
        mCropPoints = in.createFloatArray();
        mCropRect = in.readParcelable(Rect.class.getClassLoader());
        mRotation = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mUri, flags);
        dest.writeSerializable(mError);
        dest.writeFloatArray(mCropPoints);
        dest.writeParcelable(mCropRect, flags);
        dest.writeInt(mRotation);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Is the result is success or error.
     */
    public boolean isSuccessful() {
        return mError == null;
    }

    /**
     * The Android uri of the saved cropped image result
     */
    public Uri getUri() {
        return mUri;
    }

    /**
     * The error that failed the loading/cropping (null if successful)
     */
    public Exception getError() {
        return mError;
    }

    /**
     * The 4 points of the cropping window in the source image
     */
    public float[] getCropPoints() {
        return mCropPoints;
    }

    /**
     * The rectangle of the cropping window in the source image
     */
    public Rect getCropRect() {
        return mCropRect;
    }

    /**
     * The final rotation of the cropped image relative to source
     */
    public int getRotation() {
        return mRotation;
    }

    /**
     * Get {@link CropImageActivity} result data object.
     *
     * @param data result data intent as received in
     * {@link Activity#onActivityResult(int, int, Intent)}.
     * @return Crop Image Activity Result object or null if none exists
     */
    public static ActivityResult get(@Nullable Intent data) {
        if (data != null) {
            return (ActivityResult) data.getParcelableExtra(CropImage.CROP_IMAGE_EXTRA_RESULT);
        } else {
             return null;
        }
    }
}
