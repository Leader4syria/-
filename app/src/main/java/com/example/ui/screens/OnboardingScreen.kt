package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Branch
import com.example.data.models.CommitmentType
import com.example.ui.theme.*
import com.example.ui.viewmodels.OnboardingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinished: () -> Unit
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("إعداد خطة مسار • الخطوة $currentStep من 5", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    if (currentStep > 1) {
                        IconButton(onClick = { viewModel.prevStep() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "السابق")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { viewModel.prevStep() },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("onboarding_prev_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("السابق", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = {
                            if (currentStep == 5) {
                                viewModel.finishOnboarding(onFinished)
                            } else {
                                viewModel.nextStep()
                            }
                        },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("onboarding_next_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryIndigo
                        )
                    ) {
                        Text(
                            text = if (currentStep == 5) "إنشاء خطتي الذكية 🚀" else "التالي",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = { currentStep / 5.0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = PrimaryIndigo,
                trackColor = PrimaryIndigo.copy(alpha = 0.12f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedContent(
                targetState = currentStep,
                label = "OnboardingStep"
            ) { step ->
                when (step) {
                    1 -> Step1GreetingAndBranch(viewModel)
                    2 -> Step2Commitments(viewModel)
                    3 -> Step3SleepAndRest(viewModel)
                    4 -> Step4SubjectRatings(viewModel)
                    5 -> Step5StudyStyle(viewModel)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun Step1GreetingAndBranch(viewModel: OnboardingViewModel) {
    val name by viewModel.studentName.collectAsState()
    val branch by viewModel.selectedBranch.collectAsState()
    val targetScore by viewModel.targetScore.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(PrimaryIndigo, PrimaryTeal)
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "أهلاً بك بـ مسار البكالوريا! 👋",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "نساعدك تبني جدول دراسي ذكي يتكيّف تلقائياً مع ظروفك ومواعيد دوامك، مو مجرد جدول ثابت للكل.",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 22.sp
                )
            }
        }

        OutlinedTextField(
            value = name,
            onValueChange = { viewModel.studentName.value = it },
            label = { Text("اسم الطالب (اختياري)") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryIndigo) },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_student_name"),
            singleLine = true
        )

        Text("اختر فرع البكالوريا:", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.selectedBranch.value = Branch.SCIENTIFIC },
                shape = RoundedCornerShape(16.dp),
                border = if (branch == Branch.SCIENTIFIC) CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(PrimaryIndigo, PrimaryTeal)), width = 2.dp) else CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Science, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("الفرع العلمي", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            OutlinedCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.selectedBranch.value = Branch.LITERARY },
                shape = RoundedCornerShape(16.dp),
                border = if (branch == Branch.LITERARY) CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(PrimaryIndigo, PrimaryTeal)), width = 2.dp) else CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = SecondaryCyan, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("الفرع الأدبي", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }

        OutlinedTextField(
            value = targetScore.toString(),
            onValueChange = { viewModel.targetScore.value = it.toIntOrNull() ?: 2400 },
            label = { Text("المجموع المستهدف (مثلاً: 2900 أو 2300)") },
            leadingIcon = { Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = TertiaryGold) },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
private fun Step2Commitments(viewModel: OnboardingViewModel) {
    val commitments by viewModel.tempCommitments.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("أوقات الدوام والالتزامات الثابتة 🏫", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            "أضف مواعيد المدرسة، المعهد، أو الدروس الخصوصية ليقوم التطبيق بحمايتها وعدم جدولة دراسة فيها.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("add_commitment_button"),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("إضافة التزام ثابت (مدرسة / معهد)", fontWeight = FontWeight.Bold)
        }

        if (commitments.isEmpty()) {
            OutlinedCard(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "لم تضف التزامات بعد. يمكنك تخطي هذه الخطوة وإضافتها لاحقاً.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            commitments.forEach { c ->
                val daysNamesMap = mapOf(
                    6 to "السبت",
                    7 to "الأحد",
                    1 to "الاثنين",
                    2 to "الثلاثاء",
                    3 to "الأربعاء",
                    4 to "الخميس",
                    5 to "الجمعة"
                )
                val dayName = daysNamesMap[c.dayOfWeek] ?: "يوم"
                val startH = c.startTimeMinutes / 60
                val startM = c.startTimeMinutes % 60
                val endH = c.endTimeMinutes / 60
                val endM = c.endTimeMinutes % 60

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(c.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$dayName من %02d:%02d إلى %02d:%02d".format(startH, startM, endH, endM), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = PrimaryIndigo)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        EnhancedAddCommitmentDialog(
            onDismiss = { showAddDialog = false },
            onSaveCommitment = { title, daysList, startMin, endMin, type ->
                daysList.forEach { dayIso ->
                    viewModel.addTempCommitment(title, dayIso, startMin, endMin, type)
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun Step3SleepAndRest(viewModel: OnboardingViewModel) {
    val wakeMin by viewModel.wakeTimeMinutes.collectAsState()
    val sleepMin by viewModel.sleepTimeMinutes.collectAsState()
    val freeMin by viewModel.dailyFreeMinutes.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("النوم والوقت الحر 😴", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            "تطبيق مسار يحمي وقت نومك وصحتك النفسية، ولن يقوم بجدولة دراسة فيها.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("ساعة الاستيقاظ صباحاً: %02d:00".format(wakeMin / 60), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Slider(
                    value = (wakeMin / 60).toFloat(),
                    onValueChange = { viewModel.wakeTimeMinutes.value = (it.toInt() * 60) },
                    valueRange = 4f..11f,
                    steps = 6,
                    colors = SliderDefaults.colors(thumbColor = PrimaryIndigo, activeTrackColor = PrimaryIndigo)
                )

                Text("ساعة النوم مساءً: %02d:00".format(sleepMin / 60), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Slider(
                    value = (sleepMin / 60).toFloat(),
                    onValueChange = { viewModel.sleepTimeMinutes.value = (it.toInt() * 60) },
                    valueRange = 21f..25f,
                    steps = 3,
                    colors = SliderDefaults.colors(thumbColor = PrimaryIndigo, activeTrackColor = PrimaryIndigo)
                )

                Text("ساعات الاستراحة والرياضة المحمية: ${freeMin / 60} ساعة", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Slider(
                    value = (freeMin / 60).toFloat(),
                    onValueChange = { viewModel.dailyFreeMinutes.value = (it.toInt() * 60) },
                    valueRange = 1f..5f,
                    steps = 3,
                    colors = SliderDefaults.colors(thumbColor = PrimaryIndigo, activeTrackColor = PrimaryIndigo)
                )
            }
        }
    }
}

@Composable
private fun Step4SubjectRatings(viewModel: OnboardingViewModel) {
    val branch by viewModel.selectedBranch.collectAsState()
    val ratings by viewModel.subjectRatings.collectAsState()

    val subjectsList = if (branch == Branch.SCIENTIFIC) {
        listOf(
            "sub_sci_math" to "الرياضيات",
            "sub_sci_phys" to "الفيزياء",
            "sub_sci_chem" to "الكيمياء",
            "sub_sci_bio" to "علم الأحياء",
            "sub_sci_arab" to "اللغة العربية",
            "sub_sci_eng" to "اللغة الإنكليزية"
        )
    } else {
        listOf(
            "sub_lit_arab" to "اللغة العربية",
            "sub_lit_phil" to "الفلسفة",
            "sub_lit_hist" to "التاريخ",
            "sub_lit_geo" to "الجغرافيا",
            "sub_lit_eng" to "اللغة الإنكليزية"
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("تقييم مستواك في المواد 📚", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            "حدد مستواك الحالي بكل مادة ليعطي المحرك أولوية للمواد التي تحتاج تركيزاً أكثر.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        subjectsList.forEach { (subId, name) ->
            val rating = ratings[subId] ?: 3
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Surface(
                            color = PrimaryIndigo.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = when (rating) {
                                    1 -> "وضعي ممتاز 🌟"
                                    2 -> "مقبول 👍"
                                    3 -> "بحاجة شغل ⚖️"
                                    4 -> "ضعيف ⚠️"
                                    else -> "متراكم عليّ 🚨"
                                },
                                color = PrimaryIndigo,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = rating.toFloat(),
                        onValueChange = { viewModel.setSubjectRating(subId, it.toInt()) },
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(thumbColor = PrimaryIndigo, activeTrackColor = PrimaryIndigo)
                    )
                }
            }
        }
    }
}

@Composable
private fun Step5StudyStyle(viewModel: OnboardingViewModel) {
    val duration by viewModel.focusDuration.collectAsState()
    val maxStudy by viewModel.maxDailyStudy.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("أسلوبك المفضل بالدراسة ⏱️", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            "حدّد مدة الجلسة المفضلة والحد الأقصى للدراسة اليومية لتوليد خطتك الذكية.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("مدة الجلسة المفضلة: $duration دقيقة", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(25, 30, 45, 60).forEach { d ->
                        FilterChip(
                            selected = duration == d,
                            onClick = { viewModel.focusDuration.value = d },
                            label = { Text("$d دقيقة", fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("الحد الأقصى للدراسة اليومية: ${maxStudy / 60} ساعات", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Slider(
                    value = (maxStudy / 60).toFloat(),
                    onValueChange = { viewModel.maxDailyStudy.value = (it.toInt() * 60) },
                    valueRange = 3f..10f,
                    steps = 6,
                    colors = SliderDefaults.colors(thumbColor = PrimaryIndigo, activeTrackColor = PrimaryIndigo)
                )
            }
        }
    }
}

