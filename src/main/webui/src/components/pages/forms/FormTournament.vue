<template>
	<div class="formgrid grid">
		<div class="field flex w-full justify-content-end">
			<div class="col" style="flex-grow: 3">
				<label for="name">{{ t("general.name") }}</label>
				<InputText
					id="name"
					v-model="name"
					type="text"
					v-bind="nameAttrs"
					maxlength="30"
					class="w-full"
					:class="{ 'p-invalid': errors.name }"
					:disabled="disabled"
				/>
				<InlineMessage v-if="errors.name" class="mt-2"
					>{{ t(errors.name || "") }}
				</InlineMessage>
			</div>
			<div class="col flex flex-column min-w-max flex-grow-0">
				<label for="visible" class="text-900">{{
					t("TournamentSettings.visible")
				}}</label>
				<SelectButton
					v-model="visible"
					v-bind="visibleAttrs"
					:unselectable="false"
					:options="[
						{ name: t('no'), value: false },
						{ name: t('yes'), value: true },
					]"
					option-label="name"
					option-value="value"
					:disabled="disabled"
				/>
			</div>
		</div>
		<div class="field col-12">
			<label for="description">{{ t("general.description") }}</label>
			<Textarea
				id="description"
				v-model="description"
				type="text"
				rows="4"
				maxlength="255"
				class="w-full"
				v-bind="descriptionAttrs"
				:disabled="disabled"
			></Textarea>
		</div>
		<div class="field col-6">
			<label for="registration">{{
				t("TournamentSettings.registration_phase")
			}}</label>
			<Calendar
				id="registration"
				v-model="registrationPhase"
				v-bind="registrationPhaseAttrs"
				selection-mode="range"
				:manual-input="false"
				class="w-full"
				:date-format="t('date_format')"
				show-time
				show-icon
				:class="{ 'p-invalid': errors.registration_phase }"
				:disabled="disabled"
			/>
			<InlineMessage v-if="errors.registration_phase" class="mt-2"
				>{{ t(errors.registration_phase || "") }}
			</InlineMessage>
		</div>
		<div class="field col-6">
			<label for="game_phase">{{ t("TournamentSettings.game_phase") }}</label>
			<Calendar
				id="game_phase"
				v-model="gamePhase"
				v-bind="gamePhaseAttrs"
				selection-mode="range"
				:manual-input="false"
				class="w-full"
				:date-format="t('date_format')"
				show-time
				show-icon
				:class="{ 'p-invalid': errors.game_phase }"
				:disabled="disabled"
			/>
			<InlineMessage v-if="errors.game_phase" class="mt-2"
				>{{ t(errors.game_phase || "") }}
			</InlineMessage>
		</div>
	</div>
</template>

<script lang="ts" setup>
import {
	Tournament,
	TournamentForm,
	tournamentFormClientToServer,
	tournamentToForm,
} from "@/interfaces/tournament"
import { useI18n } from "vue-i18n"

import { useForm } from "vee-validate"

import { array, boolean, date, mixed, object, setLocale, string } from "yup"
import { toTypedSchema } from "@vee-validate/yup"

const { t } = useI18n()

const props = withDefaults(
	defineProps<{
		disabled: boolean
		tourDetails?: Tournament
	}>(),
	{
		disabled: false,
		tourDetails: undefined,
	},
)
const data = props.tourDetails
	? tournamentToForm(props.tourDetails)
	: {
			name: "",
			visible: false,
			description: "",
			registration_phase: undefined,
			game_phase: undefined,
		}

setLocale({
	mixed: {
		notNull: "validation.date_missing",
	},
})

const { defineField, errors, handleSubmit } = useForm<TournamentForm>({
	validationSchema: toTypedSchema(
		object({
			name: string().required().min(4),
			visible: boolean(),
			description: string().max(255),
			registration_phase: array()
				.length(2, "validation.date_missing")
				.of(mixed().nonNullable())
				.of(date())
				.test(
					"correctDates",
					"TournamentSettings.wrong_dates",
					(arr, context) => {
						if (!context.parent.game_phase?.[0]) return true
						return (
							context.parent.registration_phase[1] <
							context.parent.game_phase[0]
						)
					},
				)
				.required("validation.date_missing"),
			game_phase: array()
				.length(2, "validation.date_missing")
				.of(mixed().nonNullable())
				.of(date())
				.test(
					"correctDates",
					"TournamentSettings.wrong_dates",
					(arr, context) => {
						if (!context.parent.registration_phase?.[1]) return true
						return (
							context.parent.registration_phase[1] <
							context.parent.game_phase[0]
						)
					},
				)
				.required("validation.date_missing"),
		}),
	),
	initialValues: {
		name: data.name,
		visible: data.visible,
		description: data.description,
		registration_phase: data.registration_phase,
		game_phase: data.game_phase,
	},
})

const [name, nameAttrs] = defineField("name")
const [visible, visibleAttrs] = defineField("visible")
const [description, descriptionAttrs] = defineField("description")

const [registrationPhase, registrationPhaseAttrs] =
	defineField("registration_phase")
const [gamePhase, gamePhaseAttrs] = defineField("game_phase")

const onSubmit = handleSubmit((values) => {
	values.id = data.id
	emit("submit", tournamentFormClientToServer(<TournamentForm>values))
})
defineExpose({ onSubmit })
const emit = defineEmits(["submit"])
</script>

<style scoped></style>
