package com.stevenschoen.putionew.files

import android.app.Activity
import android.app.DownloadManager
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.os.AsyncTask
import androidx.annotation.WorkerThread
import com.stevenschoen.putionew.putioApp

class FileDownloadsMaintenanceService : JobService() {

  companion object {
    const val FILE_DOWNLOADS_MAINTENANCE_JOB_ID = 1
  }

  override fun onStartJob(job: JobParameters): Boolean {
    if (job.jobId != FILE_DOWNLOADS_MAINTENANCE_JOB_ID) return false

    val fileDownloads = putioApp.fileDownloadDatabase.fileDownloadsDao()
    AsyncTask.execute {
      fileDownloads.getAllByStatus(FileDownload.Status.Downloaded)
          .forEach { fileDownload ->
            if (!isFileDownloaded(this, fileDownload)) {
              fileDownloads.delete(fileDownload)
            }
          }
    }

    return true
  }

  override fun onStopJob(job: JobParameters?) = false
}

@WorkerThread
fun isFileDownloaded(context: Context, fileId: Long): Boolean {
  val fileDownload = putioApp(context).fileDownloadDatabase.fileDownloadsDao().getByFileIdSynchronous(fileId)
      ?: return false
  return isFileDownloaded(context, fileDownload)
}

@WorkerThread
fun isFileDownloaded(context: Context, fileDownload: FileDownload): Boolean {
  if (fileDownload.downloadId == null || fileDownload.uri == null) return false
  val status = queryDownloadStatus(context, fileDownload)
  return status == DownloadManager.STATUS_SUCCESSFUL
}

@WorkerThread
fun isFileDownloadedOrDownloading(context: Context, fileDownload: FileDownload): Boolean {
  if (fileDownload.downloadId == null) return false
  val status = queryDownloadStatus(context, fileDownload)
  return when (status) {
    DownloadManager.STATUS_PENDING,
    DownloadManager.STATUS_RUNNING,
    DownloadManager.STATUS_SUCCESSFUL -> true
    else -> false
  }
}

private fun queryDownloadStatus(context: Context, fileDownload: FileDownload): Int {
  val downloadManager = context.getSystemService(Activity.DOWNLOAD_SERVICE) as DownloadManager
  downloadManager.query(
      DownloadManager.Query()
          .setFilterById(fileDownload.downloadId!!)
  ).use { query ->
    if (query.moveToFirst()) {
      return query.getInt(query.getColumnIndex(DownloadManager.COLUMN_STATUS))
    } else {
      return DownloadManager.STATUS_FAILED
    }
  }
}

@WorkerThread
fun markFileNotDownloaded(context: Context, fileId: Long) {
  putioApp(context).fileDownloadDatabase.fileDownloadsDao().getByFileIdSynchronous(fileId)?.let {
    markFileNotDownloaded(context, it)
  }
}

@WorkerThread
fun markFileNotDownloaded(context: Context, fileDownload: FileDownload) {
  putioApp(context).fileDownloadDatabase.fileDownloadsDao().update(fileDownload.apply {
    status = FileDownload.Status.NotDownloaded
    uri = null
    downloadId = null
  })
}

@WorkerThread
fun markFileDownloaded(context: Context, fileId: Long) {
  putioApp(context).fileDownloadDatabase.fileDownloadsDao().getByFileIdSynchronous(fileId)?.let {
    markFileDownloaded(context, it)
  }
}

@WorkerThread
fun markFileDownloaded(context: Context, fileDownload: FileDownload) {
  putioApp(context).fileDownloadDatabase.fileDownloadsDao().update(fileDownload.apply {
    status = FileDownload.Status.Downloaded
  })
}
