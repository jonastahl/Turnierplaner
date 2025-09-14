<template>
	<InputGroup>
		<InputText v-model="name" size="small" />
		<Dropdown
			v-model="type"
			option-value="id"
			:options="options"
			option-label="name"
			:placeholder="t('court.type')"
		/>
		<Button size="small" @click="add">Add</Button>
	</InputGroup>
</template>

<script setup lang="ts">
import { ref } from "vue"
import { useCreateCourt } from "@/backend/court"
import { useI18n } from "vue-i18n"
import { useToast } from "primevue/usetoast"

const { t } = useI18n()
const toast = useToast()
const { mutate: addCourt } = useCreateCourt()

const name = ref("")
const type = ref(null)

const options = [
	{ id: "HARD", name: t("court.courtTypes.HARD") },
	{ id: "CLAY", name: t("court.courtTypes.CLAY") },
	{ id: "GRASS", name: t("court.courtTypes.GRASS") },
]

function add() {
	if (name.value && type.value)
		addCourt({
			name: name.value,
			courtType: type.value,
		})
	else
		toast.add({
			severity: "error",
			summary: t("general.error_saving"),
			detail: t("court.not_selected"),
			life: 3000,
		})
}
</script>

<style scoped></style>
