<template>
	<DraggablePanel
		:list="teams"
		:put="true"
		item-key="id"
		:tag="TransitionGroup"
		:component-data="{
			tag: 'div',
			type: 'transition',
			name: props.animated ? 'playerList' : 'default',
		}"
		group="teams"
		class="flex flex-wrap gap-2 border-3 border-round border-dashed justify-content-start"
		:class="{
			'min-player-height': competition?.mode === Mode.SINGLE,
			'min-team-height': competition?.mode === Mode.DOUBLE,
		}"
		style="box-sizing: content-box"
	>
		<template #default="{ item }">
			<PlayerCard
				v-if="item.playerA && competition?.mode === Mode.SINGLE"
				:player="item.playerA"
			></PlayerCard>
			<TeamCard
				v-else-if="competition?.mode === Mode.DOUBLE"
				:team="item"
			></TeamCard>
		</template>
	</DraggablePanel>
</template>

<script setup lang="ts">
import { TransitionGroup } from "vue"
import { Competition, Mode } from "@/interfaces/competition"
import { Team } from "@/interfaces/team"
import PlayerCard from "@/components/pages/management/prepare/components/PlayerCard.vue"
import DraggablePanel from "@/draggable/DraggablePanel.vue"
import TeamCard from "@/components/pages/management/prepare/components/TeamCard.vue"

const props = defineProps<{
	teams: Team[]
	competition: Competition
	animated: boolean
}>()
</script>

<style scoped></style>
