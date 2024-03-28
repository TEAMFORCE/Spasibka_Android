package com.teamforce.thanksapp.utils

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_FADE
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.teamforce.thanksapp.R
import com.teamforce.thanksapp.data.response.commons.CommonStatusResponse
import com.teamforce.thanksapp.data.response.errors.AuthErrorResponse
import com.teamforce.thanksapp.data.response.errors.CommonError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

sealed class Result<out T : Any> {
    data class Success<out T : Any>(val value: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

sealed class ResultWrapper<out T> {
    data class Success<out T>(val value: T) : ResultWrapper<T>()
    data class GenericError(val code: Int? = null, val error: String? = null) :
        ResultWrapper<Nothing>()

    data class NetworkError(val error: String = "Network error") : ResultWrapper<Nothing>()
}

fun <T, R> ResultWrapper<T>.mapWrapperData(mapper: (T) -> R): ResultWrapper<R> = when (this) {
    is ResultWrapper.Success -> ResultWrapper.Success(mapper(value))
    is ResultWrapper.GenericError -> this
    is ResultWrapper.NetworkError -> this
}

suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher,
    apiCall: suspend () -> T,
): ResultWrapper<T> {
    return withContext(dispatcher + SupervisorJob()) {
        try {
            ResultWrapper.Success(apiCall.invoke())
        } catch (throwable: Throwable) {
            Timber.e("safeApiCall >> \n$throwable")
            handleRequestError(throwable)
        }
    }
}

suspend fun safeNullableApiCall(
    dispatcher: CoroutineDispatcher,
    apiCall: suspend () -> Unit,
) {
    return withContext(dispatcher + SupervisorJob()) {
        try {
            apiCall.invoke()
        } catch (throwable: Throwable) {
            handleRequestError<Throwable>(throwable)
        }
    }
}

fun <T> handleRequestError(throwable: Throwable): ResultWrapper<T> {
    return when (throwable) {
        is IOException -> ResultWrapper.NetworkError()
        is HttpException -> {
            try {
                val code = throwable.code()
                val errorBody = throwable.response()?.errorBody()!!.string()
                val errorResponse = Gson().fromJson(errorBody, CommonError::class.java)
                val parsedError = parseErrorToString(errorResponse, throwable, errorBody)
                ResultWrapper.GenericError(code, parsedError)
            } catch (e: Exception) {
                ResultWrapper.GenericError(throwable.code(), throwable.localizedMessage)
            }
        }

        else -> {
            try {
                val errorResponse = (throwable.localizedMessage)
                ResultWrapper.GenericError(-1, errorResponse)
            } catch (e: Exception) {
                ResultWrapper.GenericError(-1, throwable.message)
            }
        }
    }
}

private fun parseErrorToString(errorResponse: CommonError, throwable: Throwable, errorBody: String): String {
    return errorResponse.detail
        ?: (errorResponse.errors
            ?: (errorResponse.status
                ?: return "${throwable.localizedMessage} $errorBody"))
}

fun showSnackBarAboutNetworkProblem(view: View, context: Context) {
    val snackbar = Snackbar.make(
        view,
        context.getString(R.string.networkError),
        Snackbar.LENGTH_LONG
    )
    val layout = snackbar.view as Snackbar.SnackbarLayout
    val snackView: View = LayoutInflater.from(context).inflate(R.layout.snackbar_template, null)
    var params: MarginLayoutParams = snackbar.getView().getLayoutParams() as MarginLayoutParams
    params.setMargins(28, 0, 28, 260)
    snackbar.getView().setLayoutParams(params)
    val imageView: ImageView = snackView.findViewById(R.id.image_snackBar)
    imageView.setImageResource(R.drawable.wifi_off)
    layout.setPadding(0, 0, 0, 0)
    layout.addView(snackView, 0);
    snackbar.show()
}

fun <T> ResultWrapper<T>.toSuccess(): T? {
    return when (this) {
        is ResultWrapper.Success -> value
        else -> null
    }
}

fun <T> ResultWrapper<T>.toResultState(
    onLoading: ((Boolean) -> Unit)? = null,
    onError: ((String?, String?) -> Unit)? = null,
    onSuccess: (T) -> Unit,
): ResultWrapper<T> {
    onLoading?.invoke(false)
    when (this) {
        is ResultWrapper.GenericError -> {
            Timber.d("Error ->> $error ")
            onError?.invoke("$error", "$code")
        }

        is ResultWrapper.NetworkError -> {
            Timber.d("Error ->> NetworkError")
            onError?.invoke("$error", "")
        }

        is ResultWrapper.Success -> onSuccess(value)
    }
    return this
}

class SingleLiveEvent<T> : MutableLiveData<T>() {
    private val pending = AtomicBoolean(false)
    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        if (hasActiveObservers()) {
            Log.w(TAG, "Multiple observers registered but only one will be notified of changes.")
        }
        // Observe the internal MutableLiveData
        super.observe(owner, Observer { t ->
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        })
    }

    @MainThread
    override fun setValue(t: T?) {
        pending.set(true)
        super.setValue(t)
    }
    /**
     * Used for cases where T is Void, to make calls cleaner.
     */
    @MainThread
    fun call() {
        value = null
    }
    companion object {
        private val TAG = "SingleLiveEvent"
    }
}