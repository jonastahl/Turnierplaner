import { ToastServiceMethods } from "primevue/toastservice"
import { computed, Ref } from "vue"
import axios, { AxiosResponse } from "axios"
import {
	Competition,
	CompetitionDefault,
	CompetitionServer,
	competitionServerToClient,
} from "@/interfaces/competition"
import { RouteLocationNormalizedLoaded } from "vue-router"
import { Player, playerClientToServer } from "@/interfaces/player"
import { TeamServer } from "@/interfaces/team"
import {
	QueryClient,
	useMutation,
	useQuery,
	useQueryClient,
} from "@tanstack/vue-query"
import { TranslateFunction } from "@/main"

export function getCompetitionsList(
	route: RouteLocationNormalizedLoaded,
	isLoggedIn: Ref<boolean>,
	t: TranslateFunction,
	toast: ToastServiceMethods,
) {
	return useQuery({
		queryKey: [
			"competitionList",
			computed(() => route.params.tourId),
			isLoggedIn,
		],
		queryFn: async () => {
			return axios
				.get<CompetitionServer[]>(
					`/tournament/${route.params.tourId}/competition/list`,
				)
				.then((response) => {
					return response.data
						.map(competitionServerToClient)
						.sort((comp1, comp2) => comp1.name.localeCompare(comp2.name))
				})
				.catch((error) => {
					toast.add({
						severity: "error",
						summary: t("competition.action.load.failed"),
						detail: error,
						life: 3000,
					})
					console.log(error)
					throw error
				})
		},
	})
}

export function getCompetitionDetails(
	route: RouteLocationNormalizedLoaded,
	t: TranslateFunction,
	toast: ToastServiceMethods,
) {
	return getCompetitionDetailsCustom(
		route,
		t,
		computed(() => <string>route.params.compId),
		toast,
	)
}
export function getCompetitionDetailsCustom(
	route: RouteLocationNormalizedLoaded,
	t: TranslateFunction,
	compId: Ref<string>,
	toast: ToastServiceMethods,
) {
	return useQuery({
		queryKey: [
			"competitionDetails",
			computed(() => route.params.tourId),
			compId,
		],
		queryFn: async (): Promise<Competition> => {
			if (!route.params.tourId || !route.params.compId)
				return CompetitionDefault

			return axios
				.get<CompetitionServer>(
					`/tournament/${route.params.tourId}/competition/${route.params.compId}/details`,
				)
				.then((response) => {
					return competitionServerToClient(response.data)
				})
				.catch((error) => {
					toast.add({
						severity: "error",
						summary: t("tournament.details.failed"),
						detail: error,
						life: 3000,
					})
					console.log(error)
					throw error
				})
		},
	})
}

export function useUpdateCompetition(
	route: RouteLocationNormalizedLoaded,
	t: TranslateFunction,
	toast: ToastServiceMethods,
	handler: {
		suc?: (competition: CompetitionServer) => void
		err?: () => void
	},
) {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: (competition: CompetitionServer) =>
			axios.post(
				`/tournament/${<string>route.params.tourId}/competition/update`,
				competition,
			),
		onSuccess(_, competition) {
			toast.add({
				severity: "success",
				summary: t("competition.action.update.label"),
				detail: t("competition.action.update.success"),
				life: 3000,
			})

			Promise.all([
				queryClient.invalidateQueries({
					queryKey: ["competitionList"],
					refetchType: "all",
				}),
				queryClient.invalidateQueries({
					queryKey: [
						"competitionDetails",
						route.params.tourId,
						competition.name,
					],
					refetchType: "all",
				}),
			]).then(() => {
				if (handler.suc) handler.suc(competition)
			})
		},
		onError(error) {
			toast.add({
				severity: "error",
				summary: t("competition.action.update.failed"),
				detail: error,
				life: 3000,
			})
			if (handler.err) handler.err()
		},
	})
}

export function useDeleteCompetition(
	route: RouteLocationNormalizedLoaded,
	t: TranslateFunction,
	toast: ToastServiceMethods,
) {
	const queryClient = useQueryClient()
	return useMutation({
		// eslint-disable-next-line @typescript-eslint/no-unused-vars
		mutationFn: (_: void) =>
			axios.delete(
				`/tournament/${<string>route.params.tourId}/competition/${<string>route.params.compId}/delete`,
			),
		onSuccess() {
			Promise.all([
				queryClient.invalidateQueries({
					queryKey: ["competitionList"],
					refetchType: "all",
				}),
				queryClient.invalidateQueries({
					queryKey: [
						"competitionDetails",
						route.params.tourId,
						route.params.compId,
					],
					refetchType: "none",
				}),
			]).then(() => {
				toast.add({
					severity: "success",
					summary: t("general.success"),
					detail: t("competition.action.delete.success"),
					life: 3000,
				})
			})
		},
		onError() {
			toast.add({
				severity: "error",
				summary: t("general.failure"),
				detail: t("general.failure"),
				life: 3000,
			})
		},
	})
}

export function useAddCompetition(
	route: RouteLocationNormalizedLoaded,
	t: TranslateFunction,
	toast: ToastServiceMethods,
	handler: {
		suc?: () => void
		err?: () => void
	},
) {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: (competition: CompetitionServer) =>
			axios.post(
				`/tournament/${<string>route.params.tourId}/competition/add`,
				competition,
			),
		onSuccess() {
			toast.add({
				severity: "success",
				summary: t("competition.action.create.label"),
				detail: t("competition.action.create.success"),
				life: 3000,
			})
			queryClient
				.invalidateQueries({
					queryKey: ["competitionList", route.params.tourId],
					refetchType: "all",
				})
				.then(() => {
					if (handler.suc) handler.suc()
				})
		},
		onError() {
			toast.add({
				severity: "error",
				summary: t("competition.action.create.label"),
				detail: t("competition.action.create.failed"),
				life: 3000,
			})
			if (handler.err) handler.err()
		},
	})
}

export function useSignUpSingle(
	route: RouteLocationNormalizedLoaded,
	t: TranslateFunction,
	toast: ToastServiceMethods,
	queryClient: QueryClient,
) {
	return useMutation({
		mutationFn: async (data: {
			player: Ref<Player | undefined>
			playerB: boolean
		}) => {
			if (!data.player.value) {
				toast.add({
					severity: "error",
					summary: t("competition.action.signup.warning.no_player_selected"),
					detail: t("competition.action.signup.warning.select_first"),
					life: 3000,
				})
				throw new Error("No player selected")
			}

			const form: TeamServer = {
				playerA: null,
				playerB: null,
			}
			if (!data.playerB) form.playerA = playerClientToServer(data.player.value)
			else form.playerB = playerClientToServer(data.player.value)

			return axios.post<void>(
				`/tournament/${route.params.tourId}/competition/${route.params.compId}/signUp`,
				form,
			)
		},
		...signUpOptions(route, t, toast, queryClient),
	})
}

export function useSignUpDoubleTog(
	route: RouteLocationNormalizedLoaded,
	t: TranslateFunction,
	toast: ToastServiceMethods,
	queryClient: QueryClient,
) {
	return useMutation({
		mutationFn: async (data: {
			playerA: Ref<Player | undefined>
			playerB: Ref<Player | undefined>
		}) => {
			if (!data.playerA.value || !data.playerB.value) {
				toast.add({
					severity: "error",
					summary: t("competition.action.signup.warning.no_player_selected"),
					detail: t("competition.action.signup.warning.select_first"),
					life: 3000,
				})
				throw new Error("No player selected")
			}

			const form = {
				playerA: playerClientToServer(data.playerA.value),
				playerB: playerClientToServer(data.playerB.value),
			}

			return axios.post<void>(
				`/tournament/${route.params.tourId}/competition/${route.params.compId}/signUp`,
				form,
			)
		},
		...signUpOptions(route, t, toast, queryClient, true),
	})
}

function signUpOptions(
	route: RouteLocationNormalizedLoaded,
	t: TranslateFunction,
	toast: ToastServiceMethods,
	queryClient: QueryClient,
	team?: boolean,
) {
	return {
		onSuccess() {
			queryClient.invalidateQueries({
				queryKey: ["signedUp", route.params.tourId, route.params.compId],
				refetchType: "all",
			})
			toast.add({
				severity: "success",
				summary: team
					? t("competition.action.signup.success")
					: t("competition.action.signup.success_team"),
				life: 3000,
			})
		},
		onError(error: { response: { status: number } }) {
			// TODO checks for team already partially registered also in backend
			if (error.response.status === 409)
				toast.add({
					severity: "error",
					summary: t("competition.action.signup.conflict"),
					life: 3000,
				})
			else
				toast.add({
					severity: "error",
					summary: t("competition.action.signup.label"),
					detail: t("competition.action.signup.failed"),
					life: 3000,
				})
		},
	}
}

export function useResetPreparation(
	route: RouteLocationNormalizedLoaded,
	t: TranslateFunction,
	toast: ToastServiceMethods,
) {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: () =>
			axios.post(
				`/tournament/${<string>route.params.tourId}/competition/${<string>route.params.compId}/resetPreparation`,
			),
		onSuccess() {
			toast.add({
				severity: "success",
				summary: t("general.success"),
				detail: t("general.action.save.success"),
				life: 3000,
			})
			queryClient.invalidateQueries({
				queryKey: ["competitionList"],
				refetchType: "all",
			})
			queryClient.invalidateQueries({
				queryKey: [
					"competitionDetails",
					route.params.tourId,
					route.params.compId,
				],
				refetchType: "all",
			})
			queryClient.invalidateQueries({
				queryKey: ["knockout", route.params.tourId, route.params.compId],
				refetchType: "all",
			})
			queryClient.invalidateQueries({
				queryKey: ["groupsDivision", route.params.tourId, route.params.compId],
				refetchType: "all",
			})
		},
		onError(error) {
			toast.add({
				severity: "error",
				summary: t("general.failure"),
				detail: t("general.failure"),
				life: 3000,
			})
			console.log(error)
		},
	})
}

export function useReopenSchedule(
	route: RouteLocationNormalizedLoaded,
	t: TranslateFunction,
	toast: ToastServiceMethods,
) {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: (compName: string) =>
			axios.post(
				`/tournament/${<string>route.params.tourId}/competition/${compName}/reopenSchedule`,
			),
		onSuccess(_, compName: string) {
			console.log("im invalidating", [
				"competitionDetails",
				route.params.tourId,
				compName,
			])
			queryClient.invalidateQueries({
				queryKey: ["competitionList"],
				refetchType: "all",
			})
			queryClient.removeQueries({
				queryKey: ["competitionDetails", route.params.tourId, compName],
			})
		},
		onError(error) {
			toast.add({
				severity: "error",
				summary: t("general.failure"),
				detail: t("general.failure"),
				life: 3000,
			})
			console.log(error)
		},
	})
}

export function usePublishCompetitions(
	route: RouteLocationNormalizedLoaded,
	t: TranslateFunction,
	toast: ToastServiceMethods,
) {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: (competitions: string[]) =>
			axios.post(
				`/tournament/${<string>route.params.tourId}/competition/publish`,
				{ competitions },
			),
		onSuccess(data: AxiosResponse, competitions: string[]) {
			let message
			if (data.status == 200) {
				if (competitions.length === 1) {
					message = "competition.action.publish.preparation.all"
				} else {
					message = "competition.action.publish.preparation.single"
				}
			} else {
				if (competitions.length === 1) {
					message = "competition.action.publish.preparation.all_missing"
				} else {
					message = "competition.action.publish.preparation.single_missing"
				}
			}
			toast.add({
				severity: data.status == 200 ? "success" : "warn",
				summary: t("general.success"),
				detail: t(message),
				life: 3000,
			})

			queryClient.invalidateQueries({
				queryKey: ["competitionList"],
				refetchType: "all",
			})
			for (const comp of competitions) {
				queryClient.invalidateQueries({
					queryKey: ["competitionDetails", route.params.tourId, comp],
					refetchType: "all",
				})
			}
		},
		onError(error) {
			toast.add({
				severity: "error",
				summary: t("general.failure"),
				detail: t("general.failure"),
				life: 3000,
			})
			console.log(error)
		},
	})
}
