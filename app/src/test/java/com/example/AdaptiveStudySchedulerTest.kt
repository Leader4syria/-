package com.example

import com.example.data.models.*
import com.example.domain.scheduler.AdaptiveStudyScheduler
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class AdaptiveStudySchedulerTest {

    private val scheduler = AdaptiveStudyScheduler()

    private val sampleProfile = StudentProfileEntity(
        id = 1,
        name = "طالب مجتهد",
        branch = Branch.SCIENTIFIC,
        wakeTimeMinutes = 360, // 06:00 AM
        sleepTimeMinutes = 1380, // 11:00 PM
        minimumDailyFreeMinutes = 120,
        maximumDailyStudyMinutes = 300, // 5 hours max
        defaultFocusDurationMinutes = 45,
        weeklyRestDay = 5 // Friday
    )

    private val sampleSubjects = listOf(
        SubjectEntity(
            id = "sub_math",
            name = "الرياضيات",
            shortName = "رياضيات",
            branch = Branch.SCIENTIFIC,
            subjectType = SubjectType.PROBLEM_SOLVING,
            importanceWeight = 5.0f,
            studentDifficulty = 4,
            currentMastery = 0.3f
        ),
        SubjectEntity(
            id = "sub_bio",
            name = "علم الأحياء",
            shortName = "علوم",
            branch = Branch.SCIENTIFIC,
            subjectType = SubjectType.MEMORIZATION,
            importanceWeight = 4.0f,
            studentDifficulty = 2,
            currentMastery = 0.7f
        )
    )

    private val sampleTasks = listOf(
        StudyTaskEntity(
            id = 101,
            subjectId = "sub_math",
            title = "حل تمارين الاشتقاق",
            taskType = TaskType.EXERCISES,
            estimatedMinutes = 90,
            remainingMinutes = 90,
            difficulty = 4
        ),
        StudyTaskEntity(
            id = 102,
            subjectId = "sub_bio",
            title = "دراسة التنسيق العصبي",
            taskType = TaskType.NEW_LESSON,
            estimatedMinutes = 60,
            remainingMinutes = 60,
            difficulty = 3
        )
    )

    @Test
    fun testSchedulerDoesNotScheduleDuringCommitments() {
        val schoolCommitment = FixedCommitmentEntity(
            id = 1,
            title = "المدرسة الرسمية",
            dayOfWeek = 1, // Monday
            startTimeMinutes = 480, // 08:00 AM
            endTimeMinutes = 840,  // 02:00 PM
            type = CommitmentType.SCHOOL
        )

        val result = scheduler.schedulePeriod(
            startDate = LocalDate.of(2026, 8, 10), // Monday
            daysCount = 1,
            profile = sampleProfile,
            subjects = sampleSubjects,
            units = emptyList(),
            commitments = listOf(schoolCommitment),
            pendingTasks = sampleTasks,
            exams = emptyList(),
            dueReviews = emptyList()
        )

        // Verify no session overlaps with 08:00 - 14:00 (480 - 840)
        for (session in result.sessions) {
            val overlapsWithSchool = (session.startTimeMinutes < 840) && (session.endTimeMinutes > 480)
            assertFalse("Session should not overlap with school commitment hours", overlapsWithSchool)
        }
    }

    @Test
    fun testSchedulerProtectsSleepTime() {
        val result = scheduler.schedulePeriod(
            startDate = LocalDate.of(2026, 8, 10),
            daysCount = 1,
            profile = sampleProfile,
            subjects = sampleSubjects,
            units = emptyList(),
            commitments = emptyList(),
            pendingTasks = sampleTasks,
            exams = emptyList(),
            dueReviews = emptyList()
        )

        for (session in result.sessions) {
            assertTrue("Session start must be at or after wake time", session.startTimeMinutes >= sampleProfile.wakeTimeMinutes)
            assertTrue("Session end must be at or before sleep time", session.endTimeMinutes <= sampleProfile.sleepTimeMinutes)
        }
    }

    @Test
    fun testSchedulerNoOverlappingSessions() {
        val result = scheduler.schedulePeriod(
            startDate = LocalDate.of(2026, 8, 10),
            daysCount = 1,
            profile = sampleProfile,
            subjects = sampleSubjects,
            units = emptyList(),
            commitments = emptyList(),
            pendingTasks = sampleTasks,
            exams = emptyList(),
            dueReviews = emptyList()
        )

        val sorted = result.sessions.sortedBy { it.startTimeMinutes }
        for (i in 0 until sorted.size - 1) {
            val current = sorted[i]
            val next = sorted[i + 1]
            assertTrue("Sessions must not overlap", current.endTimeMinutes <= next.startTimeMinutes)
        }
    }

    @Test
    fun testExamAutoPlannerGeneratesPreparationTasks() {
        val exam = ExamEntity(
            id = 1,
            title = "امتحان رياضيات نصف شهري",
            subjectId = "sub_math",
            examDate = "2026-08-17",
            examType = ExamType.SCHOOL_QUIZ
        )

        val mathUnits = listOf(
            CurriculumUnitEntity(
                id = "u1",
                subjectId = "sub_math",
                title = "النهايات والاستمرار",
                orderIndex = 1,
                estimatedMinutes = 180,
                difficulty = 4,
                mastery = 0.2f
            )
        )

        val generatedTasks = scheduler.planForExam(exam, sampleSubjects[0], mathUnits, sampleProfile)

        assertTrue("Should generate preparation tasks for exam", generatedTasks.isNotEmpty())
        assertTrue("Task title should mention exam preparation", generatedTasks.any { it.title.contains("امتحان") })
    }
}
