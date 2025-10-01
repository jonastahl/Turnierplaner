<template>
	<Card v-if="knockoutSystem && competition" class="w-fit max-w-full">
		<template #content>
			<div class="w-full flex flex-column align-items-center">
				<HorizontalScroller>
					<ViewKnockoutTree
						:finale="knockoutSystem.finale ?? undefined"
						:third-place="knockoutSystem.thirdPlace ?? undefined"
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
			</div>
		</template>
	</Card>
</template>

<script lang="ts" setup>
import { useRoute } from "vue-router"
import ViewKnockoutTree from "@/components/pages/competition/results/knockout/ViewKnockoutTree.vue"
import HorizontalScroller from "@/components/items/HorizontalScroller.vue"
import { getKnockout } from "@/backend/knockout"
import ViewMatch from "@/components/pages/competition/results/knockout/ViewMatch.vue"
import ViewMatchDate from "@/components/pages/competition/results/knockout/ViewMatchDate.vue"
import { knockoutTitle } from "@/components/pages/competition/results/knockout/KnockoutTitleGenerator"
import { useI18n } from "vue-i18n"
import { getCompetitionDetails } from "@/backend/competition"
import { useToast } from "primevue/usetoast"

const { t } = useI18n()
const route = useRoute()
const toast = useToast()

const { data: competition } = getCompetitionDetails(route, t, toast)
const { data: knockoutSystem } = getKnockout(route)
</script>

<style scoped>
table {
	text-align: center;
}
</style>
