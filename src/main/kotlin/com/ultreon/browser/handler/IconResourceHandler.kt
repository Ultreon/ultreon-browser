package com.ultreon.browser.handler

import com.ultreon.browser.BrowserTabs
import com.ultreon.browser.UltreonBrowser
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefResourceRequestHandlerAdapter
import org.cef.misc.BoolRef
import org.cef.network.CefRequest
import org.cef.network.CefResponse
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.SwingUtilities
import kotlin.concurrent.thread

@Suppress("unused")
class IconResourceHandler(
    val ultreonBrowser: UltreonBrowser,
    val browser: CefBrowser?,
    val frame: CefFrame?,
    val request: CefRequest?,
    val isNavigation: Boolean,
    val isDownload: Boolean,
    val requestInitiator: String?,
    val disableDefaultHandling: BoolRef?,
    val tabs: BrowserTabs,
) : CefResourceRequestHandlerAdapter() {
    override fun onResourceResponse(
        browser: CefBrowser,
        frame: CefFrame,
        request: CefRequest,
        response: CefResponse
    ): Boolean {
        if (request.resourceType == CefRequest.ResourceType.RT_FAVICON) {
            val url = request.url
            if (url.startsWith("https://")) {
                val url1 = URL(url)
                thread {
                    val image = ImageIO.read(url1)
                    SwingUtilities.invokeLater {
                        this.tabs.onIconChange(browser, image)
                    }
                }
            }
        }
        return false
    }
}