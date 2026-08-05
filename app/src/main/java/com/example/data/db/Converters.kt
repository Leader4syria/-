package com.example.data.db

import androidx.room.TypeConverter
import com.example.data.models.*

class Converters {
    @TypeConverter
    fun fromBranch(value: Branch): String = value.name

    @TypeConverter
    fun toBranch(value: String): Branch = runCatching { Branch.valueOf(value) }.getOrDefault(Branch.SCIENTIFIC)

    @TypeConverter
    fun fromSubjectType(value: SubjectType): String = value.name

    @TypeConverter
    fun toSubjectType(value: String): SubjectType = runCatching { SubjectType.valueOf(value) }.getOrDefault(SubjectType.PROBLEM_SOLVING)

    @TypeConverter
    fun fromUnitStatus(value: UnitStatus): String = value.name

    @TypeConverter
    fun toUnitStatus(value: String): UnitStatus = runCatching { UnitStatus.valueOf(value) }.getOrDefault(UnitStatus.NOT_STARTED)

    @TypeConverter
    fun fromCommitmentType(value: CommitmentType): String = value.name

    @TypeConverter
    fun toCommitmentType(value: String): CommitmentType = runCatching { CommitmentType.valueOf(value) }.getOrDefault(CommitmentType.OTHER)

    @TypeConverter
    fun fromTaskType(value: TaskType): String = value.name

    @TypeConverter
    fun toTaskType(value: String): TaskType = runCatching { TaskType.valueOf(value) }.getOrDefault(TaskType.NEW_LESSON)

    @TypeConverter
    fun fromTaskStatus(value: TaskStatus): String = value.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = runCatching { TaskStatus.valueOf(value) }.getOrDefault(TaskStatus.PLANNED)

    @TypeConverter
    fun fromExamType(value: ExamType): String = value.name

    @TypeConverter
    fun toExamType(value: String): ExamType = runCatching { ExamType.valueOf(value) }.getOrDefault(ExamType.SCHOOL_QUIZ)

    @TypeConverter
    fun fromSessionStatus(value: SessionStatus): String = value.name

    @TypeConverter
    fun toSessionStatus(value: String): SessionStatus = runCatching { SessionStatus.valueOf(value) }.getOrDefault(SessionStatus.PLANNED)

    @TypeConverter
    fun fromPreferredWorkload(value: PreferredWorkload): String = value.name

    @TypeConverter
    fun toPreferredWorkload(value: String): PreferredWorkload = runCatching { PreferredWorkload.valueOf(value) }.getOrDefault(PreferredWorkload.NORMAL)

    @TypeConverter
    fun fromRecallQuality(value: RecallQuality?): String? = value?.name

    @TypeConverter
    fun toRecallQuality(value: String?): RecallQuality? = value?.let { runCatching { RecallQuality.valueOf(it) }.getOrNull() }
}
