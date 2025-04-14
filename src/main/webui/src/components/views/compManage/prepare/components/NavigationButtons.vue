<template>
	<div class="mt-2 grid grid-nogutter justify-content-between">
		<Button
			v-if="props.reset"
			:disabled="props.loading"
			:label="t('general.reset')"
			severity="danger"
			@click="emit('reset')"
		/>
		<Button
			v-if="props.reload"
			:disabled="props.loading"
			:label="t('general.reload')"
			severity="warning"
			@click="emit('reload')"
		/>
		<Button
			:disabled="loading"
			:label="t('general.save')"
			severity="info"
			@click="emit('save')"
		/>
		<Button
			v-if="props.isComplete"
			:disabled="loading"
			icon="pi pi-check"
			icon-pos="right"
			severity="success"
			class="p-button-success"
			:label="t('general.saveandcomp')"
			@click="emit('complete')"
		/>
		<Button
			v-else
			:disabled="loading"
			icon="pi pi-angle-right"
			icon-pos="right"
			severity="success"
			:label="t('general.saveandcont')"
			@click="emit('complete')"
		/>
	</div>
</template>

<script setup lang="ts">
import Button from "primevue/button"
import { useI18n } from "vue-i18n"

const { t } = useI18n()

const props = withDefaults(
	defineProps<{
		reset?: boolean
		reload?: boolean
		isComplete?: boolean
		loading: boolean
	}>(),
	{
		reset: true,
		reload: false,
		isComplete: false,
	},
)

const emit = defineEmits<{
	reset: []
	reload: []
	save: []
	complete: []
}>()
</script>

<style scoped></style>
