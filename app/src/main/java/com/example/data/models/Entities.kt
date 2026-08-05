package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student_profile")
data class StudentProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val branch: Branch = Branch.SCIENTIFIC,
    val academicYear: String = "2025-2026",
    val targetScore: Int? = 2400, // مثلاً 2400 للعلمي أو 2800 حسب النظام
    val wakeTimeMinutes: Int = 360, // 6:00 AM (minutes from midnight)
    val sleepTimeMinutes: Int = 1380, // 11:00 PM
    val preferredStudyPeriods: String = "MORNING,EVENING", // comma separated
    val minimumDailyFreeMinutes: Int = 120, // 2 hours protected free time
    val minimumDailyStudyMinutes: Int = 120,
    val maximumDailyStudyMinutes: Int = 420, // 7 hours max
    val defaultFocusDurationMinutes: Int = 45,
    val defaultBreakDurationMinutes: Int = 10,
    val weeklyRestDay: Int = 5, // 5 = Friday (ISO 1=Mon, 7=Sun, 5=Fri)
    val dialectMode: Boolean = true, // true = لهجة سورية عامة, false = فصحى
    val reduceMotion: Boolean = false,
    val isOnboardingCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val shortName: String,
    val branch: Branch,
    val curriculumVersion: String = "2025-2026",
    val subjectType: SubjectType,
    val colorHex: String = "#1E88E5",
    val iconName: String = "menu_book",
    val importanceWeight: Float = 4.0f,
    val studentDifficulty: Int = 3, // 1=سهل, 5=صعب/متراكم
    val currentMastery: Float = 0.5f, // 0.0 to 1.0
    val weeklyMinimumMinutes: Int = 180,
    val weeklyTargetMinutes: Int = 300,
    val isActive: Boolean = true,
    val isCustom: Boolean = false
)

@Entity(tableName = "curriculum_units")
data class CurriculumUnitEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val title: String,
    val orderIndex: Int,
    val estimatedMinutes: Int = 180,
    val difficulty: Int = 3,
    val mastery: Float = 0.0f, // 0.0 to 1.0
    val status: UnitStatus = UnitStatus.NOT_STARTED,
    val lastStudiedAt: Long? = null,
    val nextReviewAt: Long? = null,
    val curriculumVersion: String = "2025-2026"
)

@Entity(tableName = "fixed_commitments")
data class FixedCommitmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val dayOfWeek: Int, // 1=Monday .. 7=Sunday (5=Friday)
    val startTimeMinutes: Int, // e.g. 480 = 08:00
    val endTimeMinutes: Int,   // e.g. 840 = 14:00
    val type: CommitmentType,
    val repeatType: String = "WEEKLY",
    val startDate: String? = null, // YYYY-MM-DD
    val endDate: String? = null,
    val isLocked: Boolean = true
)

@Entity(tableName = "study_tasks")
data class StudyTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: String,
    val curriculumUnitId: String? = null,
    val title: String,
    val taskType: TaskType,
    val estimatedMinutes: Int = 45,
    val remainingMinutes: Int = 45,
    val deadline: String? = null, // YYYY-MM-DD
    val priority: Int = 3, // 1 to 5
    val difficulty: Int = 3,
    val status: TaskStatus = TaskStatus.PLANNED,
    val isLocked: Boolean = false,
    val createdBy: String = "scheduler", // student, scheduler, examPlanner
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subjectId: String,
    val examDate: String, // YYYY-MM-DD
    val examTimeMinutes: Int = 480, // 08:00 AM
    val examType: ExamType = ExamType.SCHOOL_QUIZ,
    val includedUnitIdsJson: String = "[]", // List<String> as JSON
    val targetMastery: Float = 0.9f,
    val currentReadiness: Float = 0.4f,
    val estimatedPreparationMinutes: Int = 360,
    val isOfficial: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "planned_sessions")
data class PlannedSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val subjectId: String,
    val date: String, // YYYY-MM-DD
    val startTimeMinutes: Int,
    val endTimeMinutes: Int,
    val sessionType: TaskType,
    val status: SessionStatus = SessionStatus.PLANNED,
    val actualMinutes: Int = 0,
    val energyBefore: Int = 3, // 1..5
    val focusAfter: Int = 3,  // 1..5
    val recallQuality: RecallQuality? = null,
    val rescheduledFromId: Long? = null,
    val explanation: String = "", // Syrian Arabic explanation string
    val isLocked: Boolean = false
)

@Entity(tableName = "review_queue")
data class ReviewQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val curriculumUnitId: String,
    val dueDate: String, // YYYY-MM-DD
    val reviewStage: Int = 1, // 1, 2, 3, 4
    val recallQuality: RecallQuality? = null,
    val intervalDays: Int = 1,
    val completedAt: Long? = null
)

@Entity(tableName = "daily_check_ins")
data class DailyCheckInEntity(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val availableEnergy: Int = 3, // 1=تعبان شوي, 3=عادي, 5=طاقاتي منيحة
    val mood: String = "normal",
    val availableMinutesOverride: Int? = null,
    val unexpectedCommitmentMinutes: Int = 0,
    val preferredWorkload: PreferredWorkload = PreferredWorkload.NORMAL
)

@Entity(tableName = "error_notes")
data class ErrorNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: String,
    val curriculumUnitId: String? = null,
    val questionOrTopic: String,
    val myMistake: String,
    val correctSolution: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isReviewed: Boolean = false
)

@Entity(tableName = "settings_and_backup")
data class SettingsAndBackupEntity(
    @PrimaryKey val id: Int = 1,
    val isDarkMode: Boolean = false,
    val quietHoursStartMinutes: Int = 1320, // 22:00
    val quietHoursEndMinutes: Int = 360,   // 06:00
    val notificationsEnabled: Boolean = true,
    val notifyBeforeMinutes: Int = 15,
    val maxDailyNotifications: Int = 5,
    val lastBackupTimestamp: Long = 0L,
    val dbVersion: Int = 1
)

@Entity(tableName = "student_memories")
data class StudentMemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val note: String,
    val tag: String = "لحظة إنجاز",
    val date: String, // YYYY-MM-DD
    val createdAt: Long = System.currentTimeMillis()
)

