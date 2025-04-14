import { RouteLocationNormalizedLoaded } from "vue-router"
import { computed, ref, Ref } from "vue"
import axios from "axios"
import {
	GroupSystem,
	GroupSystemServer,
	groupSystemServerToClient,
} from "@/interfaces/groupSystem"
import { ToastServiceMethods } from "primevue/toastservice"
import { GroupsDivision } from "@/interfaces/competition"
import { Team, teamClientToServer, teamServerToClient } from "@/interfaces/team"
import { useMutation, useQuery, useQueryClient } from "@tanstack/vue-query"

export function getGroup(
	route: RouteLocationNormalizedLoaded,
	load: Ref<boolean> = ref(true),
) {
	return useQuery({
		enabled: load,
		queryKey: [
			"group",
			computed(() => route.params.tourId),
			computed(() => route.params.compId),
		],
		queryFn: async () =>
			axios
				.get<GroupSystemServer>(
					`tournament/${route.params.tourId}/competition/${route.params.compId}/groupMatches`,
				)
				.then<GroupSystem>((response) =>
					groupSystemServerToClient(response.data),
				),
	})
}

export function getGroupsDivision(
	route: RouteLocationNormalizedLoaded,
	t: (s: string) => string,
	toast: ToastServiceMethods,
) {
	return useQuery({
		queryKey: [
			"groupsDivision",
			computed(() => route.params.tourId),
			computed(() => route.params.compId),
		],
		queryFn: async () => {
			return axios
				.get<GroupsDivision>(
					`tournament/${route.params.tourId}/competition/${route.params.compId}/groupsDivision`,
				)
				.then<Team[][]>(async (response) => {
					if (response.data.groups.length === 0) return [[], []]

					return response.data.groups.map((group) =>
						group.map((team) => teamServerToClient(team)),
					)
				})
				.catch((error) => {
					toast.add({
						severity: "error",
						summary: t("general.failure"),
						detail: t("general.loading_failed"),
						life: 3000,
						closable: false,
					})
					throw error
				})
		},
	})
}

export function useInitGroups(
	route: RouteLocationNormalizedLoaded,
	t: (s: string) => string,
	toast: ToastServiceMethods,
) {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: async (data: { complete: boolean; groups: Team[][] }) => {
			return axios.post<boolean>(
				`/tournament/${route.params.tourId}/competition/${route.params.compId}/initGroups`,
				{
					complete: data.complete,
					data: {
						groups: data.groups.map((group) =>
							group.map((t) => teamClientToServer(t)),
						),
					},
				},
			)
		},
		onSuccess() {
			queryClient.invalidateQueries({
				queryKey: ["groupsDivision", route.params.tourId, route.params.compId],
				refetchType: "all",
			})
			queryClient.invalidateQueries({
				queryKey: ["competitionList", route.params.tourId],
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
				queryKey: ["group", route.params.tourId, route.params.compId],
				refetchType: "all",
			})
			toast.add({
				severity: "success",
				summary: t("general.success"),
				detail: t("general.saved"),
				life: 3000,
				closable: false,
			})
		},
		onError() {
			toast.add({
				severity: "error",
				summary: t("general.failure"),
				detail: t("general.save_failed"),
				life: 3000,
				closable: false,
			})
		},
	})
}
