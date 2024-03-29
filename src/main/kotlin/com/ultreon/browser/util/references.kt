package com.ultreon.browser.util

import com.google.gson.Gson
import com.ultreon.browser.ProductJson
import org.slf4j.LoggerFactory
import java.awt.Image
import java.awt.Toolkit
import java.lang.System.getProperty
import java.net.URL

val osName: String = getProperty("os.name")
private val gson = Gson()
private val productJson = gson.fromJson(
    ProductJson::class.java.getResourceAsStream("/product.json")!!.bufferedReader(),
    ProductJson::class.java
)

const val useOSR = false

// Resources
const val APP_ICON: String = "/icons/icon.png"
const val APP_BANNER: String = "/images/banner.png"

const val LOADING_ICON_PATH: String = "/icons/loading.gif"

val LOADING_ICON_REF: URL = ProductJson::class.java.getResource(LOADING_ICON_PATH)!!

// Resource references
val APP_ICON_REF: URL = ProductJson::class.java.getResource(APP_ICON)!!
val APP_BANNER_REF: URL = ProductJson::class.java.getResource(APP_BANNER)!!

// Product properties.
val APP_ID: String = productJson.id
val APP_NAME: String = productJson.name
val APP_VERSION: String = productJson.version
val CHROME_VERSION: String = productJson.chromeVersion
val BUILD_DATE: String = productJson.buildDate
const val SOURCE_URL: String = "https://github.com/Ultreon/ultreon-browser"
const val ISSUES_URL: String = "https://github.com/Ultreon/ultreon-browser/issues"
const val NEW_ISSUE_URL: String = "https://github.com/Ultreon/ultreon-browser/issues/new/choose"

val LOADING_ICON: Image = Toolkit.getDefaultToolkit().createImage(LOADING_ICON_REF.openStream().use { it.readBytes() })

val LOGGER = LoggerFactory.getLogger("ultreon-browser")
