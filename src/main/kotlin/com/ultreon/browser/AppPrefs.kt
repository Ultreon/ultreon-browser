package com.ultreon.browser

import com.formdev.flatlaf.FlatLaf
import com.formdev.flatlaf.FlatPropertiesLaf
import com.formdev.flatlaf.IntelliJTheme
import com.formdev.flatlaf.themes.FlatMacDarkLaf
import com.formdev.flatlaf.util.LoggingFacade
import com.formdev.flatlaf.util.StringUtils
import com.ultreon.browser.util.APP_ID
import com.ultreon.browser.intellijthemes.IJThemesPanel
import java.beans.PropertyChangeEvent
import java.io.File
import java.io.FileInputStream
import java.util.prefs.Preferences
import javax.swing.UIManager

/**
 * @author Karl Tauber
 */
object AppPrefs {
    val KEY_LAF = "$APP_ID.laf"
    val KEY_LAF_THEME = "$APP_ID.lafTheme"
    const val RESOURCE_PREFIX = "res:"
    const val FILE_PREFIX = "file:"
    val THEME_UI_KEY = "$APP_ID.theme"
    lateinit var state: Preferences
        private set

    fun init(rootPath: String?) {
        state = Preferences.userRoot().node(rootPath)
    }

    fun setupLaf(args: Array<String?>) {
        // set look and feel
        try {
            if (args.isNotEmpty()) UIManager.setLookAndFeel(args[0]) else {
                val lafClassName = state[KEY_LAF, FlatMacDarkLaf::class.java.name]
                if (IntelliJTheme.ThemeLaf::class.java.name == lafClassName) {
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
                    ) else FlatMacDarkLaf.setup()
                    if (theme.isNotEmpty()) UIManager.getLookAndFeelDefaults()[THEME_UI_KEY] = theme
                } else if (FlatPropertiesLaf::class.java.name == lafClassName) {
                    val theme = state[KEY_LAF_THEME, ""]
                    if (theme.startsWith(FILE_PREFIX)) {
                        val themeFile = File(theme.substring(FILE_PREFIX.length))
                        val themeName = StringUtils.removeTrailing(themeFile.name, ".properties")
                        FlatLaf.setup(FlatPropertiesLaf(themeName, themeFile))
                    } else FlatMacDarkLaf.setup()
                    if (theme.isNotEmpty()) UIManager.getLookAndFeelDefaults()[THEME_UI_KEY] = theme
                } else UIManager.setLookAndFeel(lafClassName)
            }
        } catch (ex: Throwable) {
            LoggingFacade.INSTANCE.logSevere(null, ex)

            // fallback
            FlatMacDarkLaf.setup()
        }

        // remember active look and feel
        UIManager.addPropertyChangeListener { e: PropertyChangeEvent ->
            if ("lookAndFeel" == e.propertyName) state.put(
                KEY_LAF, UIManager.getLookAndFeel().javaClass.name
            )
        }
    }
}