package io.github.a092devs.nexfusion.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.a092devs.nexfusion.ui.liquid.FluidFloatingBottomBar
import io.github.a092devs.nexfusion.ui.liquid.FluidTabItem
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.a092devs.nexfusion.core.PrefKeys
import io.github.a092devs.nexfusion.core.R
import io.github.a092devs.nexfusion.ui.screens.AppPatchesScreen
import io.github.a092devs.nexfusion.ui.screens.HomeScreen
import io.github.a092devs.nexfusion.ui.screens.PatchesScreen
import io.github.a092devs.nexfusion.ui.screens.SettingsScreen
import io.github.a092devs.nexfusion.ui.theme.NexFusionTheme
import io.github.a092devs.nexfusion.ui.theme.isMiuixStyle
import io.github.a092devs.nexfusion.updates.AppSettings
import top.yukonga.miuix.kmp.basic.FloatingNavigationBar
import top.yukonga.miuix.kmp.basic.FloatingToolbarDefaults
import top.yukonga.miuix.kmp.basic.NavigationBar as MiuixNavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem as MiuixNavigationBarItem
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.highlight.Highlight
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.theme.MiuixTheme

class MainActivity : ComponentActivity() {

    /** Destination requested by a notification, consumed by the navigation. */
    private var requestedDestination by mutableStateOf<String?>(null)
    /** Message requested externally or via test intent. */
    private var requestedMessage by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val maxMode = display?.supportedModes?.maxByOrNull { it.refreshRate }
            if (maxMode != null) {
                val lp = window.attributes
                lp.preferredDisplayModeId = maxMode.modeId
                window.attributes = lp
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.attributes.preferredRefreshRate = 120f
        }
        requestedDestination = intent.getStringExtra(EXTRA_DESTINATION)
        requestedMessage = intent.getStringExtra("message")
        setContent {
            val viewModel: ManagerViewModel = viewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            val navController = rememberNavController()
            NexFusionTheme(
                themeModeRaw = state.currentTheme,
                themeStyle = state.currentThemeStyle,
                colorSchemeRaw = state.currentColorScheme,
            ) {
                ManagerApp(
                    navController = navController,
                    viewModel = viewModel,
                    requestedDestination = requestedDestination,
                    requestedMessage = requestedMessage,
                    floatingNavigation = state.floatingNavigation,
                    onDestinationHandled = { requestedDestination = null },
                    onMessageHandled = { requestedMessage = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        requestedDestination = intent.getStringExtra(EXTRA_DESTINATION)
        intent.getStringExtra("message")?.let { requestedMessage = it }
    }

    companion object {
        const val EXTRA_DESTINATION = "destination"
        const val DESTINATION_HOME = "home"
        const val DESTINATION_PATCHES = "patches"
        const val DESTINATION_SETTINGS = "settings"
        const val DESTINATION_APP = "app"
    }
}

private class TopLevel(
    val route: String,
    val label: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val topLevel = listOf(
    TopLevel(
        route = MainActivity.DESTINATION_HOME,
        label = R.string.nav_home,
        selectedIcon = Icons.Rounded.Home,
        unselectedIcon = Icons.Outlined.Home,
    ),
    TopLevel(
        route = MainActivity.DESTINATION_PATCHES,
        label = R.string.nav_patches,
        selectedIcon = Icons.Rounded.Extension,
        unselectedIcon = Icons.Outlined.Extension,
    ),
    TopLevel(
        route = MainActivity.DESTINATION_SETTINGS,
        label = R.string.nav_settings,
        selectedIcon = Icons.Rounded.Settings,
        unselectedIcon = Icons.Outlined.Settings,
    ),
)

/** One floating navigation item; sized by content with a comfortable touch-target minimum. */
private val FloatingItemWidth = 92.dp
private val FloatingItemHeight = 64.dp

fun NavHostController.navigateTopLevel(route: String) = navigate(route) {
    popUpTo(MainActivity.DESTINATION_HOME) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

private fun isDetailRoute(route: String?): Boolean =
    route?.startsWith(MainActivity.DESTINATION_APP) == true

/**
 * Resolves the horizontal index of top-level destinations so screen transitions move in sync
 * with the navigation bar: clicking a tab to the right slides content right-to-left,
 * while clicking a tab to the left slides content left-to-right.
 */
private fun destinationIndex(route: String?): Int {
    val clean = route?.substringBefore('/')
    return when (clean) {
        MainActivity.DESTINATION_HOME -> 0
        MainActivity.DESTINATION_PATCHES -> 1
        MainActivity.DESTINATION_SETTINGS -> 2
        MainActivity.DESTINATION_APP -> 3
        else -> 0
    }
}

/**
 * Fluid horizontal slide transitions matching native Android and modern app navigation:
 * - Entering App detail: screen slides in from right to left (100% to 0%), with underlying screen shifting slightly left (-25%).
 * - Popping App detail (swipe back / back button): detail screen slides out from left to right (0% to 100%), with underlying screen returning from -25% to 0%.
 * - Peer tabs (Home <-> Patches <-> Settings): slide horizontally in sync with navigation bar direction.
 */
private val navEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    val initialRoute = initialState.destination.route
    val targetRoute = targetState.destination.route

    when {
        // Pushing a detail screen (Home -> App detail): slides in smoothly from right
        isDetailRoute(targetRoute) -> {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(300, easing = FastOutSlowInEasing),
            )
        }
        // Between peer top-level tabs:
        else -> {
            val initialIdx = destinationIndex(initialRoute)
            val targetIdx = destinationIndex(targetRoute)
            val direction = when {
                targetIdx > initialIdx -> 1
                targetIdx < initialIdx -> -1
                else -> 0
            }
            if (direction == 0) {
                fadeIn(animationSpec = tween(200))
            } else {
                slideInHorizontally(
                    initialOffsetX = { (it * 0.20f * direction).toInt() },
                    animationSpec = tween(260, easing = FastOutSlowInEasing),
                ) + fadeIn(animationSpec = tween(200, easing = LinearOutSlowInEasing))
            }
        }
    }
}

private val navExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    val initialRoute = initialState.destination.route
    val targetRoute = targetState.destination.route

    when {
        // Pushing a detail screen (Home -> App detail): underlying top-level screen shifts slightly left (parallax)
        isDetailRoute(targetRoute) -> {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth / 3 },
                animationSpec = tween(300, easing = FastOutSlowInEasing),
            ) + fadeOut(
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                targetAlpha = 0.85f,
            )
        }
        // Between peer top-level tabs:
        else -> {
            val initialIdx = destinationIndex(initialRoute)
            val targetIdx = destinationIndex(targetRoute)
            val direction = when {
                targetIdx > initialIdx -> -1
                targetIdx < initialIdx -> 1
                else -> 0
            }
            if (direction == 0) {
                fadeOut(animationSpec = tween(180))
            } else {
                slideOutHorizontally(
                    targetOffsetX = { (it * 0.20f * direction).toInt() },
                    animationSpec = tween(260, easing = FastOutSlowInEasing),
                ) + fadeOut(animationSpec = tween(180, easing = FastOutLinearInEasing))
            }
        }
    }
}

private val navPopEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    val initialRoute = initialState.destination.route
    val targetRoute = targetState.destination.route

    when {
        // Returning to top-level from App detail: top-level screen shifts back from left (parallax return)
        isDetailRoute(initialRoute) -> {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth / 3 },
                animationSpec = tween(300, easing = FastOutSlowInEasing),
            ) + fadeIn(
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                initialAlpha = 0.85f,
            )
        }
        // Between peer top-level tabs:
        else -> {
            val initialIdx = destinationIndex(initialRoute)
            val targetIdx = destinationIndex(targetRoute)
            val direction = when {
                targetIdx > initialIdx -> 1
                targetIdx < initialIdx -> -1
                else -> 0
            }
            if (direction == 0) {
                fadeIn(animationSpec = tween(200))
            } else {
                slideInHorizontally(
                    initialOffsetX = { (it * 0.20f * direction).toInt() },
                    animationSpec = tween(260, easing = FastOutSlowInEasing),
                ) + fadeIn(animationSpec = tween(200, easing = LinearOutSlowInEasing))
            }
        }
    }
}

private val navPopExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    val initialRoute = initialState.destination.route
    val targetRoute = targetState.destination.route

    when {
        // Leaving App detail: detail screen slides out smoothly to the right without premature fading
        isDetailRoute(initialRoute) -> {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(300, easing = FastOutSlowInEasing),
            )
        }
        // Between peer top-level tabs:
        else -> {
            val initialIdx = destinationIndex(initialRoute)
            val targetIdx = destinationIndex(targetRoute)
            val direction = when {
                targetIdx > initialIdx -> -1
                targetIdx < initialIdx -> 1
                else -> 0
            }
            if (direction == 0) {
                fadeOut(animationSpec = tween(180))
            } else {
                slideOutHorizontally(
                    targetOffsetX = { (it * 0.20f * direction).toInt() },
                    animationSpec = tween(260, easing = FastOutSlowInEasing),
                ) + fadeOut(animationSpec = tween(180, easing = FastOutLinearInEasing))
            }
        }
    }
}

@Composable
private fun ManagerApp(
    navController: NavHostController,
    viewModel: ManagerViewModel,
    requestedDestination: String?,
    requestedMessage: String?,
    floatingNavigation: Boolean,
    onDestinationHandled: () -> Unit,
    onMessageHandled: () -> Unit,
) {
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val isTopLevel = route == MainActivity.DESTINATION_HOME
    val isMiuix = isMiuixStyle
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    val navAnimSpec = remember {
        spring(
            dampingRatio = 0.95f,
            stiffness = 750f,
            visibilityThreshold = 0.001f,
        )
    }

    LaunchedEffect(requestedDestination) {
        if (requestedDestination != null) {
            val targetIdx = destinationIndex(requestedDestination)
            if (targetIdx in 0..2) {
                if (route != MainActivity.DESTINATION_HOME) {
                    navController.popBackStack(MainActivity.DESTINATION_HOME, inclusive = false)
                }
                coroutineScope.launch { pagerState.animateScrollToPage(targetIdx, animationSpec = navAnimSpec) }
            }
            onDestinationHandled()
        }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.messages.collect { msg ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(msg)
        }
    }

    LaunchedEffect(requestedMessage) {
        val msg = requestedMessage
        if (msg != null) {
            onMessageHandled()
            coroutineScope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(msg)
            }
        }
    }
    NotificationPermission()

    BackHandler(enabled = isTopLevel && (pagerState.targetPage != 0 || pagerState.currentPage != 0)) {
        coroutineScope.launch { pagerState.animateScrollToPage(0, animationSpec = navAnimSpec) }
    }

    val navBarBottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val contentBottomPadding = when {
        isMiuix && floatingNavigation -> 100.dp
        isMiuix -> 84.dp + navBarBottomInset
        else -> 80.dp + navBarBottomInset
    }

    val systemDark = isSystemInDarkTheme()
    val isDark = when (state.currentTheme) {
        AppSettings.THEME_LIGHT -> false
        AppSettings.THEME_DARK -> true
        else -> systemDark
    }

    val blurSupported = isRuntimeShaderSupported()
    val canvasBg = MaterialTheme.colorScheme.background
    val isTranslucent = state.floatingBarTranslucent
    val backdrop = if (isMiuix && floatingNavigation && blurSupported && isTranslucent) {
        rememberLayerBackdrop {
            drawRect(canvasBg)
            drawContent()
        }
    } else null


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(canvasBg)
    ) {
        // Content container hosting a single persistent NavHost that never resets across theme or design changes
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .widthIn(max = 840.dp)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = MainActivity.DESTINATION_HOME,
                    enterTransition = navEnterTransition,
                    exitTransition = navExitTransition,
                    popEnterTransition = navPopEnterTransition,
                    popExitTransition = navPopExitTransition,
                ) {
                    composable(MainActivity.DESTINATION_HOME) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                            beyondViewportPageCount = 2,
                            key = { page -> page },
                        ) { page ->
                            when (page) {
                                0 -> HomeScreen(
                                    viewModel = viewModel,
                                    navController = navController,
                                    outerPadding = PaddingValues(bottom = contentBottomPadding),
                                    onOpenPatches = { coroutineScope.launch { pagerState.animateScrollToPage(1, animationSpec = navAnimSpec) } },
                                )
                                1 -> PatchesScreen(viewModel, PaddingValues(bottom = contentBottomPadding))
                                2 -> SettingsScreen(viewModel, PaddingValues(bottom = contentBottomPadding))
                            }
                        }
                    }
                    composable(MainActivity.DESTINATION_PATCHES) {
                        LaunchedEffect(Unit) {
                            navController.popBackStack(MainActivity.DESTINATION_HOME, inclusive = false)
                            pagerState.animateScrollToPage(1, animationSpec = navAnimSpec)
                        }
                    }
                    composable(MainActivity.DESTINATION_SETTINGS) {
                        LaunchedEffect(Unit) {
                            navController.popBackStack(MainActivity.DESTINATION_HOME, inclusive = false)
                            pagerState.animateScrollToPage(2, animationSpec = navAnimSpec)
                        }
                    }
                    composable(
                        "${MainActivity.DESTINATION_APP}/{packageName}",
                        arguments = listOf(navArgument("packageName") { type = NavType.StringType }),
                    ) { entry ->
                        AppPatchesScreen(viewModel, entry.arguments!!.getString("packageName")!!, navController)
                    }
                }
            }
        }

        val selectedIndex = if (isTopLevel) pagerState.targetPage else 0

        if (isMiuix) {
            if (floatingNavigation) {
                AnimatedVisibility(
                    visible = isTopLevel,
                    enter = slideInVertically(
                        animationSpec = tween(320, easing = FastOutSlowInEasing),
                        initialOffsetY = { it },
                    ) + fadeIn(animationSpec = tween(220)),
                    exit = slideOutVertically(
                        animationSpec = tween(220),
                        targetOffsetY = { it / 2 },
                    ) + fadeOut(animationSpec = tween(160)),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal))
                        .padding(bottom = 12.dp + navBarBottomInset),
                ) {
                    val fluidTabs = remember(state.sourcesWithUpdates.size, state.appUpdate) {
                        topLevel.map { item ->
                            val badge = when (item.route) {
                                MainActivity.DESTINATION_PATCHES -> state.sourcesWithUpdates.size
                                MainActivity.DESTINATION_SETTINGS -> if (state.appUpdate != null) 1 else 0
                                else -> 0
                            }
                            FluidTabItem(
                                route = item.route,
                                label = item.label,
                                selectedIcon = item.selectedIcon,
                                unselectedIcon = item.unselectedIcon,
                                badgeCount = badge,
                            )
                        }
                    }
                    FluidFloatingBottomBar(
                        selectedIndex = selectedIndex,
                        onSelected = { index ->
                            coroutineScope.launch { pagerState.animateScrollToPage(index, animationSpec = navAnimSpec) }
                        },
                        tabs = fluidTabs,
                        isTranslucent = state.floatingBarTranslucent,
                        isDark = isDark,
                        backdrop = backdrop,
                    )
                }
            } else {
                AnimatedVisibility(
                    visible = isTopLevel,
                    enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(280, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(200)),
                    exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(220, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(150)),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .widthIn(max = 840.dp),
                ) {
                    MiuixNavigationBar {
                        topLevel.forEachIndexed { index, item ->
                            val selected = selectedIndex == index
                            MiuixNavigationBarItem(
                                selected = selected,
                                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index, animationSpec = navAnimSpec) } },
                                icon = if (selected) item.selectedIcon else item.unselectedIcon,
                                label = stringResource(item.label),
                            )
                        }
                    }
                }
            }
        } else {
            AnimatedVisibility(
                visible = isTopLevel,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(280, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(200)),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(220, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(150)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .widthIn(max = 840.dp),
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    topLevel.forEachIndexed { index, item ->
                        val selected = selectedIndex == index
                        NavigationBarItem(
                            selected = selected,
                            onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index, animationSpec = navAnimSpec) } },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = null,
                                )
                            },
                            label = { Text(stringResource(item.label)) },
                        )
                    }
                }
            }
        }

        // Themed floating snackbar positioned safely above navigation bar / insets
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal))
                .padding(bottom = contentBottomPadding + 8.dp)
                .padding(horizontal = 24.dp)
                .wrapContentWidth(Alignment.CenterHorizontally)
                .widthIn(max = 560.dp),
        ) { data ->
            val containerBg = if (isDark) {
                Color(0xFF23272D)
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            }
            val textColor = if (isDark) {
                Color(0xFFE6EDF3)
            } else {
                MaterialTheme.colorScheme.onSurface
            }
            val borderColor = if (isDark) {
                Color.White.copy(alpha = 0.12f)
            } else {
                Color.Black.copy(alpha = 0.08f)
            }

            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(22.dp),
                        spotColor = Color.Black.copy(alpha = if (isDark) 0.45f else 0.18f),
                        ambientColor = Color.Black.copy(alpha = if (isDark) 0.30f else 0.10f),
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(containerBg)
                    .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(22.dp))
                    .padding(horizontal = 22.dp, vertical = 13.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = data.visuals.message,
                    color = textColor,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                )
            }
        }
    }
}

/**
 * Items of the floating navigation bar. Each item handles its own selection state
 * without an awkward horizontal slide effect across tabs.
 */
@Composable
private fun MiuixFloatingNavItems(
    selectedRoute: String?,
    isDark: Boolean,
    onSelect: (String) -> Unit,
) {
    Row {
        topLevel.forEach { item ->
            MiuixFloatingNavItem(
                selected = selectedRoute == item.route,
                onClick = { onSelect(item.route) },
                item = item,
                isDark = isDark,
            )
        }
    }
}

/**
 * Item of the floating navigation bar: the official icon treatment plus the label under the
 * glyph, sized for a comfortable touch target. Selection uses an in-place pill indicator
 * without sliding across the bar.
 */
@Composable
private fun MiuixFloatingNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    item: TopLevel,
    isDark: Boolean,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val baseColor = MiuixTheme.colorScheme.onSurfaceContainer
    val primaryColor = MiuixTheme.colorScheme.primary
    val indicatorColor = primaryColor.copy(alpha = if (isDark) 0.22f else 0.14f)

    val targetAlpha = when {
        pressed -> if (selected) 0.70f else 0.85f
        selected -> 1f
        else -> 0.65f
    }
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(150),
        label = "floatingNavItemAlpha",
    )
    val pillAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(150),
        label = "floatingNavItemPillAlpha",
    )
    val tint = if (selected) primaryColor else baseColor.copy(alpha = alpha)

    Box(
        modifier = Modifier
            .size(width = FloatingItemWidth, height = FloatingItemHeight)
            .clip(RoundedCornerShape(18.dp))
            .selectable(
                selected = selected,
                role = Role.Tab,
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (pillAlpha > 0f) {
            Box(
                modifier = Modifier
                    .size(width = FloatingItemWidth - 14.dp, height = FloatingItemHeight - 12.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(indicatorColor.copy(alpha = indicatorColor.alpha * pillAlpha)),
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Icon(
                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = stringResource(item.label),
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = tint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Update notifications need the permission on Android 13 and newer; asked for once. */
@Composable
private fun NotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences(PrefKeys.APP_PREFS, android.content.Context.MODE_PRIVATE)
        val granted = context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (!granted && !prefs.getBoolean(PrefKeys.NOTIFICATION_PERMISSION_ASKED, false)) {
            prefs.edit().putBoolean(PrefKeys.NOTIFICATION_PERMISSION_ASKED, true).apply()
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
