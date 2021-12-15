package com.profitmed.mdlp.viewmodel

import android.util.Log
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.profitmed.mdlp.model.*
import com.profitmed.mdlp.ui.ScanFragment
import com.profitmed.mdlp.ui.checkKIZ
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScanViewModel(
    // LiveData может подписывать кого либо на себя, говоря тем самым кому бы то нибыло об
    // изменениях внутри него. Конкретный экземпляр Модели для конкретного Fragment типв AppState
    private val liveDataToObserve: MutableLiveData<AppState> = MutableLiveData(),
    // Источник данных для приложения. Сам Репозиторий берет данные от туда, от куда ему нужно,
    // он лишь получает и хранит данные
    private val repository: RepositoryProfitMed = RepositoryProfitMed()
): ViewModel() {
    fun getLiveData() = liveDataToObserve

    //----------------------------------------------------------------------------------------------

    fun inputKiz(did: String, kiz: String) {
        Log.d("InputKiz", kiz)
        liveDataToObserve.value = AppState.Loading

        if (!kiz.checkKIZ(true) && !kiz.checkKIZ(false)) {
            liveDataToObserve.value = AppState.Error(Exception("Не отсканирован КИЗ (коробка или упаковка)"))
            return
        }

        Thread {
            repository.inputKiz(
                callbackInputKiz,
                did,
                kiz,
                ScanFragment.DEF_LID800,
                ScanFragment.DEF_EID,
                ScanFragment.DEF_LID4000,
                ScanFragment.DEF_VAR1,
                ScanFragment.DEF_VAR2
            )
        }.start()
    }
    private val callbackInputKiz = object :
        Callback<ResponseIdResMsg> {

        override fun onResponse(call: Call<ResponseIdResMsg>, response: Response<ResponseIdResMsg>) {
            response.setAppStateByRes()
        }

        override fun onFailure(call: Call<ResponseIdResMsg>, t: Throwable) {
            liveDataToObserve.postValue(AppState.Error(t))
        }
    }

    //----------------------------------------------------------------------------------------------

    fun scanDid(did: String) {
        Log.d("scanDid", did)
        liveDataToObserve.value = AppState.Loading

        if (!checkDidFormat(did)) {
            liveDataToObserve.value = AppState.Error(Exception("Не верный DID документа"))
            return
        }

        Thread {
            repository.checkDid(
                callbackCheckDid,
                did
            )
        }.start()
    }
    private val callbackCheckDid = object :
        Callback<ResponseCheckDid> {

        override fun onResponse(call: Call<ResponseCheckDid>, response: Response<ResponseCheckDid>) {
            val res = response.body()
            liveDataToObserve.postValue(
                if (response.isSuccessful && res != null) {
                    if (res.RES > 0) {
                        AppState.SuccessCheckDid(res.DID)
                    }
                    else {
                        AppState.Error(Exception(res.MSG))
                    }
                } else {
                    AppState.Error(Exception("rest_api_error"))
                }
            )
        }

        override fun onFailure(call: Call<ResponseCheckDid>, t: Throwable) {
            liveDataToObserve.postValue(AppState.Error(t))
        }
    }

    fun checkDidFormat(did: String): Boolean {
        return did.isNotEmpty() &&
                did.isDigitsOnly() &&
                did != "0" &&
                did.length <= 10 &&
                did <= Int.MAX_VALUE.toString()
    }

    //----------------------------------------------------------------------------------------------

    private fun Response<*>.setAppStateByRes(isShowBottomSheetSuccess: Boolean = true) {
        val res:IResMsg? = this.body() as IResMsg?
        liveDataToObserve.postValue(
            if (this.isSuccessful && res != null) {
                if (res.RES > 0) {
                    if (isShowBottomSheetSuccess)
                        AppState.Success(res.toResponseResMsg())
                    else
                        AppState.Idle
                }
                else {
                    AppState.Error(Exception(res.MSG))
                }
            } else {
                AppState.Error(Exception("rest_api_error"))
            }
        )
    }
}