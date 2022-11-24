package com.ultreon.browser.main

import com.ultreon.browser.*
import com.ultreon.browser.dialog.AboutDialog
import com.ultreon.browser.dialog.ProgressDialog
import com.ultreon.browser.dialog.settings.SettingsDialog
import com.ultreon.browser.intellijthemes.IJThemesPanel
import me.friwi.jcefmaven.CefAppBuilder
import me.friwi.jcefmaven.IProgressHandler
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter
import me.friwi.jcefmaven.impl.progress.ConsoleProgressHandler
import org.cef.CefApp
import org.cef.CefClient
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.browser.CefMessageRouter
import org.cef.handler.*
import org.cef.misc.BoolRef
import org.cef.network.CefRequest
import org.cef.network.CefResponse
import org.oxbow.swingbits.dialog.task.TaskDialog
import java.awt.*
import java.awt.event.*
import java.io.File
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.*
import kotlin.concurrent.thread
import kotlin.system.exitProcess
import me.friwi.jcefmaven.EnumProgress as SetupStage


/*
 * InternalFrameDemo.java requires:
 *   MyInternalFrame.java
 */
class UltreonBrowser : JFrame("$APP_NAME - $APP_VERSION") {
    private val dataDir: File = File(appData, "UltreonBrowser")
    internal var browserFocus: Boolean = false
    private lateinit var app: CefApp
    private lateinit var client: CefClient
    private lateinit var tabs: BrowserTabs
    private lateinit var toolBar: JToolBar
    private lateinit var address: JTextField
    private lateinit var prevBtn: JButton
    private lateinit var nextBtn: JButton
    private lateinit var searchBtn: JButton
    private val useOSR: Boolean = false

    init {
        // Set instance
        instance = this

        themesPanel = IJThemesPanel()

        // Get screen information.
        val environment = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val screen = environment.defaultScreenDevice
        val displayMode = screen.displayMode
        val position = screen.defaultConfiguration.bounds.location
        this.setSize(1024, 640)

        this.iconImage = ImageIO.read(APP_ICON_REF)

        // Set the frame in the middle of the default screen.
        this.setLocation(
            position.x + (displayMode.width - this.width) / 2,
            position.y + (displayMode.height - this.height) / 2
        )

        defaultCloseOperation = DO_NOTHING_ON_CLOSE

        contentPane = createContentPane()
        jMenuBar = createMenuBar()
    }

    private fun createContentPane(): Container {
        val pane = JPanel(BorderLayout())

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
        builder.setInstallDir(File("cef")) //Default
        builder.setProgressHandler(ConsoleProgressHandler()) //Default
        builder.cefSettings.windowless_rendering_enabled = useOSR //Default - select OSR mode
        builder.cefSettings.cache_path = dataDir.toString()
        builder.cefSettings.user_agent_product = "UltreonBrowser/$APP_VERSION Chrome/$CHROME_VERSION"

//        val progress = ProgressDialog(null, "Setting up CEF")
//        progress.size = Dimension(600, 450)
//        progress.pack()
//        progress.show()
//        builder.setProgressHandler { stage: SetupStage, fl: Float ->
//            progress.message = when (stage) {
//                SetupStage.INSTALL -> "Installing CEF..."
//                SetupStage.DOWNLOADING -> "Downloading files..."
//                SetupStage.EXTRACTING -> "Extracting files..."
//                SetupStage.LOCATING -> "Locating CEF..."
//                SetupStage.INITIALIZING -> "Initializing..."
//                SetupStage.INITIALIZED -> "Ready"
//            }
//
//            if (stage == SetupStage.INITIALIZED) {
//                println("Stop")
//            }
//
//            progress.setValue(fl)
//            progress.revalidate()
//        }

        builder.addJcefArgs(*argv)

        //Build a CefApp instance using the configuration above
        app = builder.build()
        client = app.createClient()

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                app.dispose()
                dispose()
            }
        })

        val msgRouter = CefMessageRouter.create()
        client.addMessageRouter(msgRouter)

        toolBar = createToolbar()
        tabs = BrowserTabs(client, this)

        pane.add(tabs, BorderLayout.CENTER)
        pane.add(toolBar, BorderLayout.NORTH)

        return pane
    }

    private fun createToolbar(): JToolBar {
        val toolBar = JToolBar()
        prevBtn = JButton("  ◀  ").also {
            it.addActionListener { tabs.selected.goBack()}
            toolBar.add(it)
        }
        nextBtn = JButton("  ▶  ").also {
            it.addActionListener { tabs.selected.goForward()}
            toolBar.add(it)
        }
        address = JTextField().also {
            it.addActionListener { _ -> tabs.selected.goTo(it.text) }
            toolBar.add(it)
        }
        searchBtn = JButton("  🔎  ").also {
            it.addActionListener { _ -> tabs.selected.goTo(it.text) }
            toolBar.add(it)
        }

        searchBtn = JButton("  ➕  ").also {
            it.addActionListener { tabs.createTab("https://google.com") }
            toolBar.add(it)
        }

        client.addFocusHandler(object : CefFocusHandlerAdapter() {
            override fun onGotFocus(browser: CefBrowser) {
                if (browserFocus) return
                browserFocus = true
                KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner()
                browser.setFocus(true)
            }

            override fun onTakeFocus(browser: CefBrowser, next: Boolean) {
                browserFocus = false
            }
        })

        // Update the address field when the browser URL changes.
        client.addDisplayHandler(object : CefDisplayHandlerAdapter() {
            override fun onAddressChange(browser: CefBrowser, frame: CefFrame, url: String) {
                address.text = url
            }

            override fun onTitleChange(browser: CefBrowser, title: String?) {
                tabs.onTitleChange(browser, title)
            }
        })

        // Update the address field when the browser URL changes.
        client.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser, frame: CefFrame, httpStatusCode: Int) {
                tabs.onLoadEnd(browser)
            }

            override fun onLoadStart(
                browser: CefBrowser, frame: CefFrame, transitionType: CefRequest.TransitionType) {
                tabs.onLoadStart(browser)
            }
        })

        // Update the address field when the browser URL changes.
        client.addRequestHandler(object : CefRequestHandlerAdapter() {
            override fun onOpenURLFromTab(
                browser: CefBrowser,
                frame: CefFrame,
                target_url: String,
                user_gesture: Boolean
            ): Boolean {
                tabs.createTab(target_url)
                return true
            }

            override fun getResourceRequestHandler(
                browser: CefBrowser?,
                frame: CefFrame?,
                request: CefRequest?,
                isNavigation: Boolean,
                isDownload: Boolean,
                requestInitiator: String?,
                disableDefaultHandling: BoolRef?
            ): CefResourceRequestHandler {
                return IconResourceHandler(
                    browser,
                    frame,
                    request,
                    isNavigation,
                    isDownload,
                    requestInitiator,
                    disableDefaultHandling
                )
            }
        })

        address.addFocusListener(object : FocusAdapter() {
            override fun focusGained(e: FocusEvent) {
                if (!browserFocus) return
                browserFocus = false
                KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner()
                address.requestFocus()
            }
        })

        return toolBar
    }

    private fun createMenuBar(): JMenuBar {
        val menuBar = JMenuBar()

        //Set up the lone menu.
        val windowMenu = JMenu("File")
        windowMenu.mnemonic = KeyEvent.VK_W
        menuBar.add(windowMenu)

        //Set up the first menu item.
        val openItem = JMenuItem("Open...")
        openItem.mnemonic = KeyEvent.VK_O
        openItem.accelerator = KeyStroke.getKeyStroke("control O")
        openItem.action = action("Open") { openFile() }
        windowMenu.add(openItem)

        //Set up the first menu item.
        val settingsItem = JMenuItem("Settings...")
        settingsItem.mnemonic = KeyEvent.VK_O
        settingsItem.accelerator = KeyStroke.getKeyStroke("control O")
        settingsItem.action = action("Settings") { configureTheme() }
        windowMenu.add(settingsItem)

        //Set up the second menu item.
        val quitItem = JMenuItem("Quit")
        quitItem.mnemonic = KeyEvent.VK_Q
        quitItem.accelerator = KeyStroke.getKeyStroke("alt F4")
        quitItem.action = action("Quit") { quit() }
        windowMenu.add(quitItem)

        val helpMenu = JMenu("Help")
        windowMenu.mnemonic = KeyEvent.VK_H
        menuBar.add(helpMenu)

        //Set up the first menu item.
        val aboutItem = JMenuItem("About")
        aboutItem.mnemonic = KeyEvent.VK_A
        aboutItem.accelerator = KeyStroke.getKeyStroke("F1")
        aboutItem.action = action("About") { showAbout() }
        helpMenu.add(aboutItem)

        //Set up the first menu item.
        val newIssueItem = JMenuItem("New Issue")
        newIssueItem.mnemonic = KeyEvent.VK_I
        newIssueItem.accelerator = KeyStroke.getKeyStroke("F8")
        newIssueItem.action = action("New Issue") { openNewIssuePage() }
        helpMenu.add(newIssueItem)

        //Set up the first menu item.
        val issueTrackerItem = JMenuItem("Issue Tracker")
        issueTrackerItem.mnemonic = KeyEvent.VK_S
        issueTrackerItem.accelerator = KeyStroke.getKeyStroke("control F8")
        issueTrackerItem.action = action("Issue Tracker") { openIssueTracker() }
        helpMenu.add(issueTrackerItem)

        return menuBar
    }

    /**
     * Opens the issues tracker page in the default browser.
     */
    private fun openIssueTracker() {
        tabs.createTab(ISSUES_URL)
    }

    private fun openNewIssuePage() {
        tabs.createTab(NEW_ISSUE_URL)
    }

    private fun showAbout() {
        AboutDialog(this, "About $APP_NAME", true).apply {
            setLocationRelativeTo(this@UltreonBrowser)
            isVisible = true
        }
    }

    private fun configureTheme() {
        SettingsDialog(this, "Settings", true).apply {
            setLocationRelativeTo(this@UltreonBrowser)
            isVisible = true
        }
    }

    private fun openFile() {
        val fileChooser = JFileChooser()
        fileChooser.showOpenDialog(this)
        val selectedFile = fileChooser.selectedFile
        selectedFile?.let {
            openFile(it)
        }
    }

    private fun openFile(selectedFile: File) {
        tabs.createTab(selectedFile.toURI().toString())
    }

    //Quit the application.
    private fun quit() {
        exitProcess(0)
    }

    companion object {
        lateinit var instance: UltreonBrowser
            private set

        lateinit var themesPanel: IJThemesPanel

        /**
         * Create the GUI and show it.  For thread safety,
         * this method should be invoked from the
         * event-dispatching thread.
         */
        fun start() {
            //Make sure we have nice window decorations.
            setDefaultLookAndFeelDecorated(true)

            //Create and set up the window.
            val frame = UltreonBrowser()

            //Display the window.
            frame.isVisible = true
        }
    }

    @Suppress("unused")
    inner class IconResourceHandler(
        val browser: CefBrowser?,
        val frame: CefFrame?,
        val request: CefRequest?,
        val isNavigation: Boolean,
        val isDownload: Boolean,
        val requestInitiator: String?,
        val disableDefaultHandling: BoolRef?
    ) : CefResourceRequestHandlerAdapter() {
        override fun onResourceResponse(
            browser: CefBrowser,
            frame: CefFrame,
            request: CefRequest,
            response: CefResponse
        ): Boolean {
            if (request.resourceType == CefRequest.ResourceType.RT_FAVICON) {
                val url = request.url
                if (url.startsWith("https://")) {
                    val url1 = URL(url)
                    thread {
                        val image = ImageIO.read(url1)
                        SwingUtilities.invokeLater {
                            tabs.onIconChange(browser, image)
                        }
                    }
                }
            }
            return false
        }
    }
}