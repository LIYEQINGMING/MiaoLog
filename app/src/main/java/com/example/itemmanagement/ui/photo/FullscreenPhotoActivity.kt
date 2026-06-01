package com.example.itemmanagement.ui.photo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class FullscreenPhotoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Dummy implementation for compilation
    }

    companion object {
        fun createIntent(context: Context, photos: List<String>, position: Int): Intent {
            val intent = Intent(context, FullscreenPhotoActivity::class.java)
            intent.putStringArrayListExtra("photos", ArrayList(photos))
            intent.putExtra("position", position)
            return intent
        }
    }
}