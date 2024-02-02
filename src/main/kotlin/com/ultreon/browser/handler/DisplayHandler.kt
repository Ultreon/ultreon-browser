package com.ultreon.browser.handler

import com.ultreon.browser.BrowserTab
import com.ultreon.browser.BrowserTabs
import com.ultreon.browser.UltreonBrowser
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefDisplayHandlerAdapter
import java.io.File
import java.net.URL
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

class DisplayHandler(
    private val tabs: BrowserTabs
) : CefDisplayHandlerAdapter() {
    override fun onAddressChange(browser: CefBrowser, frame: CefFrame, url: String) {
        val selected = tabs.selected
        if (selected != null && browser == selected.browser) {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                if (url.startsWith("file://") || url.startsWith("jar:")) {
                    if (url.startsWith("jar:")) {
                        val fileUrl = url.substring(4)
                        val split = fileUrl.split("!")
                        if (split.size == 2) {
                            if (setPath(split[0], split[1], selected)) {
                                return
                            }
                        }
                    } else {
                        val requestedURL = File(URL(url).toURI())
                        val codeSource = File(URL(UltreonBrowser::class.java.getResource("/.ultreon-browser-root").let {
                            val str = it.toString()
                            str.substring(0, str.length - ".ultreon-browser-root".length)
                        }).toURI())
                        if (requestedURL.startsWith(codeSource)) {
                            val relativeTo = requestedURL.relativeTo(codeSource)
                            if (relativeTo.startsWith("pages/")) {
                                selected.toolbar.address.text = "ultreon:" + relativeTo.toString().replace("\\", "/").substring("pages/".length).replace(".html", "")
                                return
                            } else {
                                SwingUtilities.invokeLater {
                                    JOptionPane.showMessageDialog(null, "Illegal URL: $url\nRelative to: $relativeTo\nRequested: $requestedURL\nCode Source: $codeSource", "Error", JOptionPane.ERROR_MESSAGE)
                                }
                            }
                        } else {
                            SwingUtilities.invokeLater {
                                JOptionPane.showMessageDialog(null, "Invalid URL: $url\nRequested: $requestedURL\nCode Source: $codeSource", "Error", JOptionPane.ERROR_MESSAGE)
                            }
                            return
                        }
                    }
                }
            }
            selected.toolbar.address.text = url
        }
    }

    private fun setPath(
        url: String,
        path: String,
        selected: BrowserTab
    ): Boolean {
        if (url.startsWith("file:")) {
            val requestedURL = File(URL(url).toURI())
            val codeSource = File(UltreonBrowser::class.java.protectionDomain.codeSource.location.toURI())
            if (requestedURL == codeSource) {
                if (path.startsWith("/pages/")) {
                    selected.toolbar.address.text = "ultreon:" + path.substring("/pages/".length)
                    return true
                } else {
                    SwingUtilities.invokeLater {
                        JOptionPane.showMessageDialog(null, "Illegal URL: $url\nRelative to: $path\nRequested: $requestedURL\nCode Source: $codeSource", "Error", JOptionPane.ERROR_MESSAGE)
                    }
                }
            } else {
                SwingUtilities.invokeLater {
                    JOptionPane.showMessageDialog(null, "Invalid URL: $url\nRequested: $requestedURL\nCode Source: $codeSource", "Error", JOptionPane.ERROR_MESSAGE)
                }
            }
        } else {
            SwingUtilities.invokeLater {
                JOptionPane.showMessageDialog(null, "Invalid URL: $url", "Error", JOptionPane.ERROR_MESSAGE)
            }
        }

        return false
    }

    override fun onTitleChange(browser: CefBrowser, title: String?) {
        tabs.onTitleChange(browser, title)
    }
}
