package com.ultreon.browser.main

import com.google.gson.annotations.SerializedName
import org.jetbrains.annotations.ApiStatus

/**
 * Product info.
 *
 * @author Qboi123
 */
@ApiStatus.Internal
class ProductJson internal constructor() {
    var version: String = ""
        private set

    var name: String = ""
        private set

    var id: String = ""
        private set

    @field:SerializedName("build-date")
    var buildDate: String = ""
        private set
}