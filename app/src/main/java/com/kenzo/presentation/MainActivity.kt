package com.kenzo.presentation

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.kenzo.presentation.ui.theme.PresentationTheme
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Presentation()
        }
    }
}

@Composable
fun Presentation() {
    PresentationTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            val scope = rememberCoroutineScope()
            val context = LocalContext.current
            var data by remember { mutableStateOf(Data()) }
            val systemUiController = rememberSystemUiController()
            LaunchedEffect(Unit) {
                scope.launch {
                    data = readProfile(context)
                    systemUiController.setStatusBarColor(Color(data.color))
                }
            }

            var showingDetail by remember { mutableStateOf<Data.Item?>(null) }
            Main(data) { showingDetail = it }

            showingDetail?.let {
                Detail(it) { showingDetail = null }
            }
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
private fun readProfile(context: Context): Data =
    context.assets.open("profile.json").bufferedReader().use { it.readText() }.let {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
            .adapter<Data>()
            .fromJson(it)
    } ?: Data()

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Main(data: Data, showDetail: (Data.Item) -> Unit) {
    val pagerState by remember { mutableStateOf(PagerState(0)) }
    val scope = rememberCoroutineScope()
    val color = Color(data.color)
    HorizontalPager(
        pageCount = data.pageCount + 2,
        state = pagerState
    ) {
        when (val page = Page.find(it, data)) {
            Page.Title -> Title(data.title, data.iconUrl, color) { scrollToNextPage(scope, pagerState) }
            is Page.Profile -> Profile(page.profile) { scrollToNextPage(scope, pagerState) }
            is Page.Timeline -> Timeline(data.birthYear, page.timeline, showDetail) {
                scrollToNextPage(
                    scope,
                    pagerState
                )
            }
            Page.End -> End(data.iconUrl, color) { scrollToNextPage(scope, pagerState, -pagerState.currentPage) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun scrollToNextPage(scope: CoroutineScope, pagerState: PagerState, pages: Int = 1) {
    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + pages) }
}

@Composable
fun Title(title: String, iconUrl: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = iconUrl,
            contentDescription = null,
            modifier = Modifier
                .size(180.dp)
                .padding(16.dp)
                .align(Alignment.BottomEnd)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.h3,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun End(iconUrl: String, color: Color, reset: () -> Unit) {
    var dialogVisible by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .clickable { dialogVisible = true }
    ) {
        AsyncImage(
            model = iconUrl,
            contentDescription = null,
            modifier = Modifier
                .size(180.dp)
                .padding(16.dp)
                .align(Alignment.BottomEnd)
        )
        Text(
            text = "End",
            style = MaterialTheme.typography.h3,
            modifier = Modifier.align(Alignment.Center)
        )
    }
    ResetConfirmationDialog(
        visible = dialogVisible,
        reset = { reset() },
        dismiss = { dialogVisible = false }
    )
}

@Composable
fun Profile(
    profile: Data.Profile,
    next: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = profile.name,
                style = MaterialTheme.typography.h4
            )
            profile.items.forEach {
                Text(
                    text = it,
                    style = MaterialTheme.typography.h5,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                )
            }
        }
        Image(
            painter = painterResource(id = R.drawable.baseline_chevron_right_24),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .clip(RoundedCornerShape(9999.dp))
                .clickable(onClick = next)
                .padding(8.dp)
        )
    }
}

@Composable
fun Timeline(
    birthYear: Int,
    timeline: Data.Timeline,
    showDetail: (Data.Item) -> Unit,
    next: () -> Unit
) {
    val density = LocalDensity.current
    var yearWidth by remember { mutableStateOf(0.dp) }
    val yearRange = timeline.startYear..
        (timeline.endYear ?: Calendar.getInstance().get(Calendar.YEAR))
    Column(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { yearWidth = with(density) { (it.width / yearRange.count()).toDp() } }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp)
        ) {
            Text(
                text = timeline.title,
                style = MaterialTheme.typography.h4
            )
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(id = R.drawable.baseline_chevron_right_24),
                contentDescription = null,
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .clickable(onClick = next)
                    .padding(8.dp)
            )
        }
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Years(birthYear, yearRange, yearWidth, timeline.useConvertedYear)
            Column {
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    timeline.items.forEachIndexed { index, item ->
                        TimelineItem(item, yearRange, yearWidth, index, timeline.yearStrict, showDetail)
                    }
                }
            }
            Text(
                text = timeline.memo,
                style = MaterialTheme.typography.subtitle2,
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.BottomStart)
            )
        }
    }
}

@Composable
fun Years(birthYear: Int, yearRange: IntRange, yearWidth: Dp, useConvertedYear: Boolean) {
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        yearRange.forEach {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(yearWidth)
            ) {
                Text(
                    text = if (useConvertedYear) it.convertYearToGrade(birthYear) else it.toString(),
                    style = MaterialTheme.typography.subtitle2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(color = colorResource(id = R.color.border))
                )
            }
        }
    }
}

@Composable
fun TimelineItem(
    item: Data.Item,
    yearRange: IntRange,
    yearWidth: Dp,
    index: Int,
    yearStrict: Boolean,
    showDetail: (Data.Item) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        val startMargin = yearWidth * (item.startYear - yearRange.first).coerceAtLeast(0) +
            if (yearStrict) 8.dp else yearWidth / 4
        Spacer(modifier = Modifier.width(startMargin))
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.CenterStart)
            ) {
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { showDetail(item) }
                        .background(color = timelineColors[index % timelineColors.size])
                        .padding(8.dp)
                ) {
                    Text(
                        text = "",
                        style = MaterialTheme.typography.subtitle2
                    )
                }
                val endMargin = yearWidth * (yearRange.last - (item.endYear ?: yearRange.last)).coerceAtLeast(0) +
                    if (yearStrict) 8.dp else yearWidth / 2
                Spacer(modifier = Modifier.width(endMargin))
            }
            Text(
                text = item.name,
                style = MaterialTheme.typography.subtitle2,
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { showDetail(item) }
            )
        }
    }
}

@Composable
fun Detail(item: Data.Item, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colors.background)
            .verticalScroll(rememberScrollState())
    ) {
        Row {
            Image(
                painter = painterResource(id = R.drawable.baseline_chevron_left_24),
                contentDescription = null,
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .clickable(onClick = onBack)
                    .padding(8.dp)
            )
            Text(
                text = item.name,
                style = MaterialTheme.typography.h4,
                modifier = Modifier.padding(
                    bottom = 16.dp
                )
            )
        }
        item.texts.forEach {
            Text(
                text = it,
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(
                    start = 16.dp,
                    bottom = 8.dp
                )
            )
        }
    }
}

@Composable
fun ResetConfirmationDialog(
    visible: Boolean,
    reset: () -> Unit,
    dismiss: () -> Unit
) {
    if (visible) {
        AlertDialog(
            onDismissRequest = {},
            text = {
                Text("Reset?")
            },
            confirmButton = {
                TextButton(
                    onClick = reset
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = dismiss
                ) {
                    Text("Cancel")
                }
            },
        )
    }
}

private val timelineColors = listOf(
    Color(0xFFFDC2C9),
    Color(0xFFD9C2FE),
    Color(0xFFC2C9FE),
    Color(0xFFD6EDFE),
    Color(0xFFE1FBCE),
    Color(0xFFFEF9D6),
    Color(0xFFFEE5D6),
    Color(0xFFFED1DF)
)

private const val ElementarySchoolAge = 7
private const val JuniorHighSchoolAge = 13
private const val HighSchoolAge = 16
private const val ExtraYear = 1
private const val UniversityAge = 19
private const val UniversityYears = 4
private fun Int.convertYearToGrade(birthYear: Int): String =
    when (this - birthYear) {
        in 0..3 -> "幼児"
        in 5..6 -> "幼稚園"
        in ElementarySchoolAge until JuniorHighSchoolAge -> "小${this - birthYear - ElementarySchoolAge + 1}"
        in JuniorHighSchoolAge until HighSchoolAge -> "中${this - birthYear - JuniorHighSchoolAge + 1}"
        in HighSchoolAge until UniversityAge -> "高${this - birthYear - HighSchoolAge + 1}"
        in UniversityAge until UniversityAge + ExtraYear -> "浪人"
        in UniversityAge + ExtraYear until UniversityAge + ExtraYear + UniversityYears ->
            "大${this - birthYear - UniversityAge - ExtraYear + 1}"
        else -> "社会人${this - birthYear - UniversityAge - ExtraYear - UniversityYears + 1}年目"
    }