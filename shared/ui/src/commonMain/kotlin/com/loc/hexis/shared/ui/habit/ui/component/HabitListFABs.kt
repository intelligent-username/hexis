package com.loc.hexis.shared.ui.habit.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.loc.hexis.shared.ui.LocalWindowSizeClass
import com.loc.hexis.shared.ui.habit.HabitState
import com.loc.hexis.shared.ui.habit.HabitsAction
import hexis.shared.ui.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun BoxScope.HabitListFABs(
    onNavigateToOverallAnalytics: () -> Unit,
    state: HabitState,
    fabVisible: Boolean,
    onAction: (HabitsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val windowSizeClass = LocalWindowSizeClass.current

    Row(
        modifier =
            modifier
                .padding(16.dp)
                .then(
                    if (windowSizeClass.widthSizeClass != WindowWidthSizeClass.Expanded) Modifier
                    else Modifier.navigationBarsPadding()
                )
                .align(
                    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
                        Alignment.BottomStart
                    } else {
                        Alignment.BottomEnd
                    }
                ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        if (windowSizeClass.widthSizeClass != WindowWidthSizeClass.Expanded) {
            FloatingActionButton(
                onClick = onNavigateToOverallAnalytics,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier =
                    Modifier.animateFloatingActionButton(
                        visible = state.habitsWithAnalytics.isNotEmpty() && fabVisible,
                        alignment = Alignment.BottomEnd,
                    ),
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.analytics),
                    contentDescription = "All Analytics",
                )
            }

            MediumFloatingActionButton(
                onClick = { onAction(HabitsAction.OnAddHabitClicked) },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier =
                    Modifier.animateFloatingActionButton(
                        visible = fabVisible,
                        alignment = Alignment.BottomEnd,
                    ),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.add),
                        contentDescription = "Add Habit",
                        modifier = Modifier.size(FloatingActionButtonDefaults.MediumIconSize),
                    )

                    AnimatedVisibility(
                        visible = state.habitsWithAnalytics.isEmpty(),
                        enter = fadeIn(MaterialTheme.motionScheme.fastEffectsSpec()),
                        exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()),
                    ) {
                        Text(text = stringResource(Res.string.add_habit))
                    }
                }
            }
        } else {
            MediumFloatingActionButton(
                onClick = { onAction(HabitsAction.OnAddHabitClicked) },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier =
                    Modifier.animateFloatingActionButton(
                        visible = fabVisible,
                        alignment = Alignment.BottomEnd,
                    ),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.add),
                        contentDescription = "Add Habit",
                        modifier = Modifier.size(FloatingActionButtonDefaults.MediumIconSize),
                    )

                    AnimatedVisibility(
                        visible = state.habitsWithAnalytics.isEmpty(),
                        enter = fadeIn(MaterialTheme.motionScheme.fastEffectsSpec()),
                        exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()),
                    ) {
                        Text(text = stringResource(Res.string.add_habit))
                    }
                }
            }

            AnimatedVisibility(
                visible = state.analyticsHabitId != null,
                enter = fadeIn(MaterialTheme.motionScheme.fastEffectsSpec()),
                exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()),
            ) {
                FloatingActionButton(
                    onClick = onNavigateToOverallAnalytics,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier =
                        Modifier.animateFloatingActionButton(
                            visible = state.habitsWithAnalytics.isNotEmpty() && fabVisible,
                            alignment = Alignment.BottomEnd,
                        ),
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.analytics),
                        contentDescription = "All Analytics",
                    )
                }
            }
        }
    }
}
