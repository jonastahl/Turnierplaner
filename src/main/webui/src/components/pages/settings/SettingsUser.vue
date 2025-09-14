<template>
	<DataTable
		v-if="users"
		:value="users"
		:paginator="users.length > 5"
		:rows="5"
		striped-rows
		class="w-full mb-2"
		size="small"
	>
		<template #empty>
			{{ t("settings.no_unverified") }}
		</template>
		<Column field="username" :header="t('general.name')" />
		<Column field="role" header="Rolle">
			<template #body="{ data }">
				<Dropdown
					v-model="data.role"
					:options="options"
					@change="(event) => updateRole(data, event.value)"
				/>
			</template>
		</Column>
	</DataTable>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n"
import { getUsers, useUpdateUser } from "@/backend/user"
import { Role, User } from "@/interfaces/user"
import { ref } from "vue"

const { t } = useI18n()

const { data: users } = getUsers()
const { mutate: updateUserRole } = useUpdateUser()

const options = ref([Role.USER, Role.REPORTER, Role.DIRECTOR, Role.ADMIN])

function updateRole(user: User, role: Role) {
	updateUserRole({ id: user.id, username: user.username, role })
}
</script>

<style scoped></style>
