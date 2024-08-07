import { Match, MatchServer, matchServerToClient } from "@/interfaces/match"
import { Team } from "@/interfaces/team"

export interface KnockoutSystem {
	teams: Team[]
	finale: KnockoutMatch
	thirdPlace: KnockoutMatch
}

export interface KnockoutSystemServer {
	teams: Team[]
	finale: KnockoutMatchServer
	thirdPlace: KnockoutMatchServer
}

export interface KnockoutMatch extends Match {
	prevMatch?: {
		winner: boolean
		a: KnockoutMatch
		b: KnockoutMatch
	}
}

export interface KnockoutMatchServer extends MatchServer {
	winningPlayer: boolean
	previousA: KnockoutMatchServer | undefined
	previousB: KnockoutMatchServer | undefined
}

export function knockoutSystemServerToClient(
	knockoutSystem: KnockoutSystemServer,
): KnockoutSystem {
	const teams = new Map<string, Team>()
	knockoutSystem.teams.forEach((team) => {
		if (!team.id) {
			console.error("Team without id:", team)
			return
		}
		teams.set(team.id, team)
	})
	return {
		teams: knockoutSystem.teams,
		finale: knockoutMatchServerToClient(knockoutSystem.finale, teams),
		thirdPlace: knockoutMatchServerToClient(knockoutSystem.thirdPlace, teams),
	}
}

function knockoutMatchServerToClient(
	matchServer: KnockoutMatchServer,
	teams: Map<string, Team>,
): KnockoutMatch {
	const match: KnockoutMatch = matchServerToClient(matchServer, teams)
	if (matchServer.previousA && matchServer.previousB) {
		match.prevMatch = {
			winner: matchServer.winningPlayer,
			a: knockoutMatchServerToClient(matchServer.previousA, teams),
			b: knockoutMatchServerToClient(matchServer.previousB, teams),
		}
	}
	return match
}

export function knockoutMatchClientToServer(
	match: KnockoutMatch,
): KnockoutMatchServer {
	return {
		court: match.court ? match.court : undefined,
		begin: match.begin ? match.begin.toString() : undefined,
		end: match.end ? match.end.toISOString() : undefined,
		finished: match.finished,
		winner: match.winner ? match.winner : undefined,
		teamA: match.teamA && match.teamA.id ? match.teamA.id : null,
		teamB: match.teamB && match.teamB.id ? match.teamB.id : null,
		sets: match.sets ? match.sets : [],

		winningPlayer: match.prevMatch?.winner ?? false,

		previousA: match.prevMatch?.a
			? knockoutMatchClientToServer(match.prevMatch.a)
			: undefined,
		previousB: match.prevMatch?.b
			? knockoutMatchClientToServer(match.prevMatch.b)
			: undefined,
	}
}
