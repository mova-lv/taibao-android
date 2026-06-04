package com.zcshou.gogogo

import tech.jour.template.common.model.db.LocalLocationBean
import tech.jour.template.common.room.AppDatabase
import tech.jour.template.common.room.dao.LocationDao
import javax.inject.Inject

class MapRepository @Inject constructor() {

	@Inject
	lateinit var database: AppDatabase

	@Inject
	lateinit var locationDao: LocationDao

	fun getAll() = locationDao.getAll()

	fun delete(bean: LocalLocationBean) = locationDao.delete(bean)

	fun insert(bean: LocalLocationBean) = locationDao.insert(bean)
}