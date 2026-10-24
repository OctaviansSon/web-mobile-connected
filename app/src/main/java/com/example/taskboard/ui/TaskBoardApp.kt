package com.example.taskboard.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.taskboard.FocusViewModel
import com.example.taskboard.PlannerApp
import com.example.taskboard.vm.AuthViewModel
import com.example.taskboard.vm.TaskEditViewModel
import com.example.taskboard.vm.NEW_TASK
import com.example.taskboard.vm.TeamViewModel
import com.example.taskboard.vm.TeamsViewModel
import com.example.taskboard.vm.factoryOf

object Routes {
    const val AUTH = "auth"
    const val TEAMS = "teams"
    const val CHEATS = "cheats"
    const val FOCUS = "focus"
    const val ACCOUNT = "account"
    const val TEAM = "team/{teamId}"
    const val TASK = "task/{teamId}/{taskId}"
    fun team(id: Int) = "team/$id"
    fun task(teamId: Int, taskId: Int) = "task/$teamId/$taskId"   // taskId -1 = new task
}

private data class TopLevelItem(val route: String, val label: String, val icon: ImageVector)

private val topLevelItems = listOf(
    TopLevelItem(Routes.TEAMS, "Teams", Icons.Default.Home),
    TopLevelItem(Routes.CHEATS, "Cheats", Icons.Default.Build),
    TopLevelItem(Routes.FOCUS, "Focus", Icons.Default.PlayArrow),
    TopLevelItem(Routes.ACCOUNT, "Account", Icons.Default.Person)
)

@Composable
fun AppBottomBar(current: String, onNavigate: (String) -> Unit) {
    NavigationBar {
        topLevelItems.forEach { item ->
            NavigationBarItem(
                selected = current == item.route,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

@Composable
fun TaskBoardApp(app: PlannerApp) {
    val nav = rememberNavController()
    val authVm: AuthViewModel = viewModel(factory = factoryOf { AuthViewModel(app.repository, app.session) })
    val focusVm: FocusViewModel = viewModel()
    val auth by authVm.state.collectAsStateWithLifecycle()
    val focus by focusVm.state.collectAsStateWithLifecycle()
    val startDestination = remember { if (app.session.token != null) Routes.TEAMS else Routes.AUTH }

    // login -> Teams; logout / expired session -> back to the login screen
    LaunchedEffect(auth.loggedIn) {
        val route = nav.currentDestination?.route
        if (auth.loggedIn && route == Routes.AUTH) {
            nav.navigate(Routes.TEAMS) { popUpTo(Routes.AUTH) { inclusive = true } }
        }
        if (!auth.loggedIn && route != Routes.AUTH) {
            nav.navigate(Routes.AUTH) { popUpTo(nav.graph.id) { inclusive = true } }
        }
    }

    val goTop: (String) -> Unit = { route ->
        nav.navigate(route) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(nav, startDestination = startDestination) {
        composable(Routes.AUTH) {
            AuthScreen(
                state = auth,
                onBaseUrlChange = authVm::onBaseUrlChange,
                onLogin = authVm::login,
                onRegister = authVm::register
            )
        }

        composable(Routes.TEAMS) {
            val vm: TeamsViewModel = viewModel(factory = factoryOf { TeamsViewModel(app.repository) })
            val state by vm.state.collectAsStateWithLifecycle()
            TeamsScreen(
                state = state,
                onRefresh = vm::refresh,
                onOpenTeam = { nav.navigate(Routes.team(it)) },
                onCreateTeam = { name, description -> vm.create(name, description) { id -> nav.navigate(Routes.team(id)) } },
                onConsumeMessage = vm::consumeMessage,
                bottomBar = { AppBottomBar(Routes.TEAMS, goTop) }
            )
        }

        composable(Routes.TEAM, arguments = listOf(navArgument("teamId") { type = NavType.IntType })) { entry ->
            val teamId = entry.arguments?.getInt("teamId") ?: 0
            val vm: TeamViewModel = viewModel(
                key = "team-$teamId",
                factory = factoryOf { TeamViewModel(app.repository, teamId, app.session.userId) }
            )
            val state by vm.state.collectAsStateWithLifecycle()
            TeamScreen(
                state = state,
                myUserId = app.session.userId,
                onBack = { nav.popBackStack() },
                onReload = vm::load,
                onQueryChange = vm::onQueryChange,
                onStatusFilter = vm::onStatusFilter,
                onCategoryFilter = vm::onCategoryFilter,
                onCycleStatus = vm::cycleStatus,
                onOpenTask = { taskId -> nav.navigate(Routes.task(teamId, taskId)) },
                onLoadStats = vm::loadStats,
                onAddMember = vm::addMember,
                onRemoveMember = vm::removeMember,
                onSaveTeam = vm::saveTeam,
                onDeleteTeam = vm::deleteTeam,
                onConsumeMessage = vm::consumeMessage
            )
        }

        composable(
            Routes.TASK,
            arguments = listOf(
                navArgument("teamId") { type = NavType.IntType },
                navArgument("taskId") { type = NavType.IntType }
            )
        ) { entry ->
            val teamId = entry.arguments?.getInt("teamId") ?: 0
            val taskId = entry.arguments?.getInt("taskId") ?: NEW_TASK
            val vm: TaskEditViewModel = viewModel(
                key = "task-$teamId-$taskId",
                factory = factoryOf { TaskEditViewModel(app.repository, teamId, taskId) }
            )
            val state by vm.state.collectAsStateWithLifecycle()
            TaskEditScreen(
                state = state,
                isNew = taskId == NEW_TASK,
                onBack = { nav.popBackStack() },
                onSave = vm::save,
                onDelete = vm::delete,
                onRetry = vm::load
            )
        }

        composable(Routes.CHEATS) {
            CheatsScreen(bottomBar = { AppBottomBar(Routes.CHEATS, goTop) })
        }

        composable(Routes.FOCUS) {
            FocusScreen(
                state = focus,
                onToggle = focusVm::toggle,
                onReset = focusVm::reset,
                onSelectMinutes = focusVm::setSessionMinutes,
                onSelectTopic = focusVm::setTopic,
                bottomBar = { AppBottomBar(Routes.FOCUS, goTop) }
            )
        }

        composable(Routes.ACCOUNT) {
            AccountScreen(
                username = auth.username,
                email = auth.email,
                serverUrl = app.session.baseUrl,
                onLogout = authVm::logout,
                bottomBar = { AppBottomBar(Routes.ACCOUNT, goTop) }
            )
        }
    }
}
