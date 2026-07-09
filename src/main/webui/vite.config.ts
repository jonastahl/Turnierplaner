import { resolve } from "path"
import { defineConfig } from "vite"
import vue from "@vitejs/plugin-vue"
import Icons from "unplugin-icons/vite"
import Components from "unplugin-vue-components/vite"
import { PrimeVueResolver } from "unplugin-vue-components/resolvers"
import IconsResolver from "unplugin-icons/resolver"

// https://vitejs.dev/config/
export default defineConfig({
	build: {
		rollupOptions: {
			input: {
				main: "index.html",
				callbackSignIn: "callbackSignIn.html",
				callbackSignOut: "callbackSignOut.html",
				callbackSilent: "callbackSilent.html",
			},
		},
	},
	server: {
		host: true,
	},
	plugins: [
		vue(),
		Components({
			resolvers: [
				PrimeVueResolver(),
				IconsResolver({
					prefix: "i",
				}),
			],
		}),
		Icons({
			compiler: "vue3",
			autoInstall: true,
		}),
	],
	resolve: {
		alias: {
			"@": resolve(__dirname, "src"),
		},
		extensions: [".js", ".ts"],
	},
	define: {
		__VUE_I18N_FULL_INSTALL__: true,
		__VUE_I18N_LEGACY_API__: false,
		__INTLIFY_PROD_DEVTOOLS__: false,
	},
})
