package me.alejandro.glovotechtest.model

import com.squareup.moshi.Json

data class Country(
    @Json(name = "code")
    val code: String,
    @Json(name = "name")
    val name: String)