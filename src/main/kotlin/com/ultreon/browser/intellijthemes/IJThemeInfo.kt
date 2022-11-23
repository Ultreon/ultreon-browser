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
package com.ultreon.browser.intellijthemes

import java.io.File

/**
 * @author Karl Tauber
 */
internal class IJThemeInfo(
    val name: String, val resourceName: String?, val dark: Boolean,
    val license: String?, val licenseFile: String?,
    val sourceCodeUrl: String?, val sourceCodePath: String?,
    val themeFile: File?, val lafClassName: String?, val isSystemTheme: Boolean = false
)