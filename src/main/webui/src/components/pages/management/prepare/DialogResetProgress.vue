<template>
	<Dialog v-model:visible="visible">
		<div class="flex flex-column gap-3 w-20rem">
			<span>{{ t("competition.manage.warning.prepare_reset") }}</span>
			<Button severity="danger" :label="t('general.reset')" @click="trReset" />
		</div>
	</Dialog>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n"
import { useResetPreparation } from "@/backend/competition"
import { useRoute } from "vue-router"
import { useToast } from "primevue/usetoast"

const { t } = useI18n()
const route = useRoute()
const toast = useToast()

const visible = defineModel<boolean>()

const { mutate: reset } = useResetPreparation(route, t, toast)

function trReset() {
	reset(undefined, {
		onSuccess() {
			visible.value = false
		},
	})
}
</script>

<style scoped></style>
