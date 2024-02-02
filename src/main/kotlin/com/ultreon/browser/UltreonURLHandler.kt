package com.ultreon.browser

import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler
import java.net.URLStreamHandlerFactory

class UltreonURLHandler : URLStreamHandlerFactory {
    fun setHandler() {
        URL.setURLStreamHandlerFactory(this)
    }

    override fun createURLStreamHandler(protocol: String?): URLStreamHandler? {
        if (protocol != "ultreon") {
            return null
        }

        return object : URLStreamHandler() {
            override fun openConnection(u: URL?): URLConnection {
                if (u == null) throw IllegalArgumentException("Parameter u must not be null.")
                if (u.protocol != "ultreon") throw UnsupportedOperationException("Unsupported protocol: " + u.protocol)

                return UltreonURLConnection(u)
            }
        }
    }


    class UltreonURLConnection(u: URL?) : URLConnection(u) {
        override fun connect() {
            // Nothing
        }
    }
}