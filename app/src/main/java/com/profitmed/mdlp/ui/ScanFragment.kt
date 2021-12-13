package com.profitmed.mdlp.ui

import android.Manifest
import android.content.Context
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
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
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.content.DialogInterface
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.text.isDigitsOnly
import com.google.zxing.BarcodeFormat
import java.lang.Exception
import android.app.Activity





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

        const val FRAME_ASPECT_W_WIDE = 2f
        const val FRAME_ASPECT_W_CUBE = 1f
        const val FRAME_SIZE_WIDE = 0.85f
        const val FRAME_SIZE_CUBE = 0.50f
    }



    //----------------------------------------------------------------------------------------------
    //region INITIAL
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
            go(savedInstanceState != null)
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

    private fun go(isSavedInstanceState: Boolean = false) {
        init()
        initClickListeners()

        setModeScanDid()
        if (isSavedInstanceState)
            setDid(did)
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
        binding.fabScan.setOnClickListener {
            inputKizByCamera()
        }
        binding.inputDidByKeyboard.setOnClickListener{
            inputDidByKeyboard()
        }
        binding.inputDidByCamera.setOnClickListener {
            inputDidByCamera()
        }
    }

    private fun renderData(appState: AppState) {
        binding.loadingLayout.visibility = View.GONE
        when (appState) {
            is AppState.Success -> {
                successAction(appState.res.MSG)
                showCurrentScanMode()
                appStateToIdle()
            }
            is AppState.SuccessCheckDid -> {
                setDid(appState.did.toString())
            }
            is AppState.Loading -> {
                binding.loadingLayout.visibility = View.VISIBLE
                stopScanner()
            }
            is AppState.Error -> {
                errorAction(appState.error.message ?: getString(R.string.rest_api_error))
                showCurrentScanMode()
                appStateToIdle()
            }
            else -> {
                binding.loadingLayout.visibility = View.GONE
            }
        }
    }

    private fun appStateToIdle() {
        viewModel.getLiveData().value = AppState.Idle
    }

    //region Разрешения
    private fun checkPermissions(): Boolean {
        return checkPermissionCamera()
    }

    private fun checkPermissionCamera(): Boolean {
        return context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) } ==
                PackageManager.PERMISSION_GRANTED
    }

    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
        //showToast(getString(R.string.permission_grand_camera))
    }

    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
        showToast(getString(R.string.permission_denied_camera))
    }

    override fun onPermissionRationaleShouldBeShown(
        permission: PermissionRequest?,
        token: PermissionToken?
    ) {
        //TODO: Реализовать
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
    //endregion



    //----------------------------------------------------------------------------------------------
    // WORK

    //region Методы работы с камерой
    private fun startScanner() {
        if (checkPermissions()) {
            codeScanner.startPreview()
        } else {
            requestPermission()
        }
    }

    private fun stopScanner() {
        if (checkPermissions()) {
            if (this::codeScanner.isInitialized)
                codeScanner.stopPreview()
        } else {
            //TODO: ЗАПРОС ПРАВ???
        }
    }
    //endregion

    private fun scannerCallback(code: String) {
        if (isDidMode) {
            scanDid(code)
        }
        else {
            scanKiz(code)
        }
    }

    //region Режим работы
    private fun setModeScanDid() {
        setModeAsDid(true)
    }

    private fun setModeScanKiz() {
        setModeAsDid(false)
    }

    private fun setModeAsDid(isDidMode: Boolean) {
        if (!isDidMode && !viewModel.checkDidFormat(this.did)) {
            errorAction(getString(R.string.no_format_did))
            throw Exception(getString(R.string.no_format_did))
        }

        // Настройки границы сканирования
        changeScannerFrame(isDidMode)

        this.isDidMode = isDidMode
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

    private fun changeScannerFrame(isDidMode: Boolean) {
        binding.scannerView.frameAspectRatioWidth = if (isDidMode) FRAME_ASPECT_W_WIDE else FRAME_ASPECT_W_CUBE
        binding.scannerView.frameSize = if (isDidMode) FRAME_SIZE_WIDE else FRAME_SIZE_CUBE
    }
    //endregion

    //region КЛЮЧЕВЫЕ МЕТОДЫ
    //-----------------------------------------
    // Сканирование камерой или Ввод с клавиатуры
    private fun scanDid(did: String) {
        if (viewModel.checkDidFormat(did)) {
            viewModel.checkDid(did)
        }
        else {
            errorAction(getString(R.string.no_format_did))
        }
    }

    private fun setDid(did: String) {
        if (viewModel.checkDidFormat(did)) {
            this.did = did
            binding.inputDid.setText(this.did)
            setModeScanKiz()
        }
        else {
            errorAction(getString(R.string.no_format_did))
        }
    }

    //-----------------------------------------

    private fun scanKiz(kiz: String) {
        this.kiz = kiz
        viewModel.inputKiz(did, kiz)
    }
    //endregion

    //region Методы ввода данных
    private fun inputDidByKeyboard() {
        stopScanner()
        AlertDialog.Builder(requireContext()).apply {
            this.setTitle("Введите DID документа")

            val input = EditText(context).apply {
                this.inputType = InputType.TYPE_CLASS_TEXT
            }
            this.setView(input)

            this.setPositiveButton(getString(android.R.string.ok),
                DialogInterface.OnClickListener { dialog, which -> inputDidByKeyboardHandler(input.text.toString()) })
            this.setNegativeButton(getString(android.R.string.cancel),
                DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
        }.show()
    }

    private fun inputDidByKeyboardHandler(did: String) {
        scanDid(did)
    }

    private fun inputDidByCamera() {
        setModeScanDid()
        startScanner()
    }

    private fun inputKizByCamera() {
        try {
            setModeScanKiz()
            startScanner()
        }
        catch (ex: Exception) {
            ex.message?.let { showToast(it) }
        }
    }
    //endregion

    //region Реакция системы на ответ от RestApi
    private fun successAction(msg: String) {
        context?.showBottomSheet(msg, R.drawable.ic_ok_circle)
    }

    private fun errorAction(errMsg: String) {
        context?.showBottomSheet(errMsg, R.drawable.ic_cancel_circle)
    }

    private fun Context.showBottomSheet(msg: String, iconId: Int) {
        BottomSheetDialog(this).apply {
            this.setContentView(R.layout.bottom_sheet)
            (this.findViewById(R.id.modalTvRes) as TextView?)?.apply {
                this.visibility = if (msg.isEmpty()) View.GONE else View.VISIBLE
                this.text = msg
            }
            (this.findViewById(R.id.modalTvScannedCode) as TextView?)?.text = kiz
            (this.findViewById(R.id.modalImgRes) as ImageView?)?.setImageResource(iconId)
        }.show()
    }
    //endregion


    //----------------------------------------------------------------------------------------------
    // SYSTEM
//region SYSTEM

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
//endregion
}