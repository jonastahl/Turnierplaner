<template>
	<div class="flex flex-column">
		<div class="grid">
			<div class="col-6">
				<Dropdown
					v-model="selectedPlayerA"
					class="w-full"
					:options="suggestionsPlayerA"
					:auto-filter-focus="true"
					:filter-placeholder="t('player.action.search.label')"
					:placeholder="t('player.select')"
					:loading="loadingA"
					option-label="name"
					data-key="id"
					filter
					:disabled="mutDoubleLoading"
					@filter="(ev) => queryPlayer(true, ev)"
				>
					<template #empty>
						{{ t("validation.string.at_least_one") }}
					</template>
					<template #emptyfilter>
						{{ t("player.action.search.no_result") }}
					</template>
				</Dropdown>
			</div>
			<div clasS="col-6">
				<Dropdown
					v-model="selectedPlayerB"
					class="w-full"
					:options="suggestionsPlayerB"
					:auto-filter-focus="true"
					:filter-placeholder="t('player.action.search.label')"
					:placeholder="t('player.select')"
					:loading="loadingB"
					option-label="name"
					data-key="id"
					filter
					:disabled="mutDoubleLoading"
					@filter="(ev) => queryPlayer(false, ev)"
				>
					<template #empty>
						{{ t("validation.string.at_least_one") }}
					</template>
					<template #emptyfilter>
						{{ t("player.action.search.no_result") }}
					</template>
				</Dropdown>
			</div>
		</div>
		<Button
			class="w-full"
			:disabled="mutDoubleLoading"
			@click="signUpDoubleTog"
		>
			{{ t("competition.action.signup.action") }}
		</Button>
	</div>
</template>

<script setup lang="ts">
import { useRoute } from "vue-router"
import { useI18n } from "vue-i18n"
import { useToast } from "primevue/usetoast"
import { useSignUpDoubleTog } from "@/backend/competition"
import { useQueryClient } from "@tanstack/vue-query"
import { ref } from "vue"
import { Player } from "@/interfaces/player"
import { findCompPlayers } from "@/backend/player"
import { DropdownFilterEvent } from "primevue/dropdown"
import { getSignedUpSepByComp } from "@/backend/signup"

const { t } = useI18n()
const toast = useToast()

const route = useRoute()
const queryClient = useQueryClient()

const selectedPlayerA = ref<Player | undefined>(undefined)
const searchA = ref<string>("")
const { data: suggestionsPlayerA, isFetching: loadingA } = findCompPlayers(
	searchA,
	route,
	false,
	t,
	toast,
)
const selectedPlayerB = ref<Player | undefined>(undefined)
const searchB = ref<string>("")
const { data: suggestionsPlayerB, isFetching: loadingB } = findCompPlayers(
	searchB,
	route,
	true,
	t,
	toast,
)
const { data: signedUp } = getSignedUpSepByComp(route, t, toast)

function queryPlayer(playerA: boolean, event: DropdownFilterEvent) {
	if (playerA) searchA.value = event.value
	else searchB.value = event.value
}

const { mutate: mutateSignUpDoubleTog, isPending: mutDoubleLoading } =
	useSignUpDoubleTog(route, t, toast, queryClient)

function signUpDoubleTog() {
	const dupA = signedUp.value.teams.find(
		(t) => t.playerA?.id == selectedPlayerA.value?.id,
	)
	if (dupA && dupA.playerA) {
		toast.add({
			severity: "error",
			summary: t("competition.action.signup.conflict"),
			detail: t("competition.action.signup.exists").replace(
				"$name",
				dupA.playerA.name,
			),
			life: 3000,
		})
		return
	}
	const dupB = signedUp.value.teams.find(
		(t) => t.playerB?.id == selectedPlayerB.value?.id,
	)
	if (dupB && dupB.playerB) {
		toast.add({
			severity: "error",
			summary: t("competition.action.signup.warning.no_player_selected"),
			detail: t("competition.action.signup.exists").replace(
				"$name",
				dupB.playerB.name,
			),
			life: 3000,
		})
		return
	}

	mutateSignUpDoubleTog({ playerA: selectedPlayerA, playerB: selectedPlayerB })
}
</script>
<style scoped></style>
