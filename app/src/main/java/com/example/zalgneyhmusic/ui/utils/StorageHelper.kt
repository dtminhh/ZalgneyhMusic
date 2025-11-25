package com.example.zalgneyhmusic.ui.utils

import android.content.Context
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Utility object for handling application storage and cache operations.
 */
object StorageHelper {

    /**
     * Calculates the total size of the application cache.
     * Includes both internal and external cache directories.
     *
     * @param context Context to access cache directories.
     * @return Total size in bytes.
     */
    fun getCacheSize(context: Context): Long {
        var size: Long = 0
        size += getFolderSize(context.cacheDir)
        context.externalCacheDir?.let { size += getFolderSize(it) }
        return size
    }

    /**
     * Recursively calculates the size of a file or directory.
     */
    private fun getFolderSize(file: File?): Long {
        if (file == null || !file.exists()) return 0
        var size: Long = 0
        if (file.isDirectory) {
            file.listFiles()?.forEach { size += getFolderSize(it) }
        } else {
            size = file.length()
        }
        return size
    }

    /**
     * Clears the application cache asynchronously on the IO thread.
     * specifically targets Glide disk cache and system cache directories.
     *
     * @param context Context to access cache directories.
     */
    suspend fun clearAppCache(context: Context) {
        withContext(Dispatchers.IO) {
            // 1. Clear Glide Disk Cache (Often the largest consumer)
            Glide.get(context).clearDiskCache()

            // 2. Clear System Cache
            deleteDir(context.cacheDir)
            context.externalCacheDir?.let { deleteDir(it) }
        }
    }

    /**
     * Recursively deletes a directory and its contents.
     * @return true if successful, false otherwise.
     */
    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            children?.forEach {
                val success = deleteDir(File(dir, it))
                if (!success) return false
            }
            return dir.delete()
        } else if (dir != null && dir.isFile) {
            return dir.delete()
        }
        return false
    }

    /**
     * Formats a size in bytes into a human-readable string (e.g., "12.5 MB").
     */
    fun formatSize(context: Context, size: Long): String {
        return android.text.format.Formatter.formatFileSize(context, size)
    }
}