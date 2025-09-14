<template>
	<Card
		id="card"
		class="relative cursor-pointer md:w-23rem w-full"
		@click="selected"
	>
		<template #title>
			{{ name }}
		</template>
		<template #content>
			{{ description }}
		</template>
		<template #footer>
			<Button outlined disabled style="visibility: hidden"> </Button>
			<Button v-if="type === 'GROUPS'" text outlined disabled class="icon">
				<template #icon>
					<span class="material-symbols-outlined">grid_view</span>
				</template>
			</Button>
			<Button
				v-else-if="type === 'KNOCKOUT'"
				disabled
				text
				outlined
				class="icon"
			>
				<template #icon>
					<span class="material-symbols-outlined">groups</span>
				</template>
			</Button>
			<Button
				v-if="canEdit"
				rounded
				outlined
				class="settings"
				@click="settings"
				@click.stop
			>
				<template #icon>
					<span class="material-symbols-outlined">settings</span>
				</template>
			</Button>
		</template>
	</Card>
</template>

<script lang="ts" setup>
const props = defineProps<{
	name: string
	description: string
	type: string
	canEdit: boolean
}>()

const emit = defineEmits(["selected", "settings"])

function selected() {
	emit("selected", props.name)
}

function settings() {
	emit("settings", props.name)
}
</script>

<style scoped>
.icon {
	position: absolute;
	left: 1.25rem;
	bottom: 1.25rem;
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
