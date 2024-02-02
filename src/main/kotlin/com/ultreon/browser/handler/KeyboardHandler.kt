package com.ultreon.browser.handler

import com.ultreon.browser.UltreonBrowser
import org.cef.browser.CefBrowser
import org.cef.handler.CefKeyboardHandler
import org.cef.handler.CefKeyboardHandler.CefKeyEvent.EventType
import org.cef.misc.BoolRef

class KeyboardHandler(private val ultreonBrowser: UltreonBrowser) : CefKeyboardHandler {
    override fun onPreKeyEvent(
        browser: CefBrowser?,
        event: CefKeyboardHandler.CefKeyEvent?,
        is_keyboard_shortcut: BoolRef?
    ): Boolean {
        if (event?.type == EventType.KEYEVENT_RAWKEYDOWN) {
            return ultreonBrowser.onKeyDown(event)
        }

        return false
    }

    override fun onKeyEvent(browser: CefBrowser?, event: CefKeyboardHandler.CefKeyEvent?): Boolean {
        if (event?.type == EventType.KEYEVENT_RAWKEYDOWN) {
            return ultreonBrowser.onKeyDown(event)
        }

        return false
    }

}
