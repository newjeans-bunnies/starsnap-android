package com.photo.starsnap.main.ui.screen.main.upload

import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.photo.starsnap.designsystem.CustomColor
import com.photo.starsnap.designsystem.R
import com.photo.starsnap.designsystem.StarSnapColor
import com.photo.starsnap.designsystem.text.CustomTextStyle
import com.photo.starsnap.designsystem.text.CustomTextStyle.title9
import com.photo.starsnap.main.ui.component.TextEditHint
import com.photo.starsnap.main.ui.component.TopAppBar
import com.photo.starsnap.main.utils.NavigationRoute
import com.photo.starsnap.main.utils.clickableSingle
import com.photo.starsnap.main.viewmodel.main.CroppingImage
import com.photo.starsnap.main.viewmodel.main.UploadViewModel
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

@Composable
fun SetSnapScreen(navController: NavController, uploadViewModel: UploadViewModel) {
    LaunchedEffect(Unit) {
        Log.d("화면", "SetSnapScreen")
    }
    val context = LocalContext.current
    val selectedPhotos by uploadViewModel.selectedPhotos.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { selectedPhotos.size })

    var showDots by remember { mutableStateOf(false) }
    var showModalInput by remember { mutableStateOf(false) }

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    // snap 정보
    var title by remember { mutableStateOf("") } // 제목
    var source by remember { mutableStateOf("") } // 출처
    var tags by remember { mutableStateOf(listOf<String>()) } // 태그
    val stars by uploadViewModel.selectedStars.collectAsStateWithLifecycle()
    val starGroups by uploadViewModel.selectedStarGroups.collectAsStateWithLifecycle()
    var dateTaken by remember { mutableStateOf("YYYY-MM-DD") }
    var aiState by remember { mutableStateOf(false) } // AI 여부
    var commentsEnabled by remember { mutableStateOf(true) } // 댓글 허용 여부

    LaunchedEffect(pagerState.isScrollInProgress) {
        if (pagerState.isScrollInProgress) {
            showDots = true
        } else {
            // Hide after a short delay when scrolling stops
            delay(600)
            showDots = false
        }
    }

    val canSubmit = title.isNotBlank() && selectedPhotos.isNotEmpty()

    Scaffold(
        topBar = { TopAppBar("새로운 스냅", onBack = { navController.popBackStack() }) },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (canSubmit) CustomColor.yellow_500 else CustomColor.button)
                    .then(
                        if (canSubmit) {
                            Modifier.clickableSingle {
                                uploadViewModel.uploadSnap(
                                    context,
                                    title,
                                    tags,
                                    source,
                                    dateTaken,
                                    aiState,
                                    commentsEnabled
                                )
                            }
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (canSubmit) "스냅 만들기" else "제목을 입력해 주세요",
                    color = if (canSubmit) CustomColor.light_black else CustomColor.gray,
                    style = CustomTextStyle.TitleSmall
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState(), enabled = true, flingBehavior = null)
                .padding(bottom = 8.dp),
        ) {
            val starGridState = rememberLazyGridState()
            val starGroupGridState = rememberLazyGridState()

            selectedPhoto(pagerState, selectedPhotos)
            PagerDots(showDots = showDots, pagerState = pagerState, selectedPhotos = selectedPhotos)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "선택된 사진 ${selectedPhotos.size}장",
                    style = CustomTextStyle.title9,
                    color = CustomColor.sub_title
                )
            }

            SectionCard(
                title = "기본 정보",
                subtitle = "제목은 필수입니다. 태그는 최대 5개까지 가능해요."
            ) {
                InputText(
                    hint = "제목을 입력하세요",
                    text = title,
                    maxLength = 60,
                    isRequired = true
                ) {
                    title = it
                }
                Spacer(Modifier.height(10.dp))
                InputText(
                    hint = "출처를 입력 해주세요",
                    text = source,
                    maxLength = 60,
                ) {
                    source = it
                }
                Spacer(Modifier.height(10.dp))
                ChipTextField(tags = tags, onTagsChange = { tags = it })
            }

            SectionCard(
                title = "Star"
            ) {
            LazyHorizontalGrid(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                state = starGridState,
                rows = GridCells.Fixed(1),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // +1 to place the "add" tile at index 0, followed by the selected stars
                items(stars.size + 1) { index ->
                    if (index == 0) {
                        // Add tile
                        Column(
                            modifier = Modifier
                                .width(70.dp)
                                .clickableSingle { navController.navigate(NavigationRoute.PICK_STAR) }) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        CustomColor.light_gray.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                                    .width(70.dp)
                                    .height(70.dp),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                Icon(
                                    modifier = Modifier.size(20.dp),
                                    imageVector = ImageVector.vectorResource(R.drawable.plus_circle_icon),
                                    contentDescription = "plus_circle_icon",
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                text = "추가하기",
                                maxLines = 1,
                                style = title9
                            )
                        }
                    } else {
                        // Star item from the collected list
                        val star = stars[index - 1]
                        Column(
                            modifier = Modifier.width(70.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        CustomColor.light_gray.copy(alpha = 0.15f),
                                        shape = CircleShape
                                    )
                                    .width(70.dp)
                                    .height(70.dp),
                                contentAlignment = Alignment.Center
                            ) {}
                            Spacer(Modifier.height(8.dp))
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                text = star.name,
                                maxLines = 1,
                                style = title9
                            )
                        }
                    }
                }
            }
            }

            SectionCard(title = "StarGroup") {
            LazyHorizontalGrid(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(87.dp),
                state = starGroupGridState,
                rows = GridCells.Fixed(1),
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {

                items(starGroups.size + 1) { index ->
                    if (index == 0) {
                        // Add tile
                        Column(
                            modifier = Modifier
                                .width(100.dp)
                                .clickableSingle {
                                    navController.navigate(NavigationRoute.PICK_STAR_GROUP)
                                }) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        CustomColor.light_gray.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .width(95.dp)
                                    .height(60.dp),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                Icon(
                                    modifier = Modifier.size(20.dp),
                                    imageVector = ImageVector.vectorResource(R.drawable.plus_circle_icon),
                                    contentDescription = "plus_circle_icon",
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                text = "추가하기",
                                maxLines = 1,
                                style = title9
                            )
                        }
                    } else {
                        // Star item from the collected list
                        val starGroup = starGroups[index - 1]
                        Column(
                            modifier = Modifier
                                .width(100.dp)
                                .clickableSingle {
//                                    uploadViewModel.selectedStarGroup(starGroup)
                                }) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        CustomColor.light_gray.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .width(95.dp)
                                    .height(60.dp),
                                contentAlignment = Alignment.BottomEnd
                            ) {

                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                text = starGroup.name,
                                maxLines = 1,
                                style = title9
                            )
                        }
                    }
                }
                items(1) {

                }
            }
            }

            SectionCard(title = "촬영 정보") {
                Text(
                    text = "사진 찍은 날짜",
                    style = CustomTextStyle.title9,
                    color = CustomColor.sub_title
                )
                Spacer(Modifier.height(6.dp))
                DateTextField(dateTaken) {
                    showModalInput = true
                }
            }

            SectionCard(title = "게시 옵션") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI 생성 사진",
                        style = CustomTextStyle.title2,
                        color = CustomColor.title
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "AI로 제작된 사진은 반드시 체크해 주세요",
                        style = CustomTextStyle.body1,
                        color = CustomColor.sub_title
                    )
                }
                Switch(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    checked = aiState,
                    onCheckedChange = {
                        aiState = it
                    },
                colors = SwitchDefaults.colors(
                        // 1. 켜졌을 때(Checked) 색상
                        checkedThumbColor = CustomColor.container, // 동그라미 색 (보라색 예시)
                        checkedTrackColor = CustomColor.yellow_500, // 배경 색 (연보라색 예시)

                        // 2. 꺼졌을 때(Unchecked) 색상
                        uncheckedThumbColor = CustomColor.container, // 동그라미 색
                        uncheckedTrackColor = CustomColor.yellow_200,      // 배경 색

                        // 3. (선택사항) 테두리 색상 - Material 3 등에서 사용
                        uncheckedBorderColor = CustomColor.container,
                        checkedBorderColor = CustomColor.container
                    )
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "댓글 허용",
                        style = CustomTextStyle.title2,
                        color = CustomColor.title
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "댓글 기능을 비활성화하려면 끄세요",
                        style = CustomTextStyle.body1,
                        color = CustomColor.sub_title
                    )
                }
                Switch(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    checked = commentsEnabled,
                    onCheckedChange = {
                        commentsEnabled = it
                    },
                    colors = SwitchDefaults.colors(
                        // 1. 켜졌을 때(Checked) 색상
                        checkedThumbColor = CustomColor.container, // 동그라미 색 (보라색 예시)
                        checkedTrackColor = CustomColor.yellow_500, // 배경 색 (연보라색 예시)

                        // 2. 꺼졌을 때(Unchecked) 색상
                        uncheckedThumbColor = CustomColor.container, // 동그라미 색
                        uncheckedTrackColor = CustomColor.yellow_200,      // 배경 색

                        // 3. (선택사항) 테두리 색상 - Material 3 등에서 사용
                        uncheckedBorderColor = CustomColor.container,
                        checkedBorderColor = CustomColor.container
                    )
                )
            }
            }

            Spacer(Modifier.height(16.dp))
        }
        if (showModalInput) {
            Dialog(onDismissRequest = { showModalInput = false }) {
                Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {

                    Column(modifier = Modifier.padding(16.dp)) {
                        // 달력 모달 본문
                        calendar(
                            currentSelectedDate = selectedDate,
                            onDateChange = { picked -> selectedDate = picked })
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = {
                                showModalInput = false
                                dateTaken = "YYYY-MM-DD"
                            }) { Text("초기화") }
                            Spacer(Modifier.weight(1F))
                            TextButton(onClick = { showModalInput = false }) { Text("취소") }
                            Spacer(Modifier.width(8.dp))
                            TextButton(onClick = {
                                showModalInput = false
                                dateTaken =
                                    selectedDate?.toString() ?: "YYYY-MM-DD"
                            }) { Text("확인") }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipTextField(
    tags: List<String>, onTagsChange: (List<String>) -> Unit
) {
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    fun addTagIfPossible(raw: String) {
        val t = raw.trim()
        if (t.isEmpty()) return
        if (t.length > 10) {
            Toast.makeText(context, "태그는 최대 10자까지 가능합니다.", Toast.LENGTH_SHORT).show()
            return
        }
        if (tags.size >= 5) {
            Toast.makeText(context, "최대 ${5}개까지 가능합니다.", Toast.LENGTH_SHORT).show()
            return
        }
        if (tags.any { it.equals(t, ignoreCase = true) }) {
            Toast.makeText(context, "이미 추가된 태그예요.", Toast.LENGTH_SHORT).show()
            return
        }
        onTagsChange(tags + t)
        input = ""
    }

    BasicTextField(
        value = input,
        onValueChange = { text ->
            // 쉼표로도 태그 확정 가능 (e.g. "kotlin,")
            if (text.endsWith(",")) {
                addTagIfPossible(text.removeSuffix(","))
            } else if (tags.size < 5) {
                input = text
            }
        },
        textStyle = CustomTextStyle.title5.copy(color = CustomColor.light_black),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { addTagIfPossible(input) }),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CustomColor.container.copy(alpha = 0.8f))
            .border(
                border = BorderStroke(0.5.dp, CustomColor.light_gray.copy(alpha = 0.55f)),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 12.dp)
            .onPreviewKeyEvent { event ->
                if (event.key == Key.Backspace && input.isEmpty() && tags.isNotEmpty()) {
                    // remove the last tag when backspace is pressed and there is no input text
                    onTagsChange(tags.dropLast(1))
                    true
                } else {
                    false
                }
            }
            .padding(vertical = 10.dp),
        decorationBox = { innerTextField ->
            // 필드 안에 Chips + 입력 커서를 함께 배치
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .background(StarSnapColor.textSoft, RoundedCornerShape(16.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(tag, color = StarSnapColor.surface)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "✕",
                                color = StarSnapColor.textMuted,
                                modifier = Modifier.clickableSingle {
                                    onTagsChange(tags - tag)
                                })
                        }
                    }
                }

                // 입력 커서가 들어갈 공간
                Box(
                    modifier = Modifier.weight(1f, fill = false),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (input.isEmpty()) {
                        TextEditHint(text = if (tags.isEmpty()) "태그 입력 후 Enter (쉼표도 가능)" else "")
                    }
                    // 실제 텍스트 입력
                    innerTextField()
                }
            }
        })
}


@Composable
fun DateTextField(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(CustomColor.container.copy(alpha = 0.8f))
            .border(
                border = BorderStroke(0.5.dp, CustomColor.light_gray.copy(alpha = 0.55f)),
                shape = RoundedCornerShape(10.dp)
            )
            .clickableSingle {
                onClick()
            }, verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(12.dp))
        Icon(
            modifier = Modifier.size(18.dp),
            imageVector = ImageVector.vectorResource(R.drawable.calendar_icon),
            contentDescription = "calendar_icon",
            tint = CustomColor.sub_title
        )
        Spacer(modifier = Modifier.width(9.dp))
        Box(
            Modifier.weight(1F)
        ) {
            if (text == "YYYY-MM-DD") TextEditHint(text)
            else Text(text = text, style = CustomTextStyle.body1)
        }
    }
}

@Composable
fun calendar(currentSelectedDate: LocalDate?, onDateChange: (LocalDate?) -> Unit) {
    val currentMonth = remember { YearMonth.now() } // 현재 날짜 구하기
    val startMonth = remember { currentMonth.minusYears(10) } // 현재 날짜에서 -10년을 마지막으로
    val endMonth = remember { YearMonth.now() } // 최대 일을 현재로
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() } // Available from the library
    val daysOfWeek = remember { daysOfWeek() }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek,
    )

    val coroutineScope = rememberCoroutineScope()
    val visibleMonth = state.firstVisibleMonth.yearMonth
    val monthTitle = "${visibleMonth.year}년 ${visibleMonth.month.value}월"

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier
                    .size(18.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        coroutineScope.launch {
                            state.animateScrollToMonth(
                                visibleMonth.minusMonths(
                                    1
                                )
                            )
                        }
                    },
                imageVector = ImageVector.vectorResource(R.drawable.chevron_left_icon),
                contentDescription = "chevron_left_icon",
                tint = CustomColor.sub_title
            )
            Text(
                text = monthTitle,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1F),
                style = CustomTextStyle.title1
            )
            // Conditionally show right arrow icon or spacer to preserve layout
            if (visibleMonth.isBefore(endMonth)) {
                Icon(
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            coroutineScope.launch {
                                state.animateScrollToMonth(
                                    visibleMonth.plusMonths(1)
                                )
                            }
                        },
                    imageVector = ImageVector.vectorResource(R.drawable.chevron_right_icon),
                    contentDescription = "chevron_right_icon",
                    tint = CustomColor.sub_title
                )
            } else {
                Spacer(modifier = Modifier.size(18.dp))
            }
        }
        HorizontalCalendar(state = state, dayContent = { day ->
            Day(
                day = day,
                isSelected = currentSelectedDate == day.date,
                onClick = { clickedDay ->
                    val newSelection =
                        if (currentSelectedDate == clickedDay.date) null else clickedDay.date
                    onDateChange(newSelection)
                })
        }, monthHeader = {
            DaysOfWeekTitle(daysOfWeek = daysOfWeek)
        })
    }
}

@Composable
fun Day(day: CalendarDay, isSelected: Boolean, onClick: (CalendarDay) -> Unit) {
    val isMonthDate = day.position == DayPosition.MonthDate
    val d = day.date

    val textColor = when {
        !isMonthDate -> CustomColor.light_gray
        d.dayOfWeek == java.time.DayOfWeek.SUNDAY -> Color.Red
        d.dayOfWeek == java.time.DayOfWeek.SATURDAY -> Color.Blue
        else -> CustomColor.light_black
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(color = if (isSelected) CustomColor.container else Color.Transparent)
            .clickable(
                enabled = isMonthDate, onClick = { onClick(day) }),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = d.dayOfMonth.toString(), color = textColor
        )
    }
}

@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = dayOfWeek.getDisplayName(
                    java.time.format.TextStyle.SHORT, Locale.getDefault()
                ),
            )
        }
    }
}

@Composable
@Preview
fun StarProfile() {
    Column(
        modifier = Modifier
            .width(70.dp)
            .clickableSingle {

            }) {
        Box(
            modifier = Modifier
                .background(
                    CustomColor.light_gray.copy(alpha = 0.3f), shape = CircleShape
                )
                .width(70.dp)
                .height(70.dp), contentAlignment = Alignment.BottomEnd
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.plus_circle_icon),
                contentDescription = "plus_circle_icon",
            )
        }
        Spacer(Modifier.height(3.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = "추가하기",
            maxLines = 1
        )
    }
}

@Composable
fun StarGroup() {

}

@Composable
fun SectionCard(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(text = title, style = CustomTextStyle.title9, color = CustomColor.sub_title)
        if (!subtitle.isNullOrBlank()) {
            Spacer(Modifier.height(3.dp))
            Text(text = subtitle, style = CustomTextStyle.body1, color = CustomColor.sub_title)
        }
        Spacer(Modifier.height(8.dp))
        content()
        Spacer(Modifier.height(8.dp))
        Divider(color = CustomColor.light_gray.copy(alpha = 0.45f), thickness = 0.6.dp)
    }
}

@Composable
fun InputText(
    modifier: Modifier = Modifier,
    hint: String,
    text: String,
    maxLength: Int = 60,
    isRequired: Boolean = false,
    inputText: (String) -> Unit
) {
    BasicTextField(
        value = text,
        onValueChange = { it ->
            if (it.length <= maxLength) {
                inputText(it)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CustomColor.container.copy(alpha = 0.8f))
            .border(
                border = BorderStroke(0.5.dp, CustomColor.light_gray.copy(alpha = 0.55f)),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        decorationBox = { innerTextField ->
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) {
                        if (text.isEmpty()) TextEditHint(hint)
                        innerTextField()
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${text.length}/$maxLength",
                        style = CustomTextStyle.body1,
                        color = CustomColor.gray
                    )
                }
                if (isRequired && text.isBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "필수 입력 항목입니다.",
                        style = CustomTextStyle.body1,
                        color = CustomColor.error
                    )
                }
            }
        })
}


// 위치 확인 하는
@Composable
fun PagerDots(showDots: Boolean, pagerState: PagerState, selectedPhotos: List<CroppingImage>) {
    // 사진 위치 상태
    val dotsAlpha by animateFloatAsState(
        targetValue = if (showDots) 1f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "dotsAlpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .alpha(dotsAlpha), // 공간 유지 + 페이드 애니메이션
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(selectedPhotos.size) { index ->
            val isSelected = pagerState.currentPage == index
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (isSelected) 8.dp else 6.dp)
                    .background(
                        color = if (isSelected) CustomColor.light_black
                        else CustomColor.light_gray.copy(alpha = 0.3f), shape = CircleShape
                    )
            )
        }
    }
}

@Composable
fun selectedPhoto(pagerState: PagerState, selectedPhotos: List<CroppingImage>) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
    ) { page ->
        val photo = selectedPhotos[page]
        GlideImage(
            modifier = Modifier
                .animateContentSize()
                .fillMaxSize(),
            imageModel = { photo.imageUri },
            imageOptions = ImageOptions(
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                alpha = 1f
            )
        )
    }
}
