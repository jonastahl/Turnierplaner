import { computed, Ref } from "vue"
import axios from "axios"
import {
	Tournament,
	TournamentServer,
	tournamentServerToClient,
} from "@/interfaces/tournament"
import { ToastServiceMethods } from "primevue/toastservice"
import { RouteLocationNormalizedLoaded } from "vue-router"
import { useMutation, useQuery, useQueryClient } from "@tanstack/vue-query"

export function getTournamentList(
	isLoggedIn: Ref<boolean>,
	t: (s: string) => string,
	toast: ToastServiceMethods,
) {
	return useQuery({
		queryKey: ["tournamentList", isLoggedIn],
		queryFn: async () => {
			return axios
				.get<TournamentServer[]>("/tournament/list")
				.then<Tournament[]>((response) => {
					return response.data.map(tournamentServerToClient)
				})
				.catch((error) => {
					toast.add({
						severity: "error",
						summary: t("ViewTournaments.loadingFailed"),
						detail: error,
						life: 3000,
					})
					console.log(error)
					throw error
				})
		},
	})
}

export function getTournamentDetails(
	route: RouteLocationNormalizedLoaded,
	t: (s: string) => string,
	toast: ToastServiceMethods,
) {
	return useQuery({
		enabled: computed(() => !!route.params.tourId),
		queryKey: ["tournament", computed(() => route.params.tourId)],
		queryFn: async () => {
			return axios
				.get<TournamentServer>(`/tournament/${route.params.tourId}/details`)
				.then<Tournament>((response) => {
					return tournamentServerToClient(response.data)
				})
				.catch((error) => {
					toast.add({
						severity: "error",
						summary: t("ViewEditTournament.loadingDetailsFailed"),
						detail: error,
						life: 3000,
					})
					console.log(error)
					throw error
				})
		},
	})
}

export function useUpdateTournament(
	t: (s: string) => string,
	toast: ToastServiceMethods,
	handler: {
		suc?: () => void
		err?: () => void
	},
) {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: (tournament: TournamentServer) =>
			axios.post("/tournament/update", tournament),
		onSuccess(__, tournament) {
			toast.add({
				severity: "success",
				summary: t("ViewEditTournament.tournamentUpdating"),
				detail: t("ViewEditTournament.tournamentUpdated"),
				life: 3000,
			})
			Promise.all([
				queryClient.invalidateQueries({
					queryKey: ["tournamentList"],
					refetchType: "all",
				}),
				queryClient.invalidateQueries({
					queryKey: ["tournament", tournament.name],
					refetchType: "all",
				}),
			]).then(() => {
				if (handler.suc) handler.suc()
			})
		},
		onError(error) {
			toast.add({
				severity: "error",
				summary: t("ViewEditTournament.tournamentUpdateFailed"),
				detail: error,
				life: 3000,
			})
			if (handler.err) handler.err()
		},
	})
}

export function useAddTournament(
	t: (s: string) => string,
	toast: ToastServiceMethods,
	handler: {
		suc?: () => void
		err?: () => void
	},
) {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: (tournament: TournamentServer) =>
			axios.post("/tournament/add", tournament),

		onSuccess() {
			queryClient.invalidateQueries({
				queryKey: ["tournamentList"],
				refetchType: "all",
			})
			toast.add({
				severity: "success",
				summary: t("ViewCreateTournament.tournamentCreating"),
				detail: t("ViewCreateTournament.tournamentCreated"),
				life: 3000,
				closable: false,
			})
			if (handler.suc) handler.suc()
		},
		onError() {
			toast.add({
				severity: "error",
				summary: t("ViewCreateTournament.tournamentCreating"),
				detail: t("ViewCreateTournament.tournamentCreationFailed"),
				life: 3000,
				closable: false,
			})
			if (handler.err) handler.err()
		},
	})
}
