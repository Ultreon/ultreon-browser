package com.ultreon.browser.dialog

import com.ultreon.browser.*
import java.awt.Graphics
import java.awt.Rectangle
import javax.swing.JPanel
import javax.swing.plaf.basic.BasicHTML
import javax.swing.text.View

class AboutPanel : JPanel() {
    private var createHTMLView: View

    init {
        isOpaque = true

        createHTMLView = BasicHTML.createHTMLView(this, javaClass.getResourceAsStream("/docs/about.html")?.let {
            val readText = it.bufferedReader().readText()
                .replace("@(project-name)", APP_NAME)
                .replace("@(version)", APP_VERSION)
                .replace("@(build-date)", BUILD_DATE)
                .replace("@(chrome-version)", CHROME_VERSION)
                .replace("@(image)", APP_BANNER_REF.toString())
            it.close()
            return@let readText
        } ?: """
<html>
<head>
    <style>
        body {
            font-family: sans-serif;
            font-size: 12px;
            padding: 10px;
        }
        b {
            font-weight: 800;
        }
    </style>
</head>
<body>
    <h2>Error Occurred</h2>
    <p>Can't read text from resource, resource is not found</p>
</body>
</html>
        """)
    }

    override fun paint(g: Graphics?) {
        super.paint(g)
        createHTMLView.paint(g, Rectangle(0, 0, width, height))
    }
}