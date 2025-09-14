<template>
	<div>
		<Card>
			<template #title>Teams</template>
			<template #content>
				<slot></slot>

				<DraggablePanel
					id="teams"
					:component-data="{
						tag: 'div',
						name: props.animated ? 'playerList' : 'default',
						type: 'transition',
					}"
					:list="teams"
					:pull="false"
					:put="['playersA', 'playersB']"
					:tag="TransitionGroup"
					:wrap="wrap"
					class="flex flex-row flex-wrap gap-2 border-3 border-round border-dashed min-team-height"
					group="teams"
					item-key="id"
					style="
						box-sizing: content-box;
						width: calc(100% - 6px);
						margin-top: 20px;
					"
				>
					<template #default="{ item, index }">
						<TeamBox :id="item.id" :different="competition.playerB.different">
							<template #playerA>
								<DraggablePanel
									id="playerA"
									:component-data="{
										tag: 'div',
										name: props.animated ? 'teamBox' : 'default',
										type: 'transition',
									}"
									:list="item.playerA"
									:put="['playersA']"
									:tag="TransitionGroup"
									group="playersA"
									item-key="name"
									style="width: 100%; height: 100%"
									single
									@on-remove="() => memberRemoved(index)"
								>
									<template #default="{ item: player }">
										<PlayerCard :key="(<Player>player).name" :player="player" />
									</template>
								</DraggablePanel>
							</template>
							<template #playerB>
								<DraggablePanel
									:id="competition.playerB.different ? 'playerB' : 'playerA'"
									:component-data="{
										tag: 'div',
										name: props.animated ? 'teamBox' : 'default',
										type: 'transition',
									}"
									:list="item.playerB"
									:group="
										competition.playerB.different ? 'playersB' : 'playersA'
									"
									:tag="TransitionGroup"
									:put="[
										competition.playerB.different ? 'playersB' : 'playersA',
									]"
									item-key="name"
									style="width: 100%; height: 100%"
									single
									@on-remove="() => memberRemoved(index)"
								>
									<template #default="{ item: player }">
										<PlayerCard
											:key="(<Player>player).name"
											:player="player"
											:secondary="competition.playerB.different"
										/>
									</template>
								</DraggablePanel>
							</template>
						</TeamBox>
					</template>
				</DraggablePanel>
			</template>
		</Card>
	</div>
</template>

<script lang="ts" setup>
import TeamBox from "@/components/pages/management/prepare/components/TeamBox.vue"
import { Competition } from "@/interfaces/competition"
import { TransitionGroup } from "vue"
import { v4 as uuidv4 } from "uuid"
import DraggablePanel from "@/draggable/DraggablePanel.vue"
import PlayerCard from "@/components/pages/management/prepare/components/PlayerCard.vue"
import { Player } from "@/interfaces/player"
import { TeamArray } from "@/interfaces/team"

const props = defineProps<{
	teams: TeamArray[]
	competition: Competition
	animated?: boolean
}>()

function memberRemoved(i: number) {
	if (
		props.teams[i].playerA.length === 0 &&
		props.teams[i].playerB.length === 0
	)
		props.teams.splice(i, 1)
	if (
		i + 1 < props.teams.length &&
		props.teams[i + 1].playerA.length === 0 &&
		props.teams[i + 1].playerB.length === 0
	)
		props.teams.splice(i + 1, 1)
}

function wrap(el: Player, fromId: string): TeamArray {
	if (fromId === "playerA") {
		return {
			id: uuidv4(),
			playerA: [el],
			playerB: [],
		}
	}
	if (fromId === "playerB") {
		return {
			id: uuidv4(),
			playerA: [],
			playerB: [el],
		}
	}

	throw new Error("Must be from playerA or playerB")
}
</script>

<style scoped></style>
