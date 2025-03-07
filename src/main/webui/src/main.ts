import { createApp } from "vue"
import App from "./App.vue"
import { createI18n, I18nOptions } from "vue-i18n"
import * as VueRouter from "vue-router"

import { settings } from "@/settings"

/* https request */
import axios from "axios"

import { access_token } from "@/security/AuthService"
/* i18n */
import languages from "./i18n"
import routes from "./routes"
/* Primevue components*/
import PrimeVue from "primevue/config"
import ToastService from "primevue/toastservice"
import Toast from "primevue/toast"
import Tooltip from "primevue/tooltip"
import FocusTrap from "primevue/focustrap"
import InputGroup from "primevue/inputgroup"

/* Primevue styling */
import "primeflex/primeflex.css"
import "primevue/resources/themes/lara-light-blue/theme.css"
import "primeicons/primeicons.css"
import "./style.css"

/* Icons */
import "material-symbols/outlined.css"

/* yup validation */
import { setLocale } from "yup"
import { VueQueryPlugin, VueQueryPluginOptions } from "@tanstack/vue-query"

axios.defaults.baseURL = settings.BACKEND

axios.interceptors.request.use(
	function (config) {
		if (access_token.value !== null)
			config.headers.Authorization = `Bearer ${access_token.value}`
		return config
	},
	function (error) {
		return Promise.reject(error)
	},
)

const messages = languages

export type MessageSchema = (typeof messages)["de"]

declare module "vue-i18n" {}

const options: I18nOptions = {
	locale: "de", // set locale
	fallbackLocale: "en", // set fallback locale
	warnHtmlMessage: false,
	missingWarn: true,
	fallbackWarn: false,
	messages,
	legacy: false,
}

const i18n = createI18n<false, typeof options>(options)

declare module "@vue/runtime-core" {
	interface ComponentCustomProperties {
		$i18n: {
			locale: string
			availableLocales: string[]
		}
		$t: (_: string) => string
	}
}

/* add font awesome icon component */
const app = createApp(App)

const router = VueRouter.createRouter({
	history: VueRouter.createWebHistory(),
	routes,
})

setLocale({
	// use constant translation keys for messages without values
	mixed: {
		default: "field_invalid",
		required: "validation.field_required",
	},
	// use functions to generate an error object that includes the value from the schema
	string: {
		min: "validation.field_too_short",
		max: "validation.field_too_big",
	},
})

const vueQueryPluginOptions: VueQueryPluginOptions = {
	queryClientConfig: {
		defaultOptions: {
			queries: {
				staleTime: 1000,
				refetchOnWindowFocus: false,
				refetchOnReconnect: false,
				refetchOnMount: false,
			},
		},
	},
	enableDevtoolsV6Plugin: true,
}

app
	.use(i18n)
	.use(axios)
	.use(router)
	/* Primevue */
	.use(PrimeVue, { ripple: true })
	.use(ToastService)
	.component("Toast", Toast)
	.component("InputGroup", InputGroup)
	.directive("tooltip", Tooltip)
	.directive("focustrap", FocusTrap)
	.use(VueQueryPlugin, vueQueryPluginOptions)
	.mount("#app")

export { router }
