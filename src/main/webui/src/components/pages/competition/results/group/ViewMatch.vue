<template>
	<p v-if="!props.match.finished">
		{{ $t("ViewGroupSystem.start") }}<br />
		<template v-if="props.match.begin">
			{{ props.match.begin.toLocaleString(t("lang"), dateOptions) }}
		</template>
		<template v-else> --- </template>
		<template v-if="props.match.court"
			><br />
			{{ props.match.court }}
		</template>
	</p>
	<div v-else-if="props.reversed">
		<p v-for="(set, index) in props.match.sets" :key="index">
			{{ set.scoreB }} - {{ set.scoreA }}
		</p>
	</div>
	<div v-else>
		<p v-for="(set, index) in props.match.sets" :key="index">
			{{ set.scoreA }} - {{ set.scoreB }}
		</p>
	</div>
</template>

<script setup lang="ts">
import { Match } from "@/interfaces/match"
import { useI18n } from "vue-i18n"

const { t } = useI18n()

const props = defineProps<{
	match: Match
	reversed: boolean
}>()

const dateOptions: Intl.DateTimeFormatOptions = {
	year: "2-digit",
	month: "numeric",
	day: "numeric",
	hour: "numeric",
	minute: "numeric",
}
</script>

<style scoped>
p {
	text-align: center;
	margin: 0;
}
</style>
