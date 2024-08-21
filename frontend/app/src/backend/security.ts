import { Ref } from "vue"
import { auth } from "@/security/AuthService"
import axios from "axios"
import { useQuery } from "@tanstack/vue-query"

export function getCanCreate(isLoggedIn: Ref<boolean>) {
	return useQuery({
		queryKey: ["can_create", isLoggedIn],
		queryFn: fetchCanCreate,
		placeholderData: false,
		staleTime: 0,
	})
}

async function fetchCanCreate() {
	const user = await auth.getUser()
	if (user === null) return false

	return await axios
		.get<boolean>("/tournament/canCreate")
		.then((response) => {
			return response.data
		})
		.catch(() => {
			return false
		})
}

export function getCanEdit(tourId: string, isLoggedIn: Ref<boolean>) {
	return useQuery({
		queryKey: ["can_edit", tourId, isLoggedIn],
		queryFn: () => fetchCanEdit(tourId),
		placeholderData: false,
		staleTime: 0,
	})
}

async function fetchCanEdit(tourId: string) {
	const user = await auth.getUser()
	if (user === null) return false

	return await axios
		.get<boolean>(`/tournament/${tourId}/competition/canEdit`)
		.then((response) => {
			return response.data
		})
		.catch(() => {
			return false
		})
}
