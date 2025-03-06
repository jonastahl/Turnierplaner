<template>
	<HeadContent />
	<div v-if="silentLoginCompleted" id="body">
		<RouterView />
	</div>
	<Toast />
	<AppFooter />
</template>

<script lang="ts" setup>
import HeadContent from "@/components/header/HeadContent.vue"
import { provide, ref, watch } from "vue"
import { access_token, auth, reloadAuth } from "@/security/AuthService"
import { getConfig } from "@/backend/config"
import { useI18n } from "vue-i18n"
import { rebuildAuthSettings } from "@/security/settings"
import { settings } from "@/settings"
import AppFooter from "@/components/footer/AppFooter.vue"

const { data: lConfig } = getConfig(ref(false))
const silentLoginCompleted = ref(false)
const loggedIn = ref(false)
provide("loggedIn", loggedIn)
watch(
	lConfig,
	() => {
		if (lConfig.value) {
			settings.AUTH_DOMAIN = lConfig.value.auth_url
			rebuildAuthSettings()
			reloadAuth()
			auth
				.silentLogin()
				.then(() => {
					silentLoginCompleted.value = true
				})
				.catch(() => {
					silentLoginCompleted.value = true
					console.log("Error logging in")
				})
			auth.addUserLoadedListener(() => {
				auth.getUser().then((user) => {
					if (user !== null) {
						access_token.value = user.access_token
						loggedIn.value = true
					} else {
						access_token.value = null
					}
				})
			})
			auth.addUserUnloadedListener(() => {
				access_token.value = null
				loggedIn.value = false
			})
		}
	},
	{ immediate: true },
)

const { data: config } = getConfig(loggedIn)
const { locale } = useI18n()

watch(
	config,
	async () => {
		if (config.value) locale.value = config.value.language
	},
	{ immediate: true },
)
</script>

<style>
body {
	margin: 0;
	padding: 0 !important;
}
</style>

<style scoped>
#body {
	margin-top: 20px;
	min-height: calc(100vh - 70px);
}

main {
	flex-grow: 1;
}

aside {
	height: 100px;
	width: 400px;
	background-color: blue;
}
</style>
