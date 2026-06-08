package com.taibao.app.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

/**
 * 文件复制工具类，用于将外部文件 URI 复制到应用私有目录。
 *
 * 兼容 Android 10+ 的分区存储（Scoped Storage），通过 ContentResolver 读取 URI 内容流，
 * 不依赖直接文件路径访问。
 */
object FileUriUtils {

    /**
     * 将指定的 URI 内容复制到目标文件。
     *
     * @param context 上下文
     * @param uri     源文件 URI（支持 content://、file:// 等 scheme）
     * @param destFile 目标文件
     * @return 目标文件（复制成功），或 null（复制失败）
     */
    fun copyUriToFile(context: Context, uri: Uri, destFile: File): File? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            destFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 将 URI 内容复制到应用私有目录下的指定子目录中。
     *
     * @param context 上下文
     * @param uri     源文件 URI
     * @param subDir  应用私有目录下的子目录名（相对于 context.filesDir）
     * @param fileName 目标文件名
     * @return 复制后的文件，或 null
     */
    fun copyUriToPrivateDir(
        context: Context,
        uri: Uri,
        subDir: String,
        fileName: String
    ): File? {
        val dir = File(context.filesDir, subDir)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val destFile = File(dir, fileName)
        return copyUriToFile(context, uri, destFile)
    }
}