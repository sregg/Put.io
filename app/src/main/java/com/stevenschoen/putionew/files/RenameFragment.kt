package com.stevenschoen.putionew.files

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.stevenschoen.putionew.R
import com.stevenschoen.putionew.model.files.PutioFile
import com.trello.rxlifecycle3.components.support.RxAppCompatDialogFragment

class RenameFragment : RxAppCompatDialogFragment() {

  companion object {
    const val EXTRA_FILE = "file"

    fun newInstance(context: Context, file: PutioFile): RenameFragment {
      val args = Bundle()
      args.putParcelable(EXTRA_FILE, file)
      return Fragment.instantiate(context, RenameFragment::class.java.name, args) as RenameFragment
    }
  }

  val file by lazy { arguments!!.getParcelable<PutioFile>(EXTRA_FILE)!! }

  var callbacks: Callbacks? = null

  lateinit var nameView: EditText

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val dialog = MaterialAlertDialogBuilder(context!!)
        .setTitle(R.string.renametitle)
        .setView(R.layout.rename_dialog)
        .setPositiveButton(R.string.rename) { _, _ ->
          callbacks?.onRenamed(nameView.text.toString())
        }
        .setNegativeButton(R.string.cancel, null)
        .show()

    nameView = dialog.findViewById(R.id.rename_name)!!
    if (savedInstanceState == null) {
      nameView.setText(file.name)
    }

    val undoView = dialog.findViewById<View>(R.id.rename_undo)!!
    undoView.setOnClickListener {
      nameView.setText(file.name)
    }

    return dialog
  }

  interface Callbacks {
    fun onRenamed(newName: String)
  }
}
