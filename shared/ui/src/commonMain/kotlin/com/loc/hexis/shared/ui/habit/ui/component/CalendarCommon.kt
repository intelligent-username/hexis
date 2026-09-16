
package com.loc.hexis.shared.ui.habit.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.core.CalendarMonth
import com.loc.hexis.shared.ui.theme.flexFontRounded
import kotlinx.datetime.YearMonth
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char

@Composable
fun CalendarMonthHeader(
    calendarMonth: CalendarMonth,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge,
    padding: PaddingValues = PaddingValues(start = 4.dp, end = 4.dp, top = 32.dp, bottom = 8.dp),
) {
    Box(modifier = modifier.padding(padding)) {
        Text(
            text =
                calendarMonth.yearMonth.format(
                    YearMonth.Format {
                        monthName(MonthNames.ENGLISH_FULL)
                        char(' ')
                        year()
                    }
                ),
            style =
                style.copy(
                    color = MaterialTheme.colorScheme.secondary,
                    fontFamily = flexFontRounded(),
                ),
            modifier = Modifier.align(Alignment.Center),
        )
    }
}
