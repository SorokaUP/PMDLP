package com.profitmed.mdlp.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.profitmed.mdlp.R
import com.profitmed.mdlp.model.Repository
import com.profitmed.mdlp.model.ResponseIdResMsg
import com.profitmed.mdlp.ui.ScanFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScanViewModel(
    // LiveData может подписывать кого либо на себя, говоря тем самым кому бы то нибыло об
    // изменениях внутри него. Конкретный экземпляр Модели для конкретного Fragment типв AppState
    private val liveDataToObserve: MutableLiveData<AppState> = MutableLiveData(),
    // Источник данных для приложения. Сам Репозиторий берет данные от туда, от куда ему нужно,
    // он лишь получает и хранит данные
    private val repository: Repository = Repository()
): ViewModel() {
    fun getLiveData() = liveDataToObserve
    fun putInputKiz(kiz: String) {
        Log.d("InputKiz", kiz)
        liveDataToObserve.value = AppState.Loading
        Thread {
            repository.putInputKiz(
                callBack,
                ScanFragment.DEF_DID,
                kiz,
                ScanFragment.DEF_LID800,
                ScanFragment.DEF_EID,
                ScanFragment.DEF_LID4000,
                ScanFragment.DEF_VAR1,
                ScanFragment.DEF_VAR2
            )
        }.start()
    }

    private val callBack = object :
        Callback<ResponseIdResMsg> {

        override fun onResponse(call: Call<ResponseIdResMsg>, response: Response<ResponseIdResMsg>) {
            val res: ResponseIdResMsg? = response.body()
            liveDataToObserve.postValue(
                if (response.isSuccessful && res != null) {
                    AppState.Success(res)
                } else {
                    AppState.Error(Exception("rest_api_error"))
                }
            )
        }

        override fun onFailure(call: Call<ResponseIdResMsg>, t: Throwable) {
            AppState.Error(t)
        }
    }
}