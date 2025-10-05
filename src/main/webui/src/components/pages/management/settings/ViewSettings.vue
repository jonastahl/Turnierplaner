<template>
	<ViewCompSettings v-if="route.params.compId" />
	<ViewTourSettings v-else />
</template>

<script setup lang="ts">
import { useRoute } from "vue-router"
import ViewCompSettings from "@/components/pages/management/settings/ViewCompSettings.vue"
import ViewTourSettings from "@/components/pages/management/settings/ViewTourSettings.vue"

const route = useRoute()
</script>

<style scoped></style>
