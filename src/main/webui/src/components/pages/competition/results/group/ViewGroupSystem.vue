<template>
	<!-- TODO add toggle to show as list? -->
	<div v-if="groupSystem && competition" class="w-full grid xl:w-11">
		<div
			class="col-12"
			:class="{
				'lg:col-8': groupSystem.finale,
			}"
		>
			<div class="grid">
				<div
					v-for="group in groupSystem.groups"
					:key="group.index"
					class="col-12"
				>
					<Card class="w-full">
						<template #content>
							<ViewGroupTable
								:group="group"
								:number-sets="competition.numberSets"
							/>
							<ViewGroupResults :group="group" />
						</template>
					</Card>
				</div>
			</div>
		</div>
		<div v-if="groupSystem.finale" class="col-12 lg:col-4">
			<Card>
				<template #content>
					<HorizontalScroller>
						<ViewKnockoutTree
							:finale="groupSystem.finale"
							:third-place="groupSystem.thirdPlace ?? undefined"
							:border-radius="0"
							:border-thickness="2"
							:margin-small="30"
							:margin-big="40"
							:titles="knockoutTitle(t)"
						>
							<template #match="{ match }">
								<ViewMatch
									v-if="match"
									:match="match"
									:mode="competition.mode"
									:number-sets="competition.numberSets"
									:allow-update="true"
								/>
							</template>
							<template #additional="{ match }">
								<ViewMatchDate v-if="match" :match="match" />
							</template>
						</ViewKnockoutTree>
					</HorizontalScroller>
				</template>
			</Card>
		</div>
	</div>

	<!-- TODO show finals -->
</template>

<script lang="ts" setup>
import { useRoute } from "vue-router"
import ViewGroupTable from "@/components/pages/competition/results/group/ViewGroupTable.vue"
import HorizontalScroller from "@/components/items/HorizontalScroller.vue"
import { getGroup } from "@/backend/group"
import ViewGroupResults from "@/components/pages/competition/results/group/ViewGroupResults.vue"
import ViewKnockoutTree from "@/components/pages/competition/results/knockout/ViewKnockoutTree.vue"
import ViewMatch from "@/components/pages/competition/results/knockout/ViewMatch.vue"
import { useI18n } from "vue-i18n"
import { knockoutTitle } from "@/components/pages/competition/results/knockout/KnockoutTitleGenerator"
import { getCompetitionDetails } from "@/backend/competition"
import { useToast } from "primevue/usetoast"
import ViewMatchDate from "@/components/pages/competition/results/knockout/ViewMatchDate.vue"

const route = useRoute()
const { t } = useI18n()
const toast = useToast()

const { data: competition } = getCompetitionDetails(route, t, toast)
const { data: groupSystem } = getGroup(route)
</script>

<style>
table {
	margin-bottom: 20px;
}
</style>
