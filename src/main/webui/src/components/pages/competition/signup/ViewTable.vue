<template>
	<template v-if="competition && signedUp">
		<!-- SINGLE -->
		<DataTable
			v-if="competition.mode === Mode.SINGLE"
			:value="signedUp.playersA"
			striped-rows
			show-gridlines
			removable-sort
		>
			<template #empty>{{
				t("competition.action.signup.participants.empty")
			}}</template>
			<Column :header="t('general.name.label')" field="name" sortable>
				<template #body="{ data }">
					<div class="flex justify-content-between align-items-center">
						<ViewPlayerName :player="<Player>data.playerA" />
						<Button
							v-if="canEdit"
							severity="danger"
							text
							rounded
							size="small"
							class="h-2rem"
							@click="deletePlayer(data)"
						>
							<i-material-symbols-delete-forever-outline
								class="size-1dot5rem"
							/>
						</Button>
					</div>
				</template>
			</Column>
		</DataTable>

		<!-- DOUBLE TOGETHER -->
		<DataTable
			v-else-if="competition.signUp === SignUp.TOGETHER"
			:value="signedUp.teams"
			striped-rows
			show-gridlines
			removable-sort
		>
			<template #empty>{{
				t("competition.action.signup.participants.empty")
			}}</template>
			<Column
				:header="t('competition.player.A')"
				sortable
				field="playerA.name"
				style="width: 50%"
			>
				<template #body="{ data }">
					<ViewPlayerName :player="<Player>data.playerA" />
				</template>
			</Column>
			<Column
				:header="t('competition.player.B')"
				sortable
				field="playerB.name"
				style="width: 50%"
			>
				<template #body="{ data }">
					<div class="flex justify-content-between align-items-center">
						<ViewPlayerName :player="<Player>data.playerB" />
						<Button
							v-if="canEdit"
							severity="danger"
							text
							rounded
							size="small"
							class="h-2rem"
							@click="deletePlayer(data)"
						>
							<i-material-symbols-delete-forever-outline
								class="size-1dot5rem"
							/>
						</Button>
					</div>
				</template>
			</Column>
		</DataTable>
		<!-- DOUBLE INDIVIDUAL SAME -->
		<DataTable
			v-else-if="!competition.playerB.different"
			:value="signedUp.playersA"
			striped-rows
			show-gridlines
			removable-sort
		>
			<template #empty>{{
				t("competition.action.signup.participants.empty")
			}}</template>
			<Column :header="t('general.name.label')" sortable field="name">
				<template #body="{ data }">
					<div class="flex justify-content-between align-items-center">
						<ViewPlayerName :player="<Player>data.playerA" />
						<Button
							v-if="canEdit"
							severity="danger"
							text
							rounded
							size="small"
							class="h-2rem"
							@click="deletePlayer(data)"
						>
							<i-material-symbols-delete-forever-outline
								class="size-1dot5rem"
							/>
						</Button>
					</div>
				</template>
			</Column>
		</DataTable>

		<!-- DOUBLE INDIVIDUAL DIFFERENT -->
		<div v-else class="grid">
			<DataTable
				class="col"
				:value="signedUp.playersA"
				striped-rows
				show-gridlines
				removable-sort
			>
				<template #empty>{{
					t("competition.action.signup.participants.empty")
				}}</template>
				<Column :header="t('competition.player.A')" sortable field="name">
					<template #body="{ data }">
						<div class="flex justify-content-between align-items-center">
							<ViewPlayerName :player="<Player>data.playerA" />
							<Button
								v-if="canEdit"
								severity="danger"
								text
								rounded
								size="small"
								class="h-2rem"
								@click="deletePlayer(data)"
							>
								<i-material-symbols-delete-forever-outline
									class="size-1dot5rem"
								/>
							</Button>
						</div>
					</template>
				</Column>
			</DataTable>
			<DataTable
				class="col"
				:value="signedUp.playersB"
				striped-rows
				show-gridlines
				removable-sort
			>
				<template #empty>{{
					t("competition.action.signup.participants.empty")
				}}</template>
				<Column :header="t('competition.player.B')" sortable field="name">
					<template #body="{ data }">
						<div class="flex justify-content-between align-items-center">
							<ViewPlayerName :player="<Player>data.playerB" />
							<Button
								v-if="canEdit"
								severity="danger"
								text
								rounded
								size="small"
								class="h-2rem"
								@click="deletePlayer(data)"
							>
								<i-material-symbols-delete-forever-outline
									class="delete_forever"
								/>
							</Button>
						</div>
					</template>
				</Column>
			</DataTable>
		</div>
	</template>
</template>

<script lang="ts" setup>
import { inject, ref } from "vue"
import { useRoute } from "vue-router"
import { Mode, SignUp } from "@/interfaces/competition"
import { useI18n } from "vue-i18n"
import { Player } from "@/interfaces/player"
import { useToast } from "primevue/usetoast"
import { getCanEdit } from "@/backend/security"
import { getCompetitionDetails } from "@/backend/competition"
import { getSignedUpSepByComp, useDeleteTeam } from "@/backend/signup"
import ViewPlayerName from "@/components/links/LinkPlayerName.vue"
import { Team } from "@/interfaces/team"

const { t } = useI18n()
const toast = useToast()

const route = useRoute()

const isLoggedIn = inject("loggedIn", ref(false))
const { data: canEdit } = getCanEdit(<string>route.params.tourId, isLoggedIn)

const { data: competition } = getCompetitionDetails(route, t, toast)
const { data: signedUp } = getSignedUpSepByComp(route, t, toast)
const { mutate: deleteTeam } = useDeleteTeam(route)

function deletePlayer(team: Team) {
	deleteTeam(team)
	toast.add({
		severity: "success",
		summary: t(
			competition.value?.mode == Mode.SINGLE
				? "player.action.delete.success"
				: "team.delete.success",
		),
		life: 3000,
	})
}
</script>

<style scoped></style>
