<template>
	<component :is="tag" :key="index" ref="container" v-bind="componentData">
		<slot
			v-for="(item, i) in list"
			:key="(<any>item)['itemKey']"
			:item="item"
			:index="i"
		/>
	</component>
</template>

<script setup lang="ts" generic="T">
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import Sortable from "sortablejs"
import { ref, watch } from "vue"

const props = withDefaults(
	defineProps<{
		list: T[]
		itemKey: string
		// eslint-disable-next-line @typescript-eslint/no-explicit-any
		tag: any
		// eslint-disable-next-line @typescript-eslint/no-explicit-any
		componentData?: any
		group: string
		pull?: boolean | "clone"
		put?: boolean | string[]
		animation?: number
		disabled?: boolean
		sort?: boolean
		ghost?: string
		single?: boolean
		// eslint-disable-next-line @typescript-eslint/no-explicit-any
		wrap?: (el: any, id: string) => T
	}>(),
	{
		componentData: {},
		animation: 150,
		disabled: false,
		sort: true,
		pull: true,
		put: true,
		ghost: "ghost",
		single: false,
		wrap: (e, _) => e,
	},
)

if (props.single && props.list.length > 1) {
	console.log("single")
	console.log(props.list)
	throw new Error("List of single cannot contain more than one element")
}

const container = ref<HTMLElement | null>(null)
const sortable = ref<Sortable | null>()
watch(container, () => {
	create()
})

const emit = defineEmits(["onRemove"])

interface Event extends Sortable.SortableEvent {
	originalEvent: {
		clientY: number
	}
}

function create() {
	let el = container.value
	if (!el) return
	if (!(el instanceof HTMLElement))
		// eslint-disable-next-line @typescript-eslint/ban-ts-comment
		// @ts-ignore
		el = <HTMLElement>el.$el

	if (sortable.value) {
		sortable.value?.destroy()
		sortable.value = null
	}

	let offsetY = 0
	sortable.value = Sortable.create(el, {
		group: { name: props.group, pull: props.pull, put: props.put },
		animation: props.animation,
		disabled: props.disabled,
		sort: props.sort,
		revertOnSpill: true,
		setData(dataTransfer) {
			dataTransfer.setData("event", JSON.stringify({ data: selectedElement }))
			dataTransfer.setData("cursor-grab-at", offsetY.toString())
		},
		onChoose: (event: Sortable.SortableEvent) => {
			if (event.oldIndex === undefined) return
			selectedElement = props.list[event.oldIndex]
			if ((<Event>event).originalEvent) {
				offsetY =
					(<Event>event).originalEvent.clientY -
					event.target.getBoundingClientRect().top
			}
		},
		onRemove: (event: Sortable.SortableEvent) => {
			if (event.oldIndex === undefined) return

			if (targetSingle) {
				// eslint-disable-next-line @typescript-eslint/ban-ts-comment
				// @ts-ignore
				props.list.splice(
					event.oldIndex,
					1,
					props.wrap(selectedElement, event.from.id),
				)
			} else {
				props.list.splice(event.oldIndex, 1)
				emit("onRemove")
			}
			reload()
		},
		onAdd: (event: Sortable.SortableEvent) => {
			if (event.newIndex === undefined) return

			targetSingle = props.single && props.list.length === 1

			if (props.single && props.list.length === 1) {
				// eslint-disable-next-line @typescript-eslint/ban-ts-comment
				// @ts-ignore
				selectedElement = props.list.splice(
					0,
					1,
					props.wrap(selectedElement, event.from.id),
				)[0]
			} else {
				// eslint-disable-next-line @typescript-eslint/ban-ts-comment
				// @ts-ignore
				props.list.splice(
					event.newIndex,
					0,
					props.wrap(selectedElement, event.from.id),
				)
			}
			reload()
		},
		onEnd: ({ from, to, oldIndex, newIndex }) => {
			if (from !== to) return
			if (oldIndex === undefined || newIndex === undefined) return

			const el = props.list.splice(oldIndex, 1)[0]
			if (el) props.list.splice(newIndex, 0, el)
			reload()
		},
		removeOnSpill: false,
	})
	setGhost()
}

watch(
	() => [props.group, props.put, props.pull],
	() => {
		if (!sortable.value) return
		sortable.value.option("group", {
			name: props.group,
			pull: props.pull,
			put: props.put,
		})
	},
)
watch(
	() => [props.animation, props.animation, props.sort],
	() => {
		if (!sortable.value) return
		sortable.value?.option("animation", props.animation)
		sortable.value?.option("disabled", props.disabled)
		sortable.value?.option("sort", props.sort)
	},
)

watch(() => [props.ghost, props.single, props.list], setGhost)

function setGhost() {
	if (!sortable.value) return
	sortable.value?.option(
		"ghostClass",
		props.single && props.list.length === 1 ? "swapGhost" : props.ghost,
	)
}

// reload list to ensure a consistent state
const index = ref(0)

function reload() {
	index.value++
}
</script>
<script lang="ts">
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
let selectedElement: T | null = null
let targetSingle = false
</script>

<style scoped></style>
