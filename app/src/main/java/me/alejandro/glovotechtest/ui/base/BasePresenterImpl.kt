package me.alejandro.glovotechtest.ui.base

abstract class BasePresenterImpl<V: BaseView>: BasePresenter<V> {

    protected var view: V? =null

    override fun attachView(view: V) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }

}