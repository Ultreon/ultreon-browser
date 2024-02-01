package com.ultreon.browser.handler

import com.ultreon.browser.BrowserTabs
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.network.CefRequest

class LoadHandler(private val tabs: BrowserTabs) : CefLoadHandlerAdapter() {
    override fun onLoadEnd(browser: CefBrowser, frame: CefFrame, httpStatusCode: Int) {
        tabs.onLoadEnd(browser)
    }

    override fun onLoadStart(
        browser: CefBrowser, frame: CefFrame, transitionType: CefRequest.TransitionType
    ) {
        tabs.onLoadStart(browser)
    }
}