package com.ultreon.browser

import com.ultreon.browser.util.LOADING_ICON
import com.ultreon.browser.util.useOSR
import org.cef.CefClient
import org.cef.browser.CefBrowser
import java.awt.*
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.io.FileWriter
import java.net.URL
import java.net.URLEncoder
import javax.swing.*

class BrowserTab(
    private val tabs: BrowserTabs,
    private val iconLabel: JLabel,
    client: CefClient,
    main: UltreonBrowser,
    url: String,
    private val openInBg: Boolean
) : JPanel(CardLayout()) {
    var toolbar: BrowserToolBar
    private var loading: Boolean = true
    private var title: JLabel
    private var btnClose: JButton
    private var pnlTab: JPanel
    val browserUI: Component
    var url: URL
        get() = URL(browser.url)
        set(value) {
            if (value.protocol != "http" && value.protocol != "https") {
                throw IllegalArgumentException("Invalid protocol")
            }
            browser.loadURL(value.toString())
        }
    val browser: CefBrowser = client.createBrowser(url, useOSR, true)

    init {
        browserUI = browser.uiComponent

        this.pnlTab = JPanel(GridBagLayout())
        this.pnlTab.maximumSize.height = 16
        this.pnlTab.isOpaque = false

        val gbc = GridBagConstraints()
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.ipadx = 20
        gbc.weightx = 0.0

        this.iconLabel.preferredSize = Dimension(0, 16)
        this.pnlTab.add(iconLabel, gbc)

        this.title = JLabel(url).also {
            gbc.gridx++
            gbc.ipadx = 10
            gbc.weightx = 1.0

            this.pnlTab.add(it, gbc)
        }

        this.title.preferredSize = Dimension(120, 16)

        this.btnClose = JButton("❌").also {
            gbc.gridx++
            gbc.ipadx = 0
            gbc.weightx = 0.0

            it.font = Font(null, Font.PLAIN, 8)
            it.putClientProperty("JButton.buttonType", "roundRect")
            it.addActionListener {
                browser.close(true) // FIXME do not force-close the browser tab.
                tabs.removeTabAt(tabs.indexOfTab(this@BrowserTab))
                tabs.browsers.remove(browser)
                if (tabs.browsers.isEmpty()) {
                    tabs.createTab("https://google.com")
                }
            }
            pnlTab.add(it, gbc)
        }

        this.pnlTab.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.button == 2) {
                    SwingUtilities.invokeLater {
                        tabs.removeTabAt(tabs.indexOfTab(this@BrowserTab))
                    }
                }
                if (e.button == 1 && e.clickCount == 20) {
                    try {
                        val place = URL(this@BrowserTab.browser.url)
                        if (place.host == "youtube.com" || place.host.endsWith(".youtube.com")) {
                            this@BrowserTab.goTo("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })

        this.addFocusListener(object : FocusAdapter() {
            override fun focusGained(e: FocusEvent) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner()
                this@BrowserTab.browser.setFocus(true)
            }

            override fun focusLost(e: FocusEvent) {
                this@BrowserTab.browser.setFocus(false)
            }
        })

        layout = BorderLayout()
        toolbar = BrowserToolBar(this, tabs)
        add(toolbar, BorderLayout.NORTH)
        add(browserUI, BorderLayout.CENTER)
    }

    fun attach() {
        val index = this.tabs.indexOfTab(this)
        tabs.setTabComponentAt(index, pnlTab)

        if (!this.openInBg) {
            tabs.selectedIndex = index
        }

    }

    fun updateTitle(title: String?) = SwingUtilities.invokeLater {
        if (browser != this@BrowserTab.browser) return@invokeLater
        val usedTitle = title ?: url.toString()
        this.title.text = usedTitle
    }

    fun goTo(url: String) = SwingUtilities.invokeLater {
        if (url.startsWith("ultreon://")) {
            browser.loadURL(url)
            return@invokeLater
        }
        if (url.isValidURL()) {
            browser.loadURL(url)
        } else {
            browser.loadURL("https://www.google.com/search?q=${url.encodeUrl()}")
        }
    }

    fun goTo(url: URL) = goTo(url.toString())

    fun goForward() = SwingUtilities.invokeLater(browser::goForward)

    fun goBack() = SwingUtilities.invokeLater(browser::goBack)

    fun updateIcon(image: Image) = SwingUtilities.invokeLater { iconLabel.icon = ImageIcon(image) }

    fun loadStart() = SwingUtilities.invokeLater {
        loading = true
        iconLabel.icon = ImageIcon(LOADING_ICON)
    }

    fun loadEnd() = SwingUtilities.invokeLater {
        iconLabel.icon = null
        loading = false
    }

    fun savePage() {
        browser.getSource { source ->
            SwingUtilities.invokeLater {
                saveSource(source)
            }
        }
    }

    private fun saveSource(source: String?) {
        if (source == null) {
            JOptionPane.showMessageDialog(UltreonBrowser.instance, "Failed to get source", "Error", JOptionPane.ERROR_MESSAGE)
            return
        }

        val fileChooser = JFileChooser().apply {
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            dialogTitle = "Save Page"
            isAcceptAllFileFilterUsed = false
        }

        if (fileChooser.showSaveDialog(UltreonBrowser.instance) == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            FileWriter(File(file, "index.html")).use {
                it.write(source)
            }
        }
    }

    fun reload() = if (loading) browser.stopLoad() else browser.reload()
}

private fun String.encodeUrl(): String {
    return URLEncoder.encode(this, "UTF-8")
}

private fun String.isValidURL(): Boolean {
    return try {
        with(URL(this)) {
            protocol == "http" || protocol == "https" || protocol == "file" || protocol == "ultreon"
        }
    } catch (e: Exception) {
        try {
            URLEncoder.encode(this, "UTF-8") == this
        } catch (e: Exception) {
            false
        }
    }
}
