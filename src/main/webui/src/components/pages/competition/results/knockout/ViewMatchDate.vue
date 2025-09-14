<template>
	<div id="date">
		{{ props.match.begin?.toLocaleString(t("lang"), dateOptions) || "" }}
	</div>
</template>

<script setup lang="ts">
import { KnockoutMatch } from "@/interfaces/knockoutSystem"
import { useI18n } from "vue-i18n"

const { t } = useI18n()

const props = defineProps<{
	match: KnockoutMatch
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
#date {
	text-align: right;
	padding-right: 20px;
	font-style: italic;
}
</style>
