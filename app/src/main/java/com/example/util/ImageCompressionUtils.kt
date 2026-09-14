package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

object ImageCompressionUtils {

    /**
     * Compresses an image file by downscaling dimensions and compressing with JPEG quality.
     * Reduces 5-15MB camera photos down to ~70-150KB while retaining high visual detail for inspection.
     */
    fun compressAndSaveImage(
        context: Context,
        sourceFile: File,
        maxDimension: Int = 1280,
        quality: Int = 78
    ): File {
        if (!sourceFile.exists()) return sourceFile

        val outputDir = File(context.filesDir, "inspections_media").apply { mkdirs() }
        val outputFile = File(outputDir, "img_${System.currentTimeMillis()}.jpg")

        return try {
            // 1. Check EXIF orientation
            val exifOrientation = try {
                val exif = ExifInterface(sourceFile.absolutePath)
                exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            } catch (e: Exception) {
                ExifInterface.ORIENTATION_NORMAL
            }

            // 2. Decode bounds only
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(sourceFile.absolutePath, options)
            val srcWidth = options.outWidth
            val srcHeight = options.outHeight

            if (srcWidth <= 0 || srcHeight <= 0) return sourceFile

            // 3. Compute inSampleSize
            var inSampleSize = 1
            val maxSrcDim = max(srcWidth, srcHeight)
            while ((maxSrcDim / inSampleSize) > maxDimension * 2) {
                inSampleSize *= 2
            }

            // 4. Decode sampled bitmap
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.RGB_565 // Saves 50% memory compared to ARGB_8888
            }
            var bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath, decodeOptions)
                ?: return sourceFile

            // 5. Rotate if EXIF specified rotation
            val rotationAngle = when (exifOrientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
            if (rotationAngle != 0f) {
                val matrix = Matrix().apply { postRotate(rotationAngle) }
                val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                if (rotated != bitmap) {
                    bitmap.recycle()
                    bitmap = rotated
                }
            }

            // 6. Scale if still larger than maxDimension
            val curMaxDim = max(bitmap.width, bitmap.height)
            if (curMaxDim > maxDimension) {
                val scale = maxDimension.toFloat() / curMaxDim
                val targetW = (bitmap.width * scale).toInt()
                val targetH = (bitmap.height * scale).toInt()
                val scaled = Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
                if (scaled != bitmap) {
                    bitmap.recycle()
                    bitmap = scaled
                }
            }

            // 7. Compress into JPEG
            FileOutputStream(outputFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos)
                fos.flush()
            }
            bitmap.recycle()

            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            sourceFile
        }
    }

    /**
     * Compresses an in-memory Bitmap directly to a compact JPEG file.
     */
    fun compressBitmapToFile(
        context: Context,
        bitmap: Bitmap,
        maxDimension: Int = 1280,
        quality: Int = 78
    ): File {
        val outputDir = File(context.filesDir, "inspections_media").apply { mkdirs() }
        val outputFile = File(outputDir, "img_${System.currentTimeMillis()}.jpg")

        return try {
            val curMax = max(bitmap.width, bitmap.height)
            val readyBitmap = if (curMax > maxDimension) {
                val scale = maxDimension.toFloat() / curMax
                val targetW = (bitmap.width * scale).toInt()
                val targetH = (bitmap.height * scale).toInt()
                Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
            } else {
                bitmap
            }

            FileOutputStream(outputFile).use { fos ->
                readyBitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos)
                fos.flush()
            }
            if (readyBitmap != bitmap) {
                readyBitmap.recycle()
            }
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            outputFile
        }
    }

    /**
     * Decodes a thumbnail bitmap specifically tailored for PDF rendering or small UI cards.
     * Prevents embedding giant raw bitmaps into the PDF document.
     */
    fun decodeThumbnail(
        filePath: String,
        reqWidth: Int = 240,
        reqHeight: Int = 180
    ): Bitmap? {
        val file = File(filePath)
        if (!file.exists()) return null

        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)

            var inSampleSize = 1
            val height = options.outHeight
            val width = options.outWidth

            if (height > reqHeight || width > reqWidth) {
                val halfHeight = height / 2
                val halfWidth = width / 2
                while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            val sampled = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return null

            // Scale precisely to box dimensions preserving ratio
            val scale = min(reqWidth.toFloat() / sampled.width, reqHeight.toFloat() / sampled.height)
            val targetW = max(1, (sampled.width * scale).toInt())
            val targetH = max(1, (sampled.height * scale).toInt())
            val scaled = Bitmap.createScaledBitmap(sampled, targetW, targetH, true)
            if (scaled != sampled) {
                sampled.recycle()
            }
            scaled
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
