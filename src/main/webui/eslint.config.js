import js from "@eslint/js"
import tseslint from "typescript-eslint"
import vue from "eslint-plugin-vue"
import vueI18n from "@intlify/eslint-plugin-vue-i18n"
import eslintConfigPrettier from "eslint-config-prettier"
import globals from "globals"

export default [
	{
		ignores: [
			"dist/**",
			"node/**",
			"node_modules/**",
			"package.json",
			"package-lock.json",
			".prettierrc.json",
			"tsconfig.json",
		],
	},

	js.configs.recommended,
	...tseslint.configs.recommended,
	...vue.configs["flat/recommended"],
	...vueI18n.configs["flat/recommended"],

	{
		settings: {
			"vue-i18n": {
				localeDir: "./src/i18n/de.json",
				messageSyntaxVersion: "^9.0.0",
			},
		},
	},

	{
		files: ["**/*.{js,mjs,cjs,ts,vue}"],
		languageOptions: {
			ecmaVersion: "latest",
			sourceType: "module",
			globals: {
				...globals.browser,
			},
			parserOptions: {
				// Tells the Vue parser to use TypeScript for <script lang="ts"> blocks
				parser: tseslint.parser,
				extraFileExtensions: [".vue"],
				ecmaFeatures: { jsx: false },
			},
		},
		rules: {
			indent: "off",
			"vue/no-mutating-props": [
				"error",
				{
					shallowOnly: true,
				},
			],
			"@intlify/vue-i18n/no-missing-keys": "error",
			"@intlify/vue-i18n/no-raw-text": [
				"error",
				{
					ignorePattern: "^.{0,3}$",
				},
			],
		},
	},

	eslintConfigPrettier,
]
