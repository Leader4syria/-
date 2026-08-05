package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.PlannedSessionEntity
import com.example.data.models.SessionStatus
import com.example.data.models.StudentMemoryEntity
import com.example.ui.theme.*
import com.example.ui.viewmodels.MasarViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: MasarViewModel,
    onStartFocus: (PlannedSessionEntity) -> Unit,
    onNavigateToPlan: (() -> Unit)? = null
) {
    val profile by viewModel.profileFlow.collectAsState()
    val sessions by viewModel.selectedDateSessionsFlow.collectAsState()
    val checkIn by viewModel.selectedDateCheckIn.collectAsState()
    val exams by viewModel.examsFlow.collectAsState()
    val activeSubjects by viewModel.activeSubjectsFlow.collectAsState()
    val memories by viewModel.memoriesFlow.collectAsState()

    val completedCount = sessions.count { it.status == SessionStatus.COMPLETED }
    val totalPlannedMinutes = sessions.sumOf { it.endTimeMinutes - it.startTimeMinutes }
    val completedMinutes = sessions.filter { it.status == SessionStatus.COMPLETED }.sumOf { it.actualMinutes }

    val nextSession = sessions.firstOrNull { it.status == SessionStatus.PLANNED }

    val hour = LocalTime.now().hour
    val greetingTime = when {
        hour in 5..11 -> "صباح الخير"
        hour in 12..16 -> "مساء الخير"
        else -> "مساء الخير"
    }

    // Nearest upcoming exam calculation
    val todayStr = remember { LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) }
    val upcomingExams = remember(exams, todayStr) {
        exams.filter { it.examDate >= todayStr }.sortedBy { it.examDate }
    }
    val nextExam = upcomingExams.firstOrNull()
    val daysToNextExam = remember(nextExam, todayStr) {
        nextExam?.let {
            try {
                val examD = LocalDate.parse(it.examDate)
                val todayD = LocalDate.now()
                java.time.temporal.ChronoUnit.DAYS.between(todayD, examD).toInt()
            } catch (e: Exception) {
                4
            }
        } ?: 4
    }

    // Interactive Student Timer State (In-line Quick Timer)
    var isTimerRunning by remember { mutableStateOf(false) }
    var timerSecondsRemaining by remember { mutableStateOf(25 * 60) }
    var showMemoryDialog by remember { mutableStateOf(false) }
    var memoryTitleInput by remember { mutableStateOf("") }
    var memoryContentInput by remember { mutableStateOf("") }

    // Quick Timer Countdown Effect
    LaunchedEffect(isTimerRunning, timerSecondsRemaining) {
        if (isTimerRunning && timerSecondsRemaining > 0) {
            delay(1000L)
            timerSecondsRemaining -= 1
        } else if (timerSecondsRemaining == 0) {
            isTimerRunning = false
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        // 1. Header Row (Avatar, Name, Greeting & Notification Bell)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User Avatar & Greeting Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(PrimaryIndigo.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile?.name?.take(1)?.ifEmpty { "م" } ?: "م",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = PrimaryIndigo
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "$greetingTime",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "شو عندك اليوم؟",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Notification Bell Button
                IconButton(
                    onClick = { /* Notifications drawer or dialog */ },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "التنبيهات",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // 2. Top Metric Horizontal Row (Matching Image Reference Cards)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Metric 1: Today Study Hours
                MetricChip(
                    modifier = Modifier.weight(1f),
                    title = "دراسة اليوم",
                    value = "${(totalPlannedMinutes / 60).coerceAtLeast(1)} ساعات",
                    icon = Icons.Outlined.Schedule,
                    iconBgColor = Color(0xFFF3E8FF),
                    iconTintColor = Color(0xFF8B5CF6)
                )

                // Metric 2: Free Time
                MetricChip(
                    modifier = Modifier.weight(1f),
                    title = "وقت فراغ",
                    value = "ساعتين",
                    icon = Icons.Outlined.Timer,
                    iconBgColor = Color(0xFFE0F2FE),
                    iconTintColor = Color(0xFF0284C7)
                )

                // Metric 3: Upcoming Exam Countdown
                MetricChip(
                    modifier = Modifier.weight(1f),
                    title = nextExam?.title ?: "امتحان فيزياء",
                    value = "بعد $daysToNextExam أيام",
                    icon = Icons.Outlined.CalendarToday,
                    iconBgColor = Color(0xFFFFE4E6),
                    iconTintColor = Color(0xFFE11D48),
                    showArrow = true
                )
            }
        }

        // 3. Featured Hero Card (Next Planned Session - הגلسة القادمة)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = PrimaryIndigo.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Side: 3D Notebook Graphic Canvas
                    StudyIllustration3D(
                        modifier = Modifier
                            .width(110.dp)
                            .height(110.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Right Side: Session Details & Action Button
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "الجلسة القادمة",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryIndigo.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val subjectName = nextSession?.let { s ->
                            activeSubjects.firstOrNull { it.id == s.subjectId }?.name ?: "رياضيات"
                        } ?: "رياضيات"

                        Text(
                            text = "$subjectName — تفاضل",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val timeDisplay = nextSession?.let {
                            val startH = it.startTimeMinutes / 60
                            val startM = it.startTimeMinutes % 60
                            val endH = it.endTimeMinutes / 60
                            val endM = it.endTimeMinutes % 60
                            "%02d:%02d - %02d:%02d".format(startH, startM, endH, endM)
                        } ?: "08:00 - 08:40"

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = timeDisplay,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Prominent Purple Pill Action Button
                        Button(
                            onClick = {
                                if (nextSession != null) {
                                    onStartFocus(nextSession)
                                } else {
                                    viewModel.triggerReschedule()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("start_session_hero_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "بلش الجلسة",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Three Stat Cards Row (Readiness %, Overdue Tasks, Today Reviews)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: Exam Readiness Circular Progress
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { 0.78f },
                                modifier = Modifier.size(44.dp),
                                color = PrimaryIndigo,
                                trackColor = PrimaryIndigo.copy(alpha = 0.12f),
                                strokeWidth = 4.dp
                            )
                            Text(
                                text = "78%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "جاهزية للاختبارات",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                // Card 2: Overdue Tasks
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "2",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFEA580C)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Outlined.FormatListNumbered,
                                contentDescription = null,
                                tint = Color(0xFFEA580C),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "مهمتان متأخرتان",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                // Card 3: Reviews Today
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "3",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = PrimaryIndigo
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Outlined.MenuBook,
                                contentDescription = null,
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "مراجعات اليوم",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 5. Today's Plan Timeline Header Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "خطة اليوم",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                TextButton(
                    onClick = { onNavigateToPlan?.invoke() }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "عرض الكل",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // 6. Timeline List of Today's Sessions
        if (sessions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EventAvailable,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لا توجد جلسات مجدولة حالياً",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.triggerReschedule() },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                        ) {
                            Text("توليد جدول اليوم تلقائياً")
                        }
                    }
                }
            }
        } else {
            items(sessions) { session ->
                val subject = activeSubjects.firstOrNull { it.id == session.subjectId }
                val startH = session.startTimeMinutes / 60
                val startM = session.startTimeMinutes % 60
                val duration = session.endTimeMinutes - session.startTimeMinutes

                TimelineSessionItem(
                    timeStr = "%02d:%02d".format(startH, startM),
                    durationStr = "$duration د",
                    subjectTitle = subject?.name ?: "مادة مخصصة",
                    topicSubtitle = session.explanation.ifEmpty { "مراجعة وتمارين" },
                    sessionStatus = session.status,
                    onStartClick = { onStartFocus(session) },
                    onCompleteClick = { viewModel.completeSession(session, duration, null) }
                )
            }
        }

        // 7. Soft Gentle Alert Box (تذكير لطيف)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimaryIndigo),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "تذكير لطيف",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = PrimaryIndigo
                            )
                            Text(
                                text = "إذا تأخرت شوي، منرتّب الباقي تلقائياً",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    TextButton(
                        onClick = { viewModel.triggerReschedule() }
                    ) {
                        Text(
                            text = "إعادة الترتيب",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo
                        )
                    }
                }
            }
        }

        // 8. Student Baccalaureate Memories Section (سجل ذكريات الطالب)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "دفتر ذكريات البكالوريا",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(
                        onClick = { showMemoryDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "إضافة ذكرى",
                            tint = PrimaryIndigo
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (memories.isEmpty()) {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showMemoryDialog = true },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "سجّل أول لحظة إنجاز اليوم!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "احتفظ بلحظات نجاحك وتحدياتك لتتذكرها لاحقاً",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(memories) { memory ->
                            Card(
                                modifier = Modifier
                                    .width(220.dp)
                                    .height(110.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = PrimaryIndigo.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = memory.tag,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryIndigo,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteStudentMemory(memory) },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "حذف",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = memory.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = memory.note,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 9. In-Line Quick Pomodoro Study Timer Tool (مؤقت دراسة سريع)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "مؤقت تركيز سريع",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        val mins = timerSecondsRemaining / 60
                        val secs = timerSecondsRemaining % 60
                        Text(
                            text = "%02d:%02d".format(mins, secs),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryIndigo
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { isTimerRunning = !isTimerRunning },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(PrimaryIndigo)
                        ) {
                            Icon(
                                imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = {
                                isTimerRunning = false
                                timerSecondsRemaining = 25 * 60
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "إعادة",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Memory Dialog
    if (showMemoryDialog) {
        AlertDialog(
            onDismissRequest = { showMemoryDialog = false },
            title = { Text("إضافة ذكرى أو إنجاز جديد", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = memoryTitleInput,
                        onValueChange = { memoryTitleInput = it },
                        label = { Text("عنوان الذكرى") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = memoryContentInput,
                        onValueChange = { memoryContentInput = it },
                        label = { Text("تفاصيل أو شعورك اليوم") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (memoryTitleInput.isNotBlank()) {
                            viewModel.addStudentMemory(
                                title = memoryTitleInput,
                                note = memoryContentInput
                            )
                            memoryTitleInput = ""
                            memoryContentInput = ""
                            showMemoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMemoryDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun MetricChip(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
    showArrow: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (showArrow) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(12.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = title,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTintColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun TimelineSessionItem(
    timeStr: String,
    durationStr: String,
    subjectTitle: String,
    topicSubtitle: String,
    sessionStatus: SessionStatus,
    onStartClick: () -> Unit,
    onCompleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time Badge
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.width(48.dp)
        ) {
            Text(
                text = timeStr,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = durationStr,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Vertical Line & Dot Node
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        if (sessionStatus == SessionStatus.COMPLETED) SuccessGreen else PrimaryIndigo
                    )
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Subject Card Content
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (sessionStatus == SessionStatus.COMPLETED)
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryIndigo.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = subjectTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = topicSubtitle,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (sessionStatus == SessionStatus.COMPLETED) {
                    Surface(
                        color = SuccessGreen.copy(alpha = 0.15f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "تم",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = onStartClick,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(PrimaryIndigo.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "بدء الجلسة",
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudyIllustration3D(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFEDE9FE), Color(0xFFDDD6FE))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(90.dp)) {
            val notebookWidth = size.width * 0.52f
            val notebookHeight = size.height * 0.68f
            val notebookLeft = size.width * 0.22f
            val notebookTop = size.height * 0.16f

            // Shadow
            drawRoundRect(
                color = Color(0x1F4F46E5),
                topLeft = Offset(notebookLeft + 4f, notebookTop + 8f),
                size = Size(notebookWidth, notebookHeight),
                cornerRadius = CornerRadius(14f, 14f)
            )

            // Main White Notebook page
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(notebookLeft, notebookTop),
                size = Size(notebookWidth, notebookHeight),
                cornerRadius = CornerRadius(14f, 14f)
            )

            // Rings
            for (i in 0..4) {
                val y = notebookTop + (notebookHeight / 6) * (i + 1)
                drawCircle(
                    color = Color(0xFF6366F1),
                    radius = 3.5f,
                    center = Offset(notebookLeft + 6f, y)
                )
            }

            // Curve y=f(x)
            val path = Path().apply {
                moveTo(notebookLeft + 14f, notebookTop + notebookHeight * 0.65f)
                cubicTo(
                    notebookLeft + notebookWidth * 0.35f, notebookTop + notebookHeight * 0.3f,
                    notebookLeft + notebookWidth * 0.65f, notebookTop + notebookHeight * 0.85f,
                    notebookLeft + notebookWidth - 10f, notebookTop + notebookHeight * 0.4f
                )
            }
            drawPath(
                path = path,
                color = Color(0xFF6366F1),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )

            // Purple Pencil
            val pencilPath = Path().apply {
                moveTo(notebookLeft + notebookWidth + 6f, notebookTop + 8f)
                lineTo(notebookLeft + notebookWidth - 6f, notebookTop + notebookHeight + 6f)
            }
            drawPath(
                path = pencilPath,
                color = Color(0xFF8B5CF6),
                style = Stroke(width = 7f, cap = StrokeCap.Round)
            )

            // Triangle Ruler
            val rulerPath = Path().apply {
                moveTo(10f, size.height - 12f)
                lineTo(48f, size.height - 12f)
                lineTo(10f, size.height - 50f)
                close()
            }
            drawPath(
                path = rulerPath,
                color = Color(0xFFA855F7),
                style = Fill
            )
        }
    }
}
