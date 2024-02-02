package com.ultreon.browser.handler

import com.ultreon.browser.UltreonBrowser
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefContextMenuParams
import org.cef.callback.CefMenuModel
import org.cef.handler.CefContextMenuHandlerAdapter
import org.cef.misc.EventFlags
import javax.swing.SwingUtilities

class ContextMenuHandler(
    private val ultreonBrowser: UltreonBrowser
) : CefContextMenuHandlerAdapter() {
    private val savePageId: Int = 0x100
    private val copyLinkId: Int = 0x101
    private val openLinkInNewTabId: Int = 0x102
    private val selectAllId: Int = 0x103
    private val screenShotId: Int = 0x104
    private val reloadId: Int = 0x105

    override fun onBeforeContextMenu(
        browser: CefBrowser,
        frame: CefFrame,
        params: CefContextMenuParams,
        model: CefMenuModel
    ) {
        params.linkUrl?.let {
            if (!it.startsWith("http://") && !it.startsWith("https://")) return@let
            model.addItem(copyLinkId, "Copy Link")
            model.addItem(openLinkInNewTabId, "Open Link in New Tab")
            model.addSeparator()
        }

        model.addItem(selectAllId, "Select All")
        model.addItem(savePageId, "Save Page As...")
        model.addSeparator()
        model.addItem(reloadId, "Reload Page")

        super.onBeforeContextMenu(browser, frame, params, model)
    }

    override fun onContextMenuCommand(
        browser: CefBrowser,
        frame: CefFrame,
        params: CefContextMenuParams,
        commandId: Int,
        eventFlags: Int
    ): Boolean {
        val linkUrl = params.linkUrl
        when (commandId) {
            savePageId -> {
                SwingUtilities.invokeLater { ultreonBrowser.savePage() }
                return true
            }
            copyLinkId -> {
                SwingUtilities.invokeLater { ultreonBrowser.copyLink(linkUrl) }
                return true
            }
            openLinkInNewTabId -> {
                SwingUtilities.invokeLater { ultreonBrowser.openLinkInNewTab(linkUrl) }
                return true
            }
            selectAllId -> {
                SwingUtilities.invokeLater { ultreonBrowser.selectAll(browser, params) }
                return true
            }
            screenShotId -> {
                SwingUtilities.invokeLater { ultreonBrowser.takeScreenshot(browser) }
                return true
            }
            reloadId -> {
                SwingUtilities.invokeLater {
                    // Check if shift is pressed
                    if (eventFlags and EventFlags.EVENTFLAG_SHIFT_DOWN != 0) {
                        ultreonBrowser.reloadIgnoreCache(browser)
                    } else {
                        ultreonBrowser.reload(browser)
                    }
                }
                return true
            }

            else -> return super.onContextMenuCommand(browser, frame, params, commandId, eventFlags)
        }
    }
}
