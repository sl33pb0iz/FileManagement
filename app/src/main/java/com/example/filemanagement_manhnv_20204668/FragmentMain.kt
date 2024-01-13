package com.example.filemanagement_manhnv_20204668

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.filemanagement_manhnv_20204668.R.*
import com.example.filemanagement_manhnv_20204668.databinding.FragmentMainBinding
import java.io.*

class FragmentMain : Fragment() {

    companion object {
        const val FILE_PATH = "FILE_PATH"
        const val FILE_PATH_COPY = "FILE_PATH_COPY"
    }

    private lateinit var binding: FragmentMainBinding
    private lateinit var currentDirectory: File
    private lateinit var fileAdapter: FileAdapter
    private var path: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        path = arguments?.getString("path")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater)
        fileAdapter = FileAdapter(emptyList()) { file -> onFileItemClick(file) }
        fileAdapter.onEditFileName = {
            showRenameDialog(it)
        }
        fileAdapter.onDeleteFile = {
            showDeleteDialog(it)
        }
        fileAdapter.onCopyFile = {
            handleCopy(it)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = fileAdapter
        }
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                LinearLayoutManager.VERTICAL
            )
        )

        val secStore = path ?: Environment.getExternalStorageDirectory().path
        currentDirectory = File(secStore)
        loadFiles(currentDirectory)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.option_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.createDictionary -> {
                showCreateFolderDialog()
                true
            }
            R.id.createTxtFile -> {
                showCreateFileDialog()
                true
            }
            else -> false
        }
        return true
    }

    private fun loadFiles(directory: File) {
        currentDirectory = directory
        val files = directory.listFiles()?.toList() ?: emptyList()
        fileAdapter.updateData(files)
    }

    private fun onFileItemClick(file: File) {
        if (file.isDirectory) {
            Log.d("CHECKKKKK", file.path)
            val fragmentManager = activity?.supportFragmentManager
            val fragmentTransaction = fragmentManager?.beginTransaction()
            val bundle = Bundle()
            bundle.putString("path", file.path)
            val newFragment = FragmentMain()
            newFragment.arguments = bundle
            fragmentTransaction?.replace(R.id.fragmentSection, newFragment)
            fragmentTransaction?.addToBackStack(null)
            fragmentTransaction?.commit()
        } else {
            showContentFile(file)
        }
    }

    private fun showContentFile(file: File) {
        if (file.extension.lowercase() in listOf(
                "bmp",
                "jpg",
                "png"
            ) || (file.extension.lowercase() == "txt")
        ) {
            val fragmentManager = activity?.supportFragmentManager
            val fragmentTransaction = fragmentManager?.beginTransaction()
            val bundle = Bundle()
            bundle.putString(FILE_PATH, file.path)
            val newFragment = FragmentContentFile()
            newFragment.arguments = bundle
            fragmentTransaction?.replace(R.id.fragmentSection, newFragment)
            fragmentTransaction?.addToBackStack(file.path.toString())
            fragmentTransaction?.commit()
        } else {
            showToast("Dạng file không được hỗ trợ")
        }
    }

    private fun showRenameDialog(file: File) {
        val input = EditText(requireContext())
        input.setText(file.name)
        AlertDialog.Builder(requireContext())
            .setTitle("Đổi tên")
            .setView(input)
            .setPositiveButton("Đổi tên") { _, _ ->
                val newName = input.text.toString()
                val newFile = File(file.parentFile, newName)
                if (file.renameTo(newFile)) {
                    loadFiles(currentDirectory)
                } else {
                    showToast("Đổi tên không thành công")
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showDeleteDialog(file: File) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xóa")
            .setMessage("Bạn có chắc muốn xóa file ${file.name}?")
            .setPositiveButton("Delete") { _, _ ->
                if (file.exists()) {
                    if (file.delete()) {
                        Toast.makeText(
                            requireContext(),
                            "Xóa file thành công",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadFiles(currentDirectory)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Xóa file không thành công",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                } else {
                    Toast.makeText(requireContext(), "File không tồn tại", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCreateFolderDialog() {
        val input = EditText(requireContext())
        AlertDialog.Builder(requireContext())
            .setTitle("Tạo thư mục")
            .setView(input)
            .setPositiveButton("Tạo") { _, _ ->
                val folderName = input.text.toString()
                val newFolder = File(currentDirectory, folderName)
                if (newFolder.mkdir()) {
                    loadFiles(currentDirectory)
                } else {
                    showToast("Tạo thất bại")
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showCreateFileDialog() {
        val dialogView = layoutInflater.inflate(layout.dialog_create_file, null)
        val fileNameInput = dialogView.findViewById<EditText>(R.id.edtFileName)
        val fileContentInput = dialogView.findViewById<EditText>(R.id.edtFileContent)

        AlertDialog.Builder(requireContext())
            .setTitle("Tạo File Text")
            .setView(dialogView)
            .setPositiveButton("Tạo") { _, _ ->
                val fileName = fileNameInput.text.toString()
                val fileContent = fileContentInput.text.toString()

                if (fileName.isNotEmpty()) {
                    val newFile = File(currentDirectory, "$fileName.txt")

                    try {
                        if (newFile.createNewFile()) {
                            // File mới được tạo thành công
                            writeToFile(newFile, fileContent)

                            showToast("Tạo file thành công")
                            loadFiles(currentDirectory)
                        } else {
                            // File đã tồn tại hoặc có lỗi khác
                            showToast("Tạo file thất bại")
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        showToast("Lỗi khi tạo file")
                    }
                } else {
                    showToast("Tên file không được trống")
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun writeToFile(file: File, content: String) {
        try {
            val outputStream = FileOutputStream(file)
            val writer = BufferedWriter(OutputStreamWriter(outputStream))
            writer.write(content)
            writer.close()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            showToast("Lỗi khi ghi dữ liệu vào file")
        }
    }

    fun copyFile(filePath: String) {
        try {
            val sourceFile = File(filePath)
            val sourceInputStream = FileInputStream(sourceFile)
            val destOutputStream = FileOutputStream(File(currentDirectory, sourceFile.name))

            sourceInputStream.use { input ->
                destOutputStream.use { output ->
                    val buffer = ByteArray(4 * 1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }
            loadFiles(currentDirectory)
            Toast.makeText(requireContext(), "Copy thành công", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(requireContext(), "Copy file không thành công", Toast.LENGTH_SHORT)
                .show()
            e.printStackTrace()
        }
    }

    private fun handleCopy(file: File) {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString(FILE_PATH_COPY, file.path)
            apply()
        }
        val mActivity: MainActivity = activity as MainActivity
        mActivity.showCopyFileView(file.name)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}