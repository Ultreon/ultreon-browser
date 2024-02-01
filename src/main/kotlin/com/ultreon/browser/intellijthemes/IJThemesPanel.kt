/*
 * Copyright 2019 FormDev Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Changes made: Reformatted code with IntelliJ IDEA. And converted to Kotlin.
 * Source: https://github.com/JFormDesigner/FlatLaf
 */
@file:Suppress("unused")

package com.ultreon.browser.intellijthemes

import com.formdev.flatlaf.*
import com.formdev.flatlaf.IntelliJTheme.ThemeLaf
import com.formdev.flatlaf.extras.FlatAnimatedLafChange
import com.formdev.flatlaf.extras.FlatSVGIcon
import com.formdev.flatlaf.themes.FlatMacDarkLaf
import com.formdev.flatlaf.themes.FlatMacLightLaf
import com.formdev.flatlaf.util.LoggingFacade
import com.formdev.flatlaf.util.StringUtils
import com.ultreon.browser.AppPrefs
import com.ultreon.browser.Settings
import net.miginfocom.swing.MigLayout
import java.awt.Component
import java.awt.Desktop
import java.awt.EventQueue
import java.awt.Window
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.function.Predicate
import javax.swing.*
import javax.swing.border.CompoundBorder
import javax.swing.event.ListSelectionEvent

/**
 * @author Karl Tauber
 */
class IJThemesPanel : JPanel() {
    private val themesManager = IJThemesManager()
    private val themes: MutableList<IJThemeInfo> = ArrayList()
    private val categories = HashMap<Int, String>()
    private val lafListener = PropertyChangeListener { e: PropertyChangeEvent -> lafChanged(e) }
    private val windowListener: WindowListener = object : WindowAdapter() {
        override fun windowActivated(e: WindowEvent) {
            this@IJThemesPanel.windowActivated()
        }
    }
    private var window: Window? = null
    private var lastDirectory: File? = null
    private var isAdjustingThemesList = false
    private fun updateThemesList() {
        val filterLightDark = filterComboBox!!.selectedIndex
        val showLight = filterLightDark != 2
        val showDark = filterLightDark != 1

        // load theme infos
        themesManager.loadBundledThemes()
        themesManager.loadThemesFromDirectory()

        // sort themes by name
        val comparator: Comparator<in IJThemeInfo> = Comparator { t1: IJThemeInfo, t2: IJThemeInfo ->
            t1.name.compareTo(
                t2.name, ignoreCase = true
            )
        }
        themesManager.bundledThemes.sortWith(comparator)
        themesManager.moreThemes.sortWith(comparator)

        // remember selection (must be invoked before clearing themes field)
        val oldSel = themesList!!.selectedValue
        themes.clear()
        categories.clear()

        // add core themes at beginning
        categories[themes.size] = "Core Themes"
        if (showLight) themes.add(
            IJThemeInfo(
                "FlatLaf Light",
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                FlatLightLaf::class.java.name
            )
        )
        if (showDark) themes.add(
            IJThemeInfo(
                "FlatLaf Dark",
                null,
                true,
                null,
                null,
                null,
                null,
                null,
                FlatDarkLaf::class.java.name
            )
        )
        if (showLight) themes.add(
            IJThemeInfo(
                "FlatLaf IntelliJ",
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                FlatIntelliJLaf::class.java.name
            )
        )
        if (showDark) themes.add(
            IJThemeInfo(
                "FlatLaf Darcula",
                null,
                true,
                null,
                null,
                null,
                null,
                null,
                FlatDarculaLaf::class.java.name
            )
        )

        if (showLight) themes.add(
            IJThemeInfo(
                "FlatLaf macOS Light",
                null,
                true,
                null,
                null,
                null,
                null,
                null,
                FlatMacLightLaf::class.java.name
            )
        )

        if (showDark) themes.add(
            IJThemeInfo(
                "FlatLaf macOS Dark",
                null,
                true,
                null,
                null,
                null,
                null,
                null,
                FlatMacDarkLaf::class.java.name
            )
        )

        // add themes from directory
        categories[themes.size] = "Current Directory"
        themes.addAll(themesManager.moreThemes)

        // add uncategorized bundled themes
        categories[themes.size] = "IntelliJ Themes"
        for (ti in themesManager.bundledThemes) {
            val show = showLight && !ti.dark || showDark && ti.dark
            if (show && !ti.name.contains("/")) themes.add(ti)
        }

        // add categorized bundled themes
        var lastCategory: String? = null
        for (ti in themesManager.bundledThemes) {
            val show = showLight && !ti.dark || showDark && ti.dark
            val sep = ti.name.indexOf('/')
            if (!show || sep < 0) continue
            val category = ti.name.substring(0, sep).trim { it <= ' ' }
            if (lastCategory != category) {
                lastCategory = category
                categories[themes.size] = category
            }
            themes.add(ti)
        }

        // fill themes list
        themesList!!.setModel(object : AbstractListModel<IJThemeInfo?>() {
            override fun getSize(): Int {
                return themes.size
            }

            override fun getElementAt(index: Int): IJThemeInfo {
                return themes[index]
            }
        })

        // restore selection
        if (oldSel != null) {
            for (i in themes.indices) {
                val theme = themes[i]
                if (oldSel.name == theme.name && oldSel.resourceName == theme.resourceName && oldSel.themeFile == theme.themeFile && oldSel.lafClassName == theme.lafClassName) {
                    themesList!!.selectedIndex = i
                    break
                }
            }

            // select first theme if none selected
            if (themesList!!.selectedIndex < 0) themesList!!.selectedIndex = 0
        }

        // scroll selection into visible area
        val sel = themesList!!.selectedIndex
        if (sel >= 0) {
            val bounds = themesList!!.getCellBounds(sel, sel)
            if (bounds != null) themesList!!.scrollRectToVisible(bounds)
        }
    }

    fun selectPreviousTheme() {
        val sel = themesList!!.selectedIndex
        if (sel > 0) themesList!!.selectedIndex = sel - 1
    }

    fun selectNextTheme() {
        val sel = themesList!!.selectedIndex
        themesList!!.selectedIndex = sel + 1
    }

    private fun themesListValueChanged(e: ListSelectionEvent) {
        val themeInfo = themesList!!.selectedValue
        val bundledTheme = themeInfo?.resourceName != null
        saveButton!!.isEnabled = bundledTheme
        sourceCodeButton!!.isEnabled = bundledTheme
        if (e.valueIsAdjusting || isAdjustingThemesList) return
        EventQueue.invokeLater { setTheme(themeInfo) }
    }

    private fun setTheme(themeInfo: IJThemeInfo?) {
        if (themeInfo == null) return

        // change look and feel
        if (themeInfo.isSystemTheme) { // CHANGE: (Ultreon) Allow system theme
            FlatAnimatedLafChange.stop() // Ultreon: Fix bug
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } else if (themeInfo.lafClassName != null) {
            if (themeInfo.lafClassName == UIManager.getLookAndFeel().javaClass.name) return
            FlatAnimatedLafChange.stop()
            FlatAnimatedLafChange.showSnapshot()
            try {
                UIManager.setLookAndFeel(themeInfo.lafClassName)
            } catch (ex: Exception) {
                LoggingFacade.INSTANCE.logSevere(null, ex)
                showInformationDialog("Failed to create '" + themeInfo.lafClassName + "'.", ex)
            }
        } else if (themeInfo.themeFile != null) {
            FlatAnimatedLafChange.stop() // Ultreon: Fix bug
            FlatAnimatedLafChange.showSnapshot()
            try {
                if (themeInfo.themeFile.name.endsWith(".properties")) {
                    FlatLaf.setup(FlatPropertiesLaf(themeInfo.name, themeInfo.themeFile))
                } else FlatLaf.setup(IntelliJTheme.createLaf(FileInputStream(themeInfo.themeFile)))
                AppPrefs.state.put(AppPrefs.KEY_LAF_THEME, AppPrefs.FILE_PREFIX + themeInfo.themeFile)
            } catch (ex: Exception) {
                LoggingFacade.INSTANCE.logSevere(null, ex)
                showInformationDialog("Failed to load '" + themeInfo.themeFile + "'.", ex)
            }
        } else {
            FlatAnimatedLafChange.stop() // Ultreon: Fix bug
            FlatAnimatedLafChange.showSnapshot()
            IntelliJTheme.setup(javaClass.getResourceAsStream(THEMES_PACKAGE + themeInfo.resourceName))
            AppPrefs.state.put(AppPrefs.KEY_LAF_THEME, AppPrefs.RESOURCE_PREFIX + themeInfo.resourceName)
        }

        // update all components
        FlatLaf.updateUI()
        FlatAnimatedLafChange.hideSnapshotWithAnimation()

        Settings.theme = themeInfo.name
    }

    private fun saveTheme() {
        val themeInfo = themesList!!.selectedValue
        if (themeInfo?.resourceName == null) return
        val fileChooser = JFileChooser()
        fileChooser.selectedFile = File(lastDirectory, themeInfo.resourceName)
        if (fileChooser.showSaveDialog(SwingUtilities.windowForComponent(this)) != JFileChooser.APPROVE_OPTION) return
        val file = fileChooser.selectedFile
        lastDirectory = file.parentFile

        // save theme
        try {
            Files.copy(
                javaClass.getResourceAsStream(THEMES_PACKAGE + themeInfo.resourceName)!!,
                file.toPath(), StandardCopyOption.REPLACE_EXISTING
            )
        } catch (ex: IOException) {
            showInformationDialog("Failed to save theme to '$file'.", ex)
            return
        }

        // save license
        if (themeInfo.licenseFile != null) {
            try {
                val licenseFile = File(
                    file.parentFile,
                    StringUtils.removeTrailing(file.name, ".theme.json") +
                            themeInfo.licenseFile.substring(themeInfo.licenseFile.indexOf('.'))
                )
                Files.copy(
                    javaClass.getResourceAsStream(THEMES_PACKAGE + themeInfo.licenseFile)!!,
                    licenseFile.toPath(), StandardCopyOption.REPLACE_EXISTING
                )
            } catch (ex: IOException) {
                showInformationDialog("Failed to save theme license to '$file'.", ex)
                return
            }
        }
    }

    private fun browseSourceCode() {
        val themeInfo = themesList!!.selectedValue
        if (themeInfo?.resourceName == null) return
        val themeUrl = (themeInfo.sourceCodeUrl + '/' + themeInfo.sourceCodePath).replace(" ", "%20")
        try {
            Desktop.getDesktop().browse(URI(themeUrl))
        } catch (ex: IOException) {
            showInformationDialog("Failed to browse '$themeUrl'.", ex)
        } catch (ex: URISyntaxException) {
            showInformationDialog("Failed to browse '$themeUrl'.", ex)
        }
    }

    private fun showInformationDialog(message: String, ex: Exception) {
        JOptionPane.showMessageDialog(
            SwingUtilities.windowForComponent(this),
            """
                $message
                
                ${ex.message}
                """.trimIndent(),
            "FlatLaf", JOptionPane.INFORMATION_MESSAGE
        )
    }

    override fun addNotify() {
        super.addNotify()
        selectedCurrentLookAndFeel()
        UIManager.addPropertyChangeListener(lafListener)
        window = SwingUtilities.windowForComponent(this)
        if (window != null) window!!.addWindowListener(windowListener)
    }

    override fun removeNotify() {
        super.removeNotify()
        UIManager.removePropertyChangeListener(lafListener)
        if (window != null) {
            window!!.removeWindowListener(windowListener)
            window = null
        }
    }

    private fun lafChanged(e: PropertyChangeEvent) {
        if ("lookAndFeel" == e.propertyName) selectedCurrentLookAndFeel()
    }

    private fun windowActivated() {
        // refresh themes list on window activation
        if (themesManager.hasThemesFromDirectoryChanged()) updateThemesList()
    }

    private fun selectedCurrentLookAndFeel() {
        val lookAndFeel = UIManager.getLookAndFeel()
        val theme = UIManager.getLookAndFeelDefaults().getString(AppPrefs.THEME_UI_KEY)
        if (theme == null && (lookAndFeel is ThemeLaf || lookAndFeel is FlatPropertiesLaf)) return
        val test: Predicate<IJThemeInfo> = if (theme != null && theme.startsWith(AppPrefs.RESOURCE_PREFIX)) {
            val resourceName = theme.substring(AppPrefs.RESOURCE_PREFIX.length)
            Predicate { ti: IJThemeInfo -> ti.resourceName == resourceName }
        } else if (theme != null && theme.startsWith(AppPrefs.FILE_PREFIX)) {
            val themeFile = File(theme.substring(AppPrefs.FILE_PREFIX.length))
            Predicate { ti: IJThemeInfo -> ti.themeFile == themeFile }
        } else {
            val lafClassName = lookAndFeel.javaClass.name
            Predicate { ti: IJThemeInfo -> ti.lafClassName == lafClassName }
        }
        var newSel = -1
        for (i in themes.indices) {
            if (test.test(themes[i])) {
                newSel = i
                break
            }
        }
        isAdjustingThemesList = true
        if (newSel >= 0) {
            if (newSel != themesList!!.selectedIndex) themesList!!.selectedIndex = newSel
        } else themesList!!.clearSelection()
        isAdjustingThemesList = false
    }

    private fun filterChanged() {
        updateThemesList()
    }

    private fun initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        val themesLabel = JLabel()
        toolBar = JToolBar()
        saveButton = JButton()
        sourceCodeButton = JButton()
        filterComboBox = JComboBox()
        themesScrollPane = JScrollPane()
        themesList = JList()

        //======== this ========
        layout = MigLayout(
            "insets dialog,hidemode 3",  // columns
            "[grow,fill]",  // rows
            "[]3" +
                    "[grow,fill]"
        )

        //---- themesLabel ----
        themesLabel.text = "Themes:"
        add(themesLabel, "cell 0 0")

        //======== toolBar ========
        run {
            toolBar!!.isFloatable = false

            //---- saveButton ----
            saveButton!!.toolTipText = "Save .theme.json of selected IntelliJ theme to file."
            saveButton!!.addActionListener { saveTheme() }
            toolBar!!.add(saveButton)

            //---- sourceCodeButton ----
            sourceCodeButton!!.toolTipText =
                "Opens the source code repository of selected IntelliJ theme in the browser."
            sourceCodeButton!!.addActionListener { browseSourceCode() }
            toolBar!!.add(sourceCodeButton)
        }
        add(toolBar!!, "cell 0 0,alignx right,growx 0")

        //---- filterComboBox ----
        filterComboBox!!.model = DefaultComboBoxModel(
            arrayOf(
                "all",
                "light",
                "dark"
            )
        )
        filterComboBox!!.putClientProperty("JComponent.minimumWidth", 0)
        filterComboBox!!.isFocusable = false
        filterComboBox!!.addActionListener { filterChanged() }
        add(filterComboBox!!, "cell 0 0,alignx right,growx 0")

        //======== themesScrollPane ========
        run {


            //---- themesList ----
            themesList!!.selectionMode = ListSelectionModel.SINGLE_SELECTION
            themesList!!.addListSelectionListener { e: ListSelectionEvent -> this.themesListValueChanged(e) }
            themesScrollPane!!.setViewportView(themesList)
        }
        add(themesScrollPane!!, "cell 0 1")
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private var toolBar: JToolBar? = null
    private var saveButton: JButton? = null
    private var sourceCodeButton: JButton? = null
    private var filterComboBox: JComboBox<String>? = null
    private var themesScrollPane: JScrollPane? = null
    private var themesList: JList<IJThemeInfo>? =
        null // JFormDesigner - End of variables declaration  //GEN-END:variables

    init {
        initComponents()
        saveButton!!.isEnabled = false
        sourceCodeButton!!.isEnabled = false
        saveButton!!.icon = FlatSVGIcon("/icons/download.svg")
        sourceCodeButton!!.icon = FlatSVGIcon("/icons/github.svg")

        // create renderer
        themesList!!.cellRenderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?, value: Any,
                index: Int, isSelected: Boolean, cellHasFocus: Boolean
            ): Component {
                val title = categories[index]
                var name = (value as IJThemeInfo).name
                val sep = name.indexOf('/')
                if (sep >= 0) name = name.substring(sep + 1).trim { it <= ' ' }
                val c = super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus) as JComponent
                c.toolTipText = buildToolTip(value)
                if (title != null) c.border = CompoundBorder(ListCellTitledBorder(themesList, title), c.border)
                return c
            }

            private fun buildToolTip(ti: IJThemeInfo): String? {
                if (ti.themeFile != null) return ti.themeFile.path
                return if (ti.resourceName == null) ti.name else """
     Name: ${ti.name}
     License: ${ti.license}
     Source Code: ${ti.sourceCodeUrl}
     """.trimIndent()
            }
        }
        updateThemesList()
    }

    companion object {
        @Suppress("SpellCheckingInspection")
        const val THEMES_PACKAGE = "/com/formdev/flatlaf/intellijthemes/themes/"
    }
}