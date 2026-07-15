<template>
	<DataTable
		v-model:filters="filters"
		data-key="id"
		resizable-columns
		:loading="!matches"
		:value="matches"
		filter-display="row"
		:global-filter-fields="['teamA', 'teamB']"
	>
		<template #empty>{{ t("general.matches.empty") }}</template>
		<template #loading>{{ t("general.matches.loading") }}</template>
		<Column
			v-if="!route.params.tourId"
			sortable
			field="tourName"
			:header="t('tournament.label')"
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
			:header="t('competition.label')"
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
			:header="t('general.court.single')"
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
			:header="t('general.time.from')"
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
			:header="t('general.time.to')"
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
			:header="t('general.team.label') + ' A'"
			:show-filter-menu="false"
		>
			<template #body="{ data }">
				<ViewTeamNames :team="<Team>data.teamA" />
			</template>
			<template #filter="{}">
				<InputText
					v-model="(filters['global'] as DataTableFilterMetaData).value"
					size="small"
					type="text"
					class="p-column-filter"
					:placeholder="t('general.filter_by_name')"
				/>
			</template>
		</Column>
		<Column
			sortable
			field="teamB"
			:header="t('general.team.label') + ' B'"
			:show-filter-menu="false"
		>
			<template #body="{ data }">
				<ViewTeamNames :team="<Team>data.teamB" />
			</template>
		</Column>
		<!-- TODO add results -->
		<Column sortable field="sets" :header="t('general.result')">
			<template #body="{ data }">
				<ResultCompact
					:edit-result="mayEditMatch(!!isDirector, !!isReporter, data)"
					:match="data"
				/>
			</template>
		</Column>
	</DataTable>
</template>

<script setup lang="ts">
import { getTournamentDetails } from "@/backend/tournament"
import { useRoute } from "vue-router"
import { useI18n } from "vue-i18n"
import { computed, inject, ref, watch } from "vue"
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
import { genTitle, getScheduledMatches } from "@/backend/match"
import ResultCompact from "@/components/items/ResultCompact.vue"
import { mayEditMatch } from "@/backend/set"
import { getIsDirector, getIsReporter } from "@/backend/security"

const route = useRoute()
const { t } = useI18n()
const toast = useToast()
const { data: tournament } = getTournamentDetails(route, t, toast)
const { data: courts } = getTournamentCourts(route)

const isLoggedIn = inject("loggedIn", ref(false))
const { data: isReporter } = getIsReporter(isLoggedIn)
const { data: isDirector } = getIsDirector(isLoggedIn)

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
		value: null,
		matchMode: FilterMatchMode.DATE_AFTER,
	},
	end: {
		value: null,
		matchMode: FilterMatchMode.DATE_BEFORE,
	},
	global: { value: null, matchMode: TEAMS_FILTER },
	teamA: { value: null, matchMode: TEAMS_FILTER },
	teamB: { value: null, matchMode: TEAMS_FILTER },
})

function teamFilter(team: Team | null, filter: string | null) {
	console.log(team)
	const filterValue = filter?.toLowerCase() || ""
	const playerA = team?.playerA?.name.toLowerCase() || ""
	const playerB = team?.playerB?.name.toLowerCase() || ""
	return playerA.includes(filterValue) || playerB.includes(filterValue)
}

watch(
	() => (filters.value.begin as DataTableFilterMetaData).value,
	(newValue) => {
		if (!newValue) {
			;(filters.value.begin as DataTableFilterMetaData).value = tournament.value
				? tournament.value?.game_phase.begin
				: new Date(new Date().getTime() - 7 * 24 * 60 * 60 * 1000)
		}
	},
	{ immediate: true },
)
watch(
	() => (filters.value.end as DataTableFilterMetaData).value,
	(newValue) => {
		if (!newValue) {
			;(filters.value.end as DataTableFilterMetaData).value = tournament.value
				? tournament.value?.game_phase.end
				: endFallback
		}
	},
	{ immediate: true },
)

function titleFilter(
	title: AnnotatedMatch["title"] | null,
	filter: string | null,
) {
	const titleValue = title ? genTitle(title, t) : ""
	const filterValue = filter?.toLowerCase() || ""
	return titleValue.toLowerCase().includes(filterValue)
}

const { data: matches } = getScheduledMatches(
	computed(() => (<DataTableFilterMetaData>filters.value.begin).value) ||
		new Date(),
	computed(() => (<DataTableFilterMetaData>filters.value.end).value) ||
		new Date().setMonth(new Date().getMonth() + 2),
	computed(() => route.params.playerId as string | undefined),
	computed(() => route.params.tourId as string | undefined),
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
