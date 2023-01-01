package com.cryptomcgrath.pyrexia.login

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.viewModels
import androidx.databinding.ObservableField
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cryptomcgrath.pyrexia.databinding.ActivityLoginBinding
import com.cryptomcgrath.pyrexia.deviceconfig.createNetworkErrorAlertDialog
import com.cryptomcgrath.pyrexia.deviceconfig.hideKeyboard
import com.cryptomcgrath.pyrexia.model.PyDevice
import com.cryptomcgrath.pyrexia.service.PyrexiaService
import com.edwardmcgrath.blueflux.core.Event
import com.edwardmcgrath.blueflux.core.EventQueue
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.regex.Pattern


class LoginActivity: FragmentActivity() {
    internal val viewModel: LoginViewModel by viewModels {
        LoginViewModel.Factory(
            application = application,
            pyDevice = intent.extras?.getParcelable(EXTRA_PYDEVICE)!!)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.eventQueue.handleEvents(this) {
            when (it) {
                LoginViewModel.UiEvent.LoginSuccess -> {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                is LoginViewModel.UiEvent.NetworkError -> {
                    createNetworkErrorAlertDialog(this, it.throwable) {}.show()
                }
            }
        }

        val binding = ActivityLoginBinding.inflate(
            LayoutInflater.from(this), null, false)
        binding.model = viewModel
        setContentView(binding.root)
    }

    companion object {
        fun createLoginIntent(ctx: Context, pyDevice: PyDevice): Intent {
            return Intent(
                ctx, LoginActivity::class.java
            ).apply {
                putExtra(EXTRA_PYDEVICE, pyDevice)
            }
        }
    }
}

private const val EXTRA_PYDEVICE = "extra_pydevice"

internal class LoginViewModel(application: Application, pyDevice: PyDevice) : AndroidViewModel(application) {

    class Factory(private val application: Application,
                  private val pyDevice: PyDevice) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(application, pyDevice) as T
        }
    }

    val eventQueue = EventQueue.create()
    private val pyrexiaService = PyrexiaService(application, pyDevice)

    val name = pyDevice.name
    val url = pyDevice.baseUrl
    var email = pyDevice.email
    var password: String = ""

    val emailError = ObservableField<String>()
    val passwordError = ObservableField<String>()

    private val disposables = CompositeDisposable()

    private fun checkErrors(): Boolean {
        var error = false
        emailError.set(null)
        passwordError.set(null)

        if (!isValidEmail(email)) {
            error = true
            emailError.set("Invalid email address")
        }

        if (password.isEmpty()) {
            error = true
            passwordError.set("Password cannot be empty")
        }
        return error
    }

    fun onClickLogin(view: View?) {
        view?.hideKeyboard()

        if (!checkErrors()) {
            pyrexiaService.login(email, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onComplete = {
                        eventQueue.post(UiEvent.LoginSuccess)
                    }, onError = {
                        eventQueue.post(UiEvent.NetworkError(it))
                    }
                ).addTo(disposables)
        }
    }

    fun onEditorAction(view: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE ||
            (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KEYCODE_ENTER)) {
            onClickLogin(view)
            return true
        }
        return false
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    sealed class UiEvent: Event {
        object LoginSuccess: UiEvent()
        data class NetworkError(val throwable: Throwable): UiEvent()
    }
}

private val EMAIL_ADDRESS_PATTERN = Pattern.compile(
    "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
            "\\@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+"
)

fun isValidEmail(email: String): Boolean {
    return EMAIL_ADDRESS_PATTERN.matcher(email).matches()
}
