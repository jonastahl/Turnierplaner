<template>
	<div class="w-full p-2">
		<Card style="margin-top: -10px !important">
			<template #header>
				<ViewMenuSelector />
				<ViewCompSelector />
			</template>
			<template #content>
				<router-view />
			</template>
		</Card>
	</div>
</template>

<script setup lang="ts">
import ViewCompSelector from "@/components/views/compManage/tabs/ViewCompSelector.vue"
import ViewMenuSelector from "@/components/views/compManage/tabs/ViewMenuSelector.vue"
</script>

<style scoped></style>
