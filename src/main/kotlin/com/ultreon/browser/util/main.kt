package com.ultreon.browser.util

import com.formdev.flatlaf.FlatLightLaf
import com.ultreon.browser.AppPrefs
import com.ultreon.browser.DownloadProgressHandler
import com.ultreon.browser.UltreonBrowser
import com.ultreon.browser.dataDir
import me.friwi.jcefmaven.CefAppBuilder
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter
import org.cef.CefApp
import org.oxbow.swingbits.dialog.task.TaskDialogs
import java.io.File
import java.util.concurrent.CompletableFuture
import javax.swing.SwingUtilities
import javax.swing.UIManager
import kotlin.system.exitProcess

lateinit var argv: Array<String>
    private set

fun main(args: Array<String>) {
    argv = args

    AppPrefs.init(APP_ID)
    AppPrefs.setupLaf(arrayOf())

    CompletableFuture.runAsync {

        //Create a new CefAppBuilder instance
        val builder = CefAppBuilder()

        builder.cefSettings.windowless_rendering_enabled = useOSR

        // USE builder.setAppHandler INSTEAD OF CefApp.addAppHandler!
        // Fixes compatibility issues with MacOSX
        builder.setAppHandler(object : MavenCefAppHandlerAdapter() {
            override fun stateHasChanged(state: CefApp.CefAppState) {
                // Shutdown the app if the native CEF part is terminated
                if (state == CefApp.CefAppState.TERMINATED) exitProcess(0)
            }
        })

        //Configure the builder instance
        builder.setInstallDir(File(dataDir, "CEF")) //Default

        builder.setProgressHandler(DownloadProgressHandler())

        builder.cefSettings.windowless_rendering_enabled = useOSR //Default - select OSR mode
        builder.cefSettings.cache_path = dataDir.toString()
        builder.cefSettings.user_agent_product = "UltreonBrowser/$APP_VERSION"

        builder.addJcefArgs(*argv)

        //Build a CefApp instance using the configuration above
        val app = builder.build()
        SwingUtilities.invokeLater {
            Thread.setDefaultUncaughtExceptionHandler { _, ex ->
                crash(ex)
            }
            UltreonBrowser.start(app)
        }
    }

}

@Throws(Exception::class)
fun crash(e: Throwable) {
    try {
        UltreonBrowser.instance.dispose()
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
