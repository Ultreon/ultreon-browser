package com.ultreon.browser.util

import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.Action

fun action(name: String, function: () -> Unit): Action {
    return object : AbstractAction(name) {
        override fun actionPerformed(e: ActionEvent) {
            function()
        }
    }
}

fun logDebug(message: String) = LOGGER.debug(message)
fun logInfo(message: String) = LOGGER.info(message)
fun logWarn(message: String) = LOGGER.warn(message)
fun logError(message: String) = LOGGER.error(message)
