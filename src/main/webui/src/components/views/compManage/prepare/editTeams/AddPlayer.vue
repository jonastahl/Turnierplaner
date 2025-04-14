<template>
	<div v-if="tournament && competition" class="flex flex-column gap-2">
		<ViewConditions
			:begin-game-phase="tournament.game_phase.begin"
			:second="competition.mode === Mode.DOUBLE"
		/>
		<div class="p-inputgroup">
			<Dropdown
				v-model="selectedPlayer"
				:options="suggestionsPlayer"
				:loading="loading"
				:disabled="props.isUpdating"
				:auto-filter-focus="true"
				:filter-placeholder="t('ViewCompetition.searchPlayer')"
				:placeholder="t('ViewCompetition.selectPlayer')"
				option-label="name"
				data-key="id"
				filter
				@filter="queryPlayer"
			>
				<template #empty>
					{{ t("ViewSignUp.atLeastOneLetter") }}
				</template>
				<template #emptyfilter>
					{{ t("ViewSignUp.noPlayerFound") }}
				</template>
			</Dropdown>
			<Button :disabled="props.isUpdating" @click="addPlayer">
				{{ t("general.signUp") }}
			</Button>
		</div>
	</div>
</template>

<script setup lang="ts">
import ViewConditions from "@/components/views/competition/signup/ViewConditions.vue"
import { useI18n } from "vue-i18n"
import { DropdownFilterEvent } from "primevue/dropdown"
import { Player } from "@/interfaces/player"
import { Mode } from "@/interfaces/competition"
import { ref } from "vue"
import { useToast } from "primevue/usetoast"
import { findCompPlayers } from "@/backend/player"
import { getCompetitionDetails } from "@/backend/competition"
import { useRoute } from "vue-router"
import { getTournamentDetails } from "@/backend/tournament"

const { t } = useI18n()
const toast = useToast()

const props = defineProps<{
	isUpdating: boolean
}>()
const emit = defineEmits(["addPlayer"])

const route = useRoute()
const { data: tournament } = getTournamentDetails(route, t, toast)
const { data: competition } = getCompetitionDetails(route, t, toast)
const selectedPlayer = ref<Player | null>(null)
const search = ref<string>("")
const { data: suggestionsPlayer, isFetching: loading } = findCompPlayers(
	search,
	route,
	false,
	t,
	toast,
)

function queryPlayer(event: DropdownFilterEvent) {
	search.value = event.value
}

function addPlayer() {
	if (selectedPlayer.value) emit("addPlayer", selectedPlayer.value)
}
</script>

<style scoped></style>
