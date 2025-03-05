<template>
  <!-- TODO mobile optimization -->
  <table style="width: calc(100% - 1px)">
    <tbody>
    <tr>
      <th id="first">
        <span>{{ t("ViewGroupSystem.group") }} {{ props.group.index }}</span>
      </th>
      <template
          v-for="(team, index) in props.group.teams.slice().reverse()"
          :key="index"
      >
        <th
            v-if="index !== props.group.teams.length - 1"
            :class="hoverIdB === index ? 'highlight' : ''"
            @mouseover="headerHover(team.id)"
            @mouseleave="hoverLeave()"
        >
          <ViewTeamNames :team="team" :inverted="true" short/>
        </th>
      </template>
    </tr>
    <template v-for="(teamA, index) in props.group.teams" :key="index">
      <tr v-if="index !== props.group.teams.length - 1">
        <th
            :class="hoverIdA === index ? 'highlight ' : ''"
            @mouseover="headerHover(teamA.id)"
            @mouseleave="hoverLeave()"
        >
          <ViewTeamNames :team="teamA" :inverted="true" short/>
        </th>
        <template
            v-for="(teamB, indexB) in props.group.teams.slice().reverse()"
            :key="indexB"
        >
          <td
              v-if="
							indexB !== props.group.teams.length - 1 &&
							index + indexB < props.group.teams.length - 1
						"
              :class="{
							'cursor-pointer': isReporter,
							highlightLow:
								hoverIdA &&
								hoverIdB &&
								((hoverIdA === index && indexB < hoverIdB) ||
									(hoverIdB === indexB && index < hoverIdA)),
							highlight: hoverTeam === teamA.id || hoverTeam === teamB.id,
						}"
              @mouseover="matchHover(index, indexB)"
              @mouseleave="hoverLeave()"
              @click="showPopUp(findMatch(teamA, teamB).match)"
          >
            <div>
              <ViewMatch v-bind="findMatch(teamA, teamB)"/>
            </div>
          </td>
        </template>
      </tr>
    </template>
    </tbody>
  </table>
  <UpdatePointsDialog
      v-if="isReporter"
      ref="dialog"
      :number-sets="props.numberSets"
  />
</template>

<script setup lang="ts">
import {Group} from "@/interfaces/groupSystem"
import {inject, ref} from "vue"
import {useI18n} from "vue-i18n"
import ViewMatch from "@/components/views/competition/groupSystem/ViewMatch.vue"
import {Match} from "@/interfaces/match"
import {Team} from "@/interfaces/team"
import ViewTeamNames from "@/components/links/LinkTeamNames.vue"
import UpdatePointsDialog from "@/components/items/UpdatePointsDialog.vue"
import {NumberSets} from "@/interfaces/competition"
import {getIsReporter} from "@/backend/security"

const {t} = useI18n()

const props = defineProps<{
  group: Group
  numberSets: NumberSets
}>()

const isLoggedIn = inject("loggedIn", ref(false))
const {data: isReporter} = getIsReporter(isLoggedIn)

function findMatch(
    teamA: Team,
    teamB: Team,
): { match: Match; reversed: boolean } {
  const match: Match | undefined = props.group.matches.find((match) => {
    if (match.teamA === null || match.teamB === null) return false
    if (match.teamA.id === teamA.id && match.teamB.id === teamB.id) return true
    if (match.teamA.id === teamB.id && match.teamB.id === teamA.id) return true
  })
  if (match === undefined) {
    console.error("Match does not exist")
    throw new Error("Match does not exist")
  }
  return {match, reversed: match.teamA?.id === teamB.id}
}

const hoverIdA = ref<null | number>(null)
const hoverIdB = ref<null | number>(null)
const hoverTeam = ref<null | string>(null)

function matchHover(indexA: number, indexB: number) {
  hoverIdA.value = indexA
  hoverIdB.value = indexB
}

function headerHover(team: string | undefined) {
  hoverTeam.value = team || ""
}

function hoverLeave() {
  hoverIdA.value = null
  hoverIdB.value = null
  hoverTeam.value = null
}

const dialog = ref()

function showPopUp(match: Match) {
  if (isReporter.value) dialog.value.showPopUp(match)
}
</script>

<style scoped>
table {
  table-layout: fixed;
  text-align: center;
  border-spacing: 0;
  border-collapse: separate;
  border-top-left-radius: 15px;
  border-top: solid black 1px;
  border-left: solid black 1px;
}

#first {
  border-top-left-radius: 15px;
}

th,
td {
  width: 1fr;
  height: 80px;
  border-bottom: solid black 1px;
  border-right: solid black 1px;
}

th {
  background-color: #f0f0f0;
}

td:hover {
  background-color: #dfdfdf;
}

.highlight {
  background-color: #dfdfdf;
}

.highlightLow {
  background-color: #f9f9f9;
}
</style>
