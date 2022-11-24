package com.ultreon.browser.main

import com.ultreon.browser.LOADING_ICON
import com.ultreon.browser.useOSR
import org.cef.CefClient
import org.cef.browser.CefBrowser
import java.awt.CardLayout
import java.awt.Component
import java.awt.Image
import java.awt.KeyboardFocusManager
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JPanel

class BrowserTab(val tabs: BrowserTabs, val icon: JLabel, client: CefClient, main: UltreonBrowser, val url: String) : JPanel(CardLayout()) {
    private val browserUI: Component
    val browser: CefBrowser

    init {
        browser = client.createBrowser(url, useOSR, true)
        browserUI = browser.uiComponent

        addFocusListener(object : FocusAdapter() {
            override fun focusGained(e: FocusEvent?) {
                if (main.browserFocus) return
                if (browser != this@BrowserTab.browser) return
                main.browserFocus = true
                KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner()
                browser.setFocus(true)
            }
            override fun focusLost(e: FocusEvent?) {
                if (browser != this@BrowserTab.browser) return
                main.browserFocus = false
            }
        })

        add(browserUI)
    }

    fun updateTitle(title: String?) {
        if (browser != this@BrowserTab.browser) return
        val usedTitle = title ?: url
        tabs.setTitleAt(tabs.indexOfTab(this), usedTitle)
    }

    fun goTo(url: String) {
        browser.loadURL(url)
    }

    fun goForward() {
        browser.goForward()
    }

    fun goBack() {
        browser.goBack()
    }

    fun updateIcon(image: Image) {
        icon.icon = ImageIcon(image)
    }

    fun loadStart() {
        icon.icon = ImageIcon(LOADING_ICON)
        icon.isVisible = true
    }

    fun loadEnd() {
        icon.icon = null
        icon.isVisible = false
    }
}
