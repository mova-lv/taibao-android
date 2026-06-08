package com.taibao.app

import android.content.Context
import android.provider.Settings
import androidx.preference.PreferenceManager
import java.security.MessageDigest
import java.util.Date
import kotlin.random.Random

object DeviceAuthUtil {

    private const val SALT = "TaiTools@2024!Secure"
    private const val PASSWORD_LENGTH = 8
    private const val KEY_FALLBACK_ANDROID_ID = "KEY_FALLBACK_ANDROID_ID"

    /**
     * 获取当前设备的 AndroidID
     * 如果系统 AndroidID 返回空，则自动生成一个伪 AndroidID 并持久化到本地
     */
    fun getAndroidId(context: Context): String {
        val realId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        if (!realId.isNullOrEmpty()) return realId

        // 托底：系统 AndroidID 不可用时，生成一个并保存到本地
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val saved = prefs.getString(KEY_FALLBACK_ANDROID_ID, null)
        if (saved != null) return saved

        val fakeId = generateFakeAndroidId()
        prefs.edit().putString(KEY_FALLBACK_ANDROID_ID, fakeId).apply()
        return fakeId
    }

    /**
     * 生成一个符合 AndroidID 格式的 16 位十六进制字符串
     * AndroidID 是 64 位十六进制（16 个字符），如 "a1b2c3d4e5f6g7h8"
     */
    private fun generateFakeAndroidId(): String {
        val timeHex = java.lang.Long.toHexString(Date().time)
        val randomHex = java.lang.Integer.toHexString(Random.nextInt())
        val raw = timeHex + randomHex
        return if (raw.length >= 16) raw.substring(0, 16) else raw.padEnd(16, '0')
    }

    /**
     * 根据 AndroidID 生成 8 位设备密码
     * 算法：AndroidID + SALT → SHA-256 → 取其中 4 字节转 int → 取绝对值 → mod 100000000
     */
    fun generatePassword(context: Context): String {
        val androidId = getAndroidId(context)

        val input = androidId + SALT
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())

        // 取前 4 字节与后 4 字节异或，得到一个 int 值
        val value1 = (digest[0].toInt() and 0xFF) or
                ((digest[1].toInt() and 0xFF) shl 8) or
                ((digest[2].toInt() and 0xFF) shl 16) or
                ((digest[3].toInt() and 0xFF) shl 24)

        val value2 = (digest[4].toInt() and 0xFF) or
                ((digest[5].toInt() and 0xFF) shl 8) or
                ((digest[6].toInt() and 0xFF) shl 16) or
                ((digest[7].toInt() and 0xFF) shl 24)

        val result = (value1 xor value2).toLong() and 0x7FFFFFFF
        val password = result % 100_000_000L

        return String.format("%08d", password)
    }

    /**
     * 验证输入的密码是否正确
     */
    fun validatePassword(context: Context, input: String): Boolean {
        if (input.length != PASSWORD_LENGTH) return false
        val expected = generatePassword(context)
        return input == expected
    }
}