package com.example.flatlaf.dialog

import com.example.flatlaf.*
import com.example.flatlaf.dialog.font.L1R2ButtonPanel
import java.awt.*
import java.awt.event.ActionEvent
import java.net.URI
import javax.swing.BorderFactory
import javax.swing.JPanel

class AboutDialog(owner: Frame?, title: String?, modal: Boolean) : StandardDialog(owner, title, modal) {
    private lateinit var aboutPanel: AboutPanel

    init {
        contentPane = createContent()
    }

    private fun createContent(): Container {
        minimumSize = Dimension(384, 500)

        val content = JPanel(BorderLayout())
        aboutPanel = AboutPanel()
        content.add(aboutPanel, BorderLayout.CENTER)
        content.add(createButtonPanel(), BorderLayout.SOUTH)
        isResizable = false

        return content

    }

    override fun actionPerformed(event: ActionEvent) {
        when (event.actionCommand) {
            "sourceButton" -> {
                Desktop.getDesktop().browse(URI(SOURCE_URL))
            }

            else -> super.actionPerformed(event)
        }
    }

    /**
     * Builds and returns the user interface for the dialog.  This method is
     * shared among the constructors.
     *
     * @return the button panel.
     */
    override fun createButtonPanel(): JPanel {
        val buttons = L1R2ButtonPanel(
            "Source Code",
            "OK",
            "Cancel"
        )
        val helpButton = buttons.leftButton
        helpButton.actionCommand = "sourceButton"
        helpButton.addActionListener(this)
        val okButton = buttons.rightButton1
        okButton.actionCommand = "okButton"
        okButton.addActionListener(this)
        okButton.isDefaultCapable = true
        rootPane.defaultButton = okButton
        val cancelButton = buttons.rightButton2
        cancelButton.actionCommand = "cancelButton"
        cancelButton.addActionListener(this)
        buttons.border = BorderFactory.createEmptyBorder(4, 0, 0, 0)
        return buttons
    }
}