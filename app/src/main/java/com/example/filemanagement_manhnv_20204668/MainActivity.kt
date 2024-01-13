package com.example.filemanagement_manhnv_20204668

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.example.filemanagement_manhnv_20204668.databinding.ActivityMainBinding
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.View

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        if (Build.VERSION.SDK_INT < 30) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                Log.v("TAG", "Permission Denied")
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1234)
            } else
                Log.v("TAG", "Permission Granted")
        } else {
            if (!Environment.isExternalStorageManager()) {
                Log.v("TAG", "Permission Denied")
                val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        uri
                    )
                )
            } else {
                Log.v("TAG", "Permission Granted")
            }
        }

        binding.btnCopyMain.setOnClickListener{
            handleCopy()
        }
        binding.btnExitMain.setOnClickListener{
            handleExit()
        }

        val view = binding.root
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentSection, FragmentMain())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
        setContentView(view)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v("TAG", "Permission Granted")
        } else {
            Log.v("TAG", "Permission Denied")
        }
    }

    fun showCopyFileView(filePath: String){
        binding.llContainerCopyViewMain.visibility = View.VISIBLE
        binding.tvFilePathMain.text = filePath
    }
    private fun hideCopyFileView(){
        binding.llContainerCopyViewMain.visibility = View.GONE
    }
    private fun handleCopy(){
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        val filePath = sharedPref.getString(FragmentMain.FILE_PATH_COPY, "") ?: ""
        val topFragment = supportFragmentManager.fragments[supportFragmentManager.fragments.size - 1]
        if(topFragment is FragmentMain){
            topFragment.copyFile(filePath)
        }
        handleExit()
    }

    private fun handleExit(){
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putString(FragmentMain.FILE_PATH_COPY, "")
            apply()
        }
        hideCopyFileView()
    }
}