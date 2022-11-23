/*
* JCommon : a free general purpose class library for the Java(tm) platform
*
*
* (C) Copyright 2000-2005, by Object Refinery Limited and Contributors.
*
* Project Info:  http://www.jfree.org/jcommon/index.html
*
* This library is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation; either version 2.1 of the License, or
* (at your option) any later version.
*
* This library is distributed in the hope that it will be useful, but
* WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
* or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
* License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
* USA.
*
* [Java is a trademark or registered trademark of Sun Microsystems, Inc.
* in the United States and other countries.]
*
* --------------------
* L1R2ButtonPanel.kt
* --------------------
* (C) Copyright 2000-2004, by Object Refinery Limited.
*
* Original Author:  David Gilbert (for Object Refinery Limited);
* Contributor(s):   -;
*
* $Id: L1R2ButtonPanel.kt,v 1.4 2007/11/02 17:50:36 taqua Exp $
*
* Changes (from 26-Oct-2001)
* --------------------------
* 26-Oct-2001 : Changed package to com.jrefinery.ui.*;
* 26-Jun-2002 : Removed unnecessary import (DG);
* 14-Oct-2002 : Fixed errors reported by Checkstyle (DG);
*
*/

package com.ultreon.browser.dialog

import com.ultreon.browser.dialog.font.L1R2ButtonPanel
import java.awt.Dialog
import java.awt.Frame
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.BorderFactory
import javax.swing.JDialog
import javax.swing.JPanel

/**
 * The base class for standard dialogs.
 *
 * @author David Gilbert
 */
open class StandardDialog : JDialog, ActionListener {
    /**
     * Returns a flag that indicates whether the dialog has been
     * cancelled.
     *
     * @return boolean.
     */
    /** Flag that indicates whether the dialog was cancelled.  */
    var isCancelled: Boolean
        private set

    /**
     * Standard constructor - builds a dialog...
     *
     * @param owner  the owner.
     * @param title  the title.
     * @param modal  modal?
     */
    constructor(
        owner: Frame?, title: String?,
        modal: Boolean
    ) : super(owner, title, modal) {
        isCancelled = false
    }

    /**
     * Standard constructor - builds a dialog...
     *
     * @param owner  the owner.
     * @param title  the title.
     * @param modal  modal?
     */
    constructor(
        owner: Dialog?, title: String?,
        modal: Boolean
    ) : super(owner, title, modal) {
        isCancelled = false
    }

    /**
     * Handles clicks on the standard buttons.
     *
     * @param event  the event.
     */
    override fun actionPerformed(event: ActionEvent) {
        when (event.actionCommand) {
            "helpButton" -> {
                // display help information
            }

            "okButton" -> {
                isCancelled = false
                isVisible = false
            }

            "cancelButton" -> {
                isCancelled = true
                isVisible = false
            }
        }
    }

    /**
     * Builds and returns the user interface for the dialog.  This method is
     * shared among the constructors.
     *
     * @return the button panel.
     */
    protected open fun createButtonPanel(): JPanel {
        val buttons = L1R2ButtonPanel(
            "Help",
            "OK",
            "Cancel"
        )
//        val helpButton = buttons.leftButton
//        helpButton.actionCommand = "helpButton"
//        helpButton.addActionListener(this)
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