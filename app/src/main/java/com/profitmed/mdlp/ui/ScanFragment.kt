package com.profitmed.mdlp.ui

import android.Manifest
import android.content.Context
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.profitmed.mdlp.R
import com.profitmed.mdlp.databinding.ScanFragmentBinding
import com.profitmed.mdlp.viewmodel.AppState
import com.profitmed.mdlp.viewmodel.ScanViewModel
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.google.android.material.bottomsheet.BottomSheetDialog

class ScanFragment : Fragment(), PermissionListener {

    private lateinit var codeScanner: CodeScanner
    private lateinit var binding: ScanFragmentBinding
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

        const val PERMISSION_REQUEST_CODE  = 1
    }



    //----------------------------------------------------------------------------------------------
    // INITIAL

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (!checkPermissions()) {
            requestPermission()
        }

        binding = ScanFragmentBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            kiz = savedInstanceState.getString(KEY_KIZ, "")
            did = savedInstanceState.getString(KEY_DID, "")
        }

        if (checkPermissions()) {
            go()
        }
        else {
            requestPermission()
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(
                Manifest.permission.CAMERA
            ),
            PERMISSION_REQUEST_CODE
        )
    }

    private fun go() {
        init()
        initClickListeners()

        scanDid(did)
        changeModeDid()
    }

    private fun init() {
        val scannerView = binding.scannerView
        val activity = requireActivity()
        codeScanner = CodeScanner(activity, scannerView)
        codeScanner.decodeCallback = DecodeCallback {
            activity.runOnUiThread {
                scannerCallback(it.text)
            }
        }

        Dexter.withActivity(this.activity)
            .withPermission(android.Manifest.permission.CAMERA)
            .withListener(this)
            .check()

        // Сообщаем фрагменту, о модели данных, с которой он будет общаться
        // Сразу же подписываемся на обновления всех данных от этой модели данных
        viewModel.getLiveData().observe(viewLifecycleOwner, Observer<AppState> {
            // Действие, выполняемое по случаю обновления данных в поставщике
            renderData(it)
        })
    }

    private fun initClickListeners() {
        binding.inputDidLayout.setEndIconOnClickListener {
            changeModeDid()
        }
        binding.fabScan.setOnClickListener {
            startScanner()
        }
    }

    private fun renderData(appState: AppState) {
        binding.resultLayout.visibility = View.GONE
        when (appState) {
            is AppState.Success -> {
                binding.loadingLayout.visibility = View.GONE
                successAction(appState.res.ID)
                showCurrentScanMode()
                viewModel.getLiveData().value = AppState.Idle
            }
            is AppState.Loading -> {
                binding.loadingLayout.visibility = View.VISIBLE
                stopScanner()
            }
            is AppState.Error -> {
                binding.loadingLayout.visibility = View.GONE
                errorAction(appState.error.message ?: getString(R.string.rest_api_error))
                showCurrentScanMode()
                viewModel.getLiveData().value = AppState.Idle
            }
            else -> {
                // Простой
            }
        }
    }

    //region Разрешения
    private fun checkPermissions(): Boolean {
        return checkPermissionCamera()
    }

    private fun checkPermissionCamera(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
        showToast(getString(R.string.permission_grand_camera))
    }

    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
        showToast(getString(R.string.permission_denied_camera))
    }

    override fun onPermissionRationaleShouldBeShown(
        permission: PermissionRequest?,
        token: PermissionToken?
    ) {
        showToast("onPermissionRationaleShouldBeShown")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        var isAllGranted = true
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // На случай, если будет запрос более одного разрешения
            for (g in grantResults) {
                if (g != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false
                }
            }
            if (isAllGranted) {
                go()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    //endregion



    //----------------------------------------------------------------------------------------------
    // WORK

    //region Работа с камерой
    private fun startScanner() {
        codeScanner.startPreview()
    }

    private fun stopScanner() {
        if (this::codeScanner.isInitialized)
            codeScanner.stopPreview()
    }
    //endregion

    private fun scannerCallback(code: String) {
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
        changeModeDid()
    }

    //region Реакция системы на ответ от RestApi
    private fun successAction(resId: Int) {
        context?.showBottomSheet(getString(R.string.added_id) + resId.toString(), R.drawable.ic_ok_circle)
    }

    private fun errorAction(errMsg: String) {
        context?.showBottomSheet(errMsg, R.drawable.ic_cancel_circle)
    }

    private fun Context.showBottomSheet(msg: String, iconId: Int) {
        BottomSheetDialog(this).apply {
            this.setContentView(R.layout.bottom_sheet)
            (this.findViewById(R.id.modalTvRes) as TextView?)?.text = msg
            (this.findViewById(R.id.modalTvScannedCode) as TextView?)?.text = kiz
            (this.findViewById(R.id.modalImgRes) as ImageView?)?.setImageResource(iconId)
        }.show()
    }
    //endregion



    //----------------------------------------------------------------------------------------------
    // SYSTEM

    override fun onResume() {
        super.onResume()
        if (checkPermissionCamera())
            codeScanner.startPreview()
    }

    override fun onPause() {
        if (checkPermissionCamera())
            codeScanner.releaseResources()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_DID, did)
        outState.putString(KEY_KIZ, kiz)
        super.onSaveInstanceState(outState)
    }
}