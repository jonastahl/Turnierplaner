import { useMutation, useQuery, useQueryClient } from "@tanstack/vue-query"
import {
	AnnotatedMatch,
	annotatedMatchServerToClient,
	Match,
	matchClientToServer,
} from "@/interfaces/match"
import axios from "axios"
import { RouteLocationNormalizedLoaded } from "vue-router"
import { ToastServiceMethods } from "primevue/toastservice"
import { computed, ref, Ref } from "vue"
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

export function getScheduledMatches(
	from: Ref<Date | undefined>,
	to: Ref<Date | undefined>,
	playerId?: Ref<string | undefined>,
	tourId?: Ref<string | undefined>,
) {
	return useQuery({
		queryKey: ["scheduledMatches", from, to, playerId, tourId],
		queryFn: async () => {
			return axios
				.get(`/matches`, {
					params: {
						player: playerId?.value,
						tour: tourId?.value,
						from: from.value,
						to: to.value,
					},
				})
				.then<AnnotatedMatch[]>((matches) =>
					matches.data.map(annotatedMatchServerToClient),
				)
		},
	})
}
export function getScheduledMatchesEvents(
	from: Ref<Date | undefined>,
	to: Ref<Date | undefined>,
	playerId?: Ref<string | undefined>,
	tourId?: Ref<string | undefined>,
) {
	const matches = getScheduledMatches(from, to, playerId, tourId)

	return {
		...matches,
		data: computed(() => {
			if (!matches.data.value) return undefined
			return matches.data.value.map(matchToEvent)
		}),
	}
}
export function getScheduledTournamentMatches(
	route: RouteLocationNormalizedLoaded,
	from: Ref<Date | undefined>,
	to: Ref<Date | undefined>,
) {
	return getScheduledMatches(
		from,
		to,
		ref(undefined),
		computed(() => <string>route.params.tourId),
	)
}
export function getScheduledTournamentMatchEvents(
	route: RouteLocationNormalizedLoaded,
	t: (_: string) => string,
	from: Ref<Date | undefined>,
	to: Ref<Date | undefined>,
) {
	const matches = getScheduledTournamentMatches(route, from, to)

	return {
		...matches,
		data: computed(() => {
			if (!matches.data.value) return undefined
			return matches.data.value.map(matchToEvent)
		}),
	}
}

export function getScheduledMatchEventsExceptCompetition(
	route: RouteLocationNormalizedLoaded,
	from: Ref<Date | undefined>,
	to: Ref<Date | undefined>,
) {
	const matches = getScheduledMatches(from, to)

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

export function matchToEvent(match: AnnotatedMatch) {
	return {
		id: match.id || uuidv4(),
		start: new Date(match.begin ?? new Date()),
		end: new Date(match.end ?? new Date()),
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
