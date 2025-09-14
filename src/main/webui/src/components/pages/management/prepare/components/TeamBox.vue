<template>
	<div>
		<div
			id="top"
			class="border-3 border-dashed h-3rem border-round-top content-box"
		>
			<slot name="playerA" />
		</div>
		<div
			:class="{
				bottomSame: !props.different,
				bottomDifferent: props.different,
			}"
			class="border-3 border-top-none border-dashed h-3rem border-round-bottom content-box"
		>
			<slot name="playerB" />
		</div>
	</div>
</template>

<script lang="ts" setup>
const props = defineProps<{
	different: boolean
}>()
</script>

<style scoped>
.content-box {
	box-sizing: content-box;
}

#top {
	border-left-color: var(--primary-400);
	border-top-color: var(--primary-400);
	border-right-color: var(--primary-400);
}

.bottomSame {
	border-left-color: var(--primary-400);
	border-bottom-color: var(--primary-400);
	border-right-color: var(--primary-400);
}

.bottomDifferent {
	border-left-color: var(--yellow-500);
	border-bottom-color: var(--yellow-500);
	border-right-color: var(--yellow-500);
}
</style>
