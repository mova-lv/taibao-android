package com.taibao.app

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.core.content.IntentCompat

class FakeCameraActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make the activity look like a real camera flash
        val view = View(this)
        view.setBackgroundColor(Color.WHITE)
        setContentView(view)
        handleFakeCapture()
    }

    private fun handleFakeCapture() {
        val fakeFile = FakeCameraManager.getFakeImageFile(this)

        if (fakeFile == null) {
            // No fake image set, return cancelled
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        val fakeBitmap = BitmapFactory.decodeFile(fakeFile.absolutePath)

        val outputUri: Uri? =
            IntentCompat.getParcelableExtra(intent, MediaStore.EXTRA_OUTPUT, Uri::class.java)

        if (outputUri != null) {
            // Caller provided EXTRA_OUTPUT — write full image to that URI
            try {
                contentResolver.openOutputStream(outputUri)?.use { os ->
                    fakeBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
                }
                setResult(RESULT_OK)
            } catch (e: Exception) {
                setResult(RESULT_CANCELED)
            }
        } else {
            // No EXTRA_OUTPUT — return thumbnail in result intent data
            val result = Intent().putExtra("data", fakeBitmap)
            setResult(RESULT_OK, result)
        }
        finish()
    }
}