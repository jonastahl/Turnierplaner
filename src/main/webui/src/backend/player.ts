import axios from "axios"
import {
	Player,
	playerClientToServer,
	PlayerDetails,
	playerDetailsClientToServer,
	PlayerDetailsServer,
	playerDetailsServerToClient,
	PlayerRegistrationForm,
	PlayerServer,
	playerServerToClient,
} from "@/interfaces/player"
import { ToastServiceMethods } from "primevue/toastservice"
import { computed, Ref } from "vue"
import { useMutation, useQuery, useQueryClient } from "@tanstack/vue-query"
import { RouteLocationNormalizedLoaded } from "vue-router"
import { Page } from "@/interfaces/general"

export function findCompPlayers(
	search: Ref<string>,
	route: RouteLocationNormalizedLoaded,
	playerB: boolean,
	t: (s: string) => string,
	toast: ToastServiceMethods,
) {
	return useQuery({
		queryKey: [
			"searchCompPlayer",
			search,
			computed(() => route.params.tourId),
			computed(() => route.params.compId),
			playerB,
		],
		queryFn: async () => {
			return axios
				.get<PlayerServer[]>(
					`/player/compFind/${route.params.tourId}/${route.params.compId}`,
					{ params: { search: search.value.toLowerCase(), playerB: playerB } },
				)
				.then<Player[]>((result) => result.data.map(playerServerToClient))
				.catch((error) => {
					toast.add({
						severity: "error",
						summary: t("ViewCompetition.query_search_failed"),
						detail: error,
						life: 3000,
					})
					console.log(error)
					throw error
				})
		},
	})
}

export function findPlayers(
	search: Ref<string>,
	page: Ref<number>,
	pageSize: Ref<number>,
	t: (s: string) => string,
	toast: ToastServiceMethods,
) {
	return useQuery({
		queryKey: ["searchPlayer", search, page, pageSize],
		queryFn: async () => {
			return axios
				.get<Page<PlayerServer[]>>(
					`/player/find?search=${search.value.toLowerCase()}&page=${page.value}&pageSize=${pageSize.value}`,
				)
				.then<Page<Player[]>>((result) => {
					return {
						total: result.data.total,
						data: result.data.data.map(playerServerToClient),
					}
				})
				.catch((error) => {
					toast.add({
						severity: "error",
						summary: t("ViewCompetition.query_search_failed"),
						detail: error,
						life: 3000,
					})
					console.log(error)
					throw error
				})
		},
	})
}

export function getUnverified(
	t: (s: string) => string,
	toast: ToastServiceMethods,
) {
	return useQuery({
		queryKey: ["unverifiedPlayer"],
		queryFn: async () => {
			return axios
				.get<PlayerServer[]>(`/player/listUnverified`)
				.then<Player[]>((result) => result.data.map(playerServerToClient))
				.catch((error) => {
					toast.add({
						severity: "error",
						summary: t("ViewCompetition.query_search_failed"),
						detail: error,
						life: 3000,
					})
					console.log(error)
					throw error
				})
		},
	})
}

export function useAdminVerify() {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: async (player: Player) =>
			axios.post(`player/adminVerify`, playerClientToServer(player)),
		onSuccess() {
			queryClient.invalidateQueries({
				queryKey: ["unverifiedPlayer"],
			})
		},
	})
}

export function useDeletePlayer() {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: async (player: Player) =>
			axios.post(`player/delete`, playerClientToServer(player)),
		onSuccess() {
			queryClient.invalidateQueries({
				queryKey: ["unverifiedPlayer"],
			})
		},
	})
}

export function getPlayer(
	route: RouteLocationNormalizedLoaded,
	t: (s: string) => string,
	toast: ToastServiceMethods,
) {
	return useQuery({
		queryKey: ["player", computed(() => route.params.playerId)],
		queryFn: async () => {
			return axios
				.get<PlayerServer>(`/player/get/${route.params.playerId}`)
				.then<Player>((result) => playerServerToClient(result.data))
				.catch((error) => {
					toast.add({
						severity: "error",
						summary: t("Player.player_not_found"),
						detail: error,
						life: 3000,
					})
					console.log(error)
					throw error
				})
		},
	})
}

export function getPlayerDetails(
	route: RouteLocationNormalizedLoaded,
	t: (s: string) => string,
	toast: ToastServiceMethods,
) {
	return useQuery({
		queryKey: ["playerDetails", computed(() => route.params.playerId)],
		queryFn: async () => {
			return axios
				.get<PlayerDetailsServer>(
					`/player/get/${route.params.playerId}/details`,
				)
				.then<PlayerDetails>((result) =>
					playerDetailsServerToClient(result.data),
				)
				.catch((error) => {
					toast.add({
						severity: "error",
						summary: t("Player.player_not_found"),
						detail: error,
						life: 3000,
					})
					console.log(error)
					throw error
				})
		},
	})
}

export function useUpdatePlayerDetails(
	t: (s: string) => string,
	toast: ToastServiceMethods,
) {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: async (player: PlayerDetails) => {
			return axios.post(`/player/update`, playerDetailsClientToServer(player))
		},
		onSuccess(_, player) {
			toast.add({
				severity: "success",
				summary: t("settings.player_updated"),
				life: 3000,
			})
			queryClient.invalidateQueries({
				queryKey: ["player", player.id],
			})
			queryClient.invalidateQueries({
				queryKey: ["playerDetails", player.id],
			})
			queryClient.invalidateQueries({
				queryKey: ["searchCompPlayer", player.id],
			})
			queryClient.invalidateQueries({
				queryKey: ["searchPlayer", player.id],
			})
		},
	})
}

export const PlayerDefault: PlayerRegistrationForm = {
	firstName: "",
	lastName: "",
	sex: undefined,
	birthday: undefined,
	email: "",
	phone: "",
}
