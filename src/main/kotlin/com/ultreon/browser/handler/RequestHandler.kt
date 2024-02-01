package com.ultreon.browser.handler

import com.ultreon.browser.BrowserTabs
import com.ultreon.browser.UltreonBrowser
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefRequestHandlerAdapter
import org.cef.handler.CefResourceRequestHandler
import org.cef.misc.BoolRef
import org.cef.network.CefRequest

class RequestHandler(
    private val ultreonBrowser: UltreonBrowser,
    private val tabs: BrowserTabs,
) : CefRequestHandlerAdapter() {
    override fun onOpenURLFromTab(
        browser: CefBrowser,
        frame: CefFrame,
        targetUrl: String,
        userGesture: Boolean
    ): Boolean {
        tabs.createTab(targetUrl)
        return true
    }

    override fun getResourceRequestHandler(
        browser: CefBrowser?,
        frame: CefFrame?,
        request: CefRequest?,
        isNavigation: Boolean,
        isDownload: Boolean,
        requestInitiator: String?,
        disableDefaultHandling: BoolRef?
    ): CefResourceRequestHandler {
        return IconResourceHandler(
            ultreonBrowser,
            browser,
            frame,
            request,
            isNavigation,
            isDownload,
            requestInitiator,
            disableDefaultHandling,
            tabs
        )
    }
}
