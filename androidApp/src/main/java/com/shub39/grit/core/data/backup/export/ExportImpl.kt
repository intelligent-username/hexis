package com.shub39.grit.core.data.backup.export

import com.shub39.grit.core.data.backup.ExportSchema
import com.shub39.grit.core.data.backup.toCategorySchema
import com.shub39.grit.core.data.backup.toHabitSchema
import com.shub39.grit.core.data.backup.toHabitStatusSchema
import com.shub39.grit.core.data.backup.toTaskSchema
import com.shub39.grit.core.habits.HabitRepo
import com.shub39.grit.core.now
import com.shub39.grit.core.settings.backup.ExportRepo
import com.shub39.grit.core.tasks.TaskRepo
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.writeString
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single(binds = [ExportRepo::class])
class ExportImpl(private val taskRepo: TaskRepo, private val habitsRepo: HabitRepo) : ExportRepo {
    @OptIn(ExperimentalTime::class)
    override suspend fun exportToJson() {
        coroutineScope {
            val habitsDef =
                async {
                        withContext(Dispatchers.IO) {
                            habitsRepo.getHabits().map { it.toHabitSchema() }
                        }
                    }
                    .await()

            val statusesDef =
                async {
                        withContext(Dispatchers.IO) {
                            habitsRepo.getHabitStatuses().map { it.toHabitStatusSchema() }
                        }
                    }
                    .await()

            val tasksDef =
                async {
                        withContext(Dispatchers.IO) {
                            taskRepo.getTasks().map { it.toTaskSchema() }
                        }
                    }
                    .await()

            val categoriesDef =
                async {
                        withContext(Dispatchers.IO) {
                            taskRepo.getCategories().map { it.toCategorySchema() }
                        }
                    }
                    .await()

            val time = LocalDateTime.now().toString().replace(":", "").replace(" ", "")
            val file =
                FileKit.openFileSaver(
                    suggestedName = "Hexis-Export-$time",
                    defaultExtension = "json",
                )

            file?.writeString(
                Json.encodeToString(
                    ExportSchema(
                        habits = habitsDef,
                        habitStatus = statusesDef,
                        tasks = tasksDef,
                        categories = categoriesDef,
                    )
                )
            )
        }
    }
}