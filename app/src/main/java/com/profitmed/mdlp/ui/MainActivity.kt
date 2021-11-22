package com.profitmed.mdlp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.zxing.Result
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.profitmed.mdlp.R
import com.profitmed.mdlp.databinding.ActivityMainBinding
import com.profitmed.mdlp.model.Repository
import com.profitmed.mdlp.model.ResponseIdResMsg
import com.profitmed.mdlp.model.RestApi
import com.profitmed.mdlp.presenter.AppState
import kotlinx.android.synthetic.main.activity_main.*
import me.dm7.barcodescanner.core.BarcodeScannerView
import me.dm7.barcodescanner.zxing.ZXingScannerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.jar.Manifest

class MainActivity : AppCompatActivity(), PermissionListener, ZXingScannerView.ResultHandler  {

    lateinit var scannerView:ZXingScannerView;
    private lateinit var binding: ActivityMainBinding;
    private val repository: Repository = Repository()
    // LiveData может подписывать кого либо на себя, говоря тем самым кому бы то нибыло об
    // изменениях внутри него. Конкретный экземпляр Модели для конкретного Fragment типв AppState
    private val liveDataToObserve: MutableLiveData<AppState> = MutableLiveData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        scannerView = binding.zxscan
        Dexter.withActivity(this)
            .withPermission(android.Manifest.permission.CAMERA)
            .withListener(this)
            .check()
    }

    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
        scannerView.setResultHandler(this)
        scannerView.startCamera()
    }

    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
        showToast("НЕТ РАЗРЕШЕНИЙ НА КАМЕРУ")
    }

    override fun onPermissionRationaleShouldBeShown(
        permission: PermissionRequest?,
        token: PermissionToken?
    ) {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        scannerView.stopCamera()
        super.onDestroy()
    }

    override fun handleResult(rawResult: Result?) {
        var kiz = rawResult.toString()
        txt_result.text = kiz
        putInputKiz(kiz)

        //scannerView.stopCamera()
        //scannerView.startCamera()
    }

    private fun putInputKiz(kiz: String) {
        Log.d("XXX", kiz)
        liveDataToObserve.value = AppState.Loading
        Thread {
            repository.putInputKiz(
                callBack,
                DEF_DID,
                kiz,
                DEF_LID800,
                DEF_EID,
                DEF_LID4000,
                DEF_VAR1,
                DEF_VAR2
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
                    AppState.Error(Exception("SERVER_ERROR"))
                }
            )
        }

        override fun onFailure(call: Call<ResponseIdResMsg>, t: Throwable) {
            AppState.Error(t)
            showToast(t.toString())
            Log.d("XXX", t.toString())
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        Log.d("XXX", msg)
    }

    companion object {
        const val DEF_DID = "0"
        const val DEF_LID800 = "0"
        const val DEF_EID = "0"
        const val DEF_LID4000 = "0"
        const val DEF_VAR1 = "0"
        const val DEF_VAR2 = "0"
    }
}