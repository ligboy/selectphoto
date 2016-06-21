package org.ligboy.selectphoto;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Ligboy.Liu ligboy@gmail.com.
 */

final class ImageUtil {
    public static final String TYPE_JPG = "jpg";
    public static final String TYPE_GIF = "gif";
    public static final String TYPE_PNG = "png";
    public static final String TYPE_BMP = "bmp";
    public static final String TYPE_TIFF = "tiff";
    public static final String TYPE_ICO = "ico";
    public static final String TYPE_CUR= "cur";
    public static final String TYPE_WEBP= "webp";
    public static final String TYPE_UNKNOWN= "unknown";

    private static final String SCHEMA_FILE = "file";

    public static String detectType(@NonNull Context context, @NonNull Uri uri) {
        try {
            InputStream inputStream;
//            if (SCHEMA_FILE.equals(uri.getScheme())) {
                ParcelFileDescriptor ignored = context.getContentResolver()
                        .openFileDescriptor(uri, "r");
                FileDescriptor fd = ignored.getFileDescriptor();
                inputStream = new FileInputStream(fd);
//            } else {
//                inputStream = context.getContentResolver().openInputStream(uri);
//            }
            return detectType(inputStream);
        } catch (FileNotFoundException ignored) {
        }
        return TYPE_UNKNOWN;
    }


    /**
     * 根据流判断图片类型
     * @param inputStream 流
     * @return jpg/png/gif/bmp
     */
    public static String detectType(InputStream inputStream) {
        //读取文件的前几个字节来判断图片格式
        byte[] b = new byte[12];
        try {
            inputStream.read(b, 0, b.length);
            int b0 = b[0] & 0xFF;
            int b1 = b[1] & 0xFF;
            int b2 = b[2] & 0xFF;
            int b3 = b[3] & 0xFF;
            int b4 = b[4] & 0xFF;
            int b5 = b[5] & 0xFF;
            int b6 = b[6] & 0xFF;
            int b7 = b[7] & 0xFF;
            int b8 = b[8] & 0xFF;
            int b9 = b[9] & 0xFF;
            int b10 = b[10] & 0xFF;
            int b11 = b[11] & 0xFF;
            if (b0 == 0xff && b1 == 0xD8) {
                return TYPE_JPG;
            } else if (b0 == 0x89 && b1 == 0x50 && b2 == 0x4E && b3 == 0x47
                    && b4 == 0x0D && b5 == 0x0A && b6 == 0x1A && b7 == 0x0A) {
                return TYPE_PNG;
            } else if (b0 == 0x47 && b1 == 0x49 && b2 == 0x46 && b4 == 0x39) {
                return TYPE_GIF;
            } else if (b0 == 0x42 && b1 == 0x4D) {
                return TYPE_BMP;
            } else if ((b0 == 0x4D && b1 == 0x4D) || (b0 == 0x49 && b1 == 0x49)) {
                return TYPE_TIFF;
            } else if (b0 == 0x00 && b1 == 0x00 && b2 == 0x01 && b3 == 0x00
                    && b4 == 0x01 && b5 == 0x00 && b6 == 0x20 && b7 == 0x20) {
                return TYPE_ICO;
            } else if (b0 == 0x00 && b1 == 0x00 && b2 == 0x02 && b3 == 0x00
                    && b4 == 0x01 && b5 == 0x00 && b6 == 0x20 && b7 == 0x20) {
                return TYPE_CUR;
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
            } else if (b0 == 0x52 && b1 == 0x49 && b2 == 0x46 && b3 == 0x46
                    && b8 == 0x57 && b9 == 45 && b10 == 0x42 && b11 == 0x50) {
                return TYPE_WEBP;
            } else {
                return TYPE_UNKNOWN;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return TYPE_UNKNOWN;
    }
}
