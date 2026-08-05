package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.CommitmentType
import com.example.data.models.FixedCommitmentEntity
import com.example.data.models.PlannedSessionEntity
import com.example.ui.theme.*
import com.example.ui.viewmodels.MasarViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(viewModel: MasarViewModel) {
    var isWeekView by remember { mutableStateOf(false) }
    val selectedDateStr by viewModel.selectedDate.collectAsState()
    val sessions by viewModel.selectedDateSessionsFlow.collectAsState()
    val commitments by viewModel.commitmentsFlow.collectAsState()
    val profile by viewModel.profileFlow.collectAsState()

    var showAddCommitmentDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Header & View Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "الخطة والجدول الأسبوعي",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "تتبع وتنظيم الجلسات الدراسية بدقة",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Segmented Pill Tab Switcher
            Surface(
                shape = CircleShape,
                color = TilePurpleBg,
                modifier = Modifier.height(38.dp)
            ) {
                Row(
                    modifier = Modifier.padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (!isWeekView) PrimaryIndigo else Color.Transparent,
                        modifier = Modifier.clickable { isWeekView = false }
                    ) {
                        Text(
                            "يومي",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isWeekView) Color.White else Color.Gray,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = if (isWeekView) PrimaryIndigo else Color.Transparent,
                        modifier = Modifier.clickable { isWeekView = true }
                    ) {
                        Text(
                            "أسبوعي",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isWeekView) Color.White else Color.Gray,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Date Picker Bar
        ElevatedCard(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dateObj = runCatching { LocalDate.parse(selectedDateStr) }.getOrDefault(LocalDate.now())
                val dayArabic = when(dateObj.dayOfWeek) {
                    java.time.DayOfWeek.SATURDAY -> "السبت"
                    java.time.DayOfWeek.SUNDAY -> "الأحد"
                    java.time.DayOfWeek.MONDAY -> "الاثنين"
                    java.time.DayOfWeek.TUESDAY -> "الثلاثاء"
                    java.time.DayOfWeek.WEDNESDAY -> "الأربعاء"
                    java.time.DayOfWeek.THURSDAY -> "الخميس"
                    java.time.DayOfWeek.FRIDAY -> "الجمعة"
                }
                IconButton(onClick = { viewModel.setSelectedDate(dateObj.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "اليوم السابق", tint = PrimaryIndigo)
                }
                Text(
                    text = "$dayArabic - $selectedDateStr",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { viewModel.setSelectedDate(dateObj.plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "اليوم التالي", tint = PrimaryIndigo)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { viewModel.triggerReschedule() },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                shape = CircleShape,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("reschedule_plan_button")
            ) {
                Icon(Icons.Default.AutoMode, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("جدولة تلقائية", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = { showAddCommitmentDialog = true },
                shape = CircleShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryIndigo),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("إضافة التزام", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hourly Timeline View
        val dayOfWeekIso = runCatching { LocalDate.parse(selectedDateStr).dayOfWeek.value }.getOrDefault(1)
        val dayCommitments = commitments.filter { it.dayOfWeek == dayOfWeekIso }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Sleep Protection Banner
            item {
                val wakeH = (profile?.wakeTimeMinutes ?: 360) / 60
                val sleepH = (profile?.sleepTimeMinutes ?: 1380) / 60
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimaryIndigo),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Bedtime, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("وقت النوم المحمي", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("من %02d:00 إلى %02d:00 (راحة محجوزة)".format(sleepH, wakeH), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Fixed Commitments for today
            if (dayCommitments.isNotEmpty()) {
                item {
                    Text("الالتزامات الثابتة اليوم (مدرسة / معهد):", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                items(dayCommitments) { c ->
                    val startH = c.startTimeMinutes / 60
                    val startM = c.startTimeMinutes % 60
                    val endH = c.endTimeMinutes / 60
                    val endM = c.endTimeMinutes % 60
                    ElevatedCard(
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(c.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            }
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface) {
                                Text(
                                    "%02d:%02d - %02d:%02d".format(startH, startM, endH, endM),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Planned Sessions Timeline
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text("الجلسات الدراسية المجدولة لليوم:", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            }

            if (sessions.isEmpty()) {
                item {
                    Text("لا توجد جلسات دراسية مجدولة لهذا اليوم.", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 12.dp))
                }
            } else {
                items(sessions) { s ->
                    val startH = s.startTimeMinutes / 60
                    val startM = s.startTimeMinutes % 60
                    val endH = s.endTimeMinutes / 60
                    val endM = s.endTimeMinutes % 60
                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(TileBlueBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = TileBlueIcon, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(s.explanation.ifEmpty { "جلسة مركزة" }, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("%02d:%02d - %02d:%02d (%d د)".format(startH, startM, endH, endM, s.endTimeMinutes - s.startTimeMinutes), fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                            IconButton(onClick = { /* Lock session toggle */ }) {
                                Icon(
                                    if (s.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = "قفل الجلسة",
                                    tint = PrimaryIndigo
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddCommitmentDialog) {
        EnhancedAddCommitmentDialog(
            onDismiss = { showAddCommitmentDialog = false },
            onSaveCommitment = { title, daysList, startMin, endMin, type ->
                daysList.forEach { dayIso ->
                    viewModel.addCommitment(
                        FixedCommitmentEntity(
                            title = title,
                            dayOfWeek = dayIso,
                            startTimeMinutes = startMin,
                            endTimeMinutes = endMin,
                            type = type
                        )
                    )
                }
                showAddCommitmentDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EnhancedAddCommitmentDialog(
    onDismiss: () -> Unit,
    onSaveCommitment: (title: String, selectedDays: List<Int>, startMin: Int, endMin: Int, type: CommitmentType) -> Unit
) {
    var title by remember { mutableStateOf("المدرسة") }
    var selectedType by remember { mutableStateOf(CommitmentType.SCHOOL) }

    // ISO days list: 6 = Saturday (السبت), 7 = Sunday (الأحد), 1 = Monday (الاثنين), 2 = Tuesday (الثلاثاء), 3 = Wednesday (الأربعاء), 4 = Thursday (الخميس), 5 = Friday (الجمعة)
    val allDays = remember {
        listOf(
            6 to "السبت",
            7 to "الأحد",
            1 to "الاثنين",
            2 to "الثلاثاء",
            3 to "الأربعاء",
            4 to "الخميس",
            5 to "الجمعة"
        )
    }

    // Default selection: Saturday through Thursday
    var selectedDays by remember { mutableStateOf(setOf(6, 7, 1, 2, 3, 4)) }

    var startHour by remember { mutableStateOf(8) }
    var startMinute by remember { mutableStateOf(0) }
    var endHour by remember { mutableStateOf(14) }
    var endMinute by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PrimaryIndigo.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "إضافة التزام ثابت جديد",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Commitment Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("اسم الالتزام (مثال: المدرسة / معهد الفيزياء)") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // 2. Commitment Type Selection Chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "نوع الالتزام:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val types = listOf(
                            CommitmentType.SCHOOL to "مدرسة",
                            CommitmentType.INSTITUTE to "معهد",
                            CommitmentType.PRIVATE_LESSON to "درس خاص",
                            CommitmentType.PERSONAL to "شخصي"
                        )
                        types.forEach { (typeEnum, label) ->
                            val isSelected = selectedType == typeEnum
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedType = typeEnum
                                    if (typeEnum == CommitmentType.SCHOOL && title.isBlank()) title = "المدرسة"
                                    else if (typeEnum == CommitmentType.INSTITUTE && title == "المدرسة") title = "المعهد"
                                },
                                label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                shape = CircleShape,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryIndigo,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // 3. Days Selection (Clear Days of the Week)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "أيام الالتزام في الأسبوع:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(
                            onClick = {
                                if (selectedDays.size == 7) {
                                    selectedDays = emptySet()
                                } else {
                                    selectedDays = setOf(6, 7, 1, 2, 3, 4, 5)
                                }
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = if (selectedDays.size == 7) "إلغاء الكل" else "تحديد كل الأيام",
                                fontSize = 11.sp,
                                color = PrimaryIndigo,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allDays.forEach { (isoDay, dayName) ->
                            val isSelected = selectedDays.contains(isoDay)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedDays = if (isSelected) {
                                        selectedDays - isoDay
                                    } else {
                                        selectedDays + isoDay
                                    }
                                },
                                label = { Text(dayName, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null,
                                shape = CircleShape,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryIndigo,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // 4. Time Selection & Presets
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "التوقيت والساعات:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AssistChip(
                            onClick = {
                                startHour = 8; startMinute = 0
                                endHour = 14; endMinute = 0
                            },
                            label = { Text("صباحي (8 - 2)", fontSize = 11.sp) },
                            shape = CircleShape
                        )
                        AssistChip(
                            onClick = {
                                startHour = 15; startMinute = 0
                                endHour = 18; endMinute = 0
                            },
                            label = { Text("بعد الظهر (3 - 6)", fontSize = 11.sp) },
                            shape = CircleShape
                        )
                        AssistChip(
                            onClick = {
                                startHour = 18; startMinute = 0
                                endHour = 20; endMinute = 0
                            },
                            label = { Text("مسائي (6 - 8)", fontSize = 11.sp) },
                            shape = CircleShape
                        )
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("وقت البداية", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { if (startHour > 0) startHour-- },
                                        modifier = Modifier.size(28.dp)
                                    ) { Text("-", fontWeight = FontWeight.Bold, fontSize = 18.sp) }

                                    Text(
                                        "%02d:%02d".format(startHour, startMinute),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryIndigo,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )

                                    IconButton(
                                        onClick = { if (startHour < 23) startHour++ },
                                        modifier = Modifier.size(28.dp)
                                    ) { Text("+", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .height(30.dp)
                                    .width(1.dp)
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("وقت الانتهاء", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { if (endHour > 0) endHour-- },
                                        modifier = Modifier.size(28.dp)
                                    ) { Text("-", fontWeight = FontWeight.Bold, fontSize = 18.sp) }

                                    Text(
                                        "%02d:%02d".format(endHour, endMinute),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryIndigo,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )

                                    IconButton(
                                        onClick = { if (endHour < 23) endHour++ },
                                        modifier = Modifier.size(28.dp)
                                    ) { Text("+", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && selectedDays.isNotEmpty()) {
                        val startMin = startHour * 60 + startMinute
                        val endMin = endHour * 60 + endMinute
                        onSaveCommitment(
                            title,
                            selectedDays.toList(),
                            startMin,
                            endMin,
                            selectedType
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                shape = CircleShape,
                modifier = Modifier.height(42.dp)
            ) {
                Text("حفظ الالتزام", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = CircleShape,
                modifier = Modifier.height(42.dp)
            ) {
                Text("إلغاء")
            }
        }
    )
}


