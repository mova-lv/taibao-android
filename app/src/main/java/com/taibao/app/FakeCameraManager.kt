package com.taibao.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import androidx.core.content.edit

object FakeCameraManager {

    private const val PREFS_NAME = "fake_camera_prefs"
    private const val KEY_FAKE_IMAGE_PATH = "fake_image_path"
    private const val FAKE_IMAGE_DIR = "fake_camera"

    fun setFakeImage(context: Context, sourcePath: String): String {
        val fakeDir = File(context.filesDir, FAKE_IMAGE_DIR)
        fakeDir.mkdirs()

        // Copy source file to persistent fake camera storage
        val destFile = File(fakeDir, "fake_image_${System.currentTimeMillis()}.jpg")
        File(sourcePath).copyTo(destFile, overwrite = true)

        // Save path to SharedPreferences
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putString(KEY_FAKE_IMAGE_PATH, destFile.absolutePath)
            }

        return destFile.absolutePath
    }

    fun setFakeImageFromBitmap(context: Context, bitmap: Bitmap): String {
        val fakeDir = File(context.filesDir, FAKE_IMAGE_DIR)
        fakeDir.mkdirs()

        val destFile = File(fakeDir, "fake_image_${System.currentTimeMillis()}.jpg")
        FileOutputStream(destFile).use { os ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
        }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putString(KEY_FAKE_IMAGE_PATH, destFile.absolutePath)
            }

        return destFile.absolutePath
    }

    fun getFakeImageFile(context: Context): File? {
        val path = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_FAKE_IMAGE_PATH, null)
        if (path == null) return null
        val file = File(path)
        return if (file.exists()) file else null
    }

    fun getFakeImageBitmap(context: Context): Bitmap? {
        val file = getFakeImageFile(context) ?: return null
        return BitmapFactory.decodeFile(file.absolutePath)
    }

    fun isFakeImageSet(context: Context): Boolean {
        return getFakeImageFile(context) != null
    }

    fun clearFakeImage(context: Context) {
        val path = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_FAKE_IMAGE_PATH, null)
        if (path != null) {
            File(path).delete()
        }
        // Clean up any old fake images in the directory
        val fakeDir = File(context.filesDir, FAKE_IMAGE_DIR)
        fakeDir.listFiles()?.forEach { it.delete() }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                remove(KEY_FAKE_IMAGE_PATH)
            }
    }
}