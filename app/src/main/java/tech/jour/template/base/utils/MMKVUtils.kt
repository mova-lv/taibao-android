package tech.jour.template.base.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.tencent.mmkv.MMKV

/**
 * MMKV使用封装
 *
 * @author Qu Yunshuo
 * @since 8/28/20
 */
object MMKVUtils {

	/**
	 * 初始化
	 */
	fun initMMKV(context: Context): String? = MMKV.initialize(context)

	/**
	 * 保存数据（简化）
	 * 根据value类型自动匹配需要执行的方法
	 */
	fun put(key: String, value: Any) =
		when (value) {
			is Int -> putInt(key, value)
			is Long -> putLong(key, value)
			is Float -> putFloat(key, value)
			is Double -> putDouble(key, value)
			is String -> putString(key, value)
			is Boolean -> putBoolean(key, value)
			else -> false
		}

	fun putString(key: String, value: String): Boolean? = MMKV.defaultMMKV()?.encode(key, value)

	fun getString(key: String, defValue: String): String? =
		MMKV.defaultMMKV()?.decodeString(key, defValue)

	fun putInt(key: String, value: Int): Boolean? = MMKV.defaultMMKV()?.encode(key, value)

	fun getInt(key: String, defValue: Int): Int? = MMKV.defaultMMKV()?.decodeInt(key, defValue)

	fun putLong(key: String, value: Long): Boolean? = MMKV.defaultMMKV()?.encode(key, value)

	fun getLong(key: String, defValue: Long): Long? = MMKV.defaultMMKV()?.decodeLong(key, defValue)

	fun putDouble(key: String, value: Double): Boolean? = MMKV.defaultMMKV()?.encode(key, value)

	fun getDouble(key: String, defValue: Double): Double? =
		MMKV.defaultMMKV()?.decodeDouble(key, defValue)

	fun putFloat(key: String, value: Float): Boolean? = MMKV.defaultMMKV()?.encode(key, value)

	fun getFloat(key: String, defValue: Float): Float? =
		MMKV.defaultMMKV()?.decodeFloat(key, defValue)

	fun putBoolean(key: String, value: Boolean): Boolean? = MMKV.defaultMMKV()?.encode(key, value)

	fun getBoolean(key: String, defValue: Boolean): Boolean? =
		MMKV.defaultMMKV()?.decodeBool(key, defValue)

	fun contains(key: String): Boolean? = MMKV.defaultMMKV()?.contains(key)


	/**
	 * mmkv 存储对象
	 */
	fun putObj(key: String, obj: Any) {
		MMKV.defaultMMKV().encode(key, Gson().toJson(obj))
	}

	/**
	 * mmkv 取出对象
	 */
	inline fun <reified T> get(key: String): T? {
		val json = MMKV.defaultMMKV().decodeString(key)
		return Gson().fromJson(json, T::class.java)
	}

	/**
	 * mmkv 取出List对象
	 */
	inline fun <reified T> getList(key: String): List<T> {
		val json = MMKV.defaultMMKV().decodeString(key)
		return getObjectList(json, T::class.java)
	}


	fun <T> getObjectList(jsonString: String?, cls: Class<T>?): List<T> {
		val list: MutableList<T> = ArrayList()
		try {
			val gson = Gson()
			val arry = JsonParser.parseString(jsonString).asJsonArray
			for (jsonElement in arry) {
				list.add(gson.fromJson(jsonElement, cls))
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}
		return list
	}

}