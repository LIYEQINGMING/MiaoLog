package com.example.itemmanagement.ui.dialog

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.itemmanagement.utils.UpdateInfo

class OnlineUpdateDialog : DialogFragment() {
    companion object {
        fun newInstance(updateInfo: UpdateInfo): OnlineUpdateDialog {
            return OnlineUpdateDialog()
        }
    }
}