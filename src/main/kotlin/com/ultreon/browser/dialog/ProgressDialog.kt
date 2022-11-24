package com.ultreon.browser.dialog

import java.awt.Cursor
import java.awt.FlowLayout
import java.awt.Window
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JProgressBar
import javax.swing.SwingUtilities

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
        layout = FlowLayout()
        val size = cancelButton.preferredSize
        size.width = 400
        progressBar.preferredSize = size
        add(progressBar)
        add(cancelButton)
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

    fun setMaximum(value: Int) {
        progressBar.maximum = value
    }

    fun setValue(value: Float) {
        val progress = (100 * value).toInt()
        progressBar.string = String.format("%s\n%d%% complete", message, progress)
        progressBar.value = (10000 * value).toInt()
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