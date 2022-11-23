@file:Suppress("unused")

package com.example.flatlaf.dialog.settings

import com.example.flatlaf.dialog.StandardDialog
import java.awt.BorderLayout
import java.awt.Dialog
import java.awt.Dimension
import java.awt.Frame
import javax.swing.JPanel

class SettingsDialog : StandardDialog {
    private lateinit var settingsPanel: SettingsPanel

    constructor(owner: Frame?, title: String?, modal: Boolean) : super(owner, title, modal) {
        contentPane = createContent()
    }

    constructor(owner: Dialog?, title: String?, modal: Boolean) : super(owner, title, modal) {
        contentPane = createContent()
    }

    private fun createContent(): JPanel {
        minimumSize = Dimension(400, 400)

        val content = JPanel(BorderLayout())
        settingsPanel = SettingsPanel()
        content.add(settingsPanel, BorderLayout.CENTER)
        content.add(createButtonPanel(), BorderLayout.SOUTH)

        return content
    }
}