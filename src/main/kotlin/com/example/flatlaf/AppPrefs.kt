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
 * Changes made: Class name changed to AppPrefs, original class name was DemoPrefs
 * Changes made: Reformatted code with IntelliJ IDEA. And converted to Kotlin.
 * Changes made: Added program specific preferences.
 * Source: https://github.com/JFormDesigner/FlatLaf
 */
@file:Suppress("unused")

package com.example.flatlaf

import com.formdev.flatlaf.FlatLaf
import com.formdev.flatlaf.FlatLightLaf
import com.formdev.flatlaf.FlatPropertiesLaf
import com.formdev.flatlaf.IntelliJTheme
import com.formdev.flatlaf.IntelliJTheme.ThemeLaf
import com.formdev.flatlaf.util.LoggingFacade
import com.formdev.flatlaf.util.StringUtils
import com.example.flatlaf.intellijthemes.IJThemesPanel
import org.apache.commons.lang.SystemUtils
import java.awt.Font
import java.beans.PropertyChangeEvent
import java.io.File
import java.io.FileInputStream
import java.util.prefs.Preferences
import javax.swing.UIManager

/**
 * @author Karl Tauber
 */
object AppPrefs {
    val KEY_LAF = "${APP_ID}.laf"
    val KEY_LAF_THEME = "${APP_ID}.lafTheme"
    val KEY_WORD_WRAP = "${APP_ID}.wordWrap"
    val KEY_FONT_NAME = "${APP_ID}.fontName"
    val KEY_FONT_SIZE = "${APP_ID}.fontSize"
    val KEY_FONT_STYLE = "${APP_ID}.fontStyle"
    val KEY_RECENT_FILES = "${APP_ID}.recentFiles"
    const val RESOURCE_PREFIX = "res:"
    const val FILE_PREFIX = "file:"
    val THEME_UI_KEY = "${APP_ID}.theme"
    var fontName: String
        get() = Preferences.userRoot().get(KEY_FONT_NAME, "SansSerif")
        set(value) = Preferences.userRoot().put(KEY_FONT_NAME, value)
    var fontSize: Int
        get() = Preferences.userRoot().getInt(KEY_FONT_SIZE, 12)
        set(value) = Preferences.userRoot().putInt(KEY_FONT_SIZE, value)
    var fontStyle: Int
        get() = Preferences.userRoot().getInt(KEY_FONT_STYLE, Font.PLAIN)
        set(value) = Preferences.userRoot().putInt(KEY_FONT_STYLE, value)
    var wordWrap: Boolean
        get() = Preferences.userRoot().getBoolean(KEY_WORD_WRAP, true)
        set(value) = Preferences.userRoot().putBoolean(KEY_WORD_WRAP, value)
    var recentFiles: List<File>
        get() = Preferences.userRoot()
            .get(KEY_RECENT_FILES, "")
            .split(SystemUtils.PATH_SEPARATOR)
            .filter { it.isNotEmpty() }
            .map { File(it) }
            .filter { it.exists() && it.isFile }
        set(value) = Preferences.userRoot()
            .put(KEY_RECENT_FILES, value.joinToString(SystemUtils.PATH_SEPARATOR) { it.absolutePath })
    lateinit var state: Preferences
        private set

    fun init(rootPath: String?) {
        state = Preferences.userRoot().node(rootPath)
    }

    fun setupLaf(args: Array<String?>) {
        // set look and feel
        try {
            if (args.isNotEmpty()) UIManager.setLookAndFeel(args[0]) else {
                val lafClassName = state[KEY_LAF, FlatLightLaf::class.java.name]
                if (ThemeLaf::class.java.name == lafClassName) {
                    val theme = state[KEY_LAF_THEME, ""]
                    if (theme.startsWith(RESOURCE_PREFIX)) IntelliJTheme.setup(
                        IJThemesPanel::class.java.getResourceAsStream(
                            IJThemesPanel.THEMES_PACKAGE + theme.substring(
                                RESOURCE_PREFIX.length
                            )
                        )
                    ) else if (theme.startsWith(FILE_PREFIX)) FlatLaf.setup(
                        IntelliJTheme.createLaf(
                            FileInputStream(
                                theme.substring(
                                    FILE_PREFIX.length
                                )
                            )
                        )
                    ) else FlatLightLaf.setup()
                    if (theme.isNotEmpty()) UIManager.getLookAndFeelDefaults()[THEME_UI_KEY] = theme
                } else if (FlatPropertiesLaf::class.java.name == lafClassName) {
                    val theme = state[KEY_LAF_THEME, ""]
                    if (theme.startsWith(FILE_PREFIX)) {
                        val themeFile = File(theme.substring(FILE_PREFIX.length))
                        val themeName = StringUtils.removeTrailing(themeFile.name, ".properties")
                        FlatLaf.setup(FlatPropertiesLaf(themeName, themeFile))
                    } else FlatLightLaf.setup()
                    if (theme.isNotEmpty()) UIManager.getLookAndFeelDefaults()[THEME_UI_KEY] = theme
                } else UIManager.setLookAndFeel(lafClassName)
            }
        } catch (ex: Throwable) {
            LoggingFacade.INSTANCE.logSevere(null, ex)

            // fallback
            FlatLightLaf.setup()
        }

        // remember active look and feel
        UIManager.addPropertyChangeListener { e: PropertyChangeEvent ->
            if ("lookAndFeel" == e.propertyName) state.put(
                KEY_LAF, UIManager.getLookAndFeel().javaClass.name
            )
        }
    }
}