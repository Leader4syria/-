package com.example.data.db

import androidx.room.*
import com.example.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentProfileDao {
    @Query("SELECT * FROM student_profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<StudentProfileEntity?>

    @Query("SELECT * FROM student_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): StudentProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: StudentProfileEntity)
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects WHERE isActive = 1")
    fun getAllActiveSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects")
    suspend fun getAllSubjectsList(): List<SubjectEntity>

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    suspend fun getSubjectById(id: String): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubject(id: String)
}

@Dao
interface CurriculumUnitDao {
    @Query("SELECT * FROM curriculum_units WHERE subjectId = :subjectId ORDER BY orderIndex ASC")
    fun getUnitsForSubject(subjectId: String): Flow<List<CurriculumUnitEntity>>

    @Query("SELECT * FROM curriculum_units ORDER BY orderIndex ASC")
    suspend fun getAllUnits(): List<CurriculumUnitEntity>

    @Query("SELECT * FROM curriculum_units WHERE subjectId = :subjectId ORDER BY orderIndex ASC")
    suspend fun getUnitsListForSubject(subjectId: String): List<CurriculumUnitEntity>

    @Query("SELECT * FROM curriculum_units WHERE id = :id LIMIT 1")
    suspend fun getUnitById(id: String): CurriculumUnitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: List<CurriculumUnitEntity>)

    @Update
    suspend fun updateUnit(unit: CurriculumUnitEntity)
}

@Dao
interface FixedCommitmentDao {
    @Query("SELECT * FROM fixed_commitments ORDER BY dayOfWeek, startTimeMinutes")
    fun getAllCommitments(): Flow<List<FixedCommitmentEntity>>

    @Query("SELECT * FROM fixed_commitments ORDER BY dayOfWeek, startTimeMinutes")
    suspend fun getAllCommitmentsList(): List<FixedCommitmentEntity>

    @Query("SELECT * FROM fixed_commitments WHERE dayOfWeek = :dayOfWeek ORDER BY startTimeMinutes")
    suspend fun getCommitmentsForDay(dayOfWeek: Int): List<FixedCommitmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommitment(commitment: FixedCommitmentEntity): Long

    @Delete
    suspend fun deleteCommitment(commitment: FixedCommitmentEntity)
}

@Dao
interface StudyTaskDao {
    @Query("SELECT * FROM study_tasks WHERE status != 'COMPLETED' ORDER BY priority DESC, id DESC")
    fun getPendingTasks(): Flow<List<StudyTaskEntity>>

    @Query("SELECT * FROM study_tasks WHERE status != 'COMPLETED' ORDER BY priority DESC, id DESC")
    suspend fun getPendingTasksList(): List<StudyTaskEntity>

    @Query("SELECT * FROM study_tasks WHERE subjectId = :subjectId")
    fun getTasksForSubject(subjectId: String): Flow<List<StudyTaskEntity>>

    @Query("SELECT * FROM study_tasks WHERE id = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: Long): StudyTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: StudyTaskEntity): Long

    @Update
    suspend fun updateTask(task: StudyTaskEntity)

    @Query("DELETE FROM study_tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)
}

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams ORDER BY examDate ASC, examTimeMinutes ASC")
    fun getAllExams(): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams ORDER BY examDate ASC, examTimeMinutes ASC")
    suspend fun getAllExamsList(): List<ExamEntity>

    @Query("SELECT * FROM exams WHERE examDate >= :startDate ORDER BY examDate ASC LIMIT 1")
    fun getNextUpcomingExam(startDate: String): Flow<ExamEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamEntity): Long

    @Update
    suspend fun updateExam(exam: ExamEntity)

    @Delete
    suspend fun deleteExam(exam: ExamEntity)
}

@Dao
interface PlannedSessionDao {
    @Query("SELECT * FROM planned_sessions WHERE date = :date ORDER BY startTimeMinutes ASC")
    fun getSessionsForDate(date: String): Flow<List<PlannedSessionEntity>>

    @Query("SELECT * FROM planned_sessions WHERE date = :date ORDER BY startTimeMinutes ASC")
    suspend fun getSessionsListForDate(date: String): List<PlannedSessionEntity>

    @Query("SELECT * FROM planned_sessions WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC, startTimeMinutes ASC")
    fun getSessionsForRange(startDate: String, endDate: String): Flow<List<PlannedSessionEntity>>

    @Query("SELECT * FROM planned_sessions WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC, startTimeMinutes ASC")
    suspend fun getSessionsListForRange(startDate: String, endDate: String): List<PlannedSessionEntity>

    @Query("SELECT * FROM planned_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Long): PlannedSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<PlannedSessionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PlannedSessionEntity): Long

    @Update
    suspend fun updateSession(session: PlannedSessionEntity)

    @Query("DELETE FROM planned_sessions WHERE date >= :date AND status = 'PLANNED' AND isLocked = 0")
    suspend fun clearFutureUncompletedSessions(date: String)
}

@Dao
interface ReviewQueueDao {
    @Query("SELECT * FROM review_queue WHERE completedAt IS NULL AND dueDate <= :currentDate ORDER BY dueDate ASC")
    fun getDueReviews(currentDate: String): Flow<List<ReviewQueueEntity>>

    @Query("SELECT * FROM review_queue WHERE completedAt IS NULL AND dueDate <= :currentDate ORDER BY dueDate ASC")
    suspend fun getDueReviewsList(currentDate: String): List<ReviewQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewQueueEntity): Long

    @Update
    suspend fun updateReview(review: ReviewQueueEntity)
}

@Dao
interface DailyCheckInDao {
    @Query("SELECT * FROM daily_check_ins WHERE date = :date LIMIT 1")
    fun getCheckInFlow(date: String): Flow<DailyCheckInEntity?>

    @Query("SELECT * FROM daily_check_ins WHERE date = :date LIMIT 1")
    suspend fun getCheckIn(date: String): DailyCheckInEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: DailyCheckInEntity)
}

@Dao
interface ErrorNoteDao {
    @Query("SELECT * FROM error_notes WHERE subjectId = :subjectId ORDER BY id DESC")
    fun getNotesForSubject(subjectId: String): Flow<List<ErrorNoteEntity>>

    @Query("SELECT * FROM error_notes ORDER BY id DESC")
    fun getAllNotes(): Flow<List<ErrorNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: ErrorNoteEntity): Long

    @Update
    suspend fun updateNote(note: ErrorNoteEntity)

    @Delete
    suspend fun deleteNote(note: ErrorNoteEntity)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings_and_backup WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<SettingsAndBackupEntity?>

    @Query("SELECT * FROM settings_and_backup WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): SettingsAndBackupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: SettingsAndBackupEntity)
}

@Dao
interface StudentMemoryDao {
    @Query("SELECT * FROM student_memories ORDER BY createdAt DESC")
    fun getAllMemoriesFlow(): Flow<List<StudentMemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: StudentMemoryEntity): Long

    @Delete
    suspend fun deleteMemory(memory: StudentMemoryEntity)
}

