import { computed, ref, Ref, unref } from "vue"

export function selectRandomElement<T>(players: T[] | Ref<T[]>) {
	if (unref(players).length === 0) return null

	const r = Math.floor(Math.random() * unref(players).length)
	const element = unref(players)[r]
	unref(players).splice(r, 1)
	return element
}

export function genRandomizeItems(
	t: (_: string) => string,
	reroll: () => void,
	reset: () => void,
) {
	return ref([
		{
			label: <string>(
				(<unknown>computed(() => t("ViewPrepare.editTeams.reroll")))
			),
			icon: "pi pi-refresh",
			command: reroll,
		},
		{
			label: <string>(
				(<unknown>computed(() => t("ViewPrepare.editTeams.reset")))
			),
			icon: "pi pi-times",
			command: reset,
		},
	])
}

const duration = 2000
export function getDelays(data: Ref<{ teamCount: number }>) {
	const delay = computed(() =>
		Math.min((duration * 2) / 3 / data.value.teamCount, 50),
	)
	return {
		delay,
		delayBetween: computed(() => delay.value / 2),
	}
}
