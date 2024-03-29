package com.teamforce.thanksapp.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import java.io.FileNotFoundException

object BitmapUtils {

    private const val BITMAP_SCALE = 0.5f
    private const val BLUR_RADIUS = 25f
    private const val IMG_MAX_SIDE_SIZE = 2000
    private const val TAG = "BitmapUtils"

    fun decodeBitmap(uri: Uri, activity: Activity): Bitmap? {
        return try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(activity.contentResolver.openInputStream(uri), null, options)
            var scale = 1
            while (options.outWidth / scale / 2 >= IMG_MAX_SIDE_SIZE && options.outHeight / scale / 2 >= IMG_MAX_SIDE_SIZE) scale *= 2

            val scaleOptions = BitmapFactory.Options()
            scaleOptions.inSampleSize = scale
            BitmapFactory.decodeStream(activity.contentResolver.openInputStream(uri), null, scaleOptions)
        } catch (e: FileNotFoundException) {
            null
        }
    }


    fun blurImage(context: Context?, image: Bitmap): Bitmap {
        // Определите координаты и размер области, которую вы хотите взять
        val startY = image.height - image.height / 3
        val endY = image.height
        val croppedBitmap = Bitmap.createBitmap(image, 0, startY, image.width, endY - startY)

        val width = Math.round(croppedBitmap.width * BITMAP_SCALE)
        val height = Math.round(159 * BITMAP_SCALE)
        val inputBitmap = Bitmap.createScaledBitmap(croppedBitmap, width, height, false)
        val outputBitmap = Bitmap.createBitmap(inputBitmap)
        val rs = RenderScript.create(context)
        val theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        val tmpIn = Allocation.createFromBitmap(rs, inputBitmap)
        val tmpOut = Allocation.createFromBitmap(rs, outputBitmap)
        theIntrinsic.setRadius(BLUR_RADIUS)
        theIntrinsic.setInput(tmpIn)
        theIntrinsic.forEach(tmpOut)
        tmpOut.copyTo(outputBitmap)
        return outputBitmap
    }

    fun getBlurBitmap(context: Context): Bitmap {
        val originalBitmap = createWhiteBitmap()
        val width = Math.round(originalBitmap.width * BITMAP_SCALE)
        val height = Math.round(159 * BITMAP_SCALE)
        val inputBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false)
        val outputBitmap = Bitmap.createBitmap(inputBitmap)
        val rs = RenderScript.create(context)
        val theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        val tmpIn = Allocation.createFromBitmap(rs, inputBitmap)
        val tmpOut = Allocation.createFromBitmap(rs, outputBitmap)
        theIntrinsic.setRadius(BLUR_RADIUS)
        theIntrinsic.setInput(tmpIn)
        theIntrinsic.forEach(tmpOut)
        tmpOut.copyTo(outputBitmap)
        return outputBitmap
    }

    private fun createWhiteBitmap(): Bitmap {
        val config = Bitmap.Config.ARGB_8888
        val bitmap = Bitmap.createBitmap(300, 300, config)
        val canvas = Canvas(bitmap)
        val color = Color.argb(255, 255, 255, 255)
        canvas.drawColor(color)
        return bitmap
    }


    fun isColorLight(bitmap: Bitmap): Boolean {
        val color = calculateAverageColor(bitmap)
        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness < 0.5
    }

    private fun calculateAverageColor(bitmap: Bitmap): Int {
        var red = 0
        var green = 0
        var blue = 0

        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                val pixelColor = bitmap.getPixel(x, y)
                red += Color.red(pixelColor)
                green += Color.green(pixelColor)
                blue += Color.blue(pixelColor)
            }
        }

        val totalPixels = bitmap.width * bitmap.height
        red /= totalPixels
        green /= totalPixels
        blue /= totalPixels

        return Color.rgb(red, green, blue)
    }

}