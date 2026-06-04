package tech.jour.template.common.model.db

import androidx.room.Entity

@Entity
data class SearchLocationBean(
	val address: String = "",
	val sematicDescription: String = "",
	val latitude: Double = 0.0,
	val longitude: Double = 0.0,
)