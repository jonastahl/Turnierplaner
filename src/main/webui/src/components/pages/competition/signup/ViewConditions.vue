<template>
	<div v-if="tournament && player" class="flex flex-row gap-2">
		<Tag
			v-if="player.sex !== Sex.ANY"
			class="w-min font-medium"
			rounded
			:value="t('general.' + player.sex.toLowerCase())"
		/>
		<Tag
			v-if="player.hasMinAge"
			v-tooltip="
				t('ViewFormCompetition.born_before') +
				' ' +
				(player.minAge !== null
					? player.minAge.toLocaleString(t('lang'), dateOptions)
					: 'Error')
			"
			class="w-min font-medium"
			:value="generateAboveTag()"
			rounded
		/>
		<Tag
			v-if="player.hasMaxAge"
			v-tooltip="
				t('ViewFormCompetition.born_after') +
				' ' +
				(player.maxAge !== null
					? player.maxAge.toLocaleString(t('lang'), dateOptions)
					: 'Error')
			"
			class="w-min font-medium"
			:value="generateUnderTag()"
			rounded
		/>
	</div>
</template>

<script lang="ts" setup>
import { useI18n } from "vue-i18n"
import { Sex } from "@/interfaces/competition"
import { getCompetitionDetails } from "@/backend/competition"
import { getTournamentDetails } from "@/backend/tournament"
import { useToast } from "primevue/usetoast"
import { useRoute } from "vue-router"
import { computed } from "vue"

const { t } = useI18n()
const toast = useToast()
const route = useRoute()

const { data: competition } = getCompetitionDetails(route, t, toast)
const { data: tournament } = getTournamentDetails(route, t, toast)

const player = computed(() =>
	props.second ? competition.value?.playerB : competition.value?.playerA,
)

const props = withDefaults(
	defineProps<{
		second?: boolean
	}>(),
	{
		second: false,
	},
)

function generateAboveTag() {
	if (!tournament.value || !player.value || !player.value.minAge) return ""

	const dif =
		tournament.value.game_phase.begin.getFullYear() -
		player.value.minAge.getFullYear()
	return `${t("Player.over")}${dif}`
}

function generateUnderTag() {
	if (!tournament.value || !player.value || !player.value.maxAge) return ""

	const dif =
		tournament.value.game_phase.begin.getFullYear() -
		player.value.maxAge.getFullYear()
	return `${t("Player.under")}${dif - 1}`
}

const dateOptions: Intl.DateTimeFormatOptions = {
	year: "numeric",
	month: "numeric",
	day: "numeric",
}
</script>

<style scoped></style>
