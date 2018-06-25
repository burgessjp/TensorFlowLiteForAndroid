package me.solidev.tensorflow.lib;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * @author _SOLID
 * @since 2018/5/9.
 */
public class TFBitmapUtil {

    private static Bitmap getCropBitmap(Bitmap source, RectF rectF) {
        Float width = rectF.right - rectF.left;
        Float height = rectF.bottom - rectF.top;
        if (width <= 0) {
            width = 1f;
        }
        if (height <= 0) {
            height = 1f;
        }
        return Bitmap.createBitmap(source,
                Float.valueOf(rectF.left).intValue(),
                Float.valueOf(rectF.top).intValue(),
                width.intValue(),
                height.intValue());
    }

    public static Bitmap getClassifyBitmap(Bitmap bitmap, int inputSizeX, int inputSizeY) {
        Paint paint = new Paint();
        Bitmap target = Bitmap.createBitmap(inputSizeX, inputSizeY, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(target);
        canvas.drawColor(Color.WHITE);
        RectF dst = new RectF();
        if (bitmap.getWidth() > bitmap.getHeight()) {
            float ratio = bitmap.getWidth() * 1f / target.getWidth();
            float gapWith = target.getHeight() - bitmap.getHeight() / ratio;
            dst.left = 0f;
            dst.top = gapWith / 2;
            dst.right = target.getWidth();
            dst.bottom = dst.top + bitmap.getHeight() / ratio;
        } else if (bitmap.getWidth() < bitmap.getHeight()) {
            float ratio = bitmap.getHeight() * 1f / target.getHeight();
            float gapWith = target.getWidth() - bitmap.getWidth() / ratio;
            dst.left = gapWith / 2;
            dst.top = 0f;
            dst.right = dst.left + bitmap.getWidth() / ratio;
            dst.bottom = target.getHeight();
        } else {
            dst.top = 0f;
            dst.left = 0f;
            dst.right = target.getWidth();
            dst.bottom = target.getHeight();
        }

        canvas.drawBitmap(bitmap, null, dst, paint);
//        val name = UUID.randomUUID().toString()
//        saveToFile(bitmap, Constant.Dir.PHOTO + "/rect", "$name.jpg", 100)
//        saveToFile(target, Constant.Dir.PHOTO + "/rect", name + "_after.jpg", 100)
        return target;
    }

    public static Bitmap getClassifyBitmap(Bitmap source, RectF rectF, int inputSizeX, int inputSizeY) {
        Paint paint = new Paint();
        Bitmap bitmap = getCropBitmap(source, rectF);
        Bitmap target = Bitmap.createBitmap(inputSizeX, inputSizeY, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(target);
        canvas.drawColor(Color.WHITE);
        RectF dst = new RectF();
        if (bitmap.getWidth() > bitmap.getHeight()) {
            float ratio = bitmap.getWidth() * 1f / target.getWidth();
            float gapWith = target.getHeight() - bitmap.getHeight() / ratio;
            dst.left = 0f;
            dst.top = gapWith / 2;
            dst.right = target.getWidth();
            dst.bottom = dst.top + bitmap.getHeight() / ratio;
        } else if (bitmap.getWidth() < bitmap.getHeight()) {
            float ratio = bitmap.getHeight() * 1f / target.getHeight();
            float gapWith = target.getWidth() - bitmap.getWidth() / ratio;
            dst.left = gapWith / 2;
            dst.top = 0f;
            dst.right = dst.left + bitmap.getWidth() / ratio;
            dst.bottom = target.getHeight();
        } else {
            dst.top = 0f;
            dst.left = 0f;
            dst.right = target.getWidth();
            dst.bottom = target.getHeight();
        }

        canvas.drawBitmap(bitmap, null, dst, paint);
//        val name = UUID.randomUUID().toString()
//        saveToFile(bitmap, Constant.Dir.PHOTO + "/rect", "$name.jpg", 100)
//        saveToFile(target, Constant.Dir.PHOTO + "/rect", name + "_after.jpg", 100)
        return target;
    }

    static Matrix getTransformationMatrix(
            int srcWidth,
            int srcHeight,
            int dstWidth,
            int dstHeight,
            Boolean maintainAspectRatio) {
        Matrix matrix = new Matrix();
        int inWidth = srcWidth;
        int inHeight = srcHeight;

        // Apply scaling if necessary.
        if (inWidth != dstWidth || inHeight != dstHeight) {
            float scaleFactorX = dstWidth * 1f / inWidth;
            float scaleFactorY = dstHeight * 1f / inHeight;

            if (maintainAspectRatio) {
                // Scale by minimum factor so that dst is filled completely while
                // maintaining the aspect ratio. Some image may fall off the edge.
                float scaleFactor = Math.max(scaleFactorX, scaleFactorY);
                matrix.postScale(scaleFactor, scaleFactor);
            } else {
                // Scale exactly to fill dst from src.
                matrix.postScale(scaleFactorX, scaleFactorY);
            }
        }

        return matrix;
    }

    static Bitmap getCropBitmap(Bitmap bitmap, int cropSize, Matrix matrix) {
        Bitmap croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(bitmap, matrix, null);
        return croppedBitmap;
    }


//    fun saveToFile(data:ByteArray, file:File) {
//        if (!file.parentFile.exists()) {
//            file.parentFile.mkdirs()
//        }
//        val os = FileOutputStream(file)
//        try {
//            os.write(data)
//        } catch (e:IOException){
//            IDTLog.e(TAG, "saveToFile:ERROR," + e.message)
//        } finally{
//            os.close()
//        }
//    }

//    fun saveToFile(bitmap:Bitmap, savePath:String, fileName:String, quality:Int) {
//        var outStream:FileOutputStream ? = null
//        try {
//            // save output image
//            val dir = File(savePath)
//            if (!dir.exists()) {
//                dir.mkdirs()
//            }
//            val outFile = File(dir, fileName)
//            outStream = FileOutputStream(outFile)
//            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outStream)
//            outStream.flush()
//        } catch (e:Exception){
//        } finally{
//            if (outStream != null) {
//                try {
//                    outStream.close()
//                } catch (e:IOException){
//                    e.printStackTrace()
//                }
//
//            }
//        }
//    }
}