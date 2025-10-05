<template>
	<MultiSelect
		v-model="selectedCourts"
		:loading="!courts || !selectedCourts"
		:options="courts?.toSorted(courtComp)"
		option-label="name"
		:placeholder="t('court.select')"
		class="w-full"
	>
		<template #empty>
			<span>{{ t("court.no_courts") }}</span
			><br />
			<template v-if="isAdmin">
				<span>{{ t("court.no_courts_admin") }}</span
				><br />
				<Button
					class="mt-2"
					:label="t('general.settings')"
					@click="router.push({ name: Routes.Settings })"
				/>
			</template>
			<template v-else>
				<span>{{ t("court.no_courts_no_admin") }}</span>
			</template>
		</template>
	</MultiSelect>
</template>

<script setup lang="ts">
import { Court } from "@/interfaces/court"
import { courtComp, getCourts } from "@/backend/court"
import { inject, ref, watch } from "vue"
import { useI18n } from "vue-i18n"
import { getIsAdmin } from "@/backend/security"
import { useRouter } from "vue-router"
import { Routes } from "@/routes"

const { t } = useI18n()
const router = useRouter()

const selectedCourts = defineModel<Court[]>({ default: [] })
const { data: courts } = getCourts()
const isLoggedIn = inject("loggedIn", ref(false))
const { data: isAdmin } = getIsAdmin(isLoggedIn)

watch(
	selectedCourts,
	() => {
		selectedCourts.value.sort(courtComp)
	},
	{ immediate: true },
)
</script>

<style scoped></style>
