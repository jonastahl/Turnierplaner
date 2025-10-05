<template>
	<DataTable :value="courts" striped-rows class="w-full mb-2" size="small">
		<Column field="name" :header="t('general.name')"></Column>
		<Column field="courtType" :header="t('court.type')">
			<template #body="{ data }">
				{{ data.courtType ? t("court.courtTypes." + data.courtType) : "" }}
			</template>
		</Column>
	</DataTable>
	<ViewCreateCourtSmall />
</template>

<script setup lang="ts">
import ViewCreateCourtSmall from "@/components/pages/court/ViewCreateCourtSmall.vue"
import { getCourts } from "@/backend/court"
import { useI18n } from "vue-i18n"

const { t } = useI18n()
const { data: courts } = getCourts()
</script>

<style scoped></style>
