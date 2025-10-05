import { settings } from "@/settings"
import { UserManagerSettings, WebStorageStateStore } from "oidc-client-ts"

function buildSettings(): UserManagerSettings {
	return {
		authority: settings.AUTH_DOMAIN,
		client_id: "frontend-vue",
		response_type: "code",
		scope: "openid profile",

		redirect_uri: settings.FRONTEND + "/callbackSignIn.html",
		post_logout_redirect_uri: settings.FRONTEND + "/callbackSignOut.html",

		popup_redirect_uri: settings.FRONTEND + "/callbackSignIn.html",
		popup_post_logout_redirect_uri: settings.FRONTEND + "/callbackSignOut.html",

		automaticSilentRenew: true,
		filterProtocolClaims: true,

		userStore: new WebStorageStateStore({ store: window.localStorage }),
	}
}

export const auth_settings = buildSettings()

export function rebuildAuthSettings() {
	Object.assign(auth_settings, buildSettings())
}

rebuildAuthSettings()

export const popup = true
