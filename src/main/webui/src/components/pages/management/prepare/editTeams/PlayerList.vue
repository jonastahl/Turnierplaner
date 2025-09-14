<template>
	<div>
		<Card>
			<template #title>{{ props.title }}</template>
			<template #content>
				<div class="flex flex-column gap-3">
					<div
						class="flex flex-wrap align-items-end justify-content-end gap-3 w-full"
					>
						<AddPlayer
							:competition="props.competition"
							:tournament="props.tournament"
							class="flex-grow-1 w-1"
							:is-updating="isUpdating"
							:secondary="secondary"
							@add-player="addPlayer"
						/>
						<DeleteBox :group="props.group" :secondary="secondary" />
					</div>
					<PlayerContainerDraggable
						:id="props.id"
						:players="props.players"
						:group="props.group"
						:secondary="props.secondary"
						:is-updating="locAnimated"
					/>
				</div>
			</template>
		</Card>
	</div>
</template>

<script setup lang="ts">
import { Tournament } from "@/interfaces/tournament"
import { Competition } from "@/interfaces/competition"
import AddPlayer from "@/components/pages/management/prepare/editTeams/AddPlayer.vue"
import DeleteBox from "@/components/pages/management/prepare/editTeams/DeleteBox.vue"
import { ref } from "vue"
import { Player } from "@/interfaces/player"
import PlayerContainerDraggable from "@/components/pages/management/prepare/components/PlayerContainerDraggable.vue"

const props = withDefaults(
	defineProps<{
		id: string
		tournament: Tournament
		competition: Competition
		isUpdating?: boolean
		players: Player[]
		title: string
		group: string
		secondary?: boolean
	}>(),
	{
		isUpdating: false,
		secondary: false,
	},
)

const locAnimated = ref(false)

async function addPlayer(p: Player) {
	locAnimated.value = true
	//TODO don't add duplicates
	props.players.push(p)
	await new Promise((resolve) => setTimeout(resolve, 500))
	locAnimated.value = false
}
</script>

<style scoped></style>
