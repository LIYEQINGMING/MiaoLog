package com.example.itemmanagement.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar
import android.graphics.Color

object SnackbarHelper {
    fun show(view: View, message: String, duration: Int = Snackbar.LENGTH_SHORT) {
        Snackbar.make(view, message, duration).show()
    }

    fun showSuccess(view: View, message: String) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
        snackbar.view.setBackgroundColor(Color.parseColor("#4CAF50")) // Green
        snackbar.show()
    }

    fun showError(view: View, message: String) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
        snackbar.view.setBackgroundColor(Color.parseColor("#F44336")) // Red
        snackbar.show()
    }

    fun showWarning(view: View, message: String) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
        snackbar.view.setBackgroundColor(Color.parseColor("#FF9800")) // Orange
        snackbar.show()
    }
}