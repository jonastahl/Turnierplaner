<template>
	<DraggablePanel
		:id="id"
		:component-data="{
			tag: 'div',
			name: props.isUpdating || isUpdating ? 'playerList' : 'default',
			type: 'transition',
		}"
		:group="group"
		:list="props.players"
		:put="[group]"
		:tag="TransitionGroup"
		class="flex flex-row flex-wrap gap-2 border-3 min-player-height border-round border-dashed"
		item-key="name"
		style="box-sizing: content-box; width: calc(100% - 6px)"
	>
		<template #default="{ item }">
			<PlayerCard
				:key="(<Player>item).name"
				:player="item"
				:secondary="secondary"
			/>
		</template>
	</DraggablePanel>
</template>

<script setup lang="ts">
import { TransitionGroup } from "vue"
import { Player } from "@/interfaces/player"
import DraggablePanel from "@/draggable/DraggablePanel.vue"
import PlayerCard from "@/components/views/compManage/prepare/components/PlayerCard.vue"

const props = withDefaults(
	defineProps<{
		id?: string
		players: Player[]
		group: string
		secondary?: boolean
		isUpdating: boolean
	}>(),
	{
		id: "default",
		isUpdating: false,
		secondary: false,
	},
)
</script>

<style scoped></style>
