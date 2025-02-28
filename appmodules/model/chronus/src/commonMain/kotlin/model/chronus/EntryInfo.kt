package model.chronus

data class EntryInfo(
	val name: String,
	val url: String?, // если ссылка на группу/преподавателя/аудиторию в виде url не предоставляется, нужно оставить null
)
