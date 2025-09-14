<template>
	<div class="p-inputgroup">
		<Dropdown
			v-model="selectedPlayer"
			:options="suggestionsPlayer"
			:auto-filter-focus="true"
			:filter-placeholder="t('ViewCompetition.searchPlayer')"
			:placeholder="t('ViewCompetition.selectPlayer')"
			:loading="loading"
			option-label="name"
			data-key="id"
			filter
			:disabled="loading"
			@filter="queryPlayer"
		>
			<template #empty>
				{{ t("ViewSignUp.atLeastOneLetter") }}
			</template>
			<template #emptyfilter>
				{{ t("ViewSignUp.noPlayerFound") }}
			</template>
		</Dropdown>
		<Button :disabled="mutSingleLoading" @click="signUpPlayer">
			{{ t("general.signUp") }}
		</Button>
	</div>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n"
import { DropdownFilterEvent } from "primevue/dropdown"
import { Player } from "@/interfaces/player"
import { ref } from "vue"
import { useSignUpSingle } from "@/backend/competition"
import { useToast } from "primevue/usetoast"
import { useRoute } from "vue-router"
import { useQueryClient } from "@tanstack/vue-query"
import { findCompPlayers } from "@/backend/player"

const { t } = useI18n()
const toast = useToast()
const route = useRoute()
const queryClient = useQueryClient()

const props = withDefaults(
	defineProps<{
		isPlayerB?: boolean
	}>(),
	{
		isPlayerB: false,
	},
)

const selectedPlayer = ref<Player | undefined>(undefined)
const { mutate: mutateSignUpSingle, isPending: mutSingleLoading } =
	useSignUpSingle(route, t, toast, queryClient)

const search = ref<string>("")
const { data: suggestionsPlayer, isFetching: loading } = findCompPlayers(
	search,
	route,
	props.isPlayerB,
	t,
	toast,
)

function queryPlayer(event: DropdownFilterEvent) {
	search.value = event.value
}

function signUpPlayer() {
	mutateSignUpSingle({ player: selectedPlayer, playerB: props.isPlayerB })
}
</script>

<style scoped></style>
