<template>
	<ViewPlayerName :player="props.team?.playerA" :inverted :short :white />
	<template v-if="props.team?.playerB">
		<br />
		<ViewPlayerName :player="props.team?.playerB" :inverted :short :white />
	</template>
</template>

<script setup lang="ts">
import { Team } from "@/interfaces/team"
import ViewPlayerName from "@/components/links/LinkPlayerName.vue"

const props = withDefaults(
	defineProps<{
		team?: Team | null
		inverted?: boolean
		short?: boolean
		white?: boolean
	}>(),
	{
		team: null,
		inverted: false,
		short: false,
		white: false,
	},
)
</script>

<style scoped></style>
