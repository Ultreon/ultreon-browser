/*
* JCommon : a free general purpose class library for the Java(tm) platform
* s
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
* FontChooserPanel.java
* ---------------------
* (C) Copyright 2000-2008, by Object Refinery Limited.
*
* Original Author:  David Gilbert (for Object Refinery Limited);
* Contributor(s):   Arnaud Lelievre;
*
* $Id: FontChooserPanel.java,v 1.6 2008/12/18 09:57:32 mungady Exp $
*
* Changes (from 26-Oct-2001)
* --------------------------
* 26-Oct-2001 : Changed package to com.jrefinery.ui.*;
* 14-Oct-2002 : Fixed errors reported by Checkstyle (DG);
* 08-Sep-2003 : Added internationalization via use of properties resourceBundle (RFE 690236) (AL);
* 21-Feb-2004 : The FontParameter of the constructor was never used (TM);
* 18-Dec-2008 : Use ResourceBundleWrapper - see JFreeChart patch 1607918 by
*               Jess Thrysoee (DG);
*
*/

package com.example.flatlaf.dialog.font

import java.awt.*
import javax.swing.*

/**
 * A panel for choosing a font from the available system fonts - still a bit of
 * a hack at the moment, but good enough for demonstration applications.
 *
 * @author David Gilbert
 */
internal class FontChooserPanel(font: Font?) : JPanel() {
    private var fontDisplay: FontDisplayField

    /** The list of fonts.  */
    private val fontList: JList<*>

    /** The list of sizes.  */
    private val sizeList: JList<*>

    /** The checkbox that indicates whether the font is bold.  */
    private val bold: JCheckBox

    /** The checkbox that indicates whether the font is italic.  */
    private val italic: JCheckBox

    /**
     * Initializes the contents of the dialog from the given font
     * object.
     */
    var selectedFont: Font?
        get() = Font(
            selectedName, selectedStyle,
            selectedSize
        )
        set(font) {
            if (font == null) {
                throw NullPointerException()
            }
            bold.isSelected = font.isBold
            italic.isSelected = font.isItalic
            val fontName = font.name
            var model = fontList.model
            fontList.clearSelection()
            for (i in 0 until model.size) {
                if (fontName == model.getElementAt(i)) {
                    fontList.selectedIndex = i
                    break
                }
            }
            val fontSize = font.size.toString()
            model = sizeList.model
            sizeList.clearSelection()
            for (i in 0 until model.size) {
                if (fontSize == model.getElementAt(i)) {
                    sizeList.selectedIndex = i
                    break
                }
            }
            fontDisplay.displayFont = font
        }

    /**
     * Standard constructor - builds a FontChooserPanel initialised with the
     * specified font.
     */
    init {
//        val createEtchedBorder = BorderFactory.createEtchedBorder()
        val createEtchedBorder =
            BorderFactory.createLineBorder(UIManager.getColor("Component.disabledBorderColor") ?: Color(0x616365), 1,
                true)
        val g = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val fonts = g.availableFontFamilyNames
        layout = BorderLayout()
        val right = JPanel(BorderLayout())
        val fontPanel = JPanel(BorderLayout())
        fontPanel.border = BorderFactory.createTitledBorder(createEtchedBorder, "Font")
        fontList = JList(/* listData = */ fonts)
        val fontPane = JScrollPane(fontList)
        fontPane.border = createEtchedBorder
        fontPanel.add(fontPane)
        add(fontPanel)
        val sizePanel = JPanel(BorderLayout())
        sizePanel.border = BorderFactory.createTitledBorder(createEtchedBorder, "Size")
        sizeList = JList(SIZES)
        val sizePane = JScrollPane(sizeList)
        sizePane.border = createEtchedBorder
        sizePanel.add(sizePane)
        val attributes = JPanel(GridLayout(1, 2))
        bold = JCheckBox("Bold")
        italic = JCheckBox("Italic")
        attributes.add(bold)
        attributes.add(italic)
        attributes.border = BorderFactory.createTitledBorder(createEtchedBorder, "Attributes")
        right.add(sizePanel, BorderLayout.CENTER)
        right.add(attributes, BorderLayout.SOUTH)
        add(right, BorderLayout.EAST)
        fontDisplay = FontDisplayField(selectedFont)
        fontDisplay.preferredSize = Dimension(0, 90)
        add(fontDisplay, BorderLayout.SOUTH)
        selectedFont = font

        fontList.addListSelectionListener {
            fontDisplay.displayFont = selectedFont
        }

        sizeList.addListSelectionListener {
            fontDisplay.displayFont = selectedFont
        }
        bold.addChangeListener {
            fontDisplay.displayFont = selectedFont
        }
        italic.addChangeListener {
            fontDisplay.displayFont = selectedFont
        }
    }
    /**
     * Returns a Font object representing the selection in the panel.
     *
     * @return the font.
     */

    /**
     * Returns the selected name.
     *
     * @return the name.
     */
    val selectedName: String?
        get() = fontList.selectedValue as String?

    /**
     *
     * Returns the selected style.
     * @return the style.
     */
    val selectedStyle: Int
        get() {
            if (bold.isSelected && italic.isSelected) {
                return Font.BOLD + Font.ITALIC
            }
            if (bold.isSelected) {
                return Font.BOLD
            }
            return if (italic.isSelected) {
                Font.ITALIC
            } else {
                Font.PLAIN
            }
        }

    /**
     * Returns the selected size.
     *
     * @return the size.
     */
    val selectedSize: Int
        get() {
            val selected = sizeList.selectedValue as String?
            return selected?.toInt() ?: 10
        }

    companion object {
        /** The font sizes that can be selected.  */
        val SIZES = arrayOf<String?>(
            "9", "10", "11", "12", "14", "16",
            "18", "20", "22", "24", "28", "36", "48", "72"
        )
    }
}
