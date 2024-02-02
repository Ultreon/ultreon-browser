package com.ultreon.browser

import com.ultreon.browser.dialog.ProgressDialog
import com.ultreon.browser.util.logInfo
import me.friwi.jcefmaven.EnumProgress
import me.friwi.jcefmaven.IProgressHandler
import java.awt.Dimension
import javax.swing.SwingUtilities

class DownloadProgressHandler : IProgressHandler {
    private lateinit var progress: ProgressDialog

    init {
        SwingUtilities.invokeLater {
            progress = ProgressDialog(null, "Setting up CEF")
            progress.size = Dimension(400, 120)
            progress.isResizable = false
        }
    }

    override fun handleProgress(stage: EnumProgress, fl: Float) {
        SwingUtilities.invokeLater {
            progress.isVisible = stage == EnumProgress.INSTALL || stage == EnumProgress.DOWNLOADING || stage == EnumProgress.EXTRACTING
            progress.message = when (stage) {
                EnumProgress.INSTALL -> "Installing CEF..."
                EnumProgress.DOWNLOADING -> "Downloading files..."
                EnumProgress.EXTRACTING -> "Extracting files..."
                EnumProgress.LOCATING -> "Locating CEF..."
                EnumProgress.INITIALIZING -> "Initializing..."
                EnumProgress.INITIALIZED -> "Ready"
            }.also {
                logInfo("$it ${if (fl == -1f) "" else "$fl%"}")
            }

            if (stage == EnumProgress.INITIALIZED) {
                progress.dispose()
            }

            progress.setValue(fl / 100f)
        }
    }

}
