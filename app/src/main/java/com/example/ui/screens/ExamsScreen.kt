package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ExamEntity
import com.example.data.models.ExamType
import com.example.ui.viewmodels.MasarViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamsScreen(viewModel: MasarViewModel) {
    val exams by viewModel.examsFlow.collectAsState()
    val subjects by viewModel.activeSubjectsFlow.collectAsState()

    var showAddExamDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("الامتحانات والاختبارات", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Button(
                onClick = { showAddExamDialog = true },
                modifier = Modifier.testTag("add_exam_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("إضافة امتحان")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (exams.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Event,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("لا توجد امتحانات مضافة حالياً.", fontWeight = FontWeight.Bold)
                        Text(
                            "عند إضافة امتحان جديد، سيقوم محرك مسار تلقائياً بتوزيع ساعات التحضير والمراجعات على الأيام المتبقية.",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 13.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(exams) { exam ->
                    ExamCard(exam = exam, subjects = subjects)
                }
            }
        }
    }

    if (showAddExamDialog) {
        var title by remember { mutableStateOf("") }
        var selectedSubId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: "") }
        var examDateStr by remember { mutableStateOf(LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE)) }

        AlertDialog(
            onDismissRequest = { showAddExamDialog = false },
            title = { Text("إضافة امتحان أو اختبار جديد") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("عنوان الامتحان (مثال: مسبار فيزياء نصف شهري)") }
                    )
                    OutlinedTextField(
                        value = examDateStr,
                        onValueChange = { examDateStr = it },
                        label = { Text("تاريخ الامتحان (سنة-شهر-يوم)") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank()) {
                        viewModel.addExam(
                            ExamEntity(
                                title = title,
                                subjectId = selectedSubId,
                                examDate = examDateStr,
                                examType = ExamType.SCHOOL_QUIZ,
                                estimatedPreparationMinutes = 300
                            )
                        )
                    }
                    showAddExamDialog = false
                }) {
                    Text("إضافة وبناء الخطة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExamDialog = false }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
private fun ExamCard(
    exam: ExamEntity,
    subjects: List<com.example.data.models.SubjectEntity>
) {
    val subject = subjects.firstOrNull { it.id == exam.subjectId }
    val examDate = runCatching { LocalDate.parse(exam.examDate) }.getOrNull() ?: LocalDate.now()
    val daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), examDate)

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Badge(
                    containerColor = when {
                        daysRemaining <= 2 -> MaterialTheme.colorScheme.error
                        daysRemaining <= 5 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    }
                ) {
                    Text(
                        when {
                            daysRemaining < 0 -> "انتهى"
                            daysRemaining == 0L -> "اليوم!"
                            else -> "باقي $daysRemaining أيام"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Text(exam.examDate, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(exam.title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(subject?.name ?: "مادة", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("درجة الجاهزية المتوقعة: ${(exam.currentReadiness * 100).toInt()}%", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("ساعات التحضير: ${exam.estimatedPreparationMinutes / 60} س", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { exam.currentReadiness },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
            )
        }
    }
}
