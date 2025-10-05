import { Language } from "@/interfaces/competition"

export interface PlayerServer {
	id?: string
	firstName: string
	lastName: string
	sex?: Sex
}

export interface Player extends PlayerServer {
	name: string
}

export function playerServerToClient(player: PlayerServer): Player {
	return {
		id: player.id,
		firstName: player.firstName,
		lastName: player.lastName,
		sex: player.sex,
		get name() {
			return `${this.firstName} ${this.lastName}`
		},
	}
}

export function playerClientToServer(player: Player): PlayerServer {
	return {
		id: player.id,
		firstName: player.firstName,
		lastName: player.lastName,
	}
}

export enum Sex {
	MALE = "MALE",
	FEMALE = "FEMALE",
}

export interface PlayerRegistrationForm {
	firstName: string
	lastName: string
	sex: Sex | undefined
	birthday: Date | undefined
	language: Language
	email: string
}

export interface PlayerRegistration extends PlayerRegistrationForm {
	sex: Sex
	birthday: Date
}

export interface PlayerDetails extends PlayerRegistration {
	id: string
}

export interface PlayerDetailsServer {
	id: string
	firstName: string
	lastName: string
	sex: Sex
	birthday: Date | string
	language: Language
	email: string
}

export function playerDetailsServerToClient(
	player: PlayerDetailsServer,
): PlayerDetails {
	return {
		...player,
		birthday: new Date(player.birthday),
	}
}

export function playerDetailsClientToServer(
	player: PlayerDetails,
): PlayerDetailsServer {
	return player
}
