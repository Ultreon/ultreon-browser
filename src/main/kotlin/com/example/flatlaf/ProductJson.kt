package com.example.flatlaf

import com.google.gson.annotations.SerializedName
import org.jetbrains.annotations.ApiStatus.Internal

/**
 * Product info.
 *
 * @author Qboi123
 */
@Internal
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
