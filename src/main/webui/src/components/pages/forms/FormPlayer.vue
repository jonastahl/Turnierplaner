<template>
	<div class="formgrid grid">
		<div class="field col-6">
			<label for="first_name">{{
				t("ViewPlayerRegistration.first_name.field")
			}}</label>
			<InputText
				id="first_name"
				v-model="firstName"
				:disabled="disabled"
				type="text"
				v-bind="firstNameAttrs"
				maxlength="40"
				class="w-full"
				:class="{ 'p-invalid': errors.firstName }"
			/>
			<InlineMessage v-if="errors.firstName" class="mt-2"
				>{{ t(errors.firstName || "") }}
			</InlineMessage>
		</div>
		<div class="field col-6">
			<label for="name">{{
				t("ViewPlayerRegistration.last_name.field")
			}}</label>
			<InputText
				id="name"
				v-model="lastName"
				:disabled="disabled"
				type="text"
				v-bind="lastNameAttrs"
				maxlength="40"
				class="w-full"
				:class="{ 'p-invalid': errors.lastName }"
			/>
			<InlineMessage v-if="errors.lastName" class="mt-2"
				>{{ t(errors.lastName || "") }}
			</InlineMessage>
		</div>
		<div class="field col-6">
			<label for="birthdate">{{
				t("ViewPlayerRegistration.birthdate.field")
			}}</label>
			<Calendar
				v-model="birthday"
				show-icon
				:disabled="disabled"
				class="w-full"
				v-bind="birthdayAttrs"
				:manual-input="false"
				:date-format="t('date_format')"
				:class="{ 'p-invalid': errors.birthday }"
			/>
			<InlineMessage v-if="errors.birthday" class="mt-2"
				>{{ t(errors.birthday || "") }}
			</InlineMessage>
		</div>
		<div class="field col-6">
			<label for="sex">{{ t("ViewPlayerRegistration.sex.field") }}</label>
			<Dropdown
				v-model="sex"
				v-bind="sexAttrs"
				:disabled="disabled"
				:options="[
					{ name: t('general.male'), value: Sex.MALE },
					{ name: t('general.female'), value: Sex.FEMALE },
				]"
				option-label="name"
				option-value="value"
				:placeholder="t(`CompetitionSettings.sex`)"
				class="w-full"
				:class="{ 'p-invalid': errors.sex }"
			>
				<template #option="slotProps">
					<div>
						{{ slotProps.option.name }}
					</div>
				</template>
			</Dropdown>
			<InlineMessage v-if="errors.sex" class="mt-2"
				>{{ t(errors.sex || "") }}
			</InlineMessage>
		</div>
		<div class="field col-6">
			<label for="language">{{
				t("ViewPlayerRegistration.language.field")
			}}</label>
			<Dropdown
				v-model="language"
				v-bind="languageAttrs"
				:disabled="disabled"
				:options="[
					{ name: t('language.en'), value: Language.EN },
					{ name: t('language.de'), value: Language.DE },
				]"
				option-label="name"
				option-value="value"
				:placeholder="t(`settings.language`)"
				class="w-full"
				:class="{ 'p-invalid': errors.language }"
			>
				<template #option="slotProps">
					<div>
						{{ slotProps.option.name }}
					</div>
				</template>
			</Dropdown>
			<InlineMessage v-if="errors.language" class="mt-2"
				>{{ t(errors.language || "") }}
			</InlineMessage>
		</div>
		<div class="field col-12">
			<label for="email">{{ t("ViewPlayerRegistration.email.field") }}</label>
			<InputText
				id="email"
				v-model="email"
				:disabled="disabled"
				type="text"
				v-bind="emailAttrs"
				maxlength="100"
				class="w-full"
				:class="{ 'p-invalid': errors.email }"
			/>
			<InlineMessage v-if="errors.email" class="mt-2"
				>{{ t(errors.email || "") }}
			</InlineMessage>
		</div>
	</div>
</template>

<script lang="ts" setup>
import { useI18n } from "vue-i18n"
import { Language, Sex } from "@/interfaces/competition"
import { useForm } from "vee-validate"
import { toTypedSchema } from "@vee-validate/yup"
import { date, mixed, object, string } from "yup"
import { PlayerRegistration, PlayerRegistrationForm } from "@/interfaces/player"

const { t } = useI18n()

const props = withDefaults(
	defineProps<{
		disabled: boolean
		player: PlayerRegistrationForm
	}>(),
	{
		disabled: false,
	},
)

const { defineField, errors, handleSubmit } = useForm<PlayerRegistration>({
	validationSchema: toTypedSchema(
		object({
			firstName: string().min(4).max(40).required(),
			lastName: string().min(4).max(40).required(),
			sex: mixed().oneOf(Object.values(Sex)).required(),
			birthday: date().required(),
			language: mixed().oneOf(Object.values(Language)).required(),
			email: string().max(100).email().required(),
		}),
	),
	initialValues: {
		firstName: props.player.firstName,
		lastName: props.player.lastName,
		sex: props.player.sex,
		birthday: props.player.birthday,
		language: props.player.language,
		email: props.player.email,
	},
})

const [firstName, firstNameAttrs] = defineField("firstName")
const [lastName, lastNameAttrs] = defineField("lastName")
const [sex, sexAttrs] = defineField("sex")
const [language, languageAttrs] = defineField("language")
const [birthday, birthdayAttrs] = defineField("birthday")
const [email, emailAttrs] = defineField("email")

const onSubmit = handleSubmit((values) => {
	emit("submit", <PlayerRegistration>(<unknown>values))
})
defineExpose({ onSubmit })
const emit = defineEmits<{
	submit: [PlayerRegistration]
}>()
</script>

<style scoped></style>
