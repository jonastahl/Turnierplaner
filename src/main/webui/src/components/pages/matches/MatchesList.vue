<template>
	<DataTable
		v-model:filters="filters"
		data-key="id"
		resizable-columns
		:loading="!matches"
		:value="matches"
		filter-display="row"
	>
		<template #empty> No matches found.</template>
		<template #loading> Loading matches...</template>
		<Column
			v-if="!route.params.tourId"
			sortable
			field="tourName"
			:header="t('general.tournament')"
			:show-filter-menu="false"
		>
			<template #body="{ data }">
				<LinkTournament :tournament="data.tourName" />
			</template>
			<template #filter="{ filterModel, filterCallback }">
				<InputText
					v-model="filterModel.value"
					size="small"
					type="text"
					class="p-column-filter"
					:placeholder="t('general.filter_by_name')"
					@input="filterCallback()"
				/>
			</template>
		</Column>
		<Column
			sortable
			field="compName"
			:header="t('general.competition')"
			:show-filter-menu="false"
		>
			<template #body="{ data }">
				<LinkCompetition
					:tournament="data.tourName"
					:competition="data.compName"
				/>
			</template>
			<template #filter="{ filterModel, filterCallback }">
				<InputText
					v-model="filterModel.value"
					size="small"
					type="text"
					class="p-column-filter"
					:placeholder="t('general.filter_by_name')"
					@input="filterCallback()"
				/>
			</template>
		</Column>
		<Column
			sortable
			field="title"
			:header="t('general.title')"
			:show-filter-menu="false"
		>
			<template #body="{ data }">
				{{ genTitle(data.title, t) }}
			</template>
			<template #filter="{ filterModel, filterCallback }">
				<InputText
					v-model="filterModel.value"
					size="small"
					type="text"
					class="p-column-filter"
					:placeholder="t('general.filter_by_name')"
					@input="filterCallback()"
				/>
			</template>
		</Column>
		<Column
			sortable
			field="court"
			:header="t('general.court')"
			:show-filter-menu="false"
		>
			<template #filter="{ filterModel, filterCallback }">
				<MultiSelect
					v-model="filterModel.value"
					:options="courts?.map((court) => court.name) || []"
					:placeholder="t('general.any')"
					class="p-column-filter"
					style="min-width: 14rem"
					:max-selected-labels="3"
					@change="filterCallback()"
				/>
			</template>
		</Column>
		<Column
			sortable
			field="begin"
			:header="t('general.from')"
			data-type="date"
			:show-clear-button="false"
		>
			<template #body="{ data }">
				{{ data.begin?.toLocaleString(t("lang"), dateOptions) }}
			</template>
			<template #filter="{ filterModel, filterCallback }">
				<Calendar
					v-model="filterModel.value"
					:date-format="t('date_format')"
					:placeholder="t('date_format')"
					:mask="t('date_format')"
					show-time
					@date-select="filterCallback()"
				/>
			</template>
		</Column>
		<Column
			sortable
			field="end"
			:header="t('general.to')"
			data-type="date"
			:show-clear-button="false"
		>
			<template #body="{ data }">
				{{ data.end?.toLocaleString(t("lang"), dateOptions) }}
			</template>
			<template #filter="{ filterModel, filterCallback }">
				<Calendar
					v-model="filterModel.value"
					:date-format="t('date_format')"
					:placeholder="t('date_format')"
					:mask="t('date_format')"
					show-time
					@date-select="filterCallback()"
				/>
			</template>
		</Column>
		<Column
			sortable
			field="teamA"
			:header="t('general.team') + ' A'"
			:show-filter-menu="false"
		>
			<template #body="{ data }">
				<ViewTeamNames :team="<Team>data.teamA" />
			</template>
			<template #filter="{ filterModel, filterCallback }">
				<InputText
					v-model="filterModel.value"
					size="small"
					type="text"
					class="p-column-filter"
					:placeholder="t('general.filter_by_name')"
					@input="filterCallback()"
				/>
			</template>
		</Column>
		<Column
			sortable
			field="teamB"
			:header="t('general.team') + ' B'"
			:show-filter-menu="false"
		>
			<template #body="{ data }">
				<ViewTeamNames :team="<Team>data.teamB" />
			</template>
			<template #filter="{ filterModel, filterCallback }">
				<InputText
					v-model="filterModel.value"
					size="small"
					type="text"
					class="p-column-filter"
					:placeholder="t('general.filter_by_name')"
					@input="filterCallback()"
				/>
			</template>
		</Column>
		<!-- TODO add results -->
		<Column :header="t('general.result')" />
	</DataTable>
</template>

<script setup lang="ts">
import { genTitle, getTournamentDetails } from "@/backend/tournament"
import { useRoute } from "vue-router"
import { useI18n } from "vue-i18n"
import { computed, ref } from "vue"
import { getTournamentCourts } from "@/backend/court"
import { FilterMatchMode, FilterService } from "primevue/api"
import {
	DataTableFilterMeta,
	DataTableFilterMetaData,
} from "primevue/datatable"
import { Team } from "@/interfaces/team"
import { AnnotatedMatch } from "@/interfaces/match"
import { useToast } from "primevue/usetoast"
import ViewTeamNames from "@/components/links/LinkTeamNames.vue"
import LinkTournament from "@/components/links/LinkTournament.vue"
import LinkCompetition from "@/components/links/LinkCompetition.vue"
import { getPlayerScheduledMatches } from "@/backend/match"

const route = useRoute()
const { t } = useI18n()
const toast = useToast()
const { data: tournament } = getTournamentDetails(route, t, toast)
const { data: courts } = getTournamentCourts(route)

const TEAMS_FILTER = "TEAMS_FILTER"
const TITLE_FILTER = "TITLE_FILTER"

FilterService.register(TEAMS_FILTER, teamFilter)
FilterService.register(TITLE_FILTER, titleFilter)

const endFallback = new Date()
endFallback.setMonth(endFallback.getMonth() + 2)
const filters = ref<DataTableFilterMeta>({
	tourName: { value: null, matchMode: FilterMatchMode.CONTAINS },
	compName: { value: null, matchMode: FilterMatchMode.CONTAINS },
	title: { value: null, matchMode: TITLE_FILTER },
	court: { value: null, matchMode: FilterMatchMode.IN },
	begin: {
		value: tournament.value
			? tournament.value?.game_phase.begin
			: new Date(new Date().getTime() - 7 * 24 * 60 * 60 * 1000),
		matchMode: FilterMatchMode.DATE_AFTER,
	},
	end: {
		value: tournament.value ? tournament.value?.game_phase.end : endFallback,
		matchMode: FilterMatchMode.DATE_BEFORE,
	},
	teamA: { value: null, matchMode: TEAMS_FILTER },
	teamB: { value: null, matchMode: TEAMS_FILTER },
})

function teamFilter(team: Team | null, filter: string | null) {
	const filterValue = filter?.toLowerCase() || ""
	const playerA = team?.playerA?.name.toLowerCase() || ""
	const playerB = team?.playerB?.name.toLowerCase() || ""
	return playerA.includes(filterValue) || playerB.includes(filterValue)
}

function titleFilter(
	title: AnnotatedMatch["title"] | null,
	filter: string | null,
) {
	const titleValue = title ? genTitle(title, t) : ""
	const filterValue = filter?.toLowerCase() || ""
	return titleValue.toLowerCase().includes(filterValue)
}

const { data: matches } = getPlayerScheduledMatches(
	route,
	computed(() => (<DataTableFilterMetaData>filters.value.begin).value) ||
		new Date(),
	computed(() => (<DataTableFilterMetaData>filters.value.end).value) ||
		new Date().setMonth(new Date().getMonth() + 2),
)

const dateOptions: Intl.DateTimeFormatOptions = {
	year: "2-digit",
	month: "numeric",
	day: "numeric",
	hour: "numeric",
	minute: "numeric",
}
</script>

<style scoped></style>
