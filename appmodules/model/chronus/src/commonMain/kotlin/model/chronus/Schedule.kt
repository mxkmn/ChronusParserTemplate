package model.chronus

import kotlinx.serialization.Serializable

@Serializable
data class Schedule(
	val name: String,
	val type: ScheduleType,
	val place: Place,
	val url: String,
)
