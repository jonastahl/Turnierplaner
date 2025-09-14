<template>
	<div class="container text-center">
		<table class="table">
			<thead>
				<tr>
					<!--TOOD internalization-->
					<th scope="col">Begin Time</th>
					<th scope="col">Court</th>
					<th scope="col">Player 1</th>
					<th scope="col">Player 2</th>
					<th scope="col">Result</th>
				</tr>
			</thead>
			<tbody class="table-group-divider">
				<tr
					v-for="match in props.group.matches"
					:key="(match.teamA?.id || '') + (match.teamB?.id || '')"
				>
					<td v-if="match.begin">
						{{ new Date(match.begin).toLocaleTimeString() }}
					</td>
					<td>{{ match.court }}</td>
					<td>{{ match.teamA?.playerA?.name }}</td>
					<td>{{ match.teamB?.playerA?.name }}</td>
				</tr>
			</tbody>
		</table>
	</div>
</template>

<script lang="ts" setup>
import { Group } from "@/interfaces/groupSystem"

const props = defineProps<{
	group: Group
}>()
</script>

<style scoped></style>
