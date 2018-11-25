package com.stevenschoen.putionew;

import android.content.Context
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import com.stevenschoen.putionew.model.PutioRestInterface

abstract class PutioBaseLoader(context: Context) : Loader<Any>(context) {

  protected val api: PutioRestInterface
    get() = (context.applicationContext as PutioApplication).putioUtils!!.restInterface

  abstract class Callbacks(protected val context: Context) : LoaderManager.LoaderCallbacks<Any> {

    override fun onLoadFinished(loader: Loader<Any>, data: Any) {}

    override fun onLoaderReset(loader: Loader<Any>) {}
  }
}
