package com.loc.hexis.tasks.data.database

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories") fun getCategoriesFlow(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories") suspend fun getCategories(): List<CategoryEntity>

    @Upsert suspend fun upsertCategory(categoryEntity: CategoryEntity)

    @Delete suspend fun deleteCategory(categoryEntity: CategoryEntity)

    @Query("DELETE FROM categories") suspend fun deleteAllCategories()
}