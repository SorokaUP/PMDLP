package com.profitmed.mdlp.ui

import android.content.Context
import android.media.Image
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.zxing.Result
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.profitmed.mdlp.R
import com.profitmed.mdlp.databinding.ScanFragmentBinding
import com.profitmed.mdlp.model.Repository
import com.profitmed.mdlp.viewmodel.AppState
import com.profitmed.mdlp.viewmodel.ScanViewModel
import me.dm7.barcodescanner.zxing.ZXingScannerView
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.profitmed.mdlp.model.RestApi
import kotlinx.android.synthetic.main.activity_main.*


class ScanFragment : Fragment(), PermissionListener, ZXingScannerView.ResultHandler {

    private lateinit var scannerView: ZXingScannerView
    private lateinit var binding: ScanFragmentBinding
    // LiveData может подписывать кого либо на себя, говоря тем самым кому бы то нибыло об
    // изменениях внутри него. Конкретный экземпляр Модели для конкретного Fragment типа AppState
    private val liveDataToObserve: MutableLiveData<AppState> = MutableLiveData()
    private val viewModel: ScanViewModel by lazy {
        ViewModelProvider(this).get(ScanViewModel::class.java)
    }

    private var did: String = DEF_DID
    private var kiz: String = ""
    private var isDidMode: Boolean = false

    companion object {
        fun newInstance() = ScanFragment()

        const val DEF_DID = "0"
        const val DEF_LID800 = "0"
        const val DEF_EID = "0"
        const val DEF_LID4000 = "0"
        const val DEF_VAR1 = "0"
        const val DEF_VAR2 = "0"

        const val KEY_DID = "DID"
        const val KEY_KIZ = "KIZ"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ScanFragmentBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        init()

        if (savedInstanceState != null) {
            kiz = savedInstanceState.getString(KEY_KIZ, "")
            scanDid(savedInstanceState.getString(KEY_DID, ""))
        }

        // Сообщаем фрагменту, о модели данных, с которой он будет общаться
        // Сразу же подписываемся на обновления всех данных от этой модели данных
        viewModel.getLiveData().observe(viewLifecycleOwner, Observer<AppState> {
            // Действие, выполняемое по случаю обновления данных в поставщике
            renderData(it)
        })

        binding.inputDidLayout.setEndIconOnClickListener {
            changeModeDid()
        }

        binding.fabScan.setOnClickListener {
            startScanner()
        }

        changeModeDid()
    }

    override fun handleResult(rawResult: Result?) {
        val code = rawResult?.toString() ?: ""

        if (isDidMode) {
            scanDid(code)
        }
        else {
            putInputKiz(code)
        }
    }

    private fun changeModeDid() {
        isDidMode = !isDidMode
        showCurrentScanMode()
    }

    private fun showCurrentScanMode() {
        var msg = getString(R.string.scan_mode_on) + " "

        msg += if (isDidMode) {
            getString(R.string.did_scan_mode)
        } else {
            getString(R.string.kiz_scan_mode)
        }

        binding.inputDidLayout.helperText = msg
    }

    private fun putInputKiz(kiz: String) {
        this.kiz = kiz
        viewModel.putInputKiz(did, kiz)
    }

    private fun scanDid(did: String) {
        this.did = did
        binding.inputDid.setText(did)
        renderData(AppState.Idle)
        changeModeDid()
    }

    private fun init() {
        scannerView = binding.zxscan
        Dexter.withActivity(this.activity)
            .withPermission(android.Manifest.permission.CAMERA)
            .withListener(this)
            .check()
    }

    private fun renderData(appState: AppState) {
        // В зависимости от того, чем сейчас занят поставщик, выполняем какие-то действия, о том
        // чем он занят нам сообщается из appState который в свою очередь будет одним из вариаций
        // Success(...), Loading(), Error(...)
        binding.resultLayout.visibility = View.GONE
        when (appState) {
            is AppState.Success -> {
                binding.loadingLayout.visibility = View.GONE
                successAction(appState.res.ID)
                showCurrentScanMode()
                liveDataToObserve.value = AppState.Idle
            }
            is AppState.Loading -> {
                binding.loadingLayout.visibility = View.VISIBLE
                stopScanner()
            }
            is AppState.Error -> {
                binding.loadingLayout.visibility = View.GONE
                errorAction(appState.error.message ?: getString(R.string.rest_api_error))
                showCurrentScanMode()
                liveDataToObserve.value = AppState.Idle
            }
            is AppState.Idle -> {
                //TODO: Ожидание
            }
        }
    }

    private fun successAction(resId: Int) {
        context?.let {
            it.showBottomSheet(getString(R.string.added_id) + resId.toString(), R.drawable.ic_ok_circle)
        }
        //binding.resultLayout.visibility = View.VISIBLE
        //binding.imgRes.setImageResource(R.drawable.ic_ok_circle)
        //binding.imgRes.visibility = View.VISIBLE
        //binding.resultLayout.pmStartAnimation()
    }

    private fun errorAction(errMsg: String) {
        context?.let {
            it.showBottomSheet(errMsg, R.drawable.ic_cancel_circle)
        }
        //binding.resultLayout.visibility = View.VISIBLE
        //binding.imgRes.setImageResource(R.drawable.ic_cancel_circle)
        //binding.imgRes.visibility = View.VISIBLE
        //binding.resultLayout.pmStartAnimation()
    }

    private fun Context.showBottomSheet(msg: String, iconId: Int) {
        BottomSheetDialog(this).apply {
            this.setContentView(R.layout.bottom_sheet)
            (this.findViewById(R.id.modalTvRes) as TextView?)?.text = msg
            (this.findViewById(R.id.modalTvScannedCode) as TextView?)?.text = kiz
            (this.findViewById(R.id.modalImgRes) as ImageView?)?.setImageResource(iconId)

            /*this.setOnCancelListener {
                Toast.makeText(this.context, "Скрыт", Toast.LENGTH_SHORT).show()
                //startScanner()
            }*/
        }.show()
    }

    private fun startScanner() {
        scannerView.setResultHandler(this)
        scannerView.startCamera()
    }

    private fun stopScanner() {
        if (this::scannerView.isInitialized)
            scannerView.stopCamera()
    }

    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
        showToast("Разрешения на камеру выданы")
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
        stopScanner()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_DID, did)
        outState.putString(KEY_KIZ, kiz)
        super.onSaveInstanceState(outState)
    }
}