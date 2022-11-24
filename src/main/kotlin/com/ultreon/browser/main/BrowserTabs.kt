package com.ultreon.browser.main

import org.cef.CefClient
import org.cef.browser.CefBrowser
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTabbedPane


class BrowserTabs(val client: CefClient, val main: UltreonBrowser) : JTabbedPane(TOP) {
    private val tabs = mutableListOf<BrowserTab>()
    private val browsers = mutableMapOf<CefBrowser, BrowserTab>()

    val selected: BrowserTab
        get() {
            return tabs[selectedIndex]
        }

    init {
        createTab("https://google.com")
    }

    fun createTab(url: String, background: Boolean = false) {
        val browserTab = BrowserTab(this, client, main, url)
        tabs += browserTab
        browsers[browserTab.browser] = browserTab
        this.addTab(url, browserTab)
        val index: Int = this.indexOfTab(url)
        val pnlTab = JPanel(GridBagLayout())
        pnlTab.isOpaque = false
        val lblTitle = JLabel(url)
        val btnClose = JButton("❌")
        btnClose.font = Font(null, Font.PLAIN, 8)
        btnClose.putClientProperty( "JButton.buttonType", "roundRect" )

        val gbc = GridBagConstraints()
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.weightx = 1.0

        pnlTab.add(lblTitle, gbc)

        gbc.gridx++
        gbc.weightx = 0.0
        pnlTab.add(btnClose, gbc)

        pnlTab.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.button == 2) {
                    removeTabAt(indexOfTab(browserTab))
                }
            }
        })

        this.setTabComponentAt(index, pnlTab)

        btnClose.addActionListener(CloseButtonHandler())

        if (!background) {
            selectedIndex = indexOfTab(browserTab)
        }
    }

    override fun removeTabAt(index: Int) {
        super.removeTabAt(index)
        val browserTab = tabs.removeAt(index)
        browserTab.browser.close(true)
    }

    fun onTitleChange(browser: CefBrowser, title: String?) {
        val browserTab = browsers[browser]
        browserTab?.updateTitle(title) ?: throw IllegalArgumentException("Browser tab not found: ${browser.identifier}")
    }

    fun indexOfTab(browserTab: BrowserTab): Int {
        return tabs.indexOf(browserTab)
    }

    inner class CloseButtonHandler : ActionListener {
        override fun actionPerformed(evt: ActionEvent) {
            val selected: Int = this@BrowserTabs.selectedIndex
            if (selected >= 0) {
                this@BrowserTabs.removeTabAt(selected)
                // It would probably be worthwhile getting the source
                // casting it back to a JButton and removing
                // the action handler reference ;)
            }
        }
    }
}