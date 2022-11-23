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
 * ---------------------
 * FontDisplayField.java
 * ---------------------
 * (C) Copyright 2000-2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Arnaud Lelievre;
 *
 * $Id: FontDisplayField.java,v 1.6 2008/12/18 09:57:32 mungady Exp $
 *
 * Changes (from 26-Oct-2001)
 * ----------------------------------
 * 26-Oct-2001 : Changed package to com.jrefinery.ui.*;
 * 14-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 08-Sep-2003 : Added internationalization via use of properties
 *               resourceBundle (RFE 690236) (AL);
 * 18-Dec-2008 : Use ResourceBundleWrapper - see JFreeChart patch 1607918 by
 *               Jess Thrysoee (DG);
 *
 */

package com.example.flatlaf.dialog.font

import java.awt.Font
import javax.swing.JTextField

/**
 * A field for displaying a font selection.  The display field itself is
 * read-only, to the developer must provide another mechanism to allow the
 * user to change the font.
 *
 * @author David Gilbert
 */
internal class FontDisplayField : JTextField {
    /**
     * Standard constructor - builds a FontDescriptionField initialised with
     * the specified font.
     *
     * @param font  the font.
     */
    constructor(font: Font?) : super("") {
        displayFont = font
        isEnabled = false
    }

    /**
     * The current font.
     */
    @Suppress("RedundantGetter")
    var displayFont: Font? = null
        /**
         * Returns the current font.
         *
         * @return the font.
         */
        get() = field
        /**
         * Sets the font.
         *
         * @param font  the font.
         */
        set(font) {
            field = font
            text = fontToString(displayFont)
            this.font = font
        }

    /**
     * Returns a string representation of the specified font.
     *
     * @param font  the font.
     *
     * @return a string describing the font.
     */
    private fun fontToString(font: Font?): String {
        return if (font != null) {
            font.fontName + ", " + font.size
        } else {
            "No Font Selected"
        }
    }
}