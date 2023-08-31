import { createApp } from "vue"
import App from "./App.vue"
import { createI18n, I18nOptions } from "vue-i18n"
import * as VueRouter from "vue-router"

/* import the fontawesome core */
import { settings } from "@/settings"

/* https request */
import VueAxios from "vue-axios"
import axios from "axios"

import { access_token } from "@/security/AuthService"
import { library } from "@fortawesome/fontawesome-svg-core"
/* import font awesome icon component */
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome"
/* import specific icons */
import {
	faEyeSlash,
	faFlag,
	faGear,
	faPlus,
	faRightFromBracket,
	faRightToBracket,
	faTableCellsLarge,
	faUserGroup,
	faUserSecret,
} from "@fortawesome/free-solid-svg-icons"

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

/* add icons to the library */
library.add(
	faFlag,
	faRightToBracket,
	faRightFromBracket,
	faTableCellsLarge,
	faUserGroup,
	faPlus,
	faGear,
	faEyeSlash,
	faUserSecret,
)

/* i18n */
import languages from "./i18n"
const messages = languages

export type MessageSchema = (typeof messages)["de"]

declare module "vue-i18n" {
	export interface DefineLocaleMessage extends MessageSchema {}
}

const options: I18nOptions = {
	locale: "de", // set locale
	fallbackLocale: "en", // set fallback locale
	warnHtmlMessage: false,
	missingWarn: false,
	fallbackWarn: false,
	messages,
	legacy: false,
}

const i18n = createI18n<false, typeof options>(options)

/* add font awesome icon component */
const app = createApp(App)

import routes from "./routes"

const router = VueRouter.createRouter({
	history: VueRouter.createWebHashHistory(),
	routes,
})

/* Primevue components*/
import PrimeVue from "primevue/config"
import ToastService from "primevue/toastservice"
import Steps from "primevue/steps"
import TabView from "primevue/tabview"
import TabMenu from "primevue/tabmenu"
import Card from "primevue/card"
import Button from "primevue/button"
import Toolbar from "primevue/toolbar"
import InputText from "primevue/inputtext"
import Textarea from "primevue/textarea"
import Calendar from "primevue/calendar"
import InputSwitch from "primevue/inputswitch"
import Dropdown from "primevue/dropdown"
import Divider from "primevue/divider"
import InlineMessage from "primevue/inlinemessage"
import Toast from "primevue/toast"

/* Primevue styling */
import "primeflex/primeflex.css"
import "primevue/resources/themes/lara-light-blue/theme.css"
import "primeicons/primeicons.css"
import "./style.css"

/* Icons */
import "material-icons/iconfont/material-icons.css"
import "material-symbols"

/* yup validation */
import { setLocale } from "yup"

setLocale({
	// use constant translation keys for messages without values
	mixed: {
		default: "field_invalid",
	},
	// use functions to generate an error object that includes the value from the schema
	string: {
		min: ({ min }) => ({ key: "field_too_short", values: { min } }),
		max: ({ max }) => ({ key: "field_too_big", values: { max } }),
	},
})

app
	.use(i18n)
	.component("font-awesome-icon", FontAwesomeIcon)
	.use(VueAxios, axios)
	.use(router)
	/* Primevue */
	.use(PrimeVue, { ripple: true })
	.use(ToastService)
	.component("Steps", Steps)
	.component("TabView", TabView)
	.component("TabMenu", TabMenu)
	.component("Card", Card)
	.component("Button", Button)
	.component("Toolbar", Toolbar)
	.component("InputText", InputText)
	.component("Textarea", Textarea)
	.component("Calendar", Calendar)
	.component("InputSwitch", InputSwitch)
	.component("Dropdown", Dropdown)
	.component("Divider", Divider)
	.component("InlineMessage", InlineMessage)
	.component("Toast", Toast)
	.mount("#app")

export { router }
