import com.formdev.flatlaf.FlatLightLaf
import com.formdev.flatlaf.util.SystemInfo.javaVersion
import com.formdev.flatlaf.util.SystemInfo.osVersion
import com.ultreon.browser.*
import com.ultreon.browser.util.*
import me.friwi.jcefmaven.CefAppBuilder
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter
import org.cef.CefApp
import org.cef.OS.*
import org.oxbow.swingbits.dialog.task.TaskDialogs
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture
import javax.swing.SwingUtilities
import javax.swing.UIManager
import kotlin.system.exitProcess

lateinit var argv: Array<String>
    private set

fun main(args: Array<String>) {
    argv = args
    main()
}

private fun main() {
    logInfo("Starting browser...")

    UltreonURLHandler().setHandler()

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
        builder.cefSettings.user_agent_product = "UltreonBrowser/$APP_VERSION Chrome/$CHROME_VERSION"

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


    while (true) {
        Thread.sleep(1000)
    }
}

@Throws(Exception::class)
fun crash(e: Throwable) {
    logError("Ultreon Browser crashed!", e)

    e.stackTraceToString().let {
        val crashLog = it.run {
            replace("\t", "  ")
            return@run """
                OS: $osName
                OS Version: $osVersion
                App Version: $APP_VERSION
                CEF Version: $CHROME_VERSION
                Java Version: $javaVersion
                
                ------------
                Exception:
                ${e.stackTraceToString()}
            """.trimIndent()
        }

        runCatching {
            Files.createDirectories(path("UB-Crashes"))
            Files.writeString(
                path(
                    "UB-Crashes",
                    "crash-%s.txt".format(
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
                    )
                ), crashLog, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE
            )
        }
    }

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

fun path(path: String, vararg siblings: String): Path {
    return Paths.get(path, *siblings)
}

val appData: File = when {
    isWindows() -> File(System.getenv("APPDATA").toString())
    isLinux() -> File(System.getProperty("user.home").toString() + "/.config/")
    isMacintosh() -> File(System.getProperty("user.home").toString() + "/Library/Applications Support")
    else -> throw UnsupportedOperationException("Unsupported operating system: $osName")
}
val homeDir: File = when {
    isWindows() -> File(System.getProperty("user.home").toString())
    isLinux() -> File(System.getProperty("user.home").toString() + "/")
    isMacintosh() -> File(System.getProperty("user.home").toString() + "/")
    else -> File(System.getProperty("user.home").toString())
}
val dataDir: File = File(appData, "UltreonBrowser")
