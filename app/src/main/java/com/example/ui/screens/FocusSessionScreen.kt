package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ErrorNoteEntity
import com.example.data.models.PlannedSessionEntity
import com.example.data.models.RecallQuality
import com.example.ui.theme.*
import com.example.ui.viewmodels.MasarViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusSessionScreen(
    session: PlannedSessionEntity,
    viewModel: MasarViewModel,
    onBack: () -> Unit
) {
    val totalSeconds = (session.endTimeMinutes - session.startTimeMinutes) * 60
    var secondsRemaining by remember { mutableStateOf(totalSeconds) }
    var isRunning by remember { mutableStateOf(true) }

    var showRecallSheet by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning, secondsRemaining) {
        if (isRunning && secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining--
        } else if (secondsRemaining <= 0) {
            isRunning = false
            showRecallSheet = true
        }
    }

    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val progress = if (totalSeconds > 0) 1f - (secondsRemaining.toFloat() / totalSeconds) else 1f

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "جلسة تركيز مسار",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Info Card
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = session.sessionType.name,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = session.explanation.ifEmpty { "جلسة تركيز مركزة" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Circular Focus Timer Container
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(260.dp)
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 14.dp,
                    color = PrimaryIndigo,
                    trackColor = PrimaryIndigo.copy(alpha = 0.12f)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%02d:%02d".format(minutes, seconds),
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = if (isRunning) SuccessGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isRunning) "جاري التركيز الآن 🔥" else "متوقف مؤقتاً ⏸️",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRunning) SuccessGreen else WarningAmber,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Timer Controls
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = { isRunning = !isRunning },
                        modifier = Modifier
                            .size(68.dp)
                            .testTag("timer_play_pause_button"),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = PrimaryIndigo
                        )
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "إيقاف مؤقت" else "تشغيل",
                            modifier = Modifier.size(36.dp),
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    OutlinedIconButton(
                        onClick = { secondsRemaining += 300 }, // add 5 mins
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape
                    ) {
                        Icon(
                            Icons.Default.MoreTime,
                            contentDescription = "تمديد 5 دقائق",
                            modifier = Modifier.size(26.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showNoteDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ملاحظة بالدفتر", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showRecallSheet = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("finish_session_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryTeal
                        )
                    ) {
                        Text("إنهاء الجلسة", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Post-Session Self Assessment Sheet
    if (showRecallSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRecallSheet = false },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("ممتاز! أتممت الجلسة بنجاح 👏", fontSize = 20.sp, fontWeight = FontWeight.Black)
                }

                Text(
                    text = "عند محاولة تذكّر الأفكار والقوانين الأساسية بنفسك، كيف تقيّم أداءك واستيعابك؟",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )

                listOf(
                    RecallQuality.NOT_REMEMBERED to "ما تذكّرت (يحتاج مراجعة قريبة 🔴)",
                    RecallQuality.HARD_REMEMBERED to "تذكّرت بصعوبة 🟡",
                    RecallQuality.MOSTLY_REMEMBERED to "تذكّرت أغلبه 🟢",
                    RecallQuality.MASTERED to "ثابت في الذهن ومتقن تماماً 🔥"
                ).forEach { (quality, label) ->
                    OutlinedCard(
                        onClick = {
                            val elapsedMin = ((totalSeconds - secondsRemaining) / 60).coerceAtLeast(1)
                            viewModel.completeSession(session, elapsedMin, quality)
                            showRecallSheet = false
                            onBack()
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Add Error Note Dialog
    if (showNoteDialog) {
        var topic by remember { mutableStateOf("") }
        var mistake by remember { mutableStateOf("") }
        var solution by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("تسجيل ملاحظة لدفتر الأخطاء", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text("السؤال أو النقطة الصعبة") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = mistake,
                        onValueChange = { mistake = it },
                        label = { Text("سبب الخطأ / اللبس") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = solution,
                        onValueChange = { solution = it },
                        label = { Text("التصحيح الصحيح") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (topic.isNotBlank()) {
                            viewModel.addErrorNote(
                                ErrorNoteEntity(
                                    subjectId = session.subjectId,
                                    questionOrTopic = topic,
                                    myMistake = mistake,
                                    correctSolution = solution
                                )
                            )
                        }
                        showNoteDialog = false
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("حفظ بالدفتر", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) { Text("إلغاء") }
            }
        )
    }
}

