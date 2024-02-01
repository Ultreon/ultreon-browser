package com.ultreon.browser.dialog

import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.FlowLayout
import java.awt.Window
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JProgressBar

class ProgressDialog(parent: Window?, title: String?) : JDialog(parent, title) {
    var message: String = "..."
        set(value) {
            field = value
            val progress = 100 * progressBar.value / progressBar.maximum
            progressBar.string = String.format("%s\n%d%% complete", field, progress)
        }
    private val cancelButton: JButton = JButton("Cancel")
    private val progressBar: JProgressBar = JProgressBar()
    private var listener: ProgressDialogListener? = null

    init {
        progressBar.isStringPainted = true
        progressBar.maximum = 10000
        progressBar.string = "Retrieving messages"
        layout = BorderLayout()
        val size = cancelButton.preferredSize
        size.width = 400
        progressBar.preferredSize = size
        add(progressBar, BorderLayout.CENTER)
        add(cancelButton, BorderLayout.SOUTH)
        cancelButton.addActionListener {
            if (listener != null) {
                listener!!.progressDialogCancelled()
            }
        }
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                if (listener != null) {
                    listener!!.progressDialogCancelled()
                }
            }
        })
        pack()
        setLocationRelativeTo(parent)
    }

    fun setListener(listener: ProgressDialogListener?) {
        this.listener = listener
    }

    fun setValue(value: Float) {
        val progress = (100 * value).toInt()
        if (progress in 0..100) {
            progressBar.string = String.format("%s\n%d%% complete", message, progress)
        } else {
            progressBar.string = message
        }
        progressBar.value = (100 * value).toInt()
        progressBar.maximum = 100
    }

    override fun setVisible(visible: Boolean) {
        if (!visible) {
            try {
                cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                println("setVisible: Thread Interrupted")
            }
        } else {
            progressBar.value = 0
            cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
        }
        super@ProgressDialog.setVisible(visible)
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}