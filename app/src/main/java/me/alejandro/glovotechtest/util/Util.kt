package me.alejandro.glovotechtest.util

import android.content.Context

class Util {
    companion object {
        fun loadJSONFromAsset(activity: Context?, filename: String): String? {
            return activity?.assets?.open(filename)?.bufferedReader().use {
                it?.readText()
            }
        }
    }

}