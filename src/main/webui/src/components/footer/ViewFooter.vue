<template>
	<div id="footer" class="bg-primary-900 p-6 flex justify-content-between mt-4">
		<div class="flex items-center sm:flex-row flex-column gap-2">
			<div class="flex gap-2">
				<a
					href="https://github.com/jonastahl/Turnierplaner"
					target="_blank"
					rel="noopener noreferrer"
					class="hover:text-primary transition-colors text-white"
				>
					<i class="pi pi-globe text-xl"></i>
				</a>
				<a
					href="https://github.com/jonastahl/Turnierplaner"
					target="_blank"
					rel="noopener noreferrer"
					class="hover:text-primary transition-colors text-white"
				>
					<i class="pi pi-github text-xl"></i>
				</a>
			</div>
			<a
				class="text-white underline transition-colors cursor-pointer"
				:href="config?.legalNotice"
				target="_blank"
				rel="noopener"
			>
				{{ t("footer.legal_notice") }}
			</a>
			<a
				class="text-white underline transition-colors cursor-pointer"
				:href="config?.privacyPolicy"
				target="_blank"
				rel="noopener"
			>
				{{ t("footer.privacy_policy") }}
			</a>
		</div>
		<!-- eslint-disable-next-line @intlify/vue-i18n/no-raw-text -->
		<span>Turnierplaner v{{ version }}</span>
	</div>
</template>

<script setup lang="ts">
import { version } from "../../../package.json"
import { useI18n } from "vue-i18n"
import { getConfig } from "@/backend/config"
import { inject, ref } from "vue"

const { t } = useI18n()

const isLoggedIn = inject("loggedIn", ref(false))
const { data: config } = getConfig(isLoggedIn)
</script>

<style scoped>
footer {
	height: 70px;
	display: flex;
	flex-direction: row;
	flex-wrap: nowrap;
	align-items: center;
	justify-content: space-between;
	position: sticky;
	left: 0;
	right: 0;
	top: 0;
	background-color: white;
}

#footer {
	z-index: 10;
	box-shadow: 0 -10px 15px rgb(0, 0, 0, 0.2);
}
</style>
