package com.ultreon.browser.main

import com.ultreon.browser.LOADING_ICON
import org.cef.CefClient
import org.cef.browser.CefBrowser
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.net.URL
import javax.swing.*


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
        val icon = JLabel(ImageIcon(LOADING_ICON))

        icon.maximumSize = Dimension(16, 16)
        icon.size = Dimension(16, 16)
        icon.preferredSize = Dimension(16, 16)
        icon.minimumSize = Dimension(16, 16)
        val browserTab = BrowserTab(this, icon, client, main, url)
        tabs += browserTab
        browsers[browserTab.browser] = browserTab
        this.addTab(url, browserTab)
        val index: Int = this.indexOfTab(url)
        val pnlTab = JPanel(GridBagLayout())
        pnlTab.maximumSize.height = 16
        pnlTab.isOpaque = false
        val lblTitle = JLabel(url)
        val btnClose = JButton("❌")
        btnClose.font = Font(null, Font.PLAIN, 8)
        btnClose.putClientProperty( "JButton.buttonType", "roundRect" )

        val gbc = GridBagConstraints()
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.ipadx = 20
        gbc.weightx = 0.0
        pnlTab.add(icon, gbc)

        gbc.gridx++
        gbc.ipadx = 10
        gbc.weightx = 1.0

        pnlTab.add(lblTitle, gbc)

        gbc.gridx++
        gbc.ipadx = 0
        gbc.weightx = 0.0
        pnlTab.add(btnClose, gbc)

        pnlTab.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.button == 2) {
                    removeTabAt(indexOfTab(browserTab))
                }
                if (e.button == 1) {
                    println("LEFT CLICK :: COUNT ${e.clickCount}")
                    if (e.clickCount == 20) {
                        try {
                            val place = URL(browserTab.browser.url)
                            println(place.host)
                            if (place.host == "youtube.com" || place.host.endsWith(".youtube.com")) {
                                println("YouTube")
                                browserTab.goTo("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
                            } else {
                                println("Not YouTube")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
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

    fun onIconChange(browser: CefBrowser, image: Image): Boolean {
        val browserTab = browsers[browser]
        browserTab?.updateIcon(image) ?: return false
        return true
    }

    fun onLoadStart(browser: CefBrowser?): Boolean {
        val browserTab = browsers[browser]
        browserTab?.loadStart() ?: return false
        return true
    }

    fun onLoadEnd(browser: CefBrowser?): Boolean {
        val browserTab = browsers[browser]
        browserTab?.loadEnd() ?: return false
        return true
    }

    fun onTitleChange(browser: CefBrowser, title: String?): Boolean {
        val browserTab = browsers[browser]
        browserTab?.updateTitle(title) ?: return false
        return true
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