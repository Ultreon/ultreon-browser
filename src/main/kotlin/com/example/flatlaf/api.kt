package com.example.flatlaf

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
