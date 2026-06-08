package com.taibao.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.blankj.utilcode.util.ImageUtils
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
                // 一次性读取全部字节，避免流被 EXIF 解析消耗后无法再次读取
                val bytes = inputStream.readBytes()

                // 获取 EXIF 方向
                val rotate = bytes.inputStream().use { byteStream ->
                    val exif = ExifInterface(byteStream)
                    val orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270
                        else -> 0
                    }
                }

                FileOutputStream(destFile).use { outputStream ->
                    if (rotate != 0) {
                        // 需要旋转：从字节数组解码 Bitmap，旋转后压缩输出
                        val srcBitmap = ImageUtils.getBitmap(bytes, 0)
                        val rotatedBitmap = ImageUtils.rotate(srcBitmap, rotate, 0f, 0f)
                        rotatedBitmap.compress(
                            Bitmap.CompressFormat.JPEG, 95, outputStream
                        )
                        // 回收临时 Bitmap
                        if (srcBitmap != rotatedBitmap) {
                            srcBitmap.recycle()
                        }
                        rotatedBitmap.recycle()
                    } else {
                        // 无需旋转：直接写入原始字节
                        outputStream.write(bytes)
                    }
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