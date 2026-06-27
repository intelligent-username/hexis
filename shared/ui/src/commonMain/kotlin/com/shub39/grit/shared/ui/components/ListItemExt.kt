package com.shub39.grit.shared.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

private const val CONNECTED_CORNER_RADIUS = 4
private const val END_CORNER_RADIUS = 16

@Composable
fun listItemColors(): ListItemColors {
    return ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
}

fun leadingItemShape(
    topRadius: Int = END_CORNER_RADIUS,
    bottomRadius: Int = CONNECTED_CORNER_RADIUS,
): Shape =
    RoundedCornerShape(
        topStart = topRadius.dp,
        topEnd = topRadius.dp,
        bottomEnd = bottomRadius.dp,
        bottomStart = bottomRadius.dp,
    )

fun middleItemShape(radius: Int = CONNECTED_CORNER_RADIUS): Shape =
    RoundedCornerShape(
        topStart = radius.dp,
        topEnd = radius.dp,
        bottomStart = radius.dp,
        bottomEnd = radius.dp,
    )

fun endItemShape(
    topRadius: Int = CONNECTED_CORNER_RADIUS,
    bottomRadius: Int = END_CORNER_RADIUS,
): Shape =
    RoundedCornerShape(
        topStart = topRadius.dp,
        topEnd = topRadius.dp,
        bottomEnd = bottomRadius.dp,
        bottomStart = bottomRadius.dp,
    )

fun detachedItemShape(radius: Int = END_CORNER_RADIUS): Shape = RoundedCornerShape(radius.dp)

@Composable
fun segmentedListItemShapes(
    index: Int,
    count: Int,
    singleElement: Boolean = count == 1,
): ListItemShapes =
    ListItemDefaults.segmentedShapes(
        index,
        count,
        ListItemDefaults.shapes(
            shape = if (singleElement) shapes.large else shapes.extraSmall,
            selectedShape = shapes.extraLargeIncreased,
            pressedShape = shapes.extraLargeIncreased,
            focusedShape = shapes.large,
            hoveredShape = shapes.extraLarge,
            draggedShape = shapes.extraLargeIncreased,
        ),
    )