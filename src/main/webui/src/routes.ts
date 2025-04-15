export default [
	{
		path: "/",
		name: "Tournaments",
		component: () =>
			import("@/components/views/tournaments/ViewTournaments.vue"),
	},
	{
		path: "/settings",
		name: "Settings",
		component: () => import("@/components/views/settings/ViewSettings.vue"),
	},
	{
		path: "/createTournament",
		name: "Create tournament",
		component: () =>
			import("@/components/views/tournaments/ViewCreateTournament.vue"),
	},
	{
		path: "/tournament/:tourId/edit",
		name: "Edit tournament",
		component: () =>
			import("@/components/views/tournaments/ViewEditTournament.vue"),
	},
	{
		path: "/tournament/:tourId",
		name: "Competitions",
		component: () =>
			import("@/components/views/competitions/ViewCompetitions.vue"),
	},
	{
		path: "/tournament/:tourId/createCompetition",
		name: "Create competition",
		component: () =>
			import("@/components/views/competitions/ViewCreateCompetition.vue"),
	},
	{
		path: "/tournament/:tourId/overview",
		name: "Matches overview",
		component: () =>
			import("@/components/views/overview/ViewTournamentOverview.vue"),
	},
	{
		path: "/tournament/:tourId/competition/:compId",
		name: "Competition",
		component: () =>
			import("@/components/views/competition/ViewCompetition.vue"),
	},
	{
		path: "/tournament/:tourId/manage",
		name: "Manage competition",
		component: () =>
			import("@/components/views/compManage/ViewManageCompetitions.vue"),
		children: [
			{
				path: "settings/:compId?",
				name: "Manage settings",
				component: () =>
					import("@/components/views/compManage/settings/ViewSettings.vue"),
				meta: {
					overview: false,
					mStep: "Manage settings",
				},
			},
			{
				path: "prepare/:compId?",
				name: "Manage prepare",
				component: () =>
					import(
						"@/components/views/compManage/prepare/ViewPrepareMatches.vue"
					),
				meta: {
					overview: true,
					mStep: "Manage prepare",
				},
				children: [
					{
						path: "editTeams",
						name: "editTeams",
						component: () =>
							import(
								"@/components/views/compManage/prepare/editTeams/ViewEditTeams.vue"
							),
						meta: { step: 1, reset: true },
					},
					{
						path: "assignMatches",
						name: "assignMatches",
						component: () =>
							import(
								"@/components/views/compManage/prepare/assignMatches/ViewAssignMatches.vue"
							),
						meta: { step: 2, reset: true },
					},
					{
						path: "scheduleMatches",
						name: "scheduleMatches",
						component: () =>
							import(
								"@/components/views/compManage/prepare/scheduleMatches/ViewScheduleMatches.vue"
							),
						meta: { step: 3, reset: false },
					},
				],
			},
			{
				path: "execution/:compId",
				name: "Manage execution",
				component: () =>
					import("@/components/views/compManage/settings/ViewSettings.vue"),
				meta: {
					overview: true,
					mStep: "Manage execution",
				},
			},
		],
	},
	{
		path: "/player/registration",
		name: "Player registration",
		component: () =>
			import("@/components/views/player/ViewPlayerRegistration.vue"),
	},
	{
		path: "/player/update/:playerId",
		name: "Update player",
		component: () => import("@/components/views/player/ViewPlayerUpdate.vue"),
	},
	{
		path: "/verification",
		name: "Player verified",
		component: () => import("@/components/views/player/ViewVerification.vue"),
	},
	{
		path: "/player/overview/:playerId",
		name: "Player overview",
		component: () => import("@/components/views/player/ViewPlayerOverview.vue"),
	},
	{
		path: "/templates",
		name: "Templates",
		component: () => import("@/components/views/ViewTemplates.vue"),
	},
	{
		path: "/impressum",
		name: "Impressum",
		component: () => import("@/components/views/ViewImpressum.vue"),
	},
]
