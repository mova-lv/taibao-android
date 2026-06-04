package tech.jour.template.common.model.db

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class LocalLocationBean(
	@PrimaryKey
	val timestamp: Long = System.currentTimeMillis(),
	val address: String = "",
	val sematicDescription: String = "",
	val latitude: Double = 0.0,
	val longitude: Double = 0.0,
) {
	@Ignore
	constructor() : this(0)
}