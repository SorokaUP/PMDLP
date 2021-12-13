package com.profitmed.mdlp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.profitmed.mdlp.R
import com.profitmed.mdlp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null){
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ScanFragment.newInstance())
                .commitNow()
        }
    }
}