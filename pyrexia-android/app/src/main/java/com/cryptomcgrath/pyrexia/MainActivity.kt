package com.cryptomcgrath.pyrexia


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.cryptomcgrath.pyrexia.login.LoginActivity
import com.cryptomcgrath.pyrexia.login.RESULT_CODE_LOGIN
import com.edwardmcgrath.blueflux.core.Dispatcher
import com.edwardmcgrath.blueflux.core.EventQueue
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers


class MainActivity: AppCompatActivity() {
    private val viewModel: MainActivityViewModel by viewModels {
        MainActivityViewModel.Factory(CentralStore.getInstance(application).dispatcher)
    }

    private val navController: NavController? get() {
        val navFrag = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        return navFrag?.navController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.eventQueue.handleEvents(this) { event ->
            when (event) {
                is CentralEvent.GoToLogin -> {
                    startActivityForResult(
                        LoginActivity.createLoginIntent(this, event.pyDevice),
                        RESULT_CODE_LOGIN
                    )
                }
            }
        }

        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (requestCode == RESULT_CODE_LOGIN && resultCode != Activity.RESULT_OK) {
                navController?.popBackStack(R.id.deviceListFragment, false)
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
    }
}

class MainActivityViewModel(val dispatcher: Dispatcher) : ViewModel() {
    class Factory(private val dispatcher: Dispatcher) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainActivityViewModel(dispatcher) as T
        }
    }

    val eventQueue = EventQueue.create()
    val disposables = CompositeDisposable()

    init {
        relayEventsToActivity()
    }

    private fun relayEventsToActivity() {
        dispatcher.getEventBus()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    eventQueue.post(it)
                }
            ).addTo(disposables)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}