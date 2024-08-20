import { ToastServiceMethods } from "primevue/toastservice"
import { computed, Ref } from "vue"
import axios from "axios"
import {
	CompetitionServer,
	competitionServerToClient,
} from "@/interfaces/competition"
import { RouteLocationNormalizedLoaded } from "vue-router"
import { useQuery } from "vue-query/esm"
import { QueryClient, useMutation, useQueryClient } from "vue-query"
import { Player, playerClientToServer } from "@/interfaces/player"
import { TeamServer } from "@/interfaces/team"

export function getCompetitionsList(
	route: RouteLocationNormalizedLoaded,
	isLoggedIn: Ref<boolean>,
	t: (s: string) => string,
	toast: ToastServiceMethods,
	handler: {
		suc?: () => void
		err?: () => void
	},
) {
	return useQuery(
		["competitionList", computed(() => route.params.tourId), isLoggedIn],
		async () => {
			return axios
				.get<
					CompetitionServer[]
				>(`/tournament/${route.params.tourId}/competition/list`)
				.then((response) => {
					return response.data.map(competitionServerToClient)
				})
		},
		{
			onSuccess() {
				if (handler.suc) handler.suc()
			},
			onError(error) {
				toast.add({
					severity: "error",
					summary: t("ViewCompetitions.loadingFailed"),
					detail: error,
					life: 3000,
				})
				console.log(error)
				if (handler.err) handler.err()
			},
		},
	)
}

export function getCompetitionDetails(
	route: RouteLocationNormalizedLoaded,
	t: (s: string) => string,
	toast: ToastServiceMethods,
	handler: {
		suc?: () => void
		err?: () => void
	},
) {
	return useQuery(
		[
			"competitionDetails",
			computed(() => route.params.tourId),
			computed(() => route.params.compId),
		],
		async () => {
			return axios
				.get<CompetitionServer>(
					`/tournament/${route.params.tourId}/competition/${route.params.compId}/details`,
				)
				.then((response) => {
					return competitionServerToClient(response.data)
				})
		},
		{
			onSuccess() {
				if (handler.suc) handler.suc()
			},
			onError(error) {
				toast.add({
					severity: "error",
					summary: t("ViewEditCompetition.loadingDetailsFailed"),
					detail: error,
					life: 3000,
				})
				console.log(error)
				if (handler.err) handler.err()
			},
			keepPreviousData: true,
		},
	)
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
	return useMutation(
		(competition: CompetitionServer) =>
			axios.post(
				`/tournament/${<string>route.params.tourId}/competition/update`,
				competition,
			),
		{
			onSuccess(_, competition) {
				toast.add({
					severity: "success",
					summary: t("ViewEditCompetition.competitionUpdate"),
					detail: t("ViewEditCompetition.competitionUpdated"),
					life: 3000,
				})

				Promise.all([
					queryClient.invalidateQueries([
						"competitionList",
						route.params.tourId,
						true,
					]),
					queryClient.invalidateQueries([
						"competitionList",
						route.params.tourId,
						false,
					]),
					queryClient.invalidateQueries([
						"competitionDetails",
						route.params.tourId,
						competition.id,
					]),
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
		},
	)
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
	return useMutation(
		(competition: CompetitionServer) =>
			axios.post(
				`/tournament/${<string>route.params.tourId}/competition/add`,
				competition,
			),
		{
			onSuccess() {
				toast.add({
					severity: "success",
					summary: t("ViewCreateCompetition.competitionCreation"),
					detail: t("ViewCreateCompetition.competitionCreated"),
					life: 3000,
					closable: false,
				})
				queryClient.invalidateQueries([
					"competitionList",
					route.params.tourId,
					true,
				])
				queryClient.invalidateQueries([
					"competitionList",
					route.params.tourId,
					false,
				])
				if (handler.suc) handler.suc()
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
		},
	)
}

export function useSignUpSingle(
	route: RouteLocationNormalizedLoaded,
	t: (s: string) => string,
	toast: ToastServiceMethods,
	queryClient: QueryClient,
) {
	return useMutation(
		async (data: { player: Ref<Player | null>; playerB: boolean }) => {
			if (!data.player.value) {
				toast.add({
					severity: "error",
					summary: t("ViewSignUp.noPlayerSelected"),
					detail: t("ViewSignUp.selectPlayerFirst"),
					life: 3000,
				})
				return
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
		signUpOptions(route, t, toast, queryClient),
	)
}

export function useSignUpDoubleTog(
	route: RouteLocationNormalizedLoaded,
	t: (s: string) => string,
	toast: ToastServiceMethods,
	queryClient: QueryClient,
) {
	return useMutation(
		async (data: {
			playerA: Ref<Player | null>
			playerB: Ref<Player | null>
		}) => {
			if (!data.playerA.value || !data.playerB.value) {
				toast.add({
					severity: "error",
					summary: t("ViewSignUp.noPlayerSelected"),
					detail: t("ViewSignUp.selectPlayerFirst"),
					life: 3000,
				})
				return
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
		signUpOptions(route, t, toast, queryClient),
	)
}

function signUpOptions(
	route: RouteLocationNormalizedLoaded,
	t: (s: string) => string,
	toast: ToastServiceMethods,
	queryClient: QueryClient,
) {
	return {
		onSuccess() {
			queryClient.invalidateQueries([
				"signedUp",
				route.params.tourId,
				route.params.compId,
			])
			toast.add({
				severity: "success",
				summary: t("Player.register_success"),
				life: 3000,
			})
		},
		onError(error: { response: { status: number } }) {
			// TODO checks for team already partially registered also in backend
			if (error.response.status === 409)
				toast.add({
					severity: "error",
					summary: t("Player.already_exists"),
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
