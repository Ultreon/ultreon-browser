package com.ultreon.browser.main

import com.ultreon.browser.LOADING_ICON
import com.ultreon.browser.useOSR
import org.cef.CefClient
import org.cef.browser.CefBrowser
import java.awt.*
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.net.URL
import javax.swing.*

class BrowserTab(val tabs: BrowserTabs, val icon: JLabel, client: CefClient, main: UltreonBrowser, val url: String, val openInBg: Boolean) : JPanel(CardLayout()) {
    private var title: JLabel
    private var btnClose: JButton
    private var pnlTab: JPanel
    private val browserUI: Component
    val browser: CefBrowser = client.createBrowser(url, useOSR, true)

    private val focusAdapter = object : FocusAdapter() {
        override fun focusGained(e: FocusEvent) {
            if (main.browserFocus) return
            main.browserFocus = true
            KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner()
            browser.setFocus(true)
        }

        override fun focusLost(e: FocusEvent) {
            main.browserFocus = false
            browser.setFocus(false)
        }
    }

    init {
        browserUI = browser.uiComponent

        main.addFocusListener(focusAdapter)

        this.pnlTab = JPanel(GridBagLayout())
        this.pnlTab.maximumSize.height = 16
        this.pnlTab.isOpaque = false

        val gbc = GridBagConstraints()
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.ipadx = 20
        gbc.weightx = 0.0

        this.icon.preferredSize = Dimension(0, 16)
        this.pnlTab.add(icon, gbc)

        this.title = JLabel(url).also {
            gbc.gridx++
            gbc.ipadx = 10
            gbc.weightx = 1.0

            this.pnlTab.add(it, gbc)
        }

        this.title.preferredSize = Dimension(80, 16)

        this.btnClose = JButton("❌").also {
            gbc.gridx++
            gbc.ipadx = 0
            gbc.weightx = 0.0

            it.font = Font(null, Font.PLAIN, 8)
            it.putClientProperty("JButton.buttonType", "roundRect")
            it.addActionListener {
                browser.close(true) // FIXME do not force-close the browser tab.
                tabs.removeTabAt(tabs.indexOfTab(this@BrowserTab))
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

        add(browserUI)
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
        val usedTitle = title ?: url
        this.title.text = usedTitle
    }

    fun goTo(url: String) = SwingUtilities.invokeLater {
        browser.loadURL(url)
    }

    fun goForward() = SwingUtilities.invokeLater {
        browser.goForward()
    }

    fun goBack() = SwingUtilities.invokeLater {
        browser.goBack()
    }

    fun updateIcon(image: Image) {
        icon.icon = ImageIcon(image)
    }

    fun loadStart() = SwingUtilities.invokeLater {
        icon.icon = ImageIcon(LOADING_ICON)
    }

    fun loadEnd() = SwingUtilities.invokeLater {
        icon.icon = null
    }
}
