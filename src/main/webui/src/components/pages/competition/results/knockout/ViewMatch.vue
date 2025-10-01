<template>
	<table
		:class="{
			tableSingles: props.mode === Mode.SINGLE,
			tableDoubles: props.mode === Mode.DOUBLE,
		}"
	>
		<tbody>
			<tr :class="{ winner: props.match.finished && props.match.winner }">
				<td class="name">
					<ViewTeamNames :team="props.match.teamA" :inverted="true" />
				</td>
				<template v-if="props.match.sets && props.match.sets.length > 0">
					<td
						v-for="(set, index) in props.match.sets"
						:key="index"
						class="result"
						:class="{
							'cursor-pointer': isReporter,
						}"
						@click="showPopUp(props.match)"
					>
						{{ set.scoreA }}
					</td>
				</template>
				<template v-else-if="allowUpdate && isReporter">
					<td
						rowspan="2"
						class="empty-result"
						:class="{
							'cursor-pointer': isReporter,
						}"
						@click="showPopUp(props.match)"
					>
						<span class="material-symbols-outlined">add_circle</span>
					</td>
				</template>
			</tr>
			<tr :class="{ winner: props.match.finished && !props.match.winner }">
				<td class="name">
					<ViewTeamNames :team="props.match.teamB" :inverted="true" />
				</td>

				<template v-if="props.match.sets && props.match.sets.length > 0">
					<td
						v-for="(set, index) in props.match.sets"
						:key="index"
						class="result"
						:class="{
							'cursor-pointer': isReporter,
						}"
						@click="showPopUp(props.match)"
					>
						{{ set.scoreB }}
					</td>
				</template>
			</tr>
		</tbody>
	</table>
	<UpdatePointsDialog
		v-if="isReporter"
		ref="dialog"
		:number-sets="props.numberSets"
	/>
</template>

<script setup lang="ts">
import { KnockoutMatch } from "@/interfaces/knockoutSystem"
import { Mode, NumberSets } from "@/interfaces/competition"
import ViewTeamNames from "@/components/links/LinkTeamNames.vue"
import UpdatePointsDialog from "@/components/pages/competition/reporting/UpdatePointsDialog.vue"
import { inject, ref } from "vue"
import { Match } from "@/interfaces/match"
import { getIsReporter } from "@/backend/security"

const props = withDefaults(
	defineProps<{
		match: KnockoutMatch
		mode: Mode
		numberSets?: NumberSets | undefined
		allowUpdate?: boolean
	}>(),
	{
		allowUpdate: false,
		numberSets: NumberSets.THREE,
	},
)

const isLoggedIn = inject("loggedIn", ref(false))
const { data: isReporter } = getIsReporter(isLoggedIn)
const dialog = ref()

const showPopUp = function (match: Match) {
	if (isReporter.value) {
		dialog.value.showPopUp(match)
	}
}
</script>

<style scoped>
table {
	table-layout: fixed;
	width: 300px;
	background-color: #e0e0e0;
	margin: 0;
	border: solid black 2px;
}

.tableSingles {
	height: 80px;
}

.tableDoubles {
	height: 108px;
}

tbody > tr {
	height: 50%;
}

.winner {
	background-color: #c0c0c0;
}

tr {
	border: solid black 2px;
}

td {
	border: solid black 1px;
}

.name {
	text-align: left;
	padding-left: 20px;
}

.result {
	text-align: center;
	width: 25px;
	background-color: #fdfdfd;
}

.empty-result {
	text-align: center;
	width: 50px;
	background-color: #fdfdfd;
}

.winner > .result {
	background-color: #e0e0e0;
}
</style>
