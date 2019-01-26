package me.alejandro.glovotechtest.ui.main

import android.content.Context
import me.alejandro.glovotechtest.model.City
import me.alejandro.glovotechtest.model.CityDetailed
import me.alejandro.glovotechtest.ui.base.BasePresenter
import me.alejandro.glovotechtest.ui.base.BaseView

object MainContract{

    interface View: BaseView {
        fun storeResponse(response: String, key: String)
        fun fillCityInfo(city: CityDetailed)

    }

    interface Presenter: BasePresenter<View> {
        fun checkData(context: Context)
        fun checkIfCityExists(cityName: String?): Boolean?
        fun getCountryByCode(countryCode: String): String?
        fun getDetailedCityInfo(cityCode: String)
        fun getCities():List<City>?
        fun addMarker(markerId: String, cityCode: String)
        fun getCityFromMarker(markerId: String): String
        fun getCityCodeFromCityName(cityName: String): String?
    }

}