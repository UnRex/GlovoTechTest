package me.alejandro.glovotechtest.model

import com.squareup.moshi.Json

data class CityDetailed(
    @Json(name = "code")
    val code: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "currency")
    val currency: String,
    @Json(name = "country_code")
    val country_code: String,
    @Json(name = "enabled")
    val enabled: Boolean,
    @Json(name = "time_zone")
    val time_zone: String,
    @Json(name = "working_area")
    val working_area: List<String>,
    @Json(name = "busy")
    val busy: Boolean,
    @Json(name = "language_code")
    val language_code: String

)