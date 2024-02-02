package com.ultreon.browser

import com.ultreon.browser.dialog.AboutDialog
import com.ultreon.browser.dialog.settings.SettingsDialog
import com.ultreon.browser.handler.*
import com.ultreon.browser.intellijthemes.IJThemesPanel
import com.ultreon.browser.util.*
import homeDir
import org.cef.CefApp
import org.cef.CefClient
import org.cef.browser.CefBrowser
import org.cef.browser.CefMessageRouter
import org.cef.callback.*
import org.cef.handler.CefDownloadHandlerAdapter
import org.cef.handler.CefFocusHandlerAdapter
import org.cef.handler.CefKeyboardHandler
import java.awt.*
import java.awt.datatransfer.StringSelection
import java.awt.event.*
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.*
import kotlin.system.exitProcess


/*
 * InternalFrameDemo.java requires:
 *   MyInternalFrame.java
 */
class UltreonBrowser(val app: CefApp) : JFrame("$APP_NAME - $APP_VERSION") {
    private var menu: JMenuBar
    private var screen: GraphicsDevice? = null
    private var fullscreenTab: BrowserTab? = null
    private var browserUI: Component? = null
    private var fullscreen: Boolean = false
    private val downloadsDir: File = File(homeDir, "Downloads")
    internal var browserFocus: Boolean = false
    private lateinit var tabs: BrowserTabs
    private lateinit var client: CefClient

    init {
        // Set instance
        instance = this

        themesPanel = IJThemesPanel()

        layout = BorderLayout()
        isResizable = true

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
        menu = createMenuBar()
        jMenuBar = menu
    }

    private fun createContentPane(): Container {
        val pane = JPanel(BorderLayout())

        create(pane)

        return pane
    }

    private fun create(pane: JPanel) {
        client = app.createClient()

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                app.dispose()
                dispose()
            }
        })

        val msgRouter = CefMessageRouter.create()
        msgRouter.addHandler(MessageRouterHandler(this, msgRouter), true)
        client.addMessageRouter(msgRouter)

        tabs = BrowserTabs(client, this)

        this.setupHandlers()

        pane.add(tabs, BorderLayout.CENTER)
    }

    private fun setupHandlers() {
        // Client handlers
        client.addDisplayHandler(DisplayHandler(this.tabs))
        client.addLoadHandler(LoadHandler(this.tabs))
        client.addJSDialogHandler(JSDialogHandler(this))
        client.addRequestHandler(RequestHandler(this, this.tabs))
        client.addContextMenuHandler(ContextMenuHandler(this))

        client.addDownloadHandler(object : CefDownloadHandlerAdapter() {
            override fun onBeforeDownload(
                browser: CefBrowser?,
                downloadItem: CefDownloadItem?,
                suggestedName: String?,
                callback: CefBeforeDownloadCallback
            ) {
                SwingUtilities.invokeLater {
                    val result = JOptionPane.showConfirmDialog(
                        this@UltreonBrowser.tabs,
                        "Are you sure you want to download the following file?\n\n$suggestedName",
                        "Attempting to download: $suggestedName",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                    )

                    if (result == JOptionPane.YES_OPTION)
                        this@UltreonBrowser.downloadItem(callback, suggestedName ?: "file")
                    else callback.Continue(null, false)
                }
            }

            override fun onDownloadUpdated(
                browser: CefBrowser,
                downloadItem: CefDownloadItem,
                callback: CefDownloadItemCallback
            ) {
                this@UltreonBrowser.onDownloadUpdated(downloadItem, callback)
            }
        })

        client.addFocusHandler(object : CefFocusHandlerAdapter() {
            override fun onGotFocus(browser: CefBrowser) {
                if (browserFocus) return
                browserFocus = true
                KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner()
                tabs.browsers[browser]?.let {
                    tabs.select(it)
                }
            }

            override fun onTakeFocus(browser: CefBrowser, next: Boolean) {
                browserFocus = false
                browser.setFocus(false)
                KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent()
            }
        })

        client.addKeyboardHandler(KeyboardHandler(this))

        this.addFocusListener(object : FocusAdapter() {
            override fun focusGained(e: FocusEvent) {
                if (browserFocus) return
                browserFocus = true
            }

            override fun focusLost(e: FocusEvent) {
                browserFocus = false
                tabs.selected?.browser?.setFocus(false)
            }
        })
    }

    private fun onDownloadUpdated(
        downloadItem: CefDownloadItem,
        callback: CefDownloadItemCallback
    ) {
        val id = downloadItem.id
        val fullPath = downloadItem.fullPath
        val url = downloadItem.url
        val totalBytes = downloadItem.totalBytes
        val receivedBytes = downloadItem.receivedBytes
        val percentComplete = downloadItem.percentComplete
        val speed = downloadItem.currentSpeed
        val endTime = downloadItem.endTime
        val isComplete = downloadItem.isComplete
        val isCanceled = downloadItem.isCanceled

        SwingUtilities.invokeLater {
            DownloadManager.onUpdate(
                id,
                fullPath,
                url,
                totalBytes,
                receivedBytes,
                percentComplete,
                speed,
                endTime,
                isComplete,
                isCanceled,
                downloadItem.isValid,
                callback
            )
        }
    }

    private fun downloadItem(callback: CefBeforeDownloadCallback, suggestedName: String = "file") {
        val file = File(downloadsDir, suggestedName).let {
            if (!downloadsDir.exists()) {
                if (!downloadsDir.mkdirs()) {
                    JOptionPane.showMessageDialog(this, "Could not create downloads directory: $downloadsDir")
                    return
                }
            }

            val chooser = JFileChooser().apply {
                fileSelectionMode = JFileChooser.FILES_ONLY
                dialogType = JFileChooser.SAVE_DIALOG
                dialogTitle = "Save $it"
                selectedFile = it
            }
            val showDialog = chooser.showDialog(this, "Download Here")

            if (showDialog != JFileChooser.APPROVE_OPTION) callback.Continue(null, false)

            return@let chooser.selectedFile
        }

        callback.Continue(file.path, false)
    }

    private fun createMenuBar(): JMenuBar {
        val menuBar = JMenuBar()

        //Set up the lone menu.
        val windowMenu = JMenu("File")
        windowMenu.mnemonic = KeyEvent.VK_W
        menuBar.add(windowMenu)

        JMenuItem("New Tab").apply {
            mnemonic = KeyEvent.VK_T
            accelerator = KeyStroke.getKeyStroke("control T")
            action = action("New Tab", ::newTab)
            windowMenu.add(this)
        }

        //Set up the first menu item.
        JMenuItem("Open...").apply {
            mnemonic = KeyEvent.VK_O
            accelerator = KeyStroke.getKeyStroke("control O")
            action = action("Open", ::openFile)
            windowMenu.add(this)
        }

        JSeparator().apply { windowMenu.add(this) }

        //Set up the first menu item.
        JMenuItem("Settings...").apply {
            mnemonic = KeyEvent.VK_S
            accelerator = KeyStroke.getKeyStroke("control O")
            action = action("Settings", ::configureTheme)
            windowMenu.add(this)
        }

        JSeparator().apply { windowMenu.add(this) }

        //Set up the second menu item.
        JMenuItem("Quit").apply {
            mnemonic = KeyEvent.VK_Q
            accelerator = KeyStroke.getKeyStroke("alt F4")
            action = action("Quit", ::quit)
            windowMenu.add(this)
        }

        val helpMenu = JMenu("Help")
        windowMenu.mnemonic = KeyEvent.VK_H
        menuBar.add(helpMenu)

        //Set up the first menu item.
        JMenuItem("About").apply {
            mnemonic = KeyEvent.VK_A
            accelerator = KeyStroke.getKeyStroke("F1")
            action = action("About", ::showAbout)
            helpMenu.add(this)
        }

        //Set up the first menu item.
        JMenuItem("New Issue").apply {
            mnemonic = KeyEvent.VK_I
            accelerator = KeyStroke.getKeyStroke("F8")
            action = action("New Issue", ::openNewIssuePage)
            helpMenu.add(this)
        }

        //Set up the first menu item.
        JMenuItem("Issue Tracker").apply {
            mnemonic = KeyEvent.VK_S
            accelerator = KeyStroke.getKeyStroke("control F8")
            action = action("Issue Tracker", ::openIssueTracker)
            helpMenu.add(this)
        }

        return menuBar
    }

    private fun newTab() {
        val tab = tabs.createTabDirectly("https://google.com")
        tab.toolbar.address.grabFocus()
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

    fun savePage() {
        tabs.savePage()
    }

    fun copyLink(linkUrl: String) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(linkUrl), null)
    }

    fun openLinkInNewTab(linkUrl: String) {
        tabs.createTab(linkUrl)
    }

    fun selectAll(browser: CefBrowser, params: CefContextMenuParams) {
        browser.executeJavaScript("window.getSelection().selectAllChildren(document.body)", "ultreon:///dynamic.js", 0)
    }

    fun takeScreenshot(browser: CefBrowser) {
        browser.createScreenshot(false).thenAccept {
            if (it != null) {
                SwingUtilities.invokeLater {
                    saveTakenScreenshot(it)
                }
            }
        }
    }

    private fun saveTakenScreenshot(it: BufferedImage?) {
        val chooser = JFileChooser().apply {
            fileSelectionMode = JFileChooser.FILES_ONLY
            dialogType = JFileChooser.SAVE_DIALOG
            isAcceptAllFileFilterUsed = false
            dialogTitle = "Save Screenshot"
            selectedFile = File(homeDir, "screenshot.png")
        }

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            ImageIO.write(it, "png", chooser.selectedFile)
        }
    }

    fun reload(browser: CefBrowser) {
        browser.reload()
    }

    fun reloadIgnoreCache(browser: CefBrowser) {
        browser.reloadIgnoreCache()
    }

    fun handleUrl(request: String, callback: CefQueryCallback?) {
        URL(request).let {
            if (!it.protocol.startsWith("ultreon")) {
                return
            }

            it.path.let { path ->
                if (path == "/favicon.ico") return
                if (path == "/favicon.png") return

                // TODO: Add webpage support
                when (path) {
                    "/issues" -> {
                        openIssueTracker()
                        callback?.success("")
                    }
                    "/new-issue" -> {
                        openNewIssuePage()
                        callback?.success("")
                    }
                    "/about" -> {
                        showAbout()
                        callback?.success("")
                    }
                    "/theme" -> {
                        configureTheme()
                        callback?.success("")
                    }
                    else -> {

                    }
                }
            }
        }
    }

    fun handleUrlSafe(request: String, callback: CefQueryCallback?) {
        URL(request).let {
            if (!it.protocol.startsWith("ultreon")) {
                return
            }

            it.path.let {
                when (it) {
                    "/info" -> {
                        val info = """
                            {
                                "name": "$APP_NAME",
                                "version": "$APP_VERSION"
                            }
                        """.trimIndent()

                        callback?.success(info)
                    }

                    else -> {
                        callback?.failure(404, "Page not found")
                    }
                }
            }
        }
    }

    fun onKeyDown(event: CefKeyboardHandler.CefKeyEvent): Boolean {
        event.windows_key_code.let {
            if (it == KeyEvent.VK_F5) tabs.selected?.reload()
            else if (it == KeyEvent.VK_F11) {
                SwingUtilities.invokeLater {
                    tabs.selected?.let selectTab@{ tab ->
                        if (fullscreen) {
                            val fTab = fullscreenTab ?: return@selectTab
                            disableFullscreen(fTab)
                        } else {
                            enableFullscreen(tab)
                        }
                    }
                }
            } else if (it == KeyEvent.VK_F1) {
                showAbout()
            } else if (it == KeyEvent.VK_S && event.modifiers and KeyEvent.CTRL_DOWN_MASK != 0) {
                savePage()
            } else if (it == KeyEvent.VK_O && event.modifiers and KeyEvent.CTRL_DOWN_MASK != 0) {
                openFile()
            } else if (it == KeyEvent.VK_T && event.modifiers and KeyEvent.CTRL_DOWN_MASK != 0) {
                newTab()
            } else if (it == KeyEvent.VK_N && event.modifiers and KeyEvent.CTRL_DOWN_MASK != 0) {
                newTab()
            } else if (it == KeyEvent.VK_W && event.modifiers and KeyEvent.CTRL_DOWN_MASK != 0) {
                tabs.selected?.let { tab -> tabs.closeTab(tab) }
            } else {
                return false
            }
            return true
        }
    }

    private fun disableFullscreen(tab: BrowserTab) {
        fullscreen = false
        extendedState = NORMAL
        this.browserUI = null
        this.remove(tab.browserUI)
        this.screen?.fullScreenWindow = null
        this.screen = null
        this.tabs.isVisible = true
        tab.add(tab.browserUI)
        this.jMenuBar = createMenuBar()
        tab.revalidate()
        this.doLayout()
        this.revalidate()
        this.fullscreenTab = null
    }

    private fun enableFullscreen(tab: BrowserTab) {
        val bounds = this.bounds
        GraphicsEnvironment.getLocalGraphicsEnvironment().also { environment ->
            val screen = environment.screenDevices.filter {
                val displayMode = it.defaultConfiguration
                return@filter displayMode.bounds.intersects(bounds)
            }.first()

            fullscreen = true
            extendedState = MAXIMIZED_BOTH
            tab.remove(tab.browserUI)
            this.screen = screen
            screen.fullScreenWindow = this
            this.fullscreenTab = tab
            this.tabs.isVisible = false
            this.jMenuBar = null
            this.browserUI = tab.browserUI
            this.add(tab.browserUI)
            tab.revalidate()
            this.doLayout()
            this.revalidate()
        }
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
        fun start(app: CefApp) {
            //Make sure we have nice window decorations.
            setDefaultLookAndFeelDecorated(true)

            //Create and set up the window.
            val frame = UltreonBrowser(app)

            logInfo("Started browser!")

            //Display the window.
            frame.isVisible = true
        }
    }

}