<template>
	<Card class="relative cursor-pointer md:w-23rem w-full" @click="selected">
		<template #title>
			{{ props.tournament.name }}
		</template>
		<template #subtitle>
			<p v-if="new Date() >= props.tournament.registration_phase.end">
				{{ props.tournament.game_phase.begin.toLocaleDateString() }}
				- {{ props.tournament.game_phase.end.toLocaleDateString() }}
			</p>
			<p v-else>
				{{ props.tournament.registration_phase.begin.toLocaleDateString() }} -
				{{ props.tournament.registration_phase.end.toLocaleDateString() }}
			</p>
		</template>
		<template #content>
			<div class="content">
				{{ props.tournament.description }}
			</div>
		</template>
		<template #footer>
			<div class="grid grid-nogutter justify-content-end">
				<Button
					v-if="props.canCreate"
					outlined
					rounded
					disabled
					style="visibility: hidden"
				></Button>
				<Button
					v-if="props.canCreate"
					outlined
					rounded
					class="settings"
					@click="settings"
					@click.stop
				>
					<template #icon>
						<span class="material-symbols-outlined">settings</span>
					</template>
				</Button>
			</div>
		</template>
	</Card>
</template>

<script lang="ts" setup>
import { Tournament } from "@/interfaces/tournament"

const props = defineProps<{
	canCreate: boolean
	tournament: Tournament
}>()

const emit = defineEmits(["selected", "settings"])

function selected() {
	emit("selected", props.tournament.name)
}

function settings() {
	emit("settings", props.tournament.name)
}
</script>

<style scoped>
.content {
	word-break: break-word;
}

.settings {
	position: absolute;
	right: 1.25rem;
	bottom: 1.25rem;
}

.settings:active {
	color: #505050;
}
</style>
