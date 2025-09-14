<template>
	<MatchBox :different="false" style="width: 250px">
		<template #playerA>
			<DraggablePanel
				id="playerA"
				:component-data="{
					tag: 'div',
					type: 'transition',
					name: props.animated ? 'playerList' : 'default',
				}"
				:list="teamA"
				:tag="TransitionGroup"
				group="teams"
				item-key="id"
				style="width: 100%; height: 100%"
				:wrap="updateA"
				single
				@on-remove="removeA"
			>
				<template #default="{ item: team }">
					<TeamCard :team="team" />
				</template>
			</DraggablePanel>
		</template>
		<template #playerB>
			<DraggablePanel
				id="playerA"
				:component-data="{
					tag: 'div',
					type: 'transition',
					name: props.animated ? 'playerList' : 'default',
				}"
				:list="teamB"
				:tag="TransitionGroup"
				group="teams"
				item-key="id"
				style="width: 100%; height: 100%"
				:wrap="updateB"
				single
				@on-remove="removeB"
			>
				<template #default="{ item: team }">
					<TeamCard :team="team" />
				</template>
			</DraggablePanel>
		</template>
	</MatchBox>
</template>

<script setup lang="ts">
import { KnockoutMatch } from "@/interfaces/knockoutSystem"
import { ref, TransitionGroup, watchEffect } from "vue"
import DraggablePanel from "@/draggable/DraggablePanel.vue"
import TeamCard from "@/components/pages/management/prepare/components/TeamCard.vue"
import MatchBox from "@/components/pages/management/prepare/components/MatchBox.vue"
import { Team } from "@/interfaces/team"

const props = defineProps<{ match: KnockoutMatch; animated: boolean }>()
const emit = defineEmits(["update:teamA", "update:teamB"])

const teamA = ref<Team[]>([])
const teamB = ref<Team[]>([])

function updateA(val: Team) {
	emit("update:teamA", val)
	return val
}

function removeA() {
	emit("update:teamA", null)
}

function updateB(val: Team) {
	emit("update:teamB", val)
	return val
}

function removeB() {
	emit("update:teamB", null)
}

watchEffect(() => {
	if (props.match.teamA === null) teamA.value = []
	else if (teamA.value.length === 0 || teamA.value[0] !== props.match.teamA)
		teamA.value = [props.match.teamA]
	if (props.match.teamB === null) teamB.value = []
	else if (teamB.value.length === 0 || teamB.value[0] !== props.match.teamB)
		teamB.value = [props.match.teamB]
})
</script>

<style scoped></style>
