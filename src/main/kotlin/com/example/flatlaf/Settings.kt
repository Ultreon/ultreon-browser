package com.example.flatlaf

import java.awt.Font

object Settings {
    var font: Font
        get() = Font(AppPrefs.fontName, AppPrefs.fontStyle, AppPrefs.fontSize)
        set(value) {
            AppPrefs.fontName = value.name
            AppPrefs.fontStyle = value.style
            AppPrefs.fontSize = value.size
        }
    lateinit var theme: String
}
