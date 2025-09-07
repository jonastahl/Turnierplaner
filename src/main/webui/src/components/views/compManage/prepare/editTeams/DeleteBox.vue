<template>
	<div>
		<div
			class="h-3rem flex align-items-center pl-3 select-none w-9rem z-0"
			style="position: absolute; margin-top: 3px"
			@mouseover="enter"
			@mouseleave="leave"
		>
			<span class="text-red-500">{{
				t("ViewPrepare.editTeams.deleteBox")
			}}</span>
		</div>
		<DraggablePanel
			id="trash"
			:list="trash"
			:put="[props.group]"
			item-key="id"
			tag="div"
			:group="props.group"
			class="flex flex-row flex-wrap gap-2 border-3 h-3rem border-round border-dashed w-10rem z-1"
			style="border-color: var(--red-500); box-sizing: content-box"
			@mouseover="enter"
			@mouseleave="leave"
		>
		</DraggablePanel>
		<OverlayPanel ref="op">
			<div @mouseover="enter" @mouseleave="leave">
				<span v-if="trash.length === 0">No player deleted</span>
				<DraggablePanel
					id="trash"
					:list="trash"
					:put="[props.group]"
					item-key="id"
					tag="div"
					:group="props.group"
					class="flex flex-wrap gap-2 max-w-30rem"
					style="border-color: var(--red-500)"
					dismissable
				>
					<template #default="{ item }">
						<PlayerCard :id="item.name" :player="item" :secondary="secondary" />
					</template>
				</DraggablePanel>
			</div>
		</OverlayPanel>
	</div>
</template>

<script setup lang="ts">
import DraggablePanel from "@/draggable/DraggablePanel.vue"
import { ref } from "vue"
import { Player } from "@/interfaces/player"
import PlayerCard from "@/components/views/compManage/prepare/components/PlayerCard.vue"
import OverlayPanel from "primevue/overlaypanel"
import { useI18n } from "vue-i18n"

const { t } = useI18n()

const props = defineProps<{
	group: string
	secondary: boolean
}>()

const trash = ref<Player[]>([])

const op = ref<OverlayPanel>()

const timeout = 300
let lastEnter = new Date()

function enter(event: Event) {
	lastEnter = new Date()
	op.value?.show(event)
}

function leave() {
	setTimeout(() => {
		if (new Date().getTime() - lastEnter.getTime() > timeout) op.value?.hide()
	}, timeout)
}
</script>

<style scoped></style>
