<template>
	<div class="flex justify-content-center w-full">
		<Card v-if="!isSuccess" id="card">
			<template #title>{{ t("ViewPlayerRegistration.headline") }}</template>
			<template #content>
				<FormPlayer
					ref="form"
					:disabled="disabled"
					:player="player"
					@submit="register"
				/>
			</template>
			<template #footer>
				<div class="justify-content-end flex">
					<Button
						:label="t('general.create')"
						:disabled="disabled"
						@click="() => form !== null && form.onSubmit()"
					></Button>
				</div>
			</template>
		</Card>
		<Card v-else id="card">
			<template #title>{{ t("general.success") }}</template>
			<template #content>
				<p>
					{{ t("ViewPlayerRegistration.after") }}
				</p>
			</template>
		</Card>
	</div>
</template>

<script lang="ts" setup>
import { useI18n } from "vue-i18n"
import { useToast } from "primevue/usetoast"
import { PlayerRegistrationForm } from "@/interfaces/player"
import { reactive, ref } from "vue"
import { useRegisterPlayer } from "@/backend/registration"
import FormPlayer from "@/components/pages/forms/FormPlayer.vue"
import { Language } from "@/interfaces/competition"

const { locale, t } = useI18n()
const toast = useToast()
const form = ref<InstanceType<typeof FormPlayer> | null>(null)

const {
	mutate: register,
	isPending: disabled,
	isSuccess,
} = useRegisterPlayer(t, toast)

const langMap: Record<string, Language> = {
	en: Language.EN,
	de: Language.DE,
}
const player = reactive<PlayerRegistrationForm>({
	firstName: "",
	lastName: "",
	sex: undefined,
	birthday: undefined,
	language: langMap[locale.value],
	email: "",
})
</script>

<style scoped>
#card {
	width: min(90dvw, 50rem);
}
</style>
