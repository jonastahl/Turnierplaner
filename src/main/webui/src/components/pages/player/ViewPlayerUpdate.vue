<template>
	<div class="flex justify-content-center w-full">
		<Card id="card">
			<template #title>{{ t("settings.player_update_headline") }}</template>
			<template #content>
				<FormPlayer
					v-if="isLoading || !player"
					:disabled="disabled"
					:player="PlayerDefault"
				/>
				<FormPlayer
					v-else
					ref="form"
					:disabled="disabled"
					:player="player"
					@submit="reg"
				/>
			</template>
			<template #footer>
				<div class="justify-content-end flex">
					<Button
						:label="t('general.update')"
						:disabled="disabled"
						@click="() => form !== null && form.onSubmit()"
					></Button>
				</div>
			</template>
		</Card>
	</div>
</template>

<script lang="ts" setup>
import { useI18n } from "vue-i18n"
import { useToast } from "primevue/usetoast"
import { PlayerRegistration } from "@/interfaces/player"
import { ref } from "vue"
import FormPlayer from "@/components/pages/forms/FormPlayer.vue"
import {
	getPlayerDetails,
	PlayerDefault,
	useUpdatePlayerDetails,
} from "@/backend/player"
import { useRoute } from "vue-router"

const route = useRoute()
const { t } = useI18n()
const toast = useToast()
const form = ref<InstanceType<typeof FormPlayer> | null>(null)

const { mutate: register, isPending: disabled } = useUpdatePlayerDetails(
	t,
	toast,
)
const { data: player, isLoading } = getPlayerDetails(route, t, toast)

function reg(playerF: PlayerRegistration) {
	if (!player.value) return
	register({
		...playerF,
		id: player.value.id,
	})
}
</script>

<style scoped>
#card {
	width: min(90dvw, 50rem);
}
</style>
