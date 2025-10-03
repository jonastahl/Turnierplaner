import { useMutation, useQueryClient } from "@tanstack/vue-query"
import { Match, Set } from "@/interfaces/match"
import axios from "axios"
import { RouteLocationNormalizedLoaded } from "vue-router"
import { ToastServiceMethods } from "primevue/toastservice"
import { computed, Ref } from "vue"

export function useUpdateSet(
	route: RouteLocationNormalizedLoaded,
	t: (s: string) => string,
	toast: ToastServiceMethods,
) {
	useUpdateSetCustom(
		route,
		computed(() => <string>route.params.compId),
		t,
		toast,
	)
}

export function mayEditMatch(
	isDirector: boolean,
	isReporter: boolean,
	match: Match,
): boolean {
	return (
		(isDirector || (match.begin !== null && match.begin <= new Date())) &&
		(isReporter || !match.sets?.length)
	)
}
export function useUpdateSetCustom(
	route: RouteLocationNormalizedLoaded,
	compId: Ref<string>,
	t: (s: string) => string,
	toast: ToastServiceMethods,
) {
	const queryClient = useQueryClient()
	return useMutation({
		mutationFn: (data: { sets: Set[]; matchId: string }) =>
			axios.post(
				`/tournament/${<string>route.params.tourId}/competition/${compId.value}/set/${data.matchId}`,
				data.sets,
			),
		onSuccess() {
			toast.add({
				severity: "success",
				summary: t("general.success"),
				detail: t("general.saved"),
				life: 3000,
			})
			queryClient.invalidateQueries({
				queryKey: ["knockout", route.params.tourId, route.params.compId],
				refetchType: "all",
			})
			queryClient.invalidateQueries({
				queryKey: ["group", route.params.tourId, route.params.compId],
				refetchType: "all",
			})
			queryClient.invalidateQueries({
				queryKey: ["scheduledMatches"],
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
			console.error(error)
		},
	})
}

export function checkSets(
	sets: Set[],
	numberSets: number,
	isDirector: boolean,
) {
	if (sets.length != numberSets)
		console.error("Internal error: wrong number of sets")
	if (isDirector && sets.every((set) => set.scoreA == 0 && set.scoreB == 0)) {
		return []
	}

	const errors = []
	let dif = 0
	let i = 0
	for (; i < numberSets; i++) {
		const set = sets[i]
		if (i < numberSets - 1 ? !validSet(set) : !validTiebreak(set))
			errors.push(i)

		if (set.scoreA > set.scoreB) dif++
		else if (set.scoreA < set.scoreB) dif--

		if (Math.abs(dif) == Math.ceil(numberSets / 2)) break
	}
	for (i++; i < numberSets; i++) {
		const set = sets[i]
		if (set.scoreA != 0 || set.scoreB != 0) errors.push(i)
	}
	return errors
}

const setTill = 6
const tiebreakTill = 10
function validSet(set: Set) {
	return (
		(set.scoreA == setTill && 0 <= set.scoreB && set.scoreB <= setTill - 2) ||
		(set.scoreA == setTill + 1 &&
			setTill - 1 <= set.scoreB &&
			set.scoreB <= setTill) ||
		(set.scoreB == setTill && 0 <= set.scoreA && set.scoreA <= setTill - 2) ||
		(set.scoreB == setTill + 1 &&
			setTill - 1 <= set.scoreA &&
			set.scoreA <= setTill)
	)
}
function validTiebreak(set: Set) {
	return (
		(set.scoreA == tiebreakTill &&
			0 <= set.scoreB &&
			set.scoreB <= tiebreakTill - 2) ||
		(set.scoreB == tiebreakTill &&
			0 <= set.scoreA &&
			set.scoreA <= tiebreakTill - 2) ||
		(set.scoreA >= tiebreakTill - 1 &&
			set.scoreB >= tiebreakTill - 1 &&
			Math.abs(set.scoreA - set.scoreB) == 2)
	)
}
