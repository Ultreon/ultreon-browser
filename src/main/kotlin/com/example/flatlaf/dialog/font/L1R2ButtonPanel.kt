package com.example.flatlaf.dialog.font

import java.awt.BorderLayout
import java.awt.GridLayout
import javax.swing.JButton
import javax.swing.JPanel

/*
 * JCommon : a free general purpose class library for the Java(tm) platform
 *
 *
 * (C) Copyright 2000-2008, by Object Refinery Limited and Contributors.
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
 * -------------------
 * StandardDialog.java
 * -------------------
 * (C) Copyright 2000-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Arnaud Lelievre;
 *
 * $Id: StandardDialog.java,v 1.7 2008/12/18 09:57:32 mungady Exp $
 *
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Oct-2001 : Changed package to com.jrefinery.ui.*;
 * 08-Sep-2003 : Added internationalization via use of properties
 *               resourceBundle (RFE 690236) (AL);
 * 18-Dec-2008 : Use ResourceBundleWrapper - see JFreeChart patch 1607918 by
 *               Jess Thrysoee (DG);
 *
 */

/**
 * A 'ready-made' panel that has one button on the left and two buttons on the right - nested
 * panels and layout managers take care of resizing.
 *
 * @author David Gilbert
 */
internal class L1R2ButtonPanel
/**
 * Standard constructor - creates a three button panel with the specified button labels.
 *
 * @param label1  the label for button 1.
 * @param label2  the label for button 2.
 * @param label3  the label for button 3.
 */(label1: String?, label2: String?, label3: String?) : JPanel() {

    /**
     * Returns a reference to button 1, allowing the caller to set labels, action-listeners etc.
     *
     * @return the left button.
     */
    val leftButton: JButton

    /**
     * Returns a reference to button 2, allowing the caller to set labels, action-listeners etc.
     *
     * @return the right button 1.
     */
    val rightButton1: JButton

    /**
     * Returns a reference to button 3, allowing the caller to set labels, action-listeners etc.
     *
     * @return  the right button 2.
     */
    val rightButton2: JButton

    init {
        layout = BorderLayout()
        leftButton = JButton(label1)
        val rightButtonPanel = JPanel(GridLayout(1, 2))
        rightButton1 = JButton(label2)
        rightButton2 = JButton(label3)
        rightButtonPanel.add(rightButton1)
        rightButtonPanel.add(rightButton2)
        add(leftButton, BorderLayout.WEST)
        add(rightButtonPanel, BorderLayout.EAST)
    }

}