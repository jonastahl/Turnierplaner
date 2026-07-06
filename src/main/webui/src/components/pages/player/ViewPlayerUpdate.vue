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
				<div class="justify-content-between flex">
					<Button
						:label="t('general.delete')"
						severity="danger"
						:disabled="disabled"
						@click="askDeletePlayer"
					></Button>
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
	useDeletePlayer,
	useUpdatePlayerDetails,
} from "@/backend/player"
import { useRoute, useRouter } from "vue-router"
import { useConfirm } from "primevue/useconfirm"

const route = useRoute()
const { t } = useI18n()
const toast = useToast()
const confirm = useConfirm()
const form = ref<InstanceType<typeof FormPlayer> | null>(null)

const { mutate: register, isPending: disabled } = useUpdatePlayerDetails(
	t,
	toast,
)
const { data: player, isLoading } = getPlayerDetails(route, t, toast)
const { mutate: deletePlayer } = useDeletePlayer(t, toast)
const router = useRouter()

function reg(playerF: PlayerRegistration) {
	if (!player.value) return
	register({
		...playerF,
		id: player.value.id,
	})
}

function askDeletePlayer() {
	confirm.require({
		message: t("Player.confirm_delete"),
		header: t("Player.deleting_player"),
		icon: "pi pi-exclamation-triangle",
		rejectClass: "p-button-secondary p-button-outlined",
		rejectLabel: t("general.cancel"),
		acceptClass: "p-button-danger",
		acceptLabel: t("general.delete"),
		accept: () => {
			if (player.value)
				deletePlayer(player.value.id, {
					onSuccess: () => {
						router.back()
					},
				})
		},
	})
}
</script>

<style scoped>
#card {
	width: min(90dvw, 50rem);
}
</style>
