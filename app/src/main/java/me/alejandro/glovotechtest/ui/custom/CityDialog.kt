package me.alejandro.glovotechtest.ui.custom

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.SearchView
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import me.alejandro.glovotechtest.R
import me.alejandro.glovotechtest.model.City
import me.alejandro.glovotechtest.ui.main.MainActivity
import me.alejandro.glovotechtest.util.Constants
import me.alejandro.glovotechtest.util.Util


class CityDialog: DialogFragment() {

    lateinit var cityListView: ListView
    lateinit var citySearchView: SearchView
    lateinit var dismissButton: Button
    lateinit var adapter: ArrayAdapter<String>

    private val moshi = Moshi.Builder().build()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.city_dialog, null)
        isCancelable = false

        var citiesJson = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(Constants.CITIES_KEY, null)

        if (citiesJson.isNullOrBlank()){
            citiesJson = Util.loadJSONFromAsset(context,"cities.json")
        }

        val type = Types.newParameterizedType(List::class.java, City::class.java)
        val moshiAdapter: JsonAdapter<List<City>> = moshi.adapter(type)
        val cities = moshiAdapter.fromJson(citiesJson)

        val citiesString = mutableListOf<String>()

        cities?.forEach {
            citiesString.add(it.name)
        }

        dialog.setTitle(R.string.city_dialog_title)
        cityListView = rootView.findViewById(R.id.city_list_view)
        citySearchView = rootView.findViewById(R.id.city_search_view)

        adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, citiesString)
        cityListView.adapter = adapter

        citySearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(txt: String): Boolean {
                return false
            }

            override fun onQueryTextChange(txt: String): Boolean {
                adapter.filter.filter(txt)
                return false
            }
        })

        cityListView.setOnItemClickListener { _, _, position, _->
            cities.let {
                (activity as MainActivity).setPositionOnMapByCityName(it?.get(position)?.name)
                dismiss()
            }

        }

        return rootView
    }
}