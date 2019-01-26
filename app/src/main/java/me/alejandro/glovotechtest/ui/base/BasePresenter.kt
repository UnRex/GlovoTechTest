package me.alejandro.glovotechtest.ui.base

interface BasePresenter<in V: BaseView> {

    fun attachView(view: V)

    fun detachView()

}