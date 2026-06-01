package com.example.itemmanagement.ui.dialog

import android.os.Bundle
import androidx.fragment.app.DialogFragment

class UpdateLogDialog : DialogFragment() {
    companion object {
        fun newInstance(isFirstLaunch: Boolean): UpdateLogDialog {
            return UpdateLogDialog()
        }
    }
}