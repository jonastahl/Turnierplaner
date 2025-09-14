<template>
	<div class="w-full p-2">
		<Card style="margin-top: -10px !important">
			<template #header>
				<ViewMenuSelector />
				<ViewExecutionMenuSelector v-if="route.meta.executionMenu" />
				<ViewCompSelector v-if="route.meta.compSelector" />
			</template>
			<template #content>
				<router-view />
			</template>
		</Card>
	</div>
</template>

<script setup lang="ts">
import ViewCompSelector from "@/components/pages/management/tabs/ViewCompSelector.vue"
import ViewMenuSelector from "@/components/pages/management/tabs/ViewMenuSelector.vue"
import ViewExecutionMenuSelector from "@/components/pages/management/tabs/ViewExecutionMenuSelector.vue"
import { inject, ref } from "vue"
import { auth } from "@/security/AuthService"
import { useRoute } from "vue-router"

const route = useRoute()
const isLoggedIn = inject("loggedIn", ref(false))
if (!isLoggedIn.value) {
	auth.login()
}
</script>

<style scoped></style>
