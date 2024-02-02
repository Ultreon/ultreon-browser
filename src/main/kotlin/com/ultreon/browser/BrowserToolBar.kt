package com.ultreon.browser

import com.ultreon.browser.util.DownloadManager
import java.awt.KeyboardFocusManager
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.JButton
import javax.swing.JTextField
import javax.swing.JToolBar

class BrowserToolBar(
    val tab: BrowserTab,
    private val tabs: BrowserTabs
) : JToolBar() {
    val address: JTextField
    val nextBtn: JButton
    val prevBtn: JButton

    init {
        prevBtn = JButton("  ◀  ").apply {
            addActionListener { tab.goBack()}
            this@BrowserToolBar.add(this)
        }
        nextBtn = JButton("  ▶  ").apply {
            addActionListener { tab.goForward()}
            this@BrowserToolBar.add(this)
        }
        JButton("  🔄️  ").apply {
            addActionListener {
                tab.reload()
            }
            this@BrowserToolBar.add(this)
        }
        address = JTextField().apply {
            addActionListener { _ -> tab.goTo(text) }
            this@BrowserToolBar.add(this)
        }
        JButton("  🔎  ").apply {
            addActionListener { _ -> tab.goTo(text) }
            this@BrowserToolBar.add(this)
        }
        JButton("  📥  ").apply {
            addActionListener { _ ->
                DownloadManager.isVisible = true
            }
            this@BrowserToolBar.add(this)
        }

        JButton("  ➕  ").apply {
            addActionListener { tabs.createTab("https://google.com") }
            this@BrowserToolBar.add(this)
        }


        address.addFocusListener(object : FocusAdapter() {
            override fun focusGained(e: FocusEvent) {
                if (!UltreonBrowser.instance.browserFocus) return
                UltreonBrowser.instance.browserFocus = false
                KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner()
                address.requestFocus()
            }
        })
    }
}
