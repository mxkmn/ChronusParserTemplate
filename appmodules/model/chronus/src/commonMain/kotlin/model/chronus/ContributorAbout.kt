package model.chronus

data class ContributorAbout(
	val photoUrl: String,
	val en: ContributorAboutNameAndCards?,
	val ru: ContributorAboutNameAndCards?,
)

data class ContributorAboutNameAndCards(
	val name: String?,
	val cards: List<ContributorAboutCard>,
)

data class ContributorAboutCard(
	val title: String,
	val text: String,
	val buttons: List<ContributorAboutButton>,
)

data class ContributorAboutButton(
	val text: String,
	val url: String,
)
