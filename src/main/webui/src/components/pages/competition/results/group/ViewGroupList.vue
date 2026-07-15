<template>
	<div class="container text-center">
		<table class="table">
			<thead>
				<tr>
					<th scope="col">{{ t("general.time.begin") }}</th>
					<th scope="col">{{ t("general.court.single") }}</th>
					<th scope="col">{{ t("competition.player.A") }}</th>
					<th scope="col">{{ t("competition.player.B") }}</th>
					<th scope="col">{{ t("general.result") }}}</th>
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
import { useI18n } from "vue-i18n"

const { t } = useI18n()

const props = defineProps<{
	group: Group
}>()
</script>

<style scoped></style>
