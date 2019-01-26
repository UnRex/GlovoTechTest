package me.alejandro.glovotechtest.ui.base

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import me.alejandro.glovotechtest.R

abstract class BaseActivity<in V: BaseView, P: BasePresenter<V>>: AppCompatActivity(), BaseView {

    lateinit var spinner: ProgressBar
    protected abstract var presenter: P

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.attachView(this as V)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(R.layout.activity_base)
        val container: ViewGroup = findViewById(R.id.main_container)
        layoutInflater.inflate(layoutResID, container, true)

        spinner = findViewById(R.id.main_spinner)
    }

    override fun showLoading() {
        spinner.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        spinner.visibility = View.GONE
    }

    override fun showMessage(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}