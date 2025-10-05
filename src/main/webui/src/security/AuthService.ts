import { User, UserManager } from "oidc-client-ts"
import { auth_settings, popup } from "./settings"
import { ref } from "vue"

class AuthService {
	userManager

	constructor() {
		this.userManager = new UserManager(auth_settings)
	}

	silentLogin() {
		return new Promise<void>((resolve, reject) => {
			this.userManager.getUser().then((user) => {
				if (user != null && !user.expired) {
					this.userManager
						.signinSilent()
						.then(() => {
							console.log("update token silently")
							access_token.value = user.access_token
							resolve()
						})
						.catch(() => {
							access_token.value = null
							reject()
						})
				} else {
					resolve()
				}
			})
		})
	}

	getUser() {
		return this.userManager.getUser()
	}

	lastLogin: Date | null = null
	login() {
		if (this.lastLogin != null && this.lastLogin.getTime() + 1000 * 2 > Date.now())
			return
		this.lastLogin = new Date()
		if (popup) return this.userManager.signinPopup()
		else return this.userManager.signinRedirect()
	}

	logout() {
		access_token.value = null
		if (popup) return this.userManager.signoutPopup()
		else return this.userManager.signoutRedirect()
	}

	addUserLoadedListener(cb: (user: User) => void) {
		this.userManager.events.addUserLoaded(cb)
	}

	addUserUnloadedListener(cb: () => void) {
		this.userManager.events.addUserUnloaded(cb)
	}
}

export let auth = new AuthService()
export function reloadAuth() {
	auth = new AuthService()
}
export const access_token = ref<string | null>(null)
