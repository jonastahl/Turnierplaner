export enum Routes {
	Tournaments = "tournaments",
	Settings = "settings",
	CreateTournament = "createTournament",
	EditTournament = "editTournament",
	Competitions = "competitions",
	CreateCompetition = "createCompetition",
	MatchesOverview = "matchesOverview",
	Competition = "competition",
	ManageCompetition = "manageCompetition",
	ManageSettings = "manageSettings",
	ManagePrepare = "managePrepare",
	EditTeams = "editTeams",
	AssignMatches = "assignMatches",
	ScheduleMatches = "scheduleMatches",
	ManageExecution = "manageExecution",
	ManageExecutionCalendar = "manageExecutionCalendar",
	ManageExecutionPlans = "manageExecutionPlans",
	PlayerRegistration = "playerRegistration",
	UpdatePlayer = "updatePlayer",
	PlayerVerified = "playerVerified",
	PlayerOverview = "playerOverview",
	Templates = "templates",
	Impressum = "impressum",
}

export default [
	{
		path: "/",
		name: Routes.Tournaments,
		component: () =>
			import("@/components/views/tournaments/ViewTournaments.vue"),
	},
	{
		path: "/settings",
		name: Routes.Settings,
		component: () => import("@/components/views/settings/ViewSettings.vue"),
	},
	{
		path: "/createTournament",
		name: Routes.CreateTournament,
		component: () =>
			import("@/components/views/tournaments/ViewCreateTournament.vue"),
	},
	{
		path: "/tournament/:tourId/edit",
		name: Routes.EditTournament,
		component: () =>
			import("@/components/views/tournaments/ViewEditTournament.vue"),
	},
	{
		path: "/tournament/:tourId",
		name: Routes.Competitions,
		component: () =>
			import("@/components/views/competitions/ViewCompetitions.vue"),
	},
	{
		path: "/tournament/:tourId/createCompetition",
		name: Routes.CreateCompetition,
		component: () =>
			import("@/components/views/competitions/ViewCreateCompetition.vue"),
	},
	{
		path: "/tournament/:tourId/overview",
		name: Routes.MatchesOverview,
		component: () =>
			import("@/components/views/overview/ViewTournamentOverview.vue"),
	},
	{
		path: "/tournament/:tourId/competition/:compId",
		name: Routes.Competition,
		component: () =>
			import("@/components/views/competition/ViewCompetition.vue"),
	},
	{
		path: "/tournament/:tourId/manage",
		name: Routes.ManageCompetition,
		component: () =>
			import("@/components/views/compManage/ViewManageCompetitions.vue"),
		children: [
			{
				path: "settings/:compId?",
				name: Routes.ManageSettings,
				component: () =>
					import("@/components/views/compManage/settings/ViewSettings.vue"),
				meta: {
					overview: false,
					compSelector: true,
					managesel: Routes.ManageSettings,
					managepage: Routes.ManageSettings,
				},
			},
			{
				path: "prepare/:compId?",
				name: Routes.ManagePrepare,
				component: () =>
					import(
						"@/components/views/compManage/prepare/ViewPrepareMatches.vue"
					),
				meta: {
					overview: true,
					compSelector: true,
					managesel: Routes.ManagePrepare,
					managepage: Routes.ManagePrepare,
				},
				children: [
					{
						path: "editTeams",
						name: Routes.EditTeams,
						component: () =>
							import(
								"@/components/views/compManage/prepare/editTeams/ViewEditTeams.vue"
							),
						meta: { step: 1, reset: true },
					},
					{
						path: "assignMatches",
						name: Routes.AssignMatches,
						component: () =>
							import(
								"@/components/views/compManage/prepare/assignMatches/ViewAssignMatches.vue"
							),
						meta: { step: 2, reset: true },
					},
					{
						path: "scheduleMatches",
						name: Routes.ScheduleMatches,
						component: () =>
							import(
								"@/components/views/compManage/prepare/scheduleMatches/ViewScheduleMatches.vue"
							),
						meta: { step: 3, reset: false },
					},
				],
			},
			{
				path: "execution",
				component: () =>
					import("@/components/views/compManage/execution/ViewExecution.vue"),
				meta: {
					overview: false,
					compSelector: false,
					managesel: Routes.ManageExecution,
					executionsel: Routes.ManageExecution,
					executionMenu: true,
				},
				children: [
					{
						path: "overview/:compId?",
						name: Routes.ManageExecution,
						meta: {
							executionsel: Routes.ManageExecution,
						},
						component: () =>
							import(
								"@/components/views/compManage/execution/ViewExecutionOverview.vue"
							),
					},
					{
						path: "calendar/:compId?",
						name: Routes.ManageExecutionCalendar,
						meta: {
							executionsel: Routes.ManageExecutionCalendar,
						},
						component: () =>
							import(
								"@/components/views/compManage/execution/ViewExecutionCalendar.vue"
							),
					},
					{
						path: "plans/:compId?",
						name: Routes.ManageExecutionPlans,
						meta: {
							compSelector: true,
							executionsel: Routes.ManageExecutionPlans,
							managepage: Routes.ManageExecutionPlans,
						},
						component: () =>
							import(
								"@/components/views/compManage/execution/ViewExecutionPlans.vue"
							),
					},
				],
			},
		],
	},
	{
		path: "/player/registration",
		name: Routes.PlayerRegistration,
		component: () =>
			import("@/components/views/player/ViewPlayerRegistration.vue"),
	},
	{
		path: "/player/update/:playerId",
		name: Routes.UpdatePlayer,
		component: () => import("@/components/views/player/ViewPlayerUpdate.vue"),
	},
	{
		path: "/verification",
		name: Routes.PlayerVerified,
		component: () => import("@/components/views/player/ViewVerification.vue"),
	},
	{
		path: "/player/overview/:playerId",
		name: Routes.PlayerOverview,
		component: () => import("@/components/views/player/ViewPlayerOverview.vue"),
	},
	{
		path: "/templates",
		name: Routes.Templates,
		component: () => import("@/components/views/ViewTemplates.vue"),
	},
	{
		path: "/impressum",
		name: Routes.Impressum,
		component: () => import("@/components/views/ViewImpressum.vue"),
	},
]
