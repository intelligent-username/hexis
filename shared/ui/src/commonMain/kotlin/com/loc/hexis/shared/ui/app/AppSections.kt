package com.loc.hexis.shared.ui.app

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import hexis.shared.ui.generated.resources.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

@Serializable
sealed interface AppSections : NavKey {
    @Serializable data object HabitPages : AppSections

    @Serializable data object TaskPages : AppSections

    @Serializable data object SettingsPages : AppSections

    companion object {
        val configuration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(TaskPages::class, TaskPages.serializer())
                    subclass(HabitPages::class, HabitPages.serializer())
                    subclass(SettingsPages::class, SettingsPages.serializer())
                }
            }
        }

        val mainRoutes: List<AppSections> = listOf(TaskPages, HabitPages, SettingsPages)

        fun AppSections.toStringRes(): StringResource {
            return when (this) {
                HabitPages -> Res.string.habits
                TaskPages -> Res.string.tasks
                SettingsPages -> Res.string.settings
            }
        }

        fun AppSections.toIconRes(): DrawableResource {
            return when (this) {
                HabitPages -> Res.drawable.alarm
                TaskPages -> Res.drawable.check_list
                SettingsPages -> Res.drawable.settings
            }
        }
    }
}