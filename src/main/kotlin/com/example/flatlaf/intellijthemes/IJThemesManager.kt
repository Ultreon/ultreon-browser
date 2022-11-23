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
package com.example.flatlaf.intellijthemes

import com.formdev.flatlaf.json.Json
import com.formdev.flatlaf.util.LoggingFacade
import com.formdev.flatlaf.util.StringUtils
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

/**
 * @author Karl Tauber
 */
internal class IJThemesManager {
    val bundledThemes: MutableList<IJThemeInfo> = ArrayList()
    val moreThemes: MutableList<IJThemeInfo> = ArrayList()
    private val lastModifiedMap: MutableMap<File, Long> = HashMap()
    fun loadBundledThemes() {
        bundledThemes.clear()

        // load themes.json
        var json: Map<String?, Any>
        try {
            InputStreamReader(
                javaClass.getResourceAsStream("/themes.json")!!,
                StandardCharsets.UTF_8
            ).use { reader ->
                @Suppress("UNCHECKED_CAST")
                json = Json.parse(reader) as Map<String?, Any>
            }
        } catch (ex: IOException) {
            LoggingFacade.INSTANCE.logSevere(null, ex)
            return
        }

        // add info about bundled themes
        for ((resourceName, value1) in json) {
            @Suppress("UNCHECKED_CAST") val value = value1 as Map<String, String>
            val name = value["name"]
            val dark = java.lang.Boolean.parseBoolean(value["dark"])
            val license = value["license"]
            val licenseFile = value["licenseFile"]
            val sourceCodeUrl = value["sourceCodeUrl"]
            val sourceCodePath = value["sourceCodePath"]
            bundledThemes.add(
                IJThemeInfo(
                    name!!,
                    resourceName,
                    dark,
                    license,
                    licenseFile,
                    sourceCodeUrl,
                    sourceCodePath,
                    null,
                    null
                )
            )
        }
    }

    fun loadThemesFromDirectory() {
        // get current working directory
        val directory = File("").absoluteFile
        val themeFiles =
            directory.listFiles { _: File?, name: String ->
                name.endsWith(".theme.json") || name.endsWith(".properties")
            }
                ?: return
        lastModifiedMap.clear()
        lastModifiedMap[directory] = directory.lastModified()
        moreThemes.clear()
        for (f in themeFiles) {
            val fname = f.name
            val name = if (fname.endsWith(".properties")) StringUtils.removeTrailing(
                fname,
                ".properties"
            ) else StringUtils.removeTrailing(fname, ".theme.json")
            moreThemes.add(IJThemeInfo(name, null, false, null, null, null, null, f, null))
            lastModifiedMap[f] = f.lastModified()
        }
    }

    fun hasThemesFromDirectoryChanged(): Boolean {
        for ((key, value) in lastModifiedMap) {
            if (key.lastModified() != value) return true
        }
        return false
    }
}