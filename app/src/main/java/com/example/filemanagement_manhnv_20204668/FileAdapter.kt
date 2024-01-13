package com.example.filemanagement_manhnv_20204668

import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File

class FileAdapter(private var files: List<File>, private val onItemClick: (File) -> Unit) :
    RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    var onEditFileName: ((File) -> Unit)? = null
    var onDeleteFile: ((File) -> Unit)? = null
    var onCopyFile: ((File) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun getItemCount(): Int = files.size

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(files[position])
    }

    fun updateData(newFiles: List<File>) {
        files = newFiles
        notifyDataSetChanged()
    }

    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnCreateContextMenuListener {
        private val name: TextView = itemView.findViewById(R.id.tvNameFile)
        private val image: ImageView = itemView.findViewById(R.id.ivImageFile)
        private var curFile: File? = null

        init {
            itemView.setOnCreateContextMenuListener(this)
        }

        fun bind(file: File) {
            curFile = file
            name.text = file.name
            if (file.isDirectory) {
                image.setImageResource(R.drawable.folder)
            } else {
                if (file.extension.lowercase() in listOf("bmp", "jpg", "png")) {
                    Glide.with(itemView)
                        .load(file)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .centerCrop()
                        .into(image)
                } else if (file.extension.lowercase() == "txt") {
                    image.setImageResource(R.drawable.text_file)
                } else {
                    image.setImageResource(R.drawable.file)
                }
            }
            itemView.setOnClickListener { onItemClick(file) }
        }

        override fun onCreateContextMenu(
            menu: ContextMenu,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            if (curFile?.isDirectory == true) {
                val editName = menu.add(Menu.NONE, 1, 1, "Đổi tên")
                val delete = menu.add(Menu.NONE, 2, 2, "Xóa")
                editName.setOnMenuItemClickListener(onEditMenu)
                delete.setOnMenuItemClickListener(onEditMenu)
            } else {
                val editName = menu.add(Menu.NONE, 1, 1, "Đổi tên")
                val delete = menu.add(Menu.NONE, 2, 2, "Xóa")
                val copy = menu.add(Menu.NONE, 3, 3, "Sao chép")
                editName.setOnMenuItemClickListener(onEditMenu)
                delete.setOnMenuItemClickListener(onEditMenu)
                copy.setOnMenuItemClickListener(onEditMenu)
            }
        }

        private val onEditMenu: MenuItem.OnMenuItemClickListener =
            MenuItem.OnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> {
                        onEditFileName?.invoke(curFile!!)
                    }
                    2 -> {
                        onDeleteFile?.invoke(curFile!!)
                    }
                    3 -> {
                        onCopyFile?.invoke(curFile!!)
                    }
                }
                true
            }
    }
}