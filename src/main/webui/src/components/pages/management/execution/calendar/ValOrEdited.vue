<template>
	<span :class="{ original: original !== edited }">{{ original }}</span>
	<template v-if="original !== edited">
		->
		<span class="edited">{{ edited }}</span>
	</template>
</template>

<script setup lang="ts">
defineProps<{ original: string; edited: string }>()
</script>

<style scoped>
.edited {
	color: green;
}

.original {
	color: red;
}
</style>
