import {
	Team,
	teamClientToServer,
	TeamServer,
	teamServerToClient,
} from "@/interfaces/team"
import { CompType } from "@/interfaces/competition"

export interface Game {
	scoreA: number
	scoreB: number
}

export interface SetKey {
	match_id: string
	index: number
}

export interface Set {
	// key: SetKey
	index: number
	scoreA: number
	scoreB: number
}

export interface Match {
	id?: string
	court: string | null
	begin: Date | null
	end: Date | null
	finished: boolean
	winner: boolean | null
	teamA: Team | null
	teamB: Team | null
	sets: Array<Set> | null
	curGame: Game | null
}

export interface MatchServer {
	id?: string
	court: string | undefined
	begin: string | undefined
	end: string | undefined
	finished: boolean | undefined
	winner: boolean | undefined
	teamA: TeamServer | null
	teamB: TeamServer | null
	// TODO add result
	sets: Array<Set>
}

export interface AnnotatedMatchServer extends MatchServer {
	tourName: string
	compName: string
	type: CompType
	number: number
	total: number
	isFinal: boolean
}

export interface AnnotatedMatch extends Match {
	title: {
		isFinal: boolean
		type: CompType
		number: number
		total: number
	}
	tourName: string
	compName: string
}

export interface MatchEvent {
	id: string
	start: Date
	end: Date
	split: string
	data: AnnotatedMatch
}

export function matchServerToClient(match: MatchServer): Match {
	const teamA = match.teamA === null ? null : teamServerToClient(match.teamA)
	const teamB = match.teamB === null ? null : teamServerToClient(match.teamB)

	let sets: Set[] = []
	if (match.sets !== null) {
		sets = match.sets
	}

	return {
		id: match.id,
		court: match.court ? match.court : null,
		begin: match.begin ? new Date(match.begin) : null,
		end: match.end ? new Date(match.end) : null,
		finished: !!match.finished,
		winner: match.winner ? match.winner : null,
		teamA: teamA,
		teamB: teamB,
		sets: sets,
		curGame: null,
	}
}

export function annotatedMatchServerToClient(
	matchServer: AnnotatedMatchServer,
): AnnotatedMatch {
	const match = matchServerToClient(matchServer)
	return {
		tourName: matchServer.tourName,
		compName: matchServer.compName,
		title: {
			type: matchServer.type,
			number: matchServer.number,
			isFinal: matchServer.isFinal,
			total: matchServer.total,
		},
		...match,
	}
}

export function matchClientToServer(match: Match): MatchServer {
	return {
		id: match.id,
		court: match.court ?? undefined,
		begin: match.begin?.toISOString() ?? undefined,
		end: match.end?.toISOString() ?? undefined,
		finished: match.finished,
		winner: match.winner ?? undefined,
		teamA: match.teamA === null ? null : teamClientToServer(match.teamA),
		teamB: match.teamB === null ? null : teamClientToServer(match.teamB),
		sets: match.sets ?? [],
	}
}
