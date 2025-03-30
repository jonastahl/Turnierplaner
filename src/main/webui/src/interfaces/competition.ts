import { TeamServer } from "@/interfaces/team"

export enum Sex {
	MALE = "MALE",
	FEMALE = "FEMALE",
	ANY = "ANY",
}

export enum Language {
	EN = "EN",
	DE = "DE",
}

export enum CompType {
	KNOCKOUT = "KNOCKOUT",
	GROUPS = "GROUPS",
}

export enum Mode {
	SINGLE = "SINGLE",
	DOUBLE = "DOUBLE",
}

export enum NumberSets {
	THREE = "THREE",
	FIVE = "FIVE",
}

export enum SignUp {
	INDIVIDUAL = "INDIVIDUAL",
	TOGETHER = "TOGETHER",
}

export enum Progress {
	TEAMS = "TEAMS",
	GAMES = "GAMES",
	SCHEDULING = "SCHEDULING",
}

export function progressOrder(progress: Progress) {
	switch (progress) {
		case Progress.TEAMS:
			return 2
		case Progress.GAMES:
			return 3
		case Progress.SCHEDULING:
			return 4
	}
}

export interface Competition {
	id?: null | string
	name: string
	description: string
	tourType: CompType
	mode: Mode
	signUp: SignUp
	numberSets: NumberSets
	playerA: settingsPlayer
	playerB: settingsPlayerB
	cProgress: Progress
}

export const CompetitionDefault: Competition = {
	id: null,
	name: "",
	description: "",
	tourType: CompType.KNOCKOUT,
	mode: Mode.SINGLE,
	signUp: SignUp.INDIVIDUAL,
	numberSets: NumberSets.THREE,
	playerA: {
		sex: Sex.ANY,
		hasMinAge: false,
		minAge: null,
		hasMaxAge: false,
		maxAge: null,
	},
	playerB: {
		different: false,
		sex: Sex.ANY,
		hasMinAge: false,
		minAge: null,
		hasMaxAge: false,
		maxAge: null,
	},
	cProgress: Progress.TEAMS,
}

export interface CompetitionServer {
	id?: null | string
	name: string
	description: string
	type: CompType
	mode: Mode
	signUp: SignUp
	numberSets: NumberSets
	playerA: {
		sex: Sex
		hasMinAge: boolean
		minAge: string | null
		hasMaxAge: boolean
		maxAge: string | null
	}
	playerB: {
		different: boolean
		sex: Sex
		hasMinAge: boolean
		minAge: string | null
		hasMaxAge: boolean
		maxAge: string | null
	}
	cProgress?: Progress.TEAMS
}

export interface settingsPlayer {
	sex: Sex
	hasMinAge: boolean
	minAge: Date | null
	hasMaxAge: boolean
	maxAge: Date | null
}

export interface CompetitionForm {
	name: string
	description: string
	tourType: CompType
	mode: Mode
	signUp: SignUp
	numberSets: NumberSets
	playerA_Sex: Sex
	playerA_hasMinAge: boolean
	playerA_minAge: Date | undefined
	playerA_hasMaxAge: boolean
	playerA_maxAge: Date | undefined
	playerB_different: boolean
	playerB_Sex: Sex
	playerB_hasMinAge: boolean
	playerB_minAge: Date | undefined
	playerB_hasMaxAge: boolean
	playerB_maxAge: Date | undefined
}

export interface settingsPlayerB extends settingsPlayer {
	different: boolean
}

export function competitionFormToServer(
	form: CompetitionForm,
	id: string | null,
): CompetitionServer {
	return {
		id: id,
		name: form.name,
		description: form.description,
		type: form.tourType,
		mode: form.mode,
		signUp: form.signUp,
		numberSets: form.numberSets,
		playerA: {
			sex: form.playerA_Sex,
			hasMinAge: form.playerA_hasMinAge,
			minAge: form.playerA_minAge ? dateToJson(form.playerA_minAge) : null,
			hasMaxAge: form.playerA_hasMaxAge,
			maxAge: form.playerA_maxAge ? dateToJson(form.playerA_maxAge) : null,
		},
		playerB: {
			different: form.playerB_different,
			sex: form.playerB_Sex,
			hasMinAge: form.playerB_hasMinAge,
			minAge: form.playerB_minAge ? dateToJson(form.playerB_minAge) : null,
			hasMaxAge: form.playerB_hasMaxAge,
			maxAge: form.playerA_maxAge ? dateToJson(form.playerA_maxAge) : null,
		},
	}
}

export function competitionClientToServer(
	competition: Competition,
): CompetitionServer {
	return {
		id: competition.id,
		name: competition.name,
		description: competition.description,
		type: competition.tourType,
		mode: competition.mode,
		signUp: competition.signUp,
		numberSets: competition.numberSets,
		playerA: {
			sex: competition.playerA.sex,
			hasMinAge: competition.playerA.hasMinAge,
			minAge: competition.playerA.minAge
				? dateToJson(competition.playerA.minAge)
				: null,
			hasMaxAge: competition.playerA.hasMaxAge,
			maxAge: competition.playerA.maxAge
				? dateToJson(competition.playerA.maxAge)
				: null,
		},
		playerB: {
			different: competition.playerB.different,
			sex: competition.playerB.sex,
			hasMinAge: competition.playerB.hasMinAge,
			minAge: competition.playerB.minAge
				? dateToJson(competition.playerB.minAge)
				: null,
			hasMaxAge: competition.playerB.hasMaxAge,
			maxAge: competition.playerB.maxAge
				? dateToJson(competition.playerB.maxAge)
				: null,
		},
	}
}

export function competitionServerToClient(
	competition: CompetitionServer,
): Competition {
	return {
		id: competition.id,
		name: competition.name,
		description: competition.description,
		tourType: competition.type,
		mode: competition.mode,
		signUp: competition.signUp,
		numberSets: competition.numberSets,
		playerA: {
			sex: competition.playerA.sex,
			hasMinAge: competition.playerA.hasMinAge,
			minAge: competition.playerA.minAge
				? new Date(competition.playerA.minAge)
				: null,
			hasMaxAge: competition.playerA.hasMaxAge,
			maxAge: competition.playerA.maxAge
				? new Date(competition.playerA.maxAge)
				: null,
		},
		playerB: {
			different: competition.playerB.different,
			sex: competition.playerB.sex,
			hasMinAge: competition.playerB.hasMinAge,
			minAge: competition.playerB.minAge
				? new Date(competition.playerB.minAge)
				: null,
			hasMaxAge: competition.playerB.hasMaxAge,
			maxAge: competition.playerB.maxAge
				? new Date(competition.playerB.maxAge)
				: null,
		},
		cProgress: competition.cProgress || Progress.TEAMS,
	}
}

function dateToJson(d: Date): string {
	return `${d.getFullYear()}-${d.getMonth() < 9 ? "0" : ""}${
		d.getMonth() + 1
	}-${d.getDate() < 10 ? "0" : ""}${d.getDate()}`
}

export interface KnockoutOrder {
	teams: TeamServer[]
}

export interface GroupsDivision {
	groups: TeamServer[][]
}
