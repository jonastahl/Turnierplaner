export function knockoutTitle(t: (_: string) => string) {
	return (round: number, totalRounds: number) => {
		round += 1
		if (round === totalRounds) return t("ViewKnockout.finale")
		else if (round === totalRounds - 1) return t("ViewKnockout.semifinals")
		else if (round === totalRounds - 2) return t("ViewKnockout.quarterfinals")
		else return t("ViewKnockout.round") + " " + round
	}
}
