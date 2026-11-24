/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.screens.content

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import com.movtery.zalithlauncher.BuildConfig
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.guide.GuideSide
import com.movtery.guide.guideNode
import com.movtery.petlib.PetAnimation
import com.movtery.petlib.PetTapReaction
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.getAccountTypeName
import com.movtery.zalithlauncher.game.account.wardrobe.toArmModel
import com.movtery.zalithlauncher.game.version.installed.PlayTimeRepository
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedDirection
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.CutoutBox
import com.movtery.zalithlauncher.ui.components.MarkdownView
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.PetModelView
import com.movtery.zalithlauncher.ui.components.animated
import com.movtery.zalithlauncher.ui.components.rememberAnimatedContext
import com.movtery.zalithlauncher.ui.guide.GuideKeys
import com.iffly.compose.markdown.config.MarkdownRenderConfig
import com.iffly.compose.markdown.style.ListTheme
import com.iffly.compose.markdown.style.MarkdownTheme
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.PlayerFace
import com.movtery.zalithlauncher.ui.screens.content.elements.CommonVersionInfoLayout
import com.movtery.zalithlauncher.ui.screens.content.elements.AboutDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.SideBar
import com.movtery.zalithlauncher.ui.screens.content.elements.TeamDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionIconImage
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.rememberSettingsCardShape
import com.movtery.zalithlauncher.ui.screens.game.elements.PerformanceSettingsDialog
import com.movtery.zalithlauncher.ui.screens.game.elements.PerformanceSettingsOperation
import com.movtery.zalithlauncher.ui.screens.main.custom_home.MarkdownBlock
import com.movtery.zalithlauncher.ui.screens.main.custom_home.customHomePage
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.screens.navigateTo
import com.movtery.zalithlauncher.ui.screens.removeAndNavigateTo
import com.movtery.zalithlauncher.utils.PlayTimeUtils
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.viewmodel.HomePageState
import com.movtery.zalithlauncher.viewmodel.LocalHomePageViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import kotlin.math.roundToInt
import com.movtery.zalithlauncher.ui.components.defaultMarkdownConfig

private const val ContentWeight = 7f
private const val ActionMenuWeight = 3f

/**
 * 操作菜单停泊槽位与屏幕边缘的间距
 */
private val ActionMenuOuterPadding = 12.dp

@Composable
fun LauncherScreen(
    backStackViewModel: ScreenBackStackViewModel,
    navigateToVersions: (Version) -> Unit,
    onLaunchGame: (Version?) -> Unit,
    onOpenLink: (String) -> Unit,
    onHomePageEvent: (MarkdownBlock.Button.Event) -> Unit,
    onNavigateToStats: () -> Unit = {},
    onNavigateToPlayTimeStats: () -> Unit = {},
    onNavigateToLog: (String) -> Unit = {},
    startGuideOnce: (GuideKeys.Keys) -> Unit = {},
) {
    LaunchedEffect(Unit) {
        //等待主界面完成首次布局后再展示新手引导，确保引导锚点已经测量完毕
        delay(600)
        startGuideOnce(GuideKeys.Main)
    }

    BaseScreen(
        screenKey = NormalNavKey.LauncherMain,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { isVisible ->
        var showAboutDialog by remember { mutableStateOf(false) }
        var showTeamDialog by remember { mutableStateOf(false) }
        var performanceSettingsState by remember { mutableStateOf<PerformanceSettingsOperation>(PerformanceSettingsOperation.None) }

        if (showAboutDialog) {
            AboutDialog(onDismissRequest = { showAboutDialog = false })
        }

        if (showTeamDialog) {
            TeamDialog(onDismissRequest = { showTeamDialog = false })
        }

        PerformanceSettingsDialog(
            operation = performanceSettingsState,
            onDismissRequest = { performanceSettingsState = PerformanceSettingsOperation.None }
        )

        val wideDashboard = LocalConfiguration.current.screenWidthDp >= 760

        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            SideBar(
                modifier = Modifier
                    .guideNode(
                        key = GuideKeys.Main.Step.SideBar,
                        preferSide = GuideSide.Below
                    )
                    .padding(start = 12.dp, top = 12.dp, bottom = 12.dp),
                isVisible = isVisible,
                onFpsClick = {
                    performanceSettingsState = PerformanceSettingsOperation.Fps
                },
                onVersionsClick = {
                    backStackViewModel.mainScreen.removeAndNavigateTo(
                        remove = NestedNavKey.VersionSettings::class,
                        screenKey = NormalNavKey.VersionsManager
                    )
                },
                onInfoClick = {
                    showAboutDialog = true
                },
                onTeamClick = {
                    showTeamDialog = true
                }
            )

            val toAccountManageScreen: () -> Unit = {
                backStackViewModel.mainScreen.navigateTo(
                    screenKey = NormalNavKey.AccountManager(FirstLoginMenu.NONE)
                )
            }
            val toVersionManageScreen: () -> Unit = {
                backStackViewModel.mainScreen.removeAndNavigateTo(
                    remove = NestedNavKey.VersionSettings::class,
                    screenKey = NormalNavKey.VersionsManager
                )
            }
            val toVersionSettingsScreen: () -> Unit = {
                VersionsManager.currentVersion.value?.let { version ->
                    navigateToVersions(version)
                }
            }

            CompositionLocalProvider(
                LocalUriHandler provides object : UriHandler {
                    override fun openUri(uri: String) {
                        onOpenLink(uri)
                    }
                }
            ) {
                ContentMenu(
                    modifier = Modifier.weight(if (wideDashboard) 1f else 7f),
                    isVisible = isVisible,
                    wideDashboard = wideDashboard,
                    onHomePageEvent = onHomePageEvent,
                    onNavigateToStats = onNavigateToStats,
                    onNavigateToPlayTimeStats = onNavigateToPlayTimeStats,
                    onNavigateToLog = onNavigateToLog,
                    onLaunchGame = onLaunchGame,
                    toAccountManageScreen = toAccountManageScreen,
                    toVersionManageScreen = toVersionManageScreen,
                    toVersionSettingsScreen = toVersionSettingsScreen
                )
            }

            // RightMenu moved inside ContentMenu for centered profile layout
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ContentMenu(
    isVisible: Boolean,
    wideDashboard: Boolean,
    onHomePageEvent: (MarkdownBlock.Button.Event) -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToPlayTimeStats: () -> Unit = {},
    onNavigateToLog: (String) -> Unit,
    onLaunchGame: (Version?) -> Unit,
    toAccountManageScreen: () -> Unit,
    toVersionManageScreen: () -> Unit,
    toVersionSettingsScreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val yOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible
    )

    val homePageViewModel = LocalHomePageViewModel.current
    val pageState by homePageViewModel.pageState.collectAsStateWithLifecycle()
    val versions by VersionsManager.versions.collectAsStateWithLifecycle()
    val versionNames = remember(versions) { versions.map { it.getVersionName() } }
    val config = defaultMarkdownConfig()

    Column(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
            .padding(all = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (BuildConfig.DEBUG) {
            //debug版本关不掉的警告，防止有人把测试版当正式版用 XD
            BackgroundCard(shape = MaterialTheme.shapes.extraLarge) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.generic_warning),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.launcher_version_debug_warning, BuildKeys.LAUNCHER_NAME),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        modifier = Modifier
                            .alpha(0.8f)
                            .align(Alignment.End),
                        text = stringResource(R.string.launcher_version_debug_warning_cant_close),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Forced three-column layout: Left stats | Center Profile+Launch | Right Changelog+LastLog
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatsSideColumn(
                modifier = Modifier
                    .guideNode(
                        key = GuideKeys.Main.Step.Stats,
                        preferSide = GuideSide.End
                    )
                    .weight(1f)
                    .fillMaxHeight(),
                side = StatsSide.Left,
                versionNames = versionNames,
                onNavigateToPlayTimeStats = onNavigateToPlayTimeStats,
                onNavigateToLog = onNavigateToLog
            )
            RightMenu(
                isVisible = isVisible,
                modifier = Modifier
                    .weight(1.15f)
                    .fillMaxHeight(),
                onLaunchGame = onLaunchGame,
                toAccountManageScreen = toAccountManageScreen,
                toVersionManageScreen = toVersionManageScreen,
                toVersionSettingsScreen = toVersionSettingsScreen
            )
            StatsSideColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                side = StatsSide.Right,
                versionNames = versionNames,
                onNavigateToPlayTimeStats = onNavigateToPlayTimeStats,
                onNavigateToLog = onNavigateToLog
            )
        }

        // Home page content below (only shown when configured)
        when (val state = pageState) {
            is HomePageState.Blank -> {}
            is HomePageState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LoadingIndicator()
                        Text(
                            text = stringResource(R.string.settings_launcher_home_page_loading),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
            is HomePageState.None -> {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    customHomePage(
                        blocks = state.page,
                        config = config,
                        onEvent = onHomePageEvent
                    )
                }
            }
        }
    }
}

private const val CHANGELOGS_URL = "https://raw.githubusercontent.com/TCL-Team/TurboCraft-Launcher/refs/heads/main/CHANGELOGS_UPDATE.md"
private const val CHANGELOGS_UPDATE_TR = "https://raw.githubusercontent.com/TCL-Team/TurboCraft-Launcher/refs/heads/main/CHANGELOGS_UPDATE_TR.md"

private enum class StatsSide {
    Left,
    Right
}

@Composable
private fun StatsSideColumn(
    modifier: Modifier = Modifier,
    side: StatsSide,
    versionNames: List<String>,
    onNavigateToPlayTimeStats: () -> Unit,
    onNavigateToLog: (String) -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (side == StatsSide.Left) {
                Text(
                    text = stringResource(R.string.stats_today_header),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .alpha(0.5f)
                )
            }
        }

        if (side == StatsSide.Left) {
            WeeklyPlayTimeChart(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                versionNames = versionNames
            )
            DailyPlayTimeCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                versionNames = versionNames,
                onClick = onNavigateToPlayTimeStats
            )
        } else {
            ChangelogCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            LastLogCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                onNavigateToLog = onNavigateToLog
            )
        }
    }
}

@Composable
private fun WeeklyPlayTimeChart(
    modifier: Modifier = Modifier,
    versionNames: List<String>
) {
    val weekData = remember(versionNames) {
        val dates = PlayTimeRepository.lastNDays(7).reversed()
        dates.map { date ->
            date to PlayTimeRepository.getDailyTotalPlayTime(date, versionNames)
        }
    }

    val maxMs = remember(weekData) { weekData.maxOfOrNull { it.second } ?: 1L }
    val primaryColor = MaterialTheme.colorScheme.primary

    val dayLabels = remember(weekData) {
        val sdf = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
        val parser = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        weekData.map { (date, _) ->
            val d = parser.parse(date)
            if (d != null) sdf.format(d) else date.takeLast(5)
        }
    }

    BackgroundCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                weekData.forEachIndexed { index, (_, ms) ->
                    val fraction = if (maxMs > 0) ms.toFloat() / maxMs else 0f
                    val hours = PlayTimeUtils.getPlayHours(ms)
                    val barAlpha = 0.4f + (fraction * 0.6f).coerceAtMost(0.6f)

                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = "%.1f".format(hours),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            modifier = Modifier.alpha(0.8f)
                        )
                        Spacer(Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.55f)
                                .weight(fraction.coerceAtLeast(0.03f))
                                .background(
                                    color = primaryColor.copy(alpha = barAlpha),
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                        Spacer(Modifier.weight((1f - fraction).coerceAtLeast(0.001f)))
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = dayLabels.getOrElse(index) { "" },
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 7.sp,
                            modifier = Modifier.alpha(0.5f),
                            maxLines = 1
                        )
                    }
                }
            }

            Text(
                text = stringResource(R.string.stats_play_time_graph),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(0.5f)
                    .padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun DailyPlayTimeCard(
    modifier: Modifier = Modifier,
    versionNames: List<String>,
    onClick: () -> Unit = {}
) {
    val todayMs = remember(versionNames) {
        PlayTimeRepository.getDailyTotalPlayTime(PlayTimeRepository.today(), versionNames)
    }
    val hours = PlayTimeUtils.getPlayHours(todayMs)

    BackgroundCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = "%.1f h".format(hours),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                    fontSize = 26.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally).alpha(0.7f),
                    text = stringResource(R.string.stats_today),
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(Modifier.weight(1f))
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(R.string.stats_statistics),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.stats_click_for_more),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun LastLogCard(
    modifier: Modifier = Modifier,
    onNavigateToLog: (String) -> Unit
) {
    val currentVersion by VersionsManager.currentVersion.collectAsStateWithLifecycle()
    val logFile = remember(currentVersion) {
        currentVersion?.getLatestLog()
    }
    val logExists = remember(logFile) { logFile?.exists() == true }

    BackgroundCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        onClick = {
            if (logExists) logFile?.absolutePath?.let { onNavigateToLog(it) }
        },
        enabled = logExists
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.stats_last_log),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1
                )
                if (!logExists || logFile == null) {
                    Text(
                        text = stringResource(R.string.stats_no_log),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.alpha(0.6f)
                    )
                } else {
                    Text(
                        text = remember(logFile) {
                            try {
                                logFile.readText().take(2000)
                            } catch (e: Exception) { "" }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .alpha(0.5f)
                            .bottomFade(36.dp, cardColor())
                    )
                    Text(
                        text = stringResource(R.string.stats_click_for_more),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }
            }
            if (logExists) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_play_arrow_filled),
                        contentDescription = stringResource(R.string.generic_open_link),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountAvatarRow(
    modifier: Modifier = Modifier,
    avatarSize: Dp = 48.dp,
    placeholderSize: Dp = 40.dp,
    textSpacer: Dp = 4.dp,
    refreshKey: Any? = null,
) {
    val account0 by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()
    val account = account0

    Row(
        modifier = modifier.padding(all = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (account != null) {
            PlayerFace(
                modifier = Modifier.padding(horizontal = 4.dp),
                account = account,
                avatarSize = avatarSize,
                refreshKey = refreshKey
            )
        } else {
            Icon(
                modifier = Modifier
                    .padding(start = 8.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
                    .size(placeholderSize),
                painter = painterResource(R.drawable.ic_add),
                contentDescription = null
            )
        }

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(textSpacer)
        ) {
            Text(
                text = account?.username ?: stringResource(R.string.account_add_new_account),
                maxLines = 1,
                style = MaterialTheme.typography.titleSmall
            )
            if (account != null) {
                Text(
                    text = getAccountTypeName(account),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun VersionListMenu(
    show: Boolean,
    versions: List<Version>,
    menuAnchor: LayoutCoordinates?,
    version: Version?,
    onLaunchGame: (Version?) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val menuAnchorBounds = menuAnchor?.boundsInParent()
    val menuAnchorX = menuAnchorBounds?.left ?: 0f
    val menuAnchorHeight = menuAnchorBounds?.height ?: 0f

    DropdownMenu(
        expanded = show && menuAnchor != null && versions.isNotEmpty(),
        onDismissRequest = onDismissRequest,
        modifier = Modifier.width(260.dp),
        offset = DpOffset(
            x = with(LocalDensity.current) { menuAnchorX.toDp() },
            y = with(LocalDensity.current) { (-menuAnchorHeight).toDp() } - 8.dp
        ),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        versions.forEach { version0 ->
            DropdownMenuItem(
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CommonVersionInfoLayout(
                            modifier = Modifier.weight(1f),
                            version = version0,
                            iconSize = 28.dp
                        )
                        IconButton(
                            onClick = {
                                onLaunchGame(version0)
                                onDismissRequest()
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_play_arrow_filled),
                                contentDescription = stringResource(R.string.main_launch_game),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                onClick = {
                    if (version == version0) return@DropdownMenuItem
                    VersionsManager.saveVersion(version0)
                    onDismissRequest()
                }
            )
        }
    }
}

@Composable
private fun Modifier.versionLayout(
    swapToVersionManage: () -> Unit,
    requestOpenVersionList: () -> Unit,
) = this
    .combinedClickable(
        role = Role.Button,
        onClick = swapToVersionManage,
        onLongClick = requestOpenVersionList,
    )


@Composable
private fun VersionsContent(
    version: Version?,
    onLaunchGame: (Version?) -> Unit,
    toVersionManageScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Max),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        var showList by remember { mutableStateOf(false) }
        var versionManagerRow by remember { mutableStateOf<LayoutCoordinates?>(null) }

        if (version == null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .clickable(onClick = toVersionManageScreen)
                    .padding(all = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MarqueeText(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.versions_manage_no_versions),
                    style = MaterialTheme.typography.labelMedium,
                )

                Icon(
                    painter = painterResource(R.drawable.ic_arrow_right_rounded),
                    contentDescription = stringResource(R.string.versions_manage_install_new)
                )
            }
        } else {
            val versions by VersionsManager.versions.collectAsStateWithLifecycle()

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 版本列表
                VersionListMenu(
                    show = showList,
                    versions = versions,
                    menuAnchor = versionManagerRow,
                    version = version,
                    onLaunchGame = onLaunchGame,
                    onDismissRequest = { showList = false },
                )

                // 版本名称/描述
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .versionLayout(
                            swapToVersionManage = toVersionManageScreen,
                            requestOpenVersionList = { showList = versions.isNotEmpty() }
                        )
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                        .onGloballyPositioned { coordinates ->
                            versionManagerRow = coordinates
                        }
                ) {
                    MarqueeText(
                        text = version.getVersionName(),
                        style = MaterialTheme.typography.labelMedium,
                    )
                    if (version.isValid()) {
                        MarqueeText(
                            text = version.getVersionSummary(),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }

            // 启动游戏按钮
            IconButton(
                modifier = Modifier.padding(end = 8.dp).guideNode(
                    key = GuideKeys.Main.Step.Launch,
                    preferSide = GuideSide.Above
                ),
                onClick = {
                    onLaunchGame(null)
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_play_arrow_filled),
                    contentDescription = stringResource(R.string.main_launch_game),
                )
            }
        }
    }
}

@Composable
private fun PetModelContent(
    modifier: Modifier = Modifier,
) {
    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()
    val refreshWardrobe by AccountsManager.refreshWardrobe.collectAsStateWithLifecycle()

    val yaw = Math.toRadians(35.0).toFloat()

    val armModel = account?.skinModelType?.toArmModel()
    val skinStamp = account?.getSkinFile()?.lastModified()
    val capeStamp = account?.getSelectedCapeFile()?.lastModified()
    val skinBytes by produceState<ByteArray?>(null, account, refreshWardrobe, skinStamp) {
        value = withContext(Dispatchers.IO) {
            runCatching { account?.getSkinFile()?.takeIf { it.exists() }?.readBytes() }.getOrNull()
        }
    }
    val capeBytes by produceState<ByteArray?>(null, account, refreshWardrobe, capeStamp) {
        value = withContext(Dispatchers.IO) {
            runCatching { account?.getSelectedCapeFile()?.takeIf { it.exists() }?.readBytes() }.getOrNull()
        }
    }

    PetModelView(
        modifier = modifier,
        skin = skinBytes,
        cape = capeBytes,
        arm = armModel,
        yaw = yaw,
        animation = PetAnimation.Idle,
        tapReaction = PetTapReaction.Interact,
        dragYaw = false
    )
}

@Composable
private fun ActionMenuContent(
    modifier: Modifier = Modifier,
    onLaunchGame: (Version?) -> Unit,
    toAccountManageScreen: () -> Unit,
    toVersionManageScreen: () -> Unit,
    toVersionSettingsScreen: () -> Unit,
) {
    ConstraintLayout(
        modifier = modifier
    ) {
        val (versionLayout, petModel) = createRefs()

        PetModelContent(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(petModel) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(anchor = versionLayout.top)
                    height = Dimension.fillToConstraints
                },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(versionLayout) {
                    bottom.linkTo(anchor = parent.bottom)
                },
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val accShape = rememberSettingsCardShape(CardPosition.Top)
            val bottomShape = rememberSettingsCardShape(CardPosition.Bottom)

            BackgroundCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .guideNode(
                        key = GuideKeys.Main.Step.Account,
                        holeShape = accShape,
                        preferSide = GuideSide.Below
                    ),
                shape = accShape,
                onClick = toAccountManageScreen,
            ) {
                AccountAvatarRow(
                    modifier = Modifier.fillMaxWidth(),
                    avatarSize = 38.dp,
                    placeholderSize = 34.dp,
                    textSpacer = 0.dp,
                )
            }

            val isRefreshing by VersionsManager.isRefreshing.collectAsStateWithLifecycle()
            val version0 by VersionsManager.currentVersion.collectAsStateWithLifecycle()
            val version = version0

            Row(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .fillMaxWidth()
                    .guideNode(
                        key = GuideKeys.Main.Step.VersionList,
                        holeShape = bottomShape,
                        preferSide = GuideSide.Above,
                    ),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                if (isRefreshing) {
                    BackgroundCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = bottomShape,
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 26.dp)
                                .fillMaxWidth()
                        )
                    }
                } else {
                    // 版本图标
                    version?.let {
                        BackgroundCard(
                            modifier = Modifier.fillMaxHeight(),
                            shape = rememberSettingsCardShape(CardPosition.BottomStart),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .clickable(onClick = toVersionSettingsScreen)
                                    .padding(start = 12.dp, end = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CutoutBox(
                                    modifier = Modifier.size(38.dp),
                                    cutoutRadius = 20.dp,
                                    overlayAlignment = Alignment.BottomEnd,
                                    overlay = {
                                        Icon(
                                            modifier = Modifier.size(16.dp),
                                            painter = painterResource(R.drawable.ic_settings_filled),
                                            contentDescription = stringResource(R.string.versions_manage_settings)
                                        )
                                    }
                                ) {
                                    VersionIconImage(
                                        modifier = Modifier.fillMaxSize(),
                                        version = version,
                                    )
                                }
                            }
                        }
                    }

                    BackgroundCard(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = if (version == null) {
                            bottomShape
                        } else {
                            rememberSettingsCardShape(CardPosition.BottomEnd)
                        },
                    ) {
                        VersionsContent(
                            modifier = Modifier.fillMaxWidth(),
                            version = version,
                            onLaunchGame = onLaunchGame,
                            toVersionManageScreen = toVersionManageScreen,
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun RightMenu(
    isVisible: Boolean,
    onLaunchGame: (Version?) -> Unit,
    modifier: Modifier = Modifier,
    toAccountManageScreen: () -> Unit = {},
    toVersionManageScreen: () -> Unit = {},
    toVersionSettingsScreen: () -> Unit = {}
) {
    val xOffset by swapAnimateDpAsState(
        targetValue = 40.dp,
        swapIn = isVisible,
        isHorizontal = true
    )

    BackgroundCard(
        modifier = modifier.offset { IntOffset(x = xOffset.roundToPx(), y = 0) },
        shape = MaterialTheme.shapes.extraLarge
    ) {
        ActionMenuContent(
            modifier = Modifier.fillMaxSize(),
            onLaunchGame = onLaunchGame,
            toAccountManageScreen = toAccountManageScreen,
            toVersionManageScreen = toVersionManageScreen,
            toVersionSettingsScreen = toVersionSettingsScreen,
        )
    }
}

@Composable
private fun ChangelogCard(
    modifier: Modifier = Modifier
) {
    var content by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }

    val isTurkey = LocalConfiguration.current.locales[0].language == "tr"
    val changelogUrl = if (isTurkey) CHANGELOGS_UPDATE_TR else CHANGELOGS_URL

    LaunchedEffect(changelogUrl) {
        try {
            val text = withContext(Dispatchers.IO) {
                try {
                    java.net.URL(changelogUrl).readText()
                } catch (e: Exception) {
                    if (!isTurkey) throw e
                    // The Turkish file is not present in every upstream release; fall back to the official English changelog.
                    java.net.URL(CHANGELOGS_URL).readText()
                }
            }
            content = text.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            content = null
        }
        isLoading = false
    }

    BackgroundCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        onClick = { if (content != null) showDialog = true }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 8.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
            ) {
                val isTablet = LocalConfiguration.current.screenWidthDp >= 600

                Text(
                    text = "What's New",
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1
                )
                when {
                    isLoading -> {
                        Text(
                            text = stringResource(R.string.stats_changelog_loading),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.alpha(0.6f)
                        )
                    }
                    content == null -> {
                        Text(
                            text = stringResource(R.string.generic_error),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.alpha(0.6f)
                        )
                    }
                    else -> {
                        val contentText = content!!
                        val previewText = remember(contentText) {
                            val lines = contentText.lines()
                            if (lines.size <= 2) contentText
                            else lines.dropLast(2).joinToString("\n")
                        }
                        val bodySize = if (isTablet) MaterialTheme.typography.bodySmall.fontSize else MaterialTheme.typography.labelSmall.fontSize
                        val primary = MaterialTheme.colorScheme.primary
                        val onSurface = MaterialTheme.colorScheme.onSurface
                        val cardConfig = remember(bodySize, primary, onSurface) {
                            MarkdownRenderConfig.Builder()
                                .markdownTheme(
                                    MarkdownTheme(
                                        textStyle = TextStyle(fontSize = bodySize, lineHeight = bodySize * 1.4f, color = onSurface),
                                        headStyle = mapOf(
                                            1 to TextStyle(fontSize = bodySize * 1.2f, lineHeight = bodySize * 1.5f, fontWeight = FontWeight.Bold, color = primary),
                                            2 to TextStyle(fontSize = bodySize * 1.1f, lineHeight = bodySize * 1.4f, fontWeight = FontWeight.Bold, color = primary),
                                            3 to TextStyle(fontSize = bodySize, lineHeight = bodySize * 1.3f, fontWeight = FontWeight.SemiBold, color = primary),
                                        ),
                                        listTheme = ListTheme(
                                            markerTextStyle = TextStyle(
                                                fontSize = bodySize,
                                                lineHeight = bodySize * 1.4f,
                                                textAlign = TextAlign.End,
                                                color = onSurface,
                                            ),
                                        ),
                                    )
                                )
                                .build()
                        }
                        MarkdownView(
                            content = previewText,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .bottomFade(48.dp, cardColor()),
                            config = cardConfig,
                        )
                        Text(
                            text = stringResource(R.string.stats_click_for_more),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }

    if (showDialog && content != null) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.85f),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    val dialogConfig = defaultMarkdownConfig()
                    MarkdownView(
                        content = "# What's New\n\n${content!!}",
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        config = dialogConfig
                    )
                    Button(
                        onClick = { showDialog = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Text(text = stringResource(R.string.generic_close))
                    }
                }
            }
        }
    }
}

private fun Modifier.bottomFade(edgeHeight: androidx.compose.ui.unit.Dp, targetColor: Color = Color.Transparent): Modifier = this
    .graphicsLayer(clip = true)
    .drawWithContent {
        drawContent()
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    targetColor
                ),
                startY = size.height - edgeHeight.toPx(),
                endY = size.height
            ),
            size = size
        )
    }
