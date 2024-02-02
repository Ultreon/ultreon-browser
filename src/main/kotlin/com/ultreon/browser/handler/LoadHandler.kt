package com.ultreon.browser.handler

import com.ultreon.browser.BrowserTabs
import com.ultreon.browser.UltreonBrowser
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.network.CefRequest

class LoadHandler(private val tabs: BrowserTabs) : CefLoadHandlerAdapter() {
    override fun onLoadEnd(browser: CefBrowser, frame: CefFrame, httpStatusCode: Int) {
        tabs.onLoadEnd(browser)
    }

    override fun onLoadStart(
        browser: CefBrowser, frame: CefFrame, transitionType: CefRequest.TransitionType
    ) {
        val url = browser.url
        if (url.startsWith("ultreon:")) {
            browser.stopLoad()
            val substring = url.substring("ultreon:".length).replace("..", "")
            browser.loadURL(UltreonBrowser::class.java.getResource("/pages/$substring.html")?.toString() ?: "about:blank")
            return
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            browser.stopLoad()
            return
        }
        tabs.onLoadStart(browser)
    }

    override fun onLoadError(
        browser: CefBrowser,
        frame: CefFrame,
        errorCode: CefLoadHandler.ErrorCode?,
        errorText: String?,
        failedUrl: String
    ) {
        if (failedUrl.startsWith("ultreon:")) {
            browser.stopLoad()
            val substring = failedUrl.substring("ultreon:".length).replace("..", "")
            browser.loadURL(UltreonBrowser::class.java.getResource("/pages/$substring.html")?.toString() ?: "about:blank")
            return
        }
        super.onLoadError(browser, frame, errorCode, errorText, failedUrl)
    }
}