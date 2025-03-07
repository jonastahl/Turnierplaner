<template>
	<vue-cal
		id="vuecal"
		ref="vuecal"
		:key="calid"
		:events="events"
		:selected-date="props.selectedDate"
		:time-from="props.timeFrom"
		:time-to="props.timeTo"
		:active-view="props.activeView"
		:disable-views="props.disabledViews"
		:min-date="props.minDate"
		:max-date="props.maxDate"
		:locale="$i18n.locale"
		:split-days="props.splitDays"
		sticky-split-labels
		:min-event-width="props.minEventWidth"
		:min-cell-width="props.minCellWidth"
		:min-split-width="props.minSplitWidth"
		:time-cell-height="props.timeCellHeight"
		:snap-to-time="props.snapToTime"
		:time-step="props.timeStep"
		:editable-events="{
			title: false,
			drag: props.editableEvents,
			resize: props.editableEvents,
			delete: props.deletableEvents,
			create: false,
		}"
		@event-drop="onEventDrop"
		@event-change="onEventChange"
		@event-delete="onEventDelete"
		@view-change="onViewChange"
	>
		<template v-if="$slots.event" #event="{ event }">
			<slot name="event" v-bind="{ event: <CalEvent<T>>event }"></slot>
		</template>
		<template v-if="$slots.timeCell" #time-cell="{ hours, minutes }">
			<slot
				name="timeCell"
				v-bind="{ hours: <number>hours, minutes: <number>minutes }"
			/>
		</template>
		<template v-else #time-cell="{ hours, minutes }">
			<div class="vuecal__time-cell-line" :class="{ hours: !minutes }">
				<strong v-if="!minutes" style="font-size: 15px">{{ hours }}</strong>
				<span v-else style="font-size: 11px">{{ minutes }}</span>
			</div>
		</template>
		<template v-if="$slots.splitLabel" #split-label="{ split }">
			<slot name="splitLabel" v-bind="{ split: <DaySplit>split }" />
		</template>
		<template v-else #split-label="{ split }">
			<strong style="height: 24px">{{ split.label }}</strong>
		</template>
	</vue-cal>
</template>

<script setup lang="ts" generic="T">
// @ts-expect-error vue-cal does not have proper typescript support
import VueCal from "vue-cal"
import "vue-cal/dist/vuecal.css"
import { Ref, ref, watch } from "vue"
import { CalEvent, DaySplit, View } from "@/calendar/CalendarInterfaces"
import { sleep } from "@/backend/Tracker"

const props = withDefaults(
	defineProps<{
		selectedDate?: Date
		timeFrom?: number
		timeTo?: number
		activeView?: View
		disabledViews?: View[]
		minDate?: Date
		maxDate?: Date
		locale?: Ref<string> | string
		splitDays?: DaySplit[]
		minEventWidth?: number
		minSplitWidth?: number
		minCellWidth?: number
		timeCellHeight?: number
		snapToTime?: number
		timeStep?: number
		editableEvents?: boolean
		deletableEvents?: boolean
		automaticEvent?: boolean
	}>(),
	{
		selectedDate: () => new Date(),
		timeFrom: 0,
		timeTo: 24 * 60,
		snapToTime: 15,
		timeStep: 15,
		activeView: View.day,
		disabledViews: () => [View.years, View.year, View.month],
		locale: "en",
		editableEvents: false,
		deletableEvents: false,
		automaticEvent: true,
		minDate: undefined,
		maxDate: undefined,
		splitDays: undefined,
		minEventWidth: 80,
		minSplitWidth: 250,
		minCellWidth: 250,
		timeCellHeight: 25,
	},
)

const events = defineModel<CalEvent<T>[]>({ default: [] })

const emit = defineEmits<{
	onEventDrop: [CalEvent<T>, CalEvent<T>, boolean]
	onEventChange: [CalEvent<T>, CalEvent<T>]
	onViewChange: [Date, Date]
	onEventDelete: [CalEvent<T>]
}>()

const vuecal = ref()
const calid = ref<number>(0)

watch([vuecal, () => props.selectedDate], () => {
	if (!vuecal.value || !props.selectedDate) return
	// needed to always trigger the onViewChange
	vuecal.value.previous()
	vuecal.value.switchView("day", props.selectedDate)
})

function previous() {
	if (vuecal.value) vuecal.value.previous()
}

function next() {
	if (vuecal.value) vuecal.value.next()
}

function switchView(view: View, date?: Date) {
	if (vuecal.value) switchView(view, date)
}

function reload() {
	calid.value++
}

defineExpose({ previous, next, switchView, reload })

function onEventDrop({
	event,
	originalEvent,
	external,
}: {
	event: CalEvent<T>
	originalEvent: CalEvent<T>
	external: boolean
}) {
	emit("onEventDrop", event, originalEvent, external)
}

function onEventChange({
	event,
	originalEvent,
}: {
	event: CalEvent<T>
	originalEvent: CalEvent<T>
}) {
	if (props.automaticEvent && originalEvent && originalEvent.id) {
		events.value.splice(
			events.value.findIndex((e) => e.id === originalEvent.id),
			1,
		)
		events.value.push({
			...event,
			changed: true,
		})
	}
	emit("onEventChange", event, originalEvent)
}

function onEventDelete(event: CalEvent<T>) {
	events.value.splice(
		events.value.findIndex((e) => e.id === event.id),
		1,
	)
	emit("onEventDelete", event)
}

async function onViewChange({
	startDate,
	endDate,
}: {
	startDate: Date
	endDate: Date
}) {
	emit("onViewChange", startDate, endDate)

	await sleep(500)
	const calendar = document.querySelector("#vuecal .vuecal__bg")
	if (calendar)
		calendar.scrollTo({
			top: (60 / props.timeStep) * 8 * props.timeCellHeight,
			behavior: "smooth",
		})
}
</script>

<style>
.court1 {
	background-color: rgba(221, 238, 255, 0.5);
}

.court2 {
	background-color: rgba(255, 232, 251, 0.5);
}

.court3 {
	background-color: rgba(221, 255, 239, 0.5);
}

.court4 {
	background-color: rgba(255, 250, 196, 0.5);
}

.court5 {
	background-color: rgba(255, 206, 178, 0.5);
}

.court6 {
	background-color: rgba(0, 0, 0, 0.1);
}

.vuecal__no-event {
	display: none;
}

.vuecal__event {
	background-color: rgba(164, 230, 210, 0.9);
	border-radius: var(--border-radius);
}

.vuecal__event.extern {
	background-color: #fd9c42b9 !important;
}

.vuecal__cell--disabled {
	background-color: #a0a0a0 !important; /* Light grey background */
	color: #a0a0a0 !important; /* Grey text */
}
</style>
