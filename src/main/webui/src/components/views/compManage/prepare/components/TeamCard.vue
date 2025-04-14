<template>
	<div
		class="h-3rem border-round select-none cursor-pointer pl-3 pr-3 inline text-50 flex justify-content-center flex-column bg-primary-400"
		:class="{
			'align-items-start': props.team.playerB,
			'align-items-center': !props.team.playerB,
		}"
	>
		<span v-if="props.team.playerA">{{ props.team.playerA.name }}</span>
		<span v-if="props.team.playerB">{{ props.team.playerB.name }}</span>
	</div>
</template>

<script setup lang="ts">
import { Team } from "@/interfaces/team"

const props = defineProps<{ team: Team }>()
</script>

<style scoped></style>
