export function knockoutTitle(t: (_: string) => string) {
	return (round: number, totalRounds: number) => {
		round += 1
		if (round === totalRounds) return t("knockout.finale")
		else if (round === totalRounds - 1) return t("knockout.semifinals")
		else if (round === totalRounds - 2) return t("knockout.quarterfinals")
		else return t("knockout.round") + " " + round
	}
}
