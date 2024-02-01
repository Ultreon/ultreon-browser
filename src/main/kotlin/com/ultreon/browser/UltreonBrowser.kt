package com.ultreon.browser

import com.ultreon.browser.dialog.AboutDialog
import com.ultreon.browser.dialog.settings.SettingsDialog
import com.ultreon.browser.handler.*
import com.ultreon.browser.intellijthemes.IJThemesPanel
import com.ultreon.browser.util.*
import org.cef.CefApp
import org.cef.CefClient
import org.cef.browser.CefBrowser
import org.cef.browser.CefMessageRouter
import org.cef.callback.CefBeforeDownloadCallback
import org.cef.callback.CefContextMenuParams
import org.cef.callback.CefDownloadItem
import org.cef.callback.CefDownloadItemCallback
import org.cef.handler.CefDownloadHandlerAdapter
import org.cef.handler.CefFocusHandlerAdapter
import java.awt.*
import java.awt.datatransfer.StringSelection
import java.awt.event.*
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.*
import kotlin.system.exitProcess


val dataDir: File = File(appData, "UltreonBrowser")

/*
 * InternalFrameDemo.java requires:
 *   MyInternalFrame.java
 */
class UltreonBrowser(val app: CefApp) : JFrame("$APP_NAME - $APP_VERSION") {
    private val downloadsDir: File = File(homeDir, "Downloads")
    internal var browserFocus: Boolean = false
    private lateinit var tabs: BrowserTabs
    private lateinit var client: CefClient
    private lateinit var toolBar: JToolBar
    private lateinit var address: JTextField
    private lateinit var prevBtn: JButton
    private lateinit var nextBtn: JButton

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
        client.addMessageRouter(msgRouter)

        tabs = BrowserTabs(client, this)
        toolBar = createToolbar()

        pane.add(tabs, BorderLayout.CENTER)
        pane.add(toolBar, BorderLayout.NORTH)
    }

    private fun createToolbar(): JToolBar {
        val toolBar = JToolBar()
        prevBtn = JButton("  ◀  ").apply {
            addActionListener { tabs.selected.goBack()}
            toolBar.add(this)
        }
        nextBtn = JButton("  ▶  ").apply {
            addActionListener { tabs.selected.goForward()}
            toolBar.add(this)
        }
        JButton("  🔄️  ").apply {
            addActionListener {
                tabs.selected.reload()
            }
            toolBar.add(this)
        }
        address = JTextField().apply {
            addActionListener { _ -> tabs.selected.goTo(text) }
            toolBar.add(this)
        }
        JButton("  🔎  ").apply {
            addActionListener { _ -> tabs.selected.goTo(text) }
            toolBar.add(this)
        }
        JButton("  📥  ").apply {
            addActionListener { _ ->
                DownloadManager.isVisible = true
            }
            toolBar.add(this)
        }

        JButton("  ➕  ").apply {
            addActionListener { tabs.createTab("https://google.com") }
            toolBar.add(this)
        }

        // Client handlers
        client.addDisplayHandler(DisplayHandler(this.tabs, this.address))
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
                browser.setFocus(true)
            }

            override fun onTakeFocus(browser: CefBrowser, next: Boolean) {
                browserFocus = false
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
            DownloadManager.onUpdate(id, fullPath, url, totalBytes, receivedBytes, percentComplete, speed, endTime, isComplete, isCanceled, downloadItem.isValid, callback)
        }
    }

    private fun downloadItem(callback: CefBeforeDownloadCallback, suggestedName: String = "file") {
        val file = File(downloadsDir, suggestedName).let {
            if (!downloadsDir.exists()) {
                if (!downloadsDir.mkdirs()){
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

        //Set up the first menu item.
        JMenuItem("Open...").apply {
            mnemonic = KeyEvent.VK_O
            accelerator = KeyStroke.getKeyStroke("control O")
            action = com.ultreon.browser.util.action("Open") { openFile() }
            windowMenu.add(this)
        }

        //Set up the first menu item.
        JMenuItem("Settings...").apply {
            mnemonic = KeyEvent.VK_O
            accelerator = KeyStroke.getKeyStroke("control O")
            action = com.ultreon.browser.util.action("Settings") { configureTheme() }
            windowMenu.add(this)
        }

        //Set up the second menu item.
        JMenuItem("Quit").apply {
            mnemonic = KeyEvent.VK_Q
            accelerator = KeyStroke.getKeyStroke("alt F4")
            action = com.ultreon.browser.util.action("Quit") { quit() }
            windowMenu.add(this)
        }

        val helpMenu = JMenu("Help")
        windowMenu.mnemonic = KeyEvent.VK_H
        menuBar.add(helpMenu)

        //Set up the first menu item.
        JMenuItem("About").apply {
            mnemonic = KeyEvent.VK_A
            accelerator = KeyStroke.getKeyStroke("F1")
            action = com.ultreon.browser.util.action("About") { showAbout() }
            helpMenu.add(this)
        }

        //Set up the first menu item.
        JMenuItem("New Issue").apply {
            mnemonic = KeyEvent.VK_I
            accelerator = KeyStroke.getKeyStroke("F8")
            action = com.ultreon.browser.util.action("New Issue") { openNewIssuePage() }
            helpMenu.add(this)
        }

        //Set up the first menu item.
        JMenuItem("Issue Tracker").apply {
            mnemonic = KeyEvent.VK_S
            accelerator = KeyStroke.getKeyStroke("control F8")
            action = com.ultreon.browser.util.action("Issue Tracker") { openIssueTracker() }
            helpMenu.add(this)
        }

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

    fun savePage() {
        tabs.savePage()
    }

    fun copyLink(linkUrl: String) {
        if (linkUrl != null) {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            clipboard.setContents(StringSelection(linkUrl), null)
        } else {
            JOptionPane.showMessageDialog(this, "No link to copy")
        }
    }

    fun openLinkInNewTab(linkUrl: String) {
        if (linkUrl != null) {
            tabs.createTab(linkUrl)
        } else {
            JOptionPane.showMessageDialog(this, "No link to open")
        }
    }

    fun selectAll(browser: CefBrowser, params: CefContextMenuParams) {

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

            //Display the window.
            frame.isVisible = true
        }
    }

}