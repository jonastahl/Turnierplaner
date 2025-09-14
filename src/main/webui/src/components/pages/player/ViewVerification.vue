<template>
	<div id="container">
		<h2 v-if="isSuccess">
			{{ t("general.success") }}
		</h2>
		<h2 v-else-if="isError">
			{{ t("general.failure") }}
		</h2>
		<p v-if="isSuccess">
			{{ t("ViewPlayerRegistration.verification_success") }}
		</p>
		<p v-else-if="isError">
			{{ t("ViewPlayerRegistration.verification_failed") }}
		</p>
	</div>
</template>

<script lang="ts" setup>
import { useI18n } from "vue-i18n"
import { useVerify } from "@/backend/registration"
import { useToast } from "primevue/usetoast"

const { t } = useI18n()
const toast = useToast()

const { mutate: verify, isSuccess, isError } = useVerify()

let verificationCode = new URL(location.href).searchParams.get("code")
if (!verificationCode) {
	toast.add({
		severity: "error",
		summary: t("ViewPlayerRegistration.registration_failed"),
		detail: t("ViewPlayerRegistration.registration_failed_detail"),
		life: 3000,
	})
} else {
	verify(verificationCode)
}
</script>

<style scoped>
#container {
	width: 100%;
	display: flex;
	flex-direction: column;
	align-items: center;
}
</style>
