package com.ultreon.browser.handler

import com.ultreon.browser.BrowserTabs
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefDisplayHandlerAdapter
import javax.swing.JTextField

class DisplayHandler(
    private val tabs: BrowserTabs,
    private val address: JTextField
) : CefDisplayHandlerAdapter() {
    override fun onAddressChange(browser: CefBrowser, frame: CefFrame, url: String) {
        if (browser == tabs.selected.browser) {
            address.text = url
        }
    }

    override fun onTitleChange(browser: CefBrowser, title: String?) {
        tabs.onTitleChange(browser, title)
    }
}
