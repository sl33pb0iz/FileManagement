package com.example.filemanagement_manhnv_20204668

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.filemanagement_manhnv_20204668.databinding.FragmentContentFileBinding
import java.io.File

class FragmentContentFile : Fragment() {
    private lateinit var binding: FragmentContentFileBinding
    private var filePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        filePath = arguments?.getString(FragmentMain.FILE_PATH)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContentFileBinding.inflate(inflater)
        val file = File(filePath ?: "")
        if (file.extension.lowercase() in listOf("bmp", "jpg", "png")) {
            showImage(file)
        } else if (file.extension.lowercase() == "txt") {
            showTxtFile(file)
        }
        return binding.root
    }
    private fun showImage(file: File){
        binding.tvContentFile.visibility = View.GONE
        binding.ivContentFile.visibility = View.VISIBLE
        Glide.with(requireContext())
            .load(file)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .centerCrop()
            .into(binding.ivContentFile)
    }
    private fun showTxtFile(file: File){
        binding.tvContentFile.visibility = View.VISIBLE
        binding.ivContentFile.visibility = View.GONE
        val content = file.readText()
        binding.tvContentFile.text = content
    }
}