package com.ultreon.browser.main

import org.cef.browser.CefBrowser
import org.cef.callback.CefJSDialogCallback
import org.cef.handler.CefJSDialogHandler
import org.cef.handler.CefJSDialogHandlerAdapter
import org.cef.misc.BoolRef
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

class UBrowserJSDialogHandler(val ultreonBrowser: UltreonBrowser) : CefJSDialogHandlerAdapter() {
    override fun onJSDialog(
        browser: CefBrowser?,
        originUrl: String?,
        dialogType: CefJSDialogHandler.JSDialogType?,
        messageText: String?,
        defaultPromptText: String?,
        callback: CefJSDialogCallback?,
        suppressMessage: BoolRef?
    ): Boolean {
        SwingUtilities.invokeLater {
            when (dialogType) {
                CefJSDialogHandler.JSDialogType.JSDIALOGTYPE_ALERT -> {
                    JOptionPane.showMessageDialog(
                        this.ultreonBrowser,
                        messageText,
                        "Alert - $originUrl",
                        JOptionPane.INFORMATION_MESSAGE
                    )

                    callback?.Continue(true, null)
                }

                CefJSDialogHandler.JSDialogType.JSDIALOGTYPE_CONFIRM -> {
                    val message = JOptionPane.showConfirmDialog(
                        this.ultreonBrowser,
                        messageText,
                        "Confirm - $originUrl",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                    )

                    callback?.Continue(true, "${message == JOptionPane.YES_OPTION}")
                }

                CefJSDialogHandler.JSDialogType.JSDIALOGTYPE_PROMPT -> {
                    val message = JOptionPane.showInputDialog(
                        this.ultreonBrowser,
                        messageText,
                        "Prompt - $originUrl",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        null,
                        defaultPromptText
                    )

                    callback?.Continue(message != null, message?.toString())
                }

                null -> {

                }
            }
        }

        return true
    }
}
