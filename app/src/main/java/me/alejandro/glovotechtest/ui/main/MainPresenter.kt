package me.alejandro.glovotechtest.ui.main

import ApiClient
import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import me.alejandro.glovotechtest.model.City
import me.alejandro.glovotechtest.model.Country
import me.alejandro.glovotechtest.ui.base.BasePresenterImpl
import me.alejandro.glovotechtest.util.Constants
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.text.Collator


class MainPresenter : BasePresenterImpl<MainContract.View>(), MainContract.Presenter {

    private val moshi = Moshi.Builder().build()

    private var cities: List<City>? = listOf()
    private var countries: List<Country>? = listOf()
    private var elementsInMap = mutableListOf<Pair<String, String>>()

    override fun checkData(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)


        val storedCountries = preferences.getString(Constants.COUNTRIES_KEY, "")
        if (storedCountries.isNullOrBlank()) {
            loadCountries()
        } else {
            val type = Types.newParameterizedType(List::class.java, Country::class.java)
            val moshiAdapter: JsonAdapter<List<Country>> = moshi.adapter(type)
            countries = moshiAdapter.fromJson(storedCountries)
        }

        val storedCities = preferences.getString(Constants.CITIES_KEY, "")
        if (storedCities.isNullOrBlank()) {
            loadCities()
        } else {
            val type = Types.newParameterizedType(List::class.java, City::class.java)
            val moshiAdapter: JsonAdapter<List<City>> = moshi.adapter(type)
            cities = moshiAdapter.fromJson(storedCities)
        }
    }

    override fun addMarker(markerId: String, cityCode: String) {
        val filteredList = elementsInMap.filter {
            it.first == markerId
        }
        if (filteredList.isEmpty()) {
            elementsInMap.add(Pair(markerId, cityCode))
        }
    }

    override fun getCityFromMarker(markerId: String): String {
        val filteredList = elementsInMap.filter {
            it.first == markerId
        }

        return filteredList[0].second
    }

    private fun loadCities() {
        ApiClient.getService()
            .getCities()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { view?.showLoading() }
            .doOnTerminate { view?.hideLoading() }
            .subscribe({ response ->
                val type = Types.newParameterizedType(List::class.java, City::class.java)
                val citiesAdapter: JsonAdapter<List<City>> = moshi.adapter(type)
                cities = response
                view?.storeResponse(citiesAdapter.toJson(cities), Constants.CITIES_KEY)
            },
                { error ->
                    Log.e("error", error.message)
                    view?.showMessage(error.message)
                }
            )
    }

    private fun loadCountries() {
        ApiClient.getService()
            .getCountries()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { view?.showLoading() }
            .doOnTerminate { view?.hideLoading() }

            .subscribe({ response ->
                val type = Types.newParameterizedType(List::class.java, Country::class.java)
                val countriesAdapter: JsonAdapter<List<Country>> = moshi.adapter(type)
                countries = response
                view?.storeResponse(countriesAdapter.toJson(countries), Constants.COUNTRIES_KEY)
            },
                { error ->
                    Log.e("error", error.message)
                    view?.showMessage(error.message)
                })
    }

    override fun getCityCodeFromCityName(cityName: String): String? {
        val filteredList = cities?.filter {
            val instance = Collator.getInstance()
            instance.strength = Collator.NO_DECOMPOSITION
            instance.compare(cityName, it.name) == 0

        }

        return filteredList?.get(0)?.code
    }

    override fun getCities(): List<City>? {

        return cities

    }

    override fun getDetailedCityInfo(cityCode: String) {
        ApiClient.getService()
            .getCity(cityCode)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { view?.showLoading() }
            .doOnTerminate { view?.hideLoading() }

            .subscribe({ response ->
                view?.fillCityInfo(response)
            },
                { error ->
                    Log.e("error", error.message)
                    view?.showMessage(error.message)
                })
    }

    override fun checkIfCityExists(cityName: String?): Boolean? {
        if (cityName.isNullOrBlank()) {
            return false
        }

        val filterList = cities?.filter { city ->
            val instance = Collator.getInstance()
            instance.strength = Collator.NO_DECOMPOSITION
            instance.compare(cityName, city.name) == 0
        }

        return filterList?.isNotEmpty()

    }

    override fun getCountryByCode(countryCode: String): String? {
        val filterList = countries?.filter {
            countryCode == it.code
        }
        return filterList?.get(0)?.name
    }


}