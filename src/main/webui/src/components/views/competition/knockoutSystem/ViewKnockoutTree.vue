<template>
	<div id="wrapper">
		<template v-for="level in range(treeHeight)" :key="level">
			<!-- Data column -->
			<div class="column">
				<div v-if="props.titles" class="title" style="text-align: center">
					{{ props.titles(level, treeHeight) }}
				</div>
				<template
					v-for="index in range(Math.pow(2, treeHeight - level - 1))"
					:key="index"
				>
					<div
						class="match"
						:style="{
							height: maxHeight + 'px',
							'margin-top': getMargin(level, index) + 'px',
						}"
					>
						<div
							:ref="
								<VNodeRef>(
									(<unknown>((el: HTMLElement) => setEl(level, index, el)))
								)
							"
							style="height: fit-content; flex: 1 0 auto"
						>
							<slot
								name="match"
								:match="getMatch(level, index)"
								:level="level"
								:index="index"
								@update:teamA="
									(team: Team | null) => {
										getMatch(level, index).teamA = team
									}
								"
								@update:teamB="
									(team: Team | null) => (getMatch(level, index).teamB = team)
								"
							>
								Fallback
							</slot>
						</div>
					</div>
					<div
						v-if="level > 0 || index < Math.pow(2, treeHeight - level - 1) - 1"
						class="additionalPar"
					>
						<div class="additionalCont">
							<slot
								name="additional"
								:match="getMatch(level, index)"
								:level="level"
								:index="index"
							>
							</slot>
						</div>
					</div>
					<div v-else>
						<slot
							name="additional"
							v-bind="{ match: getMatch(level, index), level, index }"
						>
						</slot>
					</div>
				</template>
				<template v-if="thirdPlace && level === treeHeight - 1">
					<div
						class="match"
						:style="{
							height: maxHeight + 'px',
							'margin-top': props.marginSmall + 'px',
						}"
					>
						<div style="height: fit-content; flex: 1 0 auto">
							<slot
								name="match"
								v-bind="{ match: thirdPlace, level, index: 1 }"
								@update:teamA="
									(team: Team | null) => {
										if (thirdPlace) thirdPlace.teamA = team
									}
								"
								@update:teamB="
									(team: Team | null) => {
										if (thirdPlace) thirdPlace.teamB = team
									}
								"
							>
								Fallback
							</slot>
						</div>
					</div>
					<slot
						name="additional"
						v-bind="{ match: thirdPlace, level, index: 1 }"
					>
					</slot>
				</template>
			</div>
			<!-- Connection columns -->
			<div
				v-if="level < treeHeight - 1"
				class="column"
				:style="{ width: props.conWidth + props.borderThickness + 'px' }"
			>
				<div v-if="props.titles" class="title"></div>
				<div
					v-for="index in range(Math.pow(2, treeHeight - level - 2))"
					:key="index"
					class="left-con"
					:style="{
						height: getHeightCon(level, index) + 'px',
						'margin-top': getMarginConLeft(level, index) + 'px',
						'border-right': props.borderThickness + 'px solid black',
						'border-top': props.borderThickness + 'px solid black',
						'border-bottom': props.borderThickness + 'px solid black',
						'border-radius': `0 ${props.borderRadius}px ${props.borderRadius}px 0`,
					}"
				></div>
			</div>
			<div
				v-if="level < treeHeight - 1"
				class="column"
				:style="{ width: props.conWidth + 'px' }"
			>
				<div v-if="props.titles" class="title"></div>
				<div
					v-for="index in range(Math.pow(2, treeHeight - level - 2))"
					:key="index"
					class="right-con"
					:style="{
						'margin-top': getMarginConRight(level, index) + 'px',
						height: props.borderThickness + 'px',
					}"
				></div>
			</div>
		</template>
	</div>
</template>

<script setup lang="ts">
import { computed, Ref, ref, VNodeRef, watch } from "vue"
import { KnockoutMatch } from "@/interfaces/knockoutSystem"
import { Team } from "@/interfaces/team"

const finale = defineModel<KnockoutMatch>("finale")
const thirdPlace = defineModel<KnockoutMatch | undefined>("thirdPlace")

const props = withDefaults(
	defineProps<{
		marginSmall?: number
		marginBig?: number
		conWidth?: number
		borderThickness?: number
		borderRadius?: number
		titles?: (round: number, total: number) => string
	}>(),
	{
		marginSmall: 10,
		marginBig: 20,
		conWidth: 7,
		borderThickness: 1,
		borderRadius: 10,
		titles: undefined,
	},
)

function measureHeight(tree: KnockoutMatch): number {
	const a = tree.prevMatch ? measureHeight(tree.prevMatch.a) : 0
	const b = tree.prevMatch ? measureHeight(tree.prevMatch.b) : 0
	return 1 + Math.max(a, b)
}

const treeHeight = computed(() =>
	finale.value ? measureHeight(finale.value) : 0,
)

const matchRefs: Ref<HTMLElement[][]> = ref<HTMLElement[][]>([])
const margins: Ref<number[][]> = ref<number[][]>([])
const heightCon: Ref<number[][]> = ref<number[][]>([])
const marginConLeft: Ref<number[][]> = ref<number[][]>([])
const marginConRight: Ref<number[][]> = ref<number[][]>([])
const resizeObserver: Ref<ResizeObserver[][]> = ref<ResizeObserver[][]>([])

function setEl(level: number, index: number, el: HTMLElement) {
	if (!matchRefs.value[level]) matchRefs.value[level] = []
	if (!resizeObserver.value[level]) resizeObserver.value[level] = []
	if (!resizeObserver.value[level][index])
		resizeObserver.value[level][index] = new ResizeObserver(update)
	matchRefs.value[level][index] = el
	if (el) resizeObserver.value[level][index].observe(el)
}

const maxHeight = ref(0)

watch([props, matchRefs, finale, thirdPlace], update)

function update() {
	let mH = 0
	for (const row of matchRefs.value) {
		for (const match of row ?? []) {
			if (match) mH = Math.max(mH, match.getBoundingClientRect().height)
		}
	}
	maxHeight.value = mH

	if (matchRefs.value.length === 0) return

	margins.value[0] = []
	margins.value[0][0] = 0
	for (let index = 1; index < matchRefs.value[0].length; index++) {
		if (index % 2 === 1) {
			margins.value[0][index] = props.marginSmall
		} else {
			margins.value[0][index] = props.marginBig
		}
	}
	for (let level = 1; level < matchRefs.value.length; level++) {
		if (!margins.value[level]) margins.value[level] = []
		const blockHeight =
			Math.pow(2, level) * maxHeight.value +
			Math.pow(2, level - 1) * props.marginSmall +
			(Math.pow(2, Math.max(0, level - 1)) - 1) * props.marginBig

		margins.value[level][0] = blockHeight / 2 - maxHeight.value / 2
		for (let index = 1; index < matchRefs.value[level].length; index++) {
			if (
				matchRefs.value.length < level ||
				matchRefs.value[level - 1].length < 2 * index + 1
			)
				continue

			margins.value[level][index] =
				blockHeight + props.marginBig - maxHeight.value
		}
	}

	for (let level = 0; level < matchRefs.value.length; level++) {
		heightCon.value[level] = []
		marginConLeft.value[level] = []
		marginConRight.value[level] = []

		const height =
			Math.pow(2, level) * maxHeight.value +
			Math.pow(2, Math.max(level - 1, 0)) * props.marginSmall +
			(level === 0 ? 0 : Math.pow(2, level - 1)) * props.marginBig
		for (
			let index = 0;
			index < Math.pow(2, treeHeight.value - level - 2);
			index++
		) {
			heightCon.value[level][index] = height + props.borderThickness
			if (index === 0) {
				marginConLeft.value[level][index] =
					margins.value[level][index] +
					maxHeight.value / 2 -
					props.borderThickness / 2
				marginConRight.value[level][index] =
					margins.value[level][index] +
					maxHeight.value / 2 +
					height / 2 -
					props.borderThickness / 2
			} else {
				if (level === 0) {
					marginConLeft.value[level][index] =
						maxHeight.value + props.marginBig - props.borderThickness
					marginConRight.value[level][index] =
						maxHeight.value + props.marginBig + height - props.borderThickness
				} else {
					marginConLeft.value[level][index] = height - props.borderThickness
					marginConRight.value[level][index] =
						2 * height - props.borderThickness
				}
			}
		}
	}
}

function range(n: number) {
	return [...Array(n)].map((_, i) => i)
}

function getMargin(level: number, index: number) {
	return getSafeValue(level, index, margins.value)
}

function getHeightCon(level: number, index: number) {
	return getSafeValue(level, index, heightCon.value)
}

function getMarginConLeft(level: number, index: number) {
	return getSafeValue(level, index, marginConLeft.value)
}

function getMarginConRight(level: number, index: number) {
	return getSafeValue(level, index, marginConRight.value)
}

function getSafeValue(level: number, index: number, array: number[][]) {
	if (array.length <= level) return 0
	if (array[level].length <= index) return 0

	return array[level][index]
}

function getMatches(level: number): (KnockoutMatch | undefined)[] {
	let cur = [finale.value]
	for (let i = 0; i < treeHeight.value - level - 1; i++) {
		cur = cur
			.map((m) => (m?.prevMatch ? [m.prevMatch.a, m.prevMatch.b] : []))
			.flat()
	}
	return cur
}

function getMatch(level: number, index: number): KnockoutMatch {
	// let curMatch = finale.value
	// if (!curMatch) throw new Error("Finale is undefined")
	const match = getMatches(level)[index]
	if (!match) throw new Error("Match is undefined")
	return match
}
</script>

<style scoped>
#wrapper {
	padding: 5px;
	display: flex;
	width: fit-content;
	flex-direction: row;
}

.match {
	width: 100%;
	display: flex;
	align-items: center;
	justify-content: center;
}

.column {
	height: 100%;
	width: max-content;
}

.left-con {
	box-sizing: border-box;
}

.right-con {
	background-color: black;
}

.additionalPar {
	position: relative;
}

.additionalCont {
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
}

.title {
	height: 30px;
	font-weight: bold;
}
</style>
