<template>
	<div class="flex flex-col gap-3">
		<div class="w-6"></div>
		<div class="w-6 flex flex-row justify-content-around gap-2">
			<div
				v-for="n in numberSets"
				:key="n"
				style="min-width: 3rem"
				class="flex justify-content-center align-items-center"
			>
				<RadioButton
					v-model="selectedSet"
					:input-id="n.toString()"
					name="sets"
					:value="n - 1"
				/>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
const props = defineProps<{
	numberSets: number
}>()
const selectedSet = defineModel<number>("selectedSet")
</script>
<style scoped>
</style>