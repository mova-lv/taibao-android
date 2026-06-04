package tech.jour.template.common.room.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import tech.jour.template.common.model.db.LocalLocationBean

@Dao
interface LocationDao:BaseDao<LocalLocationBean> {
//    @Query("SELECT * FROM LocalLocationBean WHERE id = :id LIMIT 1")
//    fun getUserById(id: Int): LocalLocationBean?

    @Query("SELECT * FROM LocalLocationBean")
    fun getAll():Flow<List<LocalLocationBean>>
}