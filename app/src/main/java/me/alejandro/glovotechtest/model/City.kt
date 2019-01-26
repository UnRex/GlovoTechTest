package me.alejandro.glovotechtest.model

import com.squareup.moshi.Json

data class City(
    @Json(name = "working_area")
    val working_area: List<String>,
    @Json(name = "code")
    val code: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "country_code")
    val country_code: String
)