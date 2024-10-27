<template>
	<div class="formgrid grid">
		<div class="field col-12">
			<label for="name">{{ t("general.name") }}</label>
			<InputText
				id="name"
				v-model="name"
				:class="{ 'p-invalid': errors.name }"
				:disabled="disabled"
				class="w-full"
				maxlength="30"
				type="text"
				v-bind="nameAttrs"
			/>
			<InlineMessage v-if="errors.name" class="mt-2"
				>{{ t(errors.name || "") }}
			</InlineMessage>
		</div>
		<div class="field col-12">
			<label for="description">{{ t("general.description") }}</label>
			<textarea
				id="description"
				v-model="description"
				:disabled="disabled"
				class="text-base text-color surface-overlay p-2 border-1 border-solid surface-border border-round appearance-none outline-none focus:border-primary w-full"
				rows="4"
				type="text"
				v-bind="descriptionAttrs"
			></textarea>
		</div>
		<Divider align="left">
			<b>{{ t("CompetitionSettings.tournament_settings") }}</b>
		</Divider>
		<div class="field col-12 md:col-6 md:mr-8">
			<label for="name">{{ t("CompetitionSettings.type") }}</label>
			<Dropdown
				v-model="selectedTourType"
				:disabled="disabled"
				:options="[
					{
						name: t('CompetitionSettings.knockout'),
						value: CompType.KNOCKOUT,
					},
					{
						name: t('CompetitionSettings.groups'),
						value: CompType.GROUPS,
					},
				]"
				:placeholder="t(`CompetitionSettings.type`)"
				class="w-full"
				option-label="name"
				option-value="value"
				v-bind="selectedTourTypeAttrs"
			>
				<template #value="slotProps">
					<div v-if="slotProps.value" class="flex align-items-center">
						<span
							v-if="slotProps.value === 'GROUPS'"
							class="material-symbols-outlined mr-2"
							>groups</span
						>
						<span v-else class="material-symbols-outlined mr-2">grid_view</span>
						<div v-if="slotProps.value === 'GROUPS'" class="mt-1">
							{{ t("CompetitionSettings.groups") }}
						</div>
						<div v-else class="mt-1">
							{{ t("CompetitionSettings.knockout") }}
						</div>
					</div>
					<span v-else>
						{{ slotProps.placeholder }}
					</span>
				</template>
				<template #option="slotProps">
					<div class="flex flex-row align-items-center">
						<span
							v-if="slotProps.option.value === 'GROUPS'"
							class="material-symbols-outlined mr-2"
							>groups</span
						>
						<span v-else class="material-symbols-outlined mr-2">grid_view</span>
						<div class="mt-1">{{ slotProps.option.name }}</div>
					</div>
				</template>
			</Dropdown>
		</div>

		<div class="field col-6">
			<label for="name">{{ t("CompetitionSettings.mode") }}</label>
			<Dropdown
				v-model="selectedTourMode"
				:disabled="disabled"
				:options="[
					{ name: t('CompetitionSettings.single'), value: Mode.SINGLE },
					{ name: t('CompetitionSettings.double'), value: Mode.DOUBLE },
				]"
				:placeholder="t(`CompetitionSettings.mode`)"
				class="w-full"
				option-label="name"
				option-value="value"
				v-bind="selectedTourModeAttrs"
			>
				<template #value="slotProps">
					<div v-if="slotProps.value" class="flex align-items-center">
						<span
							v-if="slotProps.value === 'DOUBLE'"
							class="material-symbols-outlined mr-2"
						>
							group
						</span>
						<span v-else class="material-symbols-outlined mr-2">person</span>
						<div v-if="slotProps.value === 'DOUBLE'" class="mt-1">
							{{ t("CompetitionSettings.double") }}
						</div>
						<div v-else class="mt-1">
							{{ t("CompetitionSettings.single") }}
						</div>
					</div>
					<span v-else>
						{{ slotProps.placeholder }}
					</span>
				</template>
				<template #option="slotProps">
					<div class="flex align-items-center">
						<span
							v-if="slotProps.option.value === 'DOUBLE'"
							class="material-symbols-outlined mr-2"
							>group</span
						>
						<span v-else class="material-symbols-outlined mr-2">person</span>
						<div class="mt-1">{{ slotProps.option.name }}</div>
					</div>
				</template>
			</Dropdown>
		</div>

		<div
			v-if="values.mode === Mode.DOUBLE"
			class="field col-6 flex flex-column"
		>
			<label for="name">{{ t("CompetitionSettings.signup") }}</label>
			<Dropdown
				v-model="signUp"
				:disabled="disabled"
				:options="[
					{
						name: t('CompetitionSettings.individual'),
						value: SignUp.INDIVIDUAL,
					},
					{
						name: t('CompetitionSettings.together'),
						value: SignUp.TOGETHER,
					},
				]"
				:placeholder="t(`CompetitionSettings.signup`)"
				class="w-full"
				option-label="name"
				option-value="value"
				v-bind="signUpAttrs"
			>
				<template #value="slotProps">
					<div v-if="slotProps.value" class="flex align-items-center">
						<span
							v-if="slotProps.value == 'TOGETHER'"
							class="material-symbols-outlined mr-2"
						>
							group_work
						</span>
						<span v-else class="material-symbols-outlined mr-2"
							>workspaces</span
						>
						<div v-if="slotProps.value === 'TOGETHER'" class="mt-1">
							{{ t("CompetitionSettings.together") }}
						</div>
						<div v-else class="mt-1">
							{{ t("CompetitionSettings.individual") }}
						</div>
					</div>
					<span v-else>
						{{ slotProps.placeholder }}
					</span>
				</template>
				<template #option="slotProps">
					<div class="flex align-items-center">
						<span
							v-if="slotProps.option.value === 'TOGETHER'"
							class="material-symbols-outlined mr-2"
							>group_work</span
						>
						<span v-else class="material-symbols-outlined mr-2"
							>workspaces</span
						>
						<div>{{ slotProps.option.name }}</div>
					</div>
				</template>
			</Dropdown>
		</div>

		<div class="field col-12 md:col-6 md:mr-8">
			<label for="numberSets">{{ t("CompetitionSettings.numberSets") }}</label>
			<Dropdown
				v-model="numberSets"
				v-bind="numberSetsAtrrs"
				:options="numberSetsOptions"
				option-label="name"
				option-value="value"
				:placeholder="t('CompetitionSettings.numberSets')"
				class="w-full"
			/>
		</div>

		<Divider align="left">
			<b>{{ t("CompetitionSettings.player") }}</b>
		</Divider>
		<div
			v-if="values.mode === Mode.DOUBLE"
			class="field col-12 flex flex-row align-items-center mb-4"
		>
			<Checkbox
				id="playerB_different"
				v-model="playerB_different"
				:binary="true"
				:disabled="disabled"
				input-id="playerB_different_input"
				v-bind="playerB_differentAttrs"
			/>
			<label class="ml-2 mb-0 mt-1" for="playerB_different_input">{{
				t("CompetitionSettings.differentB")
			}}</label>
		</div>

		<div v-if="values.playerB_different" class="col-6">
			<Divider align="left" class="col-6">
				{{ t("CompetitionSettings.playerA") }}
			</Divider>
		</div>
		<div v-if="values.playerB_different" class="col-6">
			<Divider align="left" class="col-6">
				{{ t("CompetitionSettings.playerB") }}
			</Divider>
		</div>

		<div
			:class="{
				'col-12': !values.playerB_different,
				'col-6': values.playerB_different,
			}"
			class="field flex flex-column"
		>
			<label class="text-900" for="playerASex">{{
				t("CompetitionSettings.sex")
			}}</label>
			<Dropdown
				v-model="playerASex"
				v-bind="playerASexAttrs"
				:disabled="disabled"
				:options="[
					{ name: t('general.male'), value: Sex.MALE },
					{ name: t('general.female'), value: Sex.FEMALE },
					{ name: t('CompetitionSettings.any'), value: Sex.ANY },
				]"
				:placeholder="t(`CompetitionSettings.sex`)"
				class="w-full"
				option-label="name"
				option-value="value"
			>
				<template #option="slotProps">
					<div>
						{{ slotProps.option.name }}
					</div>
				</template>
			</Dropdown>
		</div>
		<div v-if="values.playerB_different" class="field col-6 flex flex-column">
			<label class="text-900" for="playerBSex">{{
				t("CompetitionSettings.sex")
			}}</label>
			<Dropdown
				v-model="playerBSex"
				:disabled="disabled"
				:options="[
					{ name: t('general.male'), value: Sex.MALE },
					{ name: t('general.female'), value: Sex.FEMALE },
					{ name: t('CompetitionSettings.any'), value: Sex.ANY },
				]"
				:placeholder="t(`CompetitionSettings.sex`)"
				class="w-full"
				option-label="name"
				option-value="value"
				v-bind="playerBSexAttrs"
			>
				<template #option="slotProps">
					<div>{{ slotProps.option.name }}</div>
				</template>
			</Dropdown>
		</div>

		<div
			:class="{
				'col-12': !values.playerB_different,
				'col-6': values.playerB_different,
			}"
			class="field flex flex-column"
		>
			<label class="text-900" for="minAge">{{
				t("CompetitionSettings.minAge")
			}}</label>
			<div class="flex flex-row align-items-center gap-2">
				<Checkbox
					id="minAgeA"
					v-model="playerAHasMinAge"
					:binary="true"
					:disabled="disabled"
					v-bind="playerAHasMinAgeAttrs"
				/>

				<Calendar
					id="minAgeA"
					v-model="playerAMinAge"
					:date-format="t('date_format')"
					:disabled="!playerAHasMinAge || disabled"
					:manual-input="false"
					class="w-full"
					show-icon
					v-bind="playerAMinAgeAttrs"
				/>
			</div>
		</div>
		<div v-if="values.playerB_different" class="field flex flex-column col-6">
			<label class="text-900" for="minAge">{{
				t("CompetitionSettings.minAge")
			}}</label>
			<div class="flex flex-row align-items-center gap-2">
				<Checkbox
					id="minAgeB"
					v-model="playerBHasMinAge"
					:binary="true"
					v-bind="playerBHasMinAgeAttrs"
				/>
				<Calendar
					id="minAgeB"
					v-model="playerBMinAge"
					:date-format="t('date_format')"
					:disabled="!playerBHasMinAge || disabled"
					:manual-input="false"
					class="w-full"
					show-icon
					v-bind="playerBMinAgeAttrs"
				/>
			</div>
		</div>

		<div
			:class="{
				'col-12': !values.playerB_different,
				'col-6': values.playerB_different,
			}"
			class="field flex flex-column"
		>
			<label class="text-900" for="minAge">{{
				t("CompetitionSettings.maxAge")
			}}</label>
			<div class="flex flex-row align-items-center gap-2">
				<Checkbox
					id="maxAgeA"
					v-model="playerAHasMaxAge"
					:binary="true"
					:disabled="disabled"
					v-bind="playerAHasMaxAgeAttrs"
				/>

				<Calendar
					id="maxAgeA"
					v-model="playerAMaxAge"
					:date-format="t('date_format')"
					:disabled="!playerAHasMaxAge || disabled"
					:manual-input="false"
					class="w-full"
					show-icon
					v-bind="playerAMaxAgeAttrs"
				/>
			</div>
		</div>
		<div v-if="values.playerB_different" class="field flex flex-column col-6">
			<label class="text-900" for="minAge">{{
				t("CompetitionSettings.maxAge")
			}}</label>
			<div class="flex flex-row align-items-center gap-2">
				<Checkbox
					id="maxAgeB"
					v-model="playerBHasMaxAge"
					:binary="true"
					:disabled="disabled"
					v-bind="playerBHasMaxAgeAttrs"
				/>

				<Calendar
					id="maxAgeB"
					v-model="playerBMaxAge"
					:date-format="t('date_format')"
					:disabled="!playerBHasMaxAge || disabled"
					:manual-input="false"
					class="w-full"
					show-icon
					v-bind="playerBMaxAgeAttrs"
				/>
			</div>
		</div>
	</div>
</template>

<script lang="ts" setup>
import { ref } from "vue"
import { useI18n } from "vue-i18n"
import {
	Competition,
	CompetitionForm,
	competitionFormToServer,
	Mode,
	NumberSets,
	Sex,
	SignUp,
	CompType,
} from "@/interfaces/competition"

import { useForm } from "vee-validate"

import { boolean, date, mixed, object, string } from "yup"
import { toTypedSchema } from "@vee-validate/yup"

const { t } = useI18n()

const props = withDefaults(
	defineProps<{
		disabled: boolean
		competition: Competition
	}>(),
	{
		disabled: false,
	},
)

const { values, defineField, errors, handleSubmit } = useForm<CompetitionForm>({
	validationSchema: toTypedSchema(
		object({
			name: string().min(4).max(40).required(),
			description: string().max(255),
			tourType: mixed().oneOf(Object.values(CompType)).required(),
			mode: mixed().oneOf(Object.values(Mode)).required(),
			signUp: mixed().oneOf(Object.values(SignUp)).required(),
			numberSets: mixed().oneOf(Object.values(NumberSets)).required(),

			playerA_Sex: mixed().oneOf(Object.values(Sex)).required(),
			playerA_hasMinAge: boolean(),
			playerA_minAge: date(),
			playerA_hasMaxAge: boolean(),
			playerA_maxAge: date(),

			playerB_different: boolean(),
			playerB_Sex: mixed().oneOf(Object.values(Sex)),
			playerB_hasMinAge: boolean(),
			playerB_minAge: date(),
			playerB_hasMaxAge: boolean(),
			playerB_maxAge: date(),
		}),
	),
	initialValues: {
		name: props.competition.name ? props.competition.name : undefined,
		description: props.competition.description,
		tourType: props.competition.tourType,
		mode: props.competition.mode,
		signUp: props.competition.signUp,
		numberSets: props.competition.numberSets,

		playerA_Sex: props.competition.playerA.sex,
		playerA_hasMinAge: props.competition.playerA.hasMinAge,
		playerA_minAge: props.competition.playerA.minAge
			? props.competition.playerA.minAge
			: new Date(),
		playerA_hasMaxAge: props.competition.playerA.hasMaxAge,
		playerA_maxAge: props.competition.playerA.maxAge
			? props.competition.playerA.maxAge
			: new Date(),

		playerB_different: props.competition.playerB.different,
		playerB_Sex: props.competition.playerB.sex,
		playerB_hasMinAge: props.competition.playerB.hasMinAge,
		playerB_minAge: props.competition.playerB.minAge
			? props.competition.playerB.minAge
			: new Date(),
		playerB_hasMaxAge: props.competition.playerB.hasMaxAge,
		playerB_maxAge: props.competition.playerB.maxAge
			? props.competition.playerB.maxAge
			: new Date(),
	},
})

const [name, nameAttrs] = defineField("name")
const [description, descriptionAttrs] = defineField("description")
const [selectedTourType, selectedTourTypeAttrs] = defineField("tourType")
const [selectedTourMode, selectedTourModeAttrs] = defineField("mode")
const [signUp, signUpAttrs] = defineField("signUp")
const [numberSets, numberSetsAtrrs] = defineField("numberSets")
const [playerB_different, playerB_differentAttrs] =
	defineField("playerB_different")
const [playerASex, playerASexAttrs] = defineField("playerA_Sex")
const [playerBSex, playerBSexAttrs] = defineField("playerB_Sex")
const [playerAHasMinAge, playerAHasMinAgeAttrs] =
	defineField("playerA_hasMinAge")
const [playerBHasMinAge, playerBHasMinAgeAttrs] =
	defineField("playerB_hasMinAge")
const [playerAHasMaxAge, playerAHasMaxAgeAttrs] =
	defineField("playerA_hasMaxAge")
const [playerBHasMaxAge, playerBHasMaxAgeAttrs] =
	defineField("playerB_hasMaxAge")
const [playerAMinAge, playerAMinAgeAttrs] = defineField("playerA_minAge")
const [playerAMaxAge, playerAMaxAgeAttrs] = defineField("playerA_maxAge")
const [playerBMinAge, playerBMinAgeAttrs] = defineField("playerB_minAge")
const [playerBMaxAge, playerBMaxAgeAttrs] = defineField("playerB_maxAge")

const numberSetsOptions = ref([
	{ name: "3", value: NumberSets.THREE },
	{ name: "5", value: NumberSets.FIVE },
])

const onSubmit = handleSubmit(
	(values) => {
		emit(
			"submit",
			competitionFormToServer(
				<CompetitionForm>values,
				<string | null>props.competition.id,
			),
		)
	},
	(ctx) => {
		console.log("Validation error #TODO")
		console.log(ctx.errors)
	},
)
defineExpose({ onSubmit })
const emit = defineEmits(["submit"])
</script>

<style scoped></style>
