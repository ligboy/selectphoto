package org.ligboy.selectphoto;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Image Type Detector
 * @author Ligboy.Liu ligboy@gmail.com.
 */

public final class ImageTypeUtil {
    public static final String TYPE_JPG = "jpg";
    public static final String TYPE_GIF = "gif";
    public static final String TYPE_PNG = "png";
    public static final String TYPE_BMP = "bmp";
    public static final String TYPE_TIFF = "tiff";
    public static final String TYPE_ICO = "ico";
    public static final String TYPE_CUR= "cur";
    public static final String TYPE_WEBP= "webp";
    public static final String TYPE_UNKNOWN= "";

    @StringDef({TYPE_JPG, TYPE_GIF, TYPE_PNG, TYPE_BMP, TYPE_TIFF,
            TYPE_ICO, TYPE_CUR, TYPE_WEBP, TYPE_UNKNOWN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ImageTypes {}

    private static final String SCHEMA_FILE = "file";

    /**
     * Detect Image Type by file header
     * @param context The Context.
     * @param uri The uri of the image.
     * @return The suffix of image type.
     * @throws IOException
     */
    @ImageTypes
    public static String detectType(@NonNull Context context, @NonNull Uri uri) throws IOException {
        InputStream inputStream = null;
        inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException ignored) {
            }
        }
        //noinspection ConstantConditions
        return detectType(inputStream);
    }

    /**
     * Detect Image Type by file header
     * @param file the file to be detected.
     * @return The suffix of image type.
     */
    @ImageTypes
    public static String detectType(@NonNull File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        String type = detectType(inputStream);
        inputStream.close();
        return type;
    }


    /**
     * Detect Image Type by file header
     * <p>The method <b>DO NOT</b> close the inputStream.</p>
     * @param inputStream InputStream of the file. The method <b>DO NOT</b> close the inputStream.
     * @return The suffix of image type.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @ImageTypes
    public static String detectType(@NonNull final InputStream inputStream) throws IOException {
        final byte[] b = new byte[12];
            inputStream.read(b, 0, b.length);
            final int b0 = b[0] & 0xFF;
            final int b1 = b[1] & 0xFF;
            if (b0 == 0xff && b1 == 0xD8) {
                return TYPE_JPG;
            } else if (b0 == 0x42 && b1 == 0x4D) {
                return TYPE_BMP;
            } else if ((b0 == 0x4D && b1 == 0x4D) || (b0 == 0x49 && b1 == 0x49)) {
                return TYPE_TIFF;
            }
            final int b2 = b[2] & 0xFF;
            final int b3 = b[3] & 0xFF;
            final int b4 = b[4] & 0xFF;
            if (b0 == 0x47 && b1 == 0x49 && b2 == 0x46 && b4 == 0x39) {
                return TYPE_GIF;
            }
            final int b5 = b[5] & 0xFF;
            final int b6 = b[6] & 0xFF;
            final int b7 = b[7] & 0xFF;
            if (b0 == 0x89 && b1 == 0x50 && b2 == 0x4E && b3 == 0x47
                    && b4 == 0x0D && b5 == 0x0A && b6 == 0x1A && b7 == 0x0A) {
                return TYPE_PNG;
            } else if (b0 == 0x00 && b1 == 0x00 && b2 == 0x01 && b3 == 0x00
                    && b4 == 0x01 && b5 == 0x00 && b6 == 0x20 && b7 == 0x20) {
                return TYPE_ICO;
            } else if (b0 == 0x00 && b1 == 0x00 && b2 == 0x02 && b3 == 0x00
                    && b4 == 0x01 && b5 == 0x00 && b6 == 0x20 && b7 == 0x20) {
                return TYPE_CUR;
            }
            final int b8 = b[8] & 0xFF;
            final int b9 = b[9] & 0xFF;
            final int b10 = b[10] & 0xFF;
            final int b11 = b[11] & 0xFF;
                /**
                 * 0                   1                   2                   3
                 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
                 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
                 * |      'R'      |      'I'      |      'F'      |      'F'      |
                 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
                 * |                           File Size                           |
                 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
                 * |      'W'      |      'E'      |      'B'      |      'P'      |
                 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
                 */
            if (b0 == 0x52 && b1 == 0x49 && b2 == 0x46 && b3 == 0x46
                    && b8 == 0x57 && b9 == 45 && b10 == 0x42 && b11 == 0x50) {
                return TYPE_WEBP;
            }

        return TYPE_UNKNOWN;
    }
}
