import { createApp } from "vue"
import App from "./App.vue"
import { createI18n } from "vue-i18n"
import * as VueRouter from "vue-router"

import { settings } from "@/settings"

/* https request */
import VueAxios from "vue-axios"
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
import ConfirmationService from "primevue/confirmationservice"

/* Primevue styling */
import "primeflex/primeflex.css"
import "primevue/resources/themes/lara-light-blue/theme.css"
import "primeicons/primeicons.css"
import "./style.css"

/* yup validation */
import { setLocale } from "yup"
import { VueQueryPlugin, VueQueryPluginOptions } from "@tanstack/vue-query"
import ConfirmDialog from "primevue/confirmdialog"

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
axios.interceptors.response.use(
	function (response) {
		return response
	},
	function (error) {
		throw error
	},
)

const messages = languages

export type MessageSchema = (typeof messages)["de"]
export type AvailableLocales = keyof typeof messages

const i18n = createI18n<[MessageSchema], AvailableLocales>({
	locale: "de",
	fallbackLocale: "en",
	warnHtmlMessage: false,
	missingWarn: true,
	fallbackWarn: false,
	messages,
	legacy: false,
})
export default i18n
export type TranslateFunction = typeof i18n.global.t

declare module "vue-i18n" {
	// eslint-disable-next-line @typescript-eslint/no-empty-object-type
	export interface DefineLocaleMessage extends MessageSchema {}
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
	.use(VueAxios, axios)
	.use(router)
	/* Primevue */
	.use(PrimeVue, { ripple: true })
	.use(ToastService)
	.use(ConfirmationService)
	.component("Toast", Toast)
	.component("InputGroup", InputGroup)
	.component("ConfirmDialog", ConfirmDialog)
	.directive("tooltip", Tooltip)
	.directive("focustrap", FocusTrap)
	.use(VueQueryPlugin, vueQueryPluginOptions)
	.mount("#app")

export { router }
