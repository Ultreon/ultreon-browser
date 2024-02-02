package com.ultreon.browser.handler

import com.ultreon.browser.UltreonBrowser
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.browser.CefMessageRouter
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefMessageRouterHandlerAdapter

class MessageRouterHandler(private val ultreonBrowser: UltreonBrowser, val msgRouter: CefMessageRouter) : CefMessageRouterHandlerAdapter() {
    override fun onQuery(
        browser: CefBrowser?,
        frame: CefFrame?,
        queryId: Long,
        request: String?,
        persistent: Boolean,
        callback: CefQueryCallback?
    ): Boolean {
        println("onQuery: $request")
        if (request?.startsWith("ultreon://") == true) {
            callback?.let { ultreonBrowser.handleUrlSafe(request, it) } ?: return false
            return true
        }

        return super.onQuery(browser, frame, queryId, request, persistent, callback)
    }
}
