package com.ultreon.browser

import com.formdev.flatlaf.FlatLightLaf
import com.ultreon.browser.main.AppPrefs
import com.ultreon.browser.main.MainFrame
import org.oxbow.swingbits.dialog.task.TaskDialogs
import javax.swing.SwingUtilities
import javax.swing.UIManager
import kotlin.system.exitProcess

lateinit var argv: Array<String>
    private set

fun main(args: Array<String>) {
    argv = args

    AppPrefs.init(APP_ID)
    AppPrefs.setupLaf(arrayOf())

    SwingUtilities.invokeLater {
        Thread.setDefaultUncaughtExceptionHandler { _, ex ->
            crash(ex)
        }
        MainFrame.start()
    }
}

@Throws(Exception::class)
fun crash(e: Throwable) {
    try {
        MainFrame.instance.dispose()
    } catch (e: Exception) {
        // ignore
    }
    SwingUtilities.invokeLater {
        val lookAndFeel = UIManager.getLookAndFeel()
        UIManager.setLookAndFeel(FlatLightLaf())
        e.printStackTrace()
        TaskDialogs.showException(e)
        UIManager.setLookAndFeel(lookAndFeel)
        exitProcess(1)
    }
}
