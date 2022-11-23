package com.example.flatlaf

import com.google.gson.Gson
import java.net.URL

private val gson = Gson()
private val productJson = gson.fromJson(ProductJson::class.java.getResourceAsStream("/product.json")!!.bufferedReader(), ProductJson::class.java)

// Resources
const val APP_ICON: String = "/icons/icon.png"
const val APP_BANNER: String = "/images/banner.png"

// Resource references
val APP_ICON_REF: URL = ProductJson::class.java.getResource(APP_ICON)!!
val APP_BANNER_REF: URL = ProductJson::class.java.getResource(APP_BANNER)!!

// Product properties.
val APP_ID: String = productJson.id
val APP_NAME: String = productJson.name
val APP_VERSION: String = productJson.version
val BUILD_DATE: String = productJson.buildDate
const val SOURCE_URL: String = "https://github.com/Ultreon/flatlaf-template"
const val ISSUES_URL: String = "https://github.com/Ultreon/flatlaf-template/issues"
const val NEW_ISSUE_URL: String = "https://github.com/Ultreon/flatlaf-template/issues/new/choose"
