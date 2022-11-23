@file:Suppress("unused")

package com.example.flatlaf.dialog.font

import com.example.flatlaf.dialog.StandardDialog
import java.awt.*
import javax.swing.BorderFactory
import javax.swing.JPanel

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
 * ----------------------
 * FontChooserDialog.java
 * ----------------------
 * (C) Copyright 2000-2004, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: FontChooserDialog.java,v 1.5 2007/11/02 17:50:36 taqua Exp $
 *
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Oct-2001 : Changed package to com.jrefinery.ui.*;
 * 14-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 *
 */ /**
 * A dialog for choosing a font from the available system fonts.
 *
 * @author David Gilbert
 */
class FontChooserDialog : StandardDialog {
    /** The panel within the dialog that contains the font selection controls.  */
    private var fontChooserPanel: FontChooserPanel? = null

    /**
     * Standard constructor - builds a font chooser dialog owned by another dialog.
     *
     * @param owner  the dialog that 'owns' this dialog.
     * @param title  the title for the dialog.
     * @param modal  a boolean that indicates whether the dialog is modal.
     * @param font  the initial font displayed.
     */
    constructor(owner: Dialog?, title: String = "Font Dialog", modal: Boolean, font: Font) : super(owner, title,
        modal) {
        contentPane = createContent(font)
        minimumSize = Dimension(500, 400)

        // Center the dialog relative to the owner.
        if (owner != null) {
            val ownerBounds = owner.bounds
            val ownerX = ownerBounds.x + ownerBounds.width / 2
            val ownerY = ownerBounds.y + ownerBounds.height / 2
            val dialogX = x + width / 2
            val dialogY = y + height / 2
            val dx = ownerX - dialogX
            val dy = ownerY - dialogY
            setLocation(x + dx, y + dy)
        }
    }

    /**
     * Standard constructor - builds a font chooser dialog owned by a frame.
     *
     * @param owner  the frame that 'owns' this dialog.
     * @param title  the title for the dialog.
     * @param modal  a boolean that indicates whether the dialog is modal.
     * @param font  the initial font displayed.
     */
    constructor(owner: Frame?, title: String = "Font Dialog", modal: Boolean, font: Font) : super(owner, title, modal) {
        contentPane = createContent(font)
        minimumSize = Dimension(500, 400)

        // Center the dialog relative to the owner.
        if (owner != null) {
            val ownerBounds = owner.bounds
            val ownerX = ownerBounds.x + ownerBounds.width / 2
            val ownerY = ownerBounds.y + ownerBounds.height / 2
            val dialogX = x + width / 2
            val dialogY = y + height / 2
            val dx = ownerX - dialogX
            val dy = ownerY - dialogY
            setLocation(x + dx, y + dy)
        }
    }

    /**
     * Returns the selected font.
     *
     * @return the font.
     */
    val selectedFont: Font
        get() = fontChooserPanel!!.selectedFont!!

    /**
     * Returns the panel that is the user interface.
     *
     * @param font  the font.
     *
     * @return the panel.
     */
    private fun createContent(font: Font?): JPanel {
        var font1: Font? = font
        val content = JPanel(BorderLayout())
        content.border = BorderFactory.createEmptyBorder(4, 4, 4, 4)
        if (font1 == null) {
            font1 = Font("Dialog", 10, Font.PLAIN)
        }
        fontChooserPanel = FontChooserPanel(font1)
        content.add(fontChooserPanel)
        val buttons = createButtonPanel()
        buttons.border = BorderFactory.createEmptyBorder(4, 0, 0, 0)
        content.add(buttons, BorderLayout.SOUTH)
        return content
    }
}