<template>
	<div>
		<div
			id="top"
			class="border-3 h-3rem border-dashed border-round-top content-box"
		>
			<slot name="playerA" />
		</div>
		<div
			id="bottom"
			class="border-3 h-3rem border-top-none border-dashed border-round-bottom content-box"
		>
			<slot name="playerB" />
		</div>
	</div>
</template>

<script lang="ts" setup></script>

<style scoped>
.content-box {
	box-sizing: content-box;
}

#top {
	border-left-color: var(--primary-400);
	border-top-color: var(--primary-400);
	border-right-color: var(--primary-400);
}

#bottom {
	border-left-color: var(--primary-400);
	border-bottom-color: var(--primary-400);
	border-right-color: var(--primary-400);
}
</style>
