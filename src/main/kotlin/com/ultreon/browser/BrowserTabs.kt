package com.ultreon.browser

import com.ultreon.browser.util.LOADING_ICON
import org.cef.CefClient
import org.cef.browser.CefBrowser
import runTask
import java.awt.Dimension
import java.awt.Image
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JTabbedPane
import javax.swing.SwingUtilities


class BrowserTabs(val client: CefClient, val main: UltreonBrowser) : JTabbedPane(TOP) {
    private val lock: Any = Any()
    private val tabs = mutableListOf<BrowserTab>()
    internal val browsers = mutableMapOf<CefBrowser, BrowserTab>()

    val selected: BrowserTab?
        get() {
            val selectedIndex = selectedIndex
            if (selectedIndex == -1) {
                return null
            }
            return tabs[selectedIndex]
        }

    val oldSelected: BrowserTab? = selected

    init {
        this.tabLayoutPolicy = SCROLL_TAB_LAYOUT
        createTab("https://google.com")

        addChangeListener {
            val browserTab = oldSelected
            browserTab?.browser?.setFocus(false)
        }
    }

    fun createTab(url: String, background: Boolean = false) {
        SwingUtilities.invokeLater {
            if (url.startsWith("ultreon://")) {
                UltreonBrowser.instance.handleUrl(url, null)
                return@invokeLater
            }
            createTabDirectly(url, background)
        }
    }

    fun createTabDirectly(url: String, background: Boolean = false): BrowserTab {
        val icon = JLabel(ImageIcon(LOADING_ICON))

        icon.maximumSize = Dimension(16, 16)
        icon.size = Dimension(16, 16)
        icon.preferredSize = Dimension(16, 16)
        icon.minimumSize = Dimension(16, 16)
        val browserTab = BrowserTab(this, icon, client, main, url, background)

        tabs += browserTab
        browsers[browserTab.browser] = browserTab

        this.addTab(url, browserTab)

        browserTab.attach()

        return browserTab
    }

    override fun removeTabAt(index: Int) {
        super.removeTabAt(index)
        val browserTab = tabs.removeAt(index)

        runTask { browserTab.browser.close(false) }
    }

    fun closeTab(browserTab: BrowserTab) {
        removeTabAt(tabs.indexOf(browserTab))
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
        browserTab?.updateTitle(title) ?: run {
            browser.close(true)
            return false
        }
        return true
    }

    fun indexOfTab(browserTab: BrowserTab): Int {
        return tabs.indexOf(browserTab)
    }

    fun savePage() {
        selected?.savePage()
    }

    fun select(it: BrowserTab) {
        selectedIndex = tabs.indexOf(it)
    }

    inner class CloseButtonHandler : ActionListener {
        override fun actionPerformed(evt: ActionEvent) {
        }
    }
}