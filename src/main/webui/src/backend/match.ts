import { useMutation, useQuery, useQueryClient } from "@tanstack/vue-query"
import {
	AnnotatedMatch,
	AnnotatedMatchServer,
	annotatedMatchServerToClient,
	Match,
	matchClientToServer,
} from "@/interfaces/match"
import axios from "axios"
import { RouteLocationNormalizedLoaded } from "vue-router"
import { ToastServiceMethods } from "primevue/toastservice"
import { computed, Ref } from "vue"
import { v4 as uuidv4 } from "uuid"
import { CompType } from "@/interfaces/competition"
import { knockoutTitle } from "@/components/pages/competition/results/knockout/KnockoutTitleGenerator"

export function useUpdateMatches(
	route: RouteLocationNormalizedLoaded,
	t: (s: string) => string,
	toast: ToastServiceMethods,
) {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: (data: { complete: boolean; matches: Match[] }) =>
			axios.post(
				`/tournament/${<string>route.params.tourId}/competition/${<string>route.params.compId}/updateSchedule`,
				{
					complete: data.complete,
					data: data.matches.map(matchClientToServer),
				},
			),
		onSuccess() {
			toast.add({
				severity: "success",
				summary: t("general.success"),
				detail: t("general.saved"),
				life: 3000,
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
		},
		onError(error) {
			toast.add({
				severity: "error",
				summary: t("general.failure"),
				detail: t("general.error_saving"),
				life: 3000,
			})
			console.log(error)
		},
	})
}

export function getTournamentScheduledMatches(
	route: RouteLocationNormalizedLoaded,
	from: Ref<Date | undefined>,
	to: Ref<Date | undefined>,
) {
	return useQuery({
		enabled: computed(() => !!from.value && !!to.value),
		queryKey: [
			"tournamentScheduledMatches",
			computed(() => route.params.playerId),
			from,
			to,
		],
		queryFn: async () => {
			return axios
				.get(`/matches`, {
					params: {
						tour: route.params.tourId,
						from: from.value,
						to: to.value,
					},
				})
				.then<AnnotatedMatchServer[]>((data) => data.data)
				.then<AnnotatedMatch[]>((matches) => {
					return matches.map(annotatedMatchServerToClient)
				})
		},
		placeholderData: (data) => data,
	})
}

export function getPlayerScheduledMatches(
	route: RouteLocationNormalizedLoaded,
	from: Ref<Date | undefined>,
	to: Ref<Date | undefined>,
) {
	return useQuery({
		enabled: computed(() => !!route.params.playerId),
		queryKey: [
			"playerScheduledMatches",
			computed(() => route.params.playerId),
			from,
			to,
		],
		queryFn: async () => {
			return axios
				.get(`/matches`, {
					params: {
						player: route.params.playerId,
						from: from.value,
						to: to.value,
					},
				})
				.then<AnnotatedMatchServer[]>((data) => data.data)
				.then<AnnotatedMatch[]>((matches) => {
					return matches.map(annotatedMatchServerToClient)
				})
		},
		placeholderData: (data) => data,
	})
}

export function getAllScheduledMatches(
	from: Ref<Date | undefined>,
	to: Ref<Date | undefined>,
) {
	return useQuery({
		queryKey: ["scheduledMatches", from, to],
		queryFn: async () => {
			return axios
				.get(`/matches`, {
					params: {
						from: from.value,
						to: to.value,
					},
				})
				.then<AnnotatedMatchServer[]>((data) => data.data)
				.then<AnnotatedMatch[]>((matches) => {
					return matches.map(annotatedMatchServerToClient)
				})
		},
		placeholderData: (data) => data,
	})
}

export function getAllMatchesEventsExceptCompetition(
	route: RouteLocationNormalizedLoaded,
	from: Ref<Date | undefined>,
	to: Ref<Date | undefined>,
) {
	const matches = getAllScheduledMatches(from, to)

	return {
		...matches,
		data: computed(() => {
			if (!matches.data.value) return undefined
			return matches.data.value
				.filter((match) => match.compName !== route.params.compId)
				.map(matchToEvent)
		}),
	}
}

export function getAllMatchesEvents(
	from: Ref<Date | undefined>,
	to: Ref<Date | undefined>,
) {
	const matches = getAllScheduledMatches(from, to)

	return {
		...matches,
		data: computed(() => {
			if (!matches.data.value) return undefined
			return matches.data.value.map(matchToEvent)
		}),
	}
}

export function matchToEvent(match: AnnotatedMatch) {
	return {
		id: match.id || uuidv4(),
		start: match.begin ?? new Date(),
		end: match.end ?? new Date(),
		split: match.court ?? "undefined court",
		data: match,
	}
}

export function genTitle(
	title: AnnotatedMatch["title"],
	t: (_: string) => string,
) {
	switch (title.type) {
		case CompType.GROUPS:
			return t("ViewGroupSystem.group") + " " + (title.number + 1)
		case CompType.KNOCKOUT:
			if (!title.isFinal) return t("ViewKnockout.thirdPlace")
			else return knockoutTitle(t)(title.number, title.total)
	}
}
