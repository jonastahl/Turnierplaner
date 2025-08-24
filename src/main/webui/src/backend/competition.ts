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

export function getCompetitionsList(
	route: RouteLocationNormalizedLoaded,
	isLoggedIn: Ref<boolean>,
	t: (s: string) => string,
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
						summary: t("ViewCompetitions.loadingFailed"),
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
	t: (s: string) => string,
	toast: ToastServiceMethods,
) {
	return useQuery({
		queryKey: [
			"competitionDetails",
			computed(() => route.params.tourId),
			computed(() => route.params.compId),
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
						summary: t("ViewSettings.loadingDetailsFailed"),
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
	t: (s: string) => string,
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
				summary: t("ViewEditCompetition.competitionUpdate"),
				detail: t("ViewEditCompetition.competitionUpdated"),
				life: 3000,
			})

			Promise.all([
				queryClient.invalidateQueries({
					queryKey: ["competitionList"],
					refetchType: "all",
				}),
				queryClient.invalidateQueries({
					queryKey: ["competitionDetails", route.params.tourId, competition.id],
					refetchType: "all",
				}),
			]).then(() => {
				if (handler.suc) handler.suc(competition)
			})
		},
		onError(error) {
			toast.add({
				severity: "error",
				summary: t("ViewEditCompetition.tournamentUpdateFailed"),
				detail: error,
				life: 3000,
			})
			if (handler.err) handler.err()
		},
	})
}

export function useAddCompetition(
	route: RouteLocationNormalizedLoaded,
	t: (s: string) => string,
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
				summary: t("ViewCreateCompetition.competitionCreation"),
				detail: t("ViewCreateCompetition.competitionCreated"),
				life: 3000,
				closable: false,
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
				summary: t("ViewCreateCompetition.competitionCreation"),
				detail: t("ViewCreateCompetition.creationFailed"),
				life: 3000,
				closable: false,
			})
			if (handler.err) handler.err()
		},
	})
}

export function useSignUpSingle(
	route: RouteLocationNormalizedLoaded,
	t: (s: string) => string,
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
					summary: t("ViewSignUp.noPlayerSelected"),
					detail: t("ViewSignUp.selectPlayerFirst"),
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
	t: (s: string) => string,
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
					summary: t("ViewSignUp.noPlayerSelected"),
					detail: t("ViewSignUp.selectPlayerFirst"),
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
	t: (s: string) => string,
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
					? t("Player.register_success_team")
					: t("Player.register_success"),
				life: 3000,
			})
		},
		onError(error: { response: { status: number } }) {
			// TODO checks for team already partially registered also in backend
			if (error.response.status === 409)
				toast.add({
					severity: "error",
					summary: t("Player.registration_conflict"),
					life: 3000,
				})
			else
				toast.add({
					severity: "error",
					summary: t("Player.register_failed"),
					detail: error,
					life: 3000,
				})
		},
	}
}

export function useResetPreparation(
	route: RouteLocationNormalizedLoaded,
	t: (s: string) => string,
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
				detail: t("general.saved"),
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

export function usePublishCompetitions(
	route: RouteLocationNormalizedLoaded,
	t: (s: string) => string,
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
					message = "ViewPrepare.published_all"
				} else {
					message = "ViewPrepare.published_single"
				}
			} else {
				if (competitions.length === 1) {
					message = "ViewPrepare.published_all_missing"
				} else {
					message = "ViewPrepare.published_single_missing"
				}
			}
			toast.add({
				severity: data.status == 200 ? "success" : "warn",
				summary: t("general.success"),
				detail: t(message),
				life: 3000,
			})

			if (competitions.length === 1)
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
