import { AnnotatedMatch } from "@/interfaces/match"
import { KnockoutMatch, KnockoutSystem } from "@/interfaces/knockoutSystem"
import { GroupMatch, GroupSystem } from "@/interfaces/groupSystem"
import { CalEvent } from "@/calendar/CalendarInterfaces"
import { CompType } from "@/interfaces/competition"

export type MatchCalEvent = CalEvent<AnnotatedMatch>

export function extractKnockoutMatches(
	knockout: KnockoutSystem,
	add: (match: KnockoutMatch, title: AnnotatedMatch["title"]) => void,
	group = false,
) {
	const rounds: KnockoutMatch[][] = []
	let queue: KnockoutMatch[] = []
	let nQueue: KnockoutMatch[] = []
	if (knockout.finale) queue.push(knockout.finale)
	if (knockout.thirdPlace) queue.push(knockout.thirdPlace)

	while (queue.length) {
		queue.forEach((match) => {
			if (match.prevMatch) {
				nQueue.push(match.prevMatch.a)
				nQueue.push(match.prevMatch.b)
			}
		})
		if (nQueue.length) rounds.push(nQueue)
		queue = nQueue
		nQueue = []
	}

	rounds.toReversed().forEach((round, i) => {
		round.forEach((match) => {
			if (i > 0 || (match.teamA && match.teamB) || group)
				add(match, {
					isFinal: true,
					type: CompType.KNOCKOUT,
					number: i,
					total: rounds.length + 1,
				})
		})
	})
	if (knockout.thirdPlace)
		add(knockout.thirdPlace, {
			isFinal: false,
			type: CompType.KNOCKOUT,
			number: rounds.length,
			total: rounds.length + 1,
		})
	if (knockout.finale)
		add(knockout.finale, {
			isFinal: true,
			type: CompType.KNOCKOUT,
			number: rounds.length,
			total: rounds.length + 1,
		})
}

export function extractGroupMatches(
	groupSystem: GroupSystem,
	add: (match: GroupMatch, title: AnnotatedMatch["title"]) => void,
) {
	groupSystem.groups.forEach((group, i) => {
		group.matches.forEach((match) => {
			add(match, {
				type: CompType.GROUPS,
				number: i,
				total: i,
				isFinal: true,
			})
		})
	})
	extractKnockoutMatches(
		{ finale: groupSystem.finale, thirdPlace: groupSystem.thirdPlace },
		add,
		true,
	)
}
