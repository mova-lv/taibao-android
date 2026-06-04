package tech.jour.template.module

import tech.jour.template.common.room.AppDatabase
import javax.inject.Inject

class DRepository @Inject constructor() {

	@Inject
	lateinit var database: AppDatabase

	/**
	 * 模拟获取数据
	 */
//	suspend fun getData() = database.accountDao().getUserById(0)
}