package com.profitmed.mdlp.ui

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
import androidx.constraintlayout.widget.ConstraintLayout


class ScanFragment : Fragment(), PermissionListener, ZXingScannerView.ResultHandler {

    lateinit var scannerView: ZXingScannerView;
    private lateinit var binding: ScanFragmentBinding;
    private val repository: Repository = Repository()
    // LiveData может подписывать кого либо на себя, говоря тем самым кому бы то нибыло об
    // изменениях внутри него. Конкретный экземпляр Модели для конкретного Fragment типв AppState
    private val liveDataToObserve: MutableLiveData<AppState> = MutableLiveData()
    private val viewModel: ScanViewModel by lazy {
        ViewModelProvider(this).get(ScanViewModel::class.java)
    }

    var did: String = DEF_DID
    var isDidMode: Boolean = false

    companion object {
        fun newInstance() = ScanFragment()

        const val DEF_DID = "0"
        const val DEF_LID800 = "0"
        const val DEF_EID = "0"
        const val DEF_LID4000 = "0"
        const val DEF_VAR1 = "0"
        const val DEF_VAR2 = "0"
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

        // Сообщаем фрагменту, о модели данных, с которой он будет общаться
        // Сразу же подписываемся на обновления всех данных от этой модели данных
        viewModel.getLiveData().observe(viewLifecycleOwner, Observer<AppState> {
            // Действие, выполняемое по случаю обновления данных в поставщике
            renderData(it)
        })

        binding.inputDidLayout.setEndIconOnClickListener {
            changeModeDid()

            //startActivity(Intent(Intent.ACTION_VIEW).apply {
            //    data = Uri.parse("https://en.wikipedia.org/wiki/${binding.inputDid.text.toString()}")
            //})
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

        //showToast(msg)
        binding.txtResult.text = msg
        binding.inputDidLayout.helperText = msg
    }

    private fun putInputKiz(kiz: String) {
        binding.txtResult.text = kiz
        viewModel.putInputKiz(did, kiz)
    }

    private fun scanDid(did: String) {
        this.did = did
        binding.inputDid.setText(did)
        renderData(AppState.Next)
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
                //startScanner()
                binding.tvRes.text = getString(R.string.added_id) + appState.res.ID.toString()
                successAction()
                showCurrentScanMode()
            }
            is AppState.Loading -> {
                binding.loadingLayout.visibility = View.VISIBLE
                stopScanner()
            }
            is AppState.Error -> {
                binding.loadingLayout.visibility = View.GONE
                startScanner()
                binding.tvRes.text = appState.error.message
                errorAction()
                showCurrentScanMode()

                /*binding.root.showToast(
                    getString(R.string.error_msg),
                    getString(R.string.reload_msg),
                    { viewModel.xxx() }
                )*/
            }
            is AppState.Next -> {
                //startScanner()
            }
        }
    }

    private fun successAction() {
        binding.resultLayout.visibility = View.VISIBLE
        binding.imgRes.setImageResource(R.drawable.ic_ok_circle)
        binding.imgRes.visibility = View.VISIBLE
        binding.resultLayout.pmStartAnimation()
    }

    private fun errorAction() {
        binding.resultLayout.visibility = View.VISIBLE
        binding.imgRes.setImageResource(R.drawable.ic_cancel_circle)
        binding.imgRes.visibility = View.VISIBLE
        binding.resultLayout.pmStartAnimation()
    }

    private fun ConstraintLayout.pmStartAnimation() {
        val animOn = AlphaAnimation(0.0f, 1.0f).apply {
            this.duration = 500
            this.startOffset = 200
            this.fillAfter = true
        }
        this.startAnimation(animOn)

        val animOff = AlphaAnimation(1.0f, 0.0f).apply {
            this.duration = 500
            this.startOffset = 500
            this.fillAfter = true
        }
        this.startAnimation(animOff)
    }

    private fun startScanner() {
        scannerView.setResultHandler(this)
        scannerView.startCamera()
    }

    private fun stopScanner() {
        scannerView.stopCamera()
    }

    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
        //startScanner()
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
}