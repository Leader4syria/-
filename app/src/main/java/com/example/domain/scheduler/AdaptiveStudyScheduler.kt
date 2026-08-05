package com.example.domain.scheduler

import com.example.data.models.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.min

class AdaptiveStudyScheduler {

    data class TimeSlot(
        val startTimeMinutes: Int,
        val endTimeMinutes: Int,
        val durationMinutes: Int = endTimeMinutes - startTimeMinutes
    )

    data class SchedulingResult(
        val sessions: List<PlannedSessionEntity>,
        val warnings: List<String>,
        val summarySyrianDialect: String
    )

    data class TaskWithScore(
        val task: StudyTaskEntity,
        val subject: SubjectEntity,
        val score: Double,
        val isExamRelated: Boolean
    )

    /**
     * Core function to generate or re-schedule study plan for a date range (e.g., next 7 days).
     */
    fun schedulePeriod(
        startDate: LocalDate,
        daysCount: Int = 7,
        profile: StudentProfileEntity,
        subjects: List<SubjectEntity>,
        units: List<CurriculumUnitEntity>,
        commitments: List<FixedCommitmentEntity>,
        pendingTasks: List<StudyTaskEntity>,
        exams: List<ExamEntity>,
        dueReviews: List<ReviewQueueEntity>,
        dailyCheckIn: DailyCheckInEntity? = null,
        existingSessions: List<PlannedSessionEntity> = emptyList()
    ): SchedulingResult {
        val newSessions = mutableListOf<PlannedSessionEntity>()
        val warnings = mutableListOf<String>()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE

        var mutableTasks = pendingTasks.toMutableList()

        for (i in 0 until daysCount) {
            val currentDate = startDate.plusDays(i.toLong())
            val dateStr = currentDate.format(formatter)
            val dayOfWeekIso = currentDate.dayOfWeek.value // 1=Mon .. 7=Sun

            // Check if rest day
            if (profile.weeklyRestDay == dayOfWeekIso) {
                // Keep rest day clear unless an exam is within 2 days
                val hasUrgentExam = exams.any { exam ->
                    val examDate = runCatching { LocalDate.parse(exam.examDate) }.getOrNull()
                    examDate != null && !examDate.isBefore(currentDate) && examDate.isBefore(currentDate.plusDays(2))
                }
                if (!hasUrgentExam) {
                    continue
                }
            }

            // Determine check-in energy for today
            val checkInForDate = if (i == 0) dailyCheckIn else null
            val energyMultiplier = when (checkInForDate?.availableEnergy) {
                1 -> 0.6f // low energy
                3 -> 1.0f // normal
                5 -> 1.2f // high energy
                else -> 1.0f
            }

            // Calculate free time slots
            val freeSlots = calculateFreeTimeSlots(
                dayOfWeekIso = dayOfWeekIso,
                profile = profile,
                commitments = commitments,
                checkIn = checkInForDate,
                energyMultiplier = energyMultiplier
            )

            if (freeSlots.isEmpty()) {
                continue
            }

            // Score and rank pending tasks for this day
            val rankedTasks = scoreTasks(
                tasks = mutableTasks,
                subjects = subjects,
                exams = exams,
                dueReviews = dueReviews,
                currentDate = currentDate
            )

            var slotIndex = 0
            var slotRemainingMinutes = if (freeSlots.isNotEmpty()) freeSlots[0].durationMinutes else 0
            var currentSlotStart = if (freeSlots.isNotEmpty()) freeSlots[0].startTimeMinutes else 0

            var consecutiveHeavySessions = 0
            var lastSubjectId: String? = null

            for (taskWithScore in rankedTasks) {
                if (slotIndex >= freeSlots.size) break

                val task = taskWithScore.task
                val subject = taskWithScore.subject

                // Preferred session duration (e.g. 45 mins)
                val sessionDuration = min(profile.defaultFocusDurationMinutes, task.remainingMinutes)
                if (sessionDuration < 20) continue // Skip tiny session chunks

                // Check if slot has space
                while (slotIndex < freeSlots.size && slotRemainingMinutes < sessionDuration) {
                    slotIndex++
                    if (slotIndex < freeSlots.size) {
                        slotRemainingMinutes = freeSlots[slotIndex].durationMinutes
                        currentSlotStart = freeSlots[slotIndex].startTimeMinutes
                        consecutiveHeavySessions = 0
                    }
                }

                if (slotIndex >= freeSlots.size) break

                // Avoid >2 consecutive heavy subjects
                val isHeavy = subject.subjectType == SubjectType.PROBLEM_SOLVING || task.difficulty >= 4
                if (isHeavy && consecutiveHeavySessions >= 2 && lastSubjectId == subject.id) {
                    // Try to switch task if possible
                    continue
                }

                // Generate Syrian Arabic explanation
                val explanation = generateDialectExplanation(
                    subject = subject,
                    task = task,
                    startTimeMinutes = currentSlotStart,
                    profile = profile,
                    taskWithScore = taskWithScore
                )

                val endTime = currentSlotStart + sessionDuration
                val newSession = PlannedSessionEntity(
                    taskId = task.id,
                    subjectId = subject.id,
                    date = dateStr,
                    startTimeMinutes = currentSlotStart,
                    endTimeMinutes = endTime,
                    sessionType = task.taskType,
                    status = SessionStatus.PLANNED,
                    explanation = explanation
                )

                newSessions.add(newSession)

                // Update slot counters
                currentSlotStart = endTime + profile.defaultBreakDurationMinutes
                slotRemainingMinutes -= (sessionDuration + profile.defaultBreakDurationMinutes)

                if (isHeavy) consecutiveHeavySessions++ else consecutiveHeavySessions = 0
                lastSubjectId = subject.id

                // Update task remaining minutes
                val updatedTask = task.copy(remainingMinutes = max(0, task.remainingMinutes - sessionDuration))
                val taskIndex = mutableTasks.indexOfFirst { it.id == task.id }
                if (taskIndex >= 0) {
                    if (updatedTask.remainingMinutes <= 0) {
                        mutableTasks.removeAt(taskIndex)
                    } else {
                        mutableTasks[taskIndex] = updatedTask
                    }
                }
            }
        }

        // Summary message in Syrian Arabic
        val summary = if (newSessions.isNotEmpty()) {
            if (profile.dialectMode) {
                "تم تجهيز خطتك بنجاح! وزّعنا الجلسات بشكل بيضمنلك تركيز عالي وراحة ممتازة، مع مراعاة أوقات دوامك ونومك."
            } else {
                "تم تنظيم خطتك الدراسية بنجاح وفقاً لأوقات دوامك وراحتك ومستوى أهدافك."
            }
        } else {
            if (profile.dialectMode) {
                "ما في جلسات دراسية جديدة لتوزيعها حالياً، أو أوقات الفراغ اليومية ضيقة."
            } else {
                "لا توجد جلسات جديدة للجدولة في الوقت الحالي."
            }
        }

        return SchedulingResult(
            sessions = newSessions,
            warnings = warnings,
            summarySyrianDialect = summary
        )
    }

    /**
     * Calculate available free time windows for a given day of the week.
     */
    private fun calculateFreeTimeSlots(
        dayOfWeekIso: Int,
        profile: StudentProfileEntity,
        commitments: List<FixedCommitmentEntity>,
        checkIn: DailyCheckInEntity?,
        energyMultiplier: Float
    ): List<TimeSlot> {
        val wake = profile.wakeTimeMinutes
        val sleep = profile.sleepTimeMinutes

        // Collect all blocked ranges during the day
        val blocked = mutableListOf<Pair<Int, Int>>()

        // Sleep / non-awake hours
        if (sleep > wake) {
            blocked.add(0 to wake)
            blocked.add(sleep to 1440)
        } else {
            // Sleep wraps past midnight
            blocked.add(sleep to wake)
        }

        // Add fixed commitments for this day of week
        commitments.filter { it.dayOfWeek == dayOfWeekIso }.forEach {
            blocked.add(it.startTimeMinutes to it.endTimeMinutes)
        }

        // Standard meal & rest buffer (e.g. 13:00 - 14:00, 19:30 - 20:00)
        blocked.add(780 to 840)  // Lunch/rest (1:00 PM - 2:00 PM)
        blocked.add(1170 to 1200) // Dinner (7:30 PM - 8:00 PM)

        // Sort blocked intervals
        blocked.sortBy { it.first }

        // Merge overlapping blocked intervals
        val mergedBlocked = mutableListOf<Pair<Int, Int>>()
        for (b in blocked) {
            if (mergedBlocked.isEmpty()) {
                mergedBlocked.add(b)
            } else {
                val last = mergedBlocked.last()
                if (b.first <= last.second) {
                    mergedBlocked[mergedBlocked.size - 1] = last.first to max(last.second, b.second)
                } else {
                    mergedBlocked.add(b)
                }
            }
        }

        // Find free slots
        val freeSlots = mutableListOf<TimeSlot>()
        var currentPointer = 0

        for (b in mergedBlocked) {
            if (b.first > currentPointer) {
                val duration = b.first - currentPointer
                if (duration >= 20) { // minimum 20 mins slot
                    freeSlots.add(TimeSlot(currentPointer, b.first))
                }
            }
            currentPointer = max(currentPointer, b.second)
        }
        if (currentPointer < 1440) {
            val duration = 1440 - currentPointer
            if (duration >= 20) {
                freeSlots.add(TimeSlot(currentPointer, 1440))
            }
        }

        // Cap maximum study time based on profile and energy
        val rawMaxDaily = profile.maximumDailyStudyMinutes
        val targetMaxMinutes = (rawMaxDaily * energyMultiplier).toInt()

        var totalAllocated = 0
        val cappedSlots = mutableListOf<TimeSlot>()

        for (slot in freeSlots) {
            if (totalAllocated >= targetMaxMinutes) break
            val allowed = min(slot.durationMinutes, targetMaxMinutes - totalAllocated)
            if (allowed >= 20) {
                cappedSlots.add(TimeSlot(slot.startTimeMinutes, slot.startTimeMinutes + allowed))
                totalAllocated += allowed
            }
        }

        return cappedSlots
    }

    /**
     * Score tasks using the weighted multi-factor formula specified in prompt section 6.
     */
    private fun scoreTasks(
        tasks: List<StudyTaskEntity>,
        subjects: List<SubjectEntity>,
        exams: List<ExamEntity>,
        dueReviews: List<ReviewQueueEntity>,
        currentDate: LocalDate
    ): List<TaskWithScore> {
        val subjectMap = subjects.associateBy { it.id }

        return tasks.map { task ->
            val subject = subjectMap[task.subjectId] ?: SubjectEntity(
                id = task.subjectId,
                name = "مادة",
                shortName = "مادة",
                branch = Branch.SCIENTIFIC,
                subjectType = SubjectType.PROBLEM_SOLVING
            )

            // 1. Exam urgency (30%)
            val nextExam = exams.filter { it.subjectId == subject.id }
                .mapNotNull { e -> runCatching { LocalDate.parse(e.examDate) }.getOrNull() }
                .filter { !it.isBefore(currentDate) }
                .minOrNull()

            val examUrgencyScore = if (nextExam != null) {
                val daysUntil = max(1L, java.time.temporal.ChronoUnit.DAYS.between(currentDate, nextExam))
                when {
                    daysUntil <= 2 -> 1.0
                    daysUntil <= 5 -> 0.8
                    daysUntil <= 10 -> 0.5
                    else -> 0.2
                }
            } else 0.1

            // 2. Mastery Deficit (20%)
            val masteryDeficitScore = (1.0 - subject.currentMastery).coerceIn(0.0, 1.0)

            // 3. Remaining Work Volume (15%)
            val remainingWorkScore = (task.remainingMinutes / 120.0).coerceIn(0.1, 1.0)

            // 4. Spaced Repetition Due (15%)
            val isDueForReview = dueReviews.any { it.curriculumUnitId == task.curriculumUnitId }
            val reviewScore = if (isDueForReview || task.taskType == TaskType.REVIEW) 1.0 else 0.2

            // 5. Difficulty Score (10%)
            val difficultyScore = (task.difficulty / 5.0).coerceIn(0.2, 1.0)

            // 6. Balance Score (10%)
            val balanceScore = 0.5

            // Weighted Total
            val totalScore = (examUrgencyScore * 0.30) +
                    (masteryDeficitScore * 0.20) +
                    (remainingWorkScore * 0.15) +
                    (reviewScore * 0.15) +
                    (difficultyScore * 0.10) +
                    (balanceScore * 0.10)

            TaskWithScore(
                task = task,
                subject = subject,
                score = totalScore,
                isExamRelated = (nextExam != null)
            )
        }.sortedByDescending { it.score }
    }

    /**
     * Generates a warm, Syrian Arabic dialect explanation for scheduling decisions.
     */
    private fun generateDialectExplanation(
        subject: SubjectEntity,
        task: StudyTaskEntity,
        startTimeMinutes: Int,
        profile: StudentProfileEntity,
        taskWithScore: TaskWithScore
    ): String {
        if (!profile.dialectMode) {
            return "تم وضع جلسة ${subject.name} بناءً على الأولوية الأسبوعية وموعد الامتحان."
        }

        val hour = startTimeMinutes / 60
        val isMorning = hour in 6..11
        val isAfternoon = hour in 12..16
        val isEvening = hour >= 17

        val timeOfDayText = when {
            isMorning -> "الصبح"
            isAfternoon -> "الظهر"
            else -> "المسا"
        }

        return when {
            taskWithScore.isExamRelated ->
                "قرب امتحان ${subject.shortName}، عشان هيك زدنا التركيز عليه بتوقيت مناسب."
            task.taskType == TaskType.REVIEW || task.taskType == TaskType.ACTIVE_RECALL ->
                "هاي مراجعة سريعة لـ ${subject.shortName} لتثبيت الفكرة بالذاكرة."
            subject.subjectType == SubjectType.PROBLEM_SOLVING && isMorning ->
                "حطّينا ${subject.shortName} بـ $timeOfDayText لأن حل المسائل والتركيز بيكون بأعلى مستوياته."
            subject.studentDifficulty >= 4 ->
                "مادة ${subject.shortName} بدها طاقة أكبر، حطينالها جلسة صفاء ذهني."
            else ->
                "جلسة متوازنة لـ ${subject.shortName} بتكمل برنامجك اليومي بكل سلاسة."
        }
    }

    /**
     * Auto plan setup for a new exam as described in section 7.
     */
    fun planForExam(
        exam: ExamEntity,
        subject: SubjectEntity,
        units: List<CurriculumUnitEntity>,
        profile: StudentProfileEntity
    ): List<StudyTaskEntity> {
        val tasks = mutableListOf<StudyTaskEntity>()
        val examDate = runCatching { LocalDate.parse(exam.examDate) }.getOrNull() ?: LocalDate.now().plusDays(7)

        for (unit in units) {
            val unmasteredFactor = (1.0f - unit.mastery).coerceIn(0.2f, 1.0f)
            val prepMinutes = (unit.estimatedMinutes * (unit.difficulty / 3.0f) * unmasteredFactor).toInt()

            // 1. Study/Review task
            tasks.add(
                StudyTaskEntity(
                    subjectId = subject.id,
                    curriculumUnitId = unit.id,
                    title = "تحضير امتحان ${exam.title}: ${unit.title}",
                    taskType = if (unit.mastery > 0.5f) TaskType.REVIEW else TaskType.NEW_LESSON,
                    estimatedMinutes = prepMinutes,
                    remainingMinutes = prepMinutes,
                    deadline = exam.examDate,
                    priority = 5,
                    createdBy = "examPlanner"
                )
            )

            // 2. Active recall session
            tasks.add(
                StudyTaskEntity(
                    subjectId = subject.id,
                    curriculumUnitId = unit.id,
                    title = "استرجاع نشط لـ ${unit.title}",
                    taskType = TaskType.ACTIVE_RECALL,
                    estimatedMinutes = 30,
                    remainingMinutes = 30,
                    deadline = exam.examDate,
                    priority = 4,
                    createdBy = "examPlanner"
                )
            )
        }

        // 3. Mock test / Practice questions
        if (subject.subjectType == SubjectType.PROBLEM_SOLVING) {
            tasks.add(
                StudyTaskEntity(
                    subjectId = subject.id,
                    title = "حل دورات ونماذج امتحانية لـ ${subject.name}",
                    taskType = TaskType.EXERCISES,
                    estimatedMinutes = 60,
                    remainingMinutes = 60,
                    deadline = exam.examDate,
                    priority = 5,
                    createdBy = "examPlanner"
                )
            )
        }

        return tasks
    }
}
