import { ToastServiceMethods } from "primevue/toastservice"
import axios from "axios"
import { PlayerRegistration } from "@/interfaces/player"
import { useMutation } from "@tanstack/vue-query"
import { TranslateFunction } from "@/main"

export function useRegisterPlayer(
	t: TranslateFunction,
	toast: ToastServiceMethods,
) {
	return useMutation({
		mutationFn: async (reg: PlayerRegistration) => {
			return axios.post("/player/register", {
				firstName: reg.firstName,
				lastName: reg.lastName,
				sex: reg.sex,
				birthday: dateToJson(reg.birthday),
				language: reg.language,
				email: reg.email,
			})
		},
		onSuccess() {
			toast.add({
				severity: "success",
				summary: t("player.action.create.label"),
				detail: t("player.action.create.success"),
				life: 3000,
			})
		},
		onError(error) {
			console.log(error)
			toast.add({
				severity: "error",
				summary: t("player.action.create.failed"),
				detail: t("player.action.create.failed_detail"),
				life: 3000,
			})
		},
	})
}

export function useVerify() {
	return useMutation({
		mutationFn: async (code: string) => {
			return axios.get(`/player/verify?code=${code}`)
		},
	})
}

function dateToJson(d: Date): string {
	return `${d.getFullYear()}-${d.getMonth() < 9 ? "0" : ""}${
		d.getMonth() + 1
	}-${d.getDate() < 10 ? "0" : ""}${d.getDate()}`
}
