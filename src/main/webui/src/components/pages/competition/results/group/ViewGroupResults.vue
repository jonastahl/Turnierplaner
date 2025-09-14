<template>
	<DataTable :value="props.group.results" size="small">
		<Column field="rank" :header="'R'" />
		<Column field="team" :header="'Team'">
			<template #body="{ data }">
				<LinkTeamNames :team="data.team" inverted short />
			</template>
		</Column>
		<Column :header="'M'">
			<template #body="{ data }">
				{{ data.matchesWon }} : {{ data.matchesLost }}
			</template>
		</Column>
		<Column :header="'S'">
			<template #body="{ data }">
				{{ data.setsWon }} : {{ data.setsLost }}
			</template>
		</Column>
		<Column :header="'G'">
			<template #body="{ data }">
				{{ data.gamesWon }} : {{ data.gamesLost }}
			</template>
		</Column>
	</DataTable>
</template>

<script setup lang="ts">
import { Group } from "@/interfaces/groupSystem"
import LinkTeamNames from "@/components/links/LinkTeamNames.vue"

const props = defineProps<{
	group: Group
}>()
</script>

<style scoped></style>
