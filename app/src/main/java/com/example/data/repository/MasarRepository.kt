package com.example.data.repository

import android.content.Context
import com.example.data.db.MasarDatabase
import com.example.data.models.*
import com.example.domain.scheduler.AdaptiveStudyScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.max

class MasarRepository(
    private val context: Context,
    private val db: MasarDatabase = MasarDatabase.getDatabase(context)
) {
    private val profileDao = db.studentProfileDao()
    private val subjectDao = db.subjectDao()
    private val unitDao = db.curriculumUnitDao()
    private val commitmentDao = db.fixedCommitmentDao()
    private val taskDao = db.studyTaskDao()
    private val examDao = db.examDao()
    private val sessionDao = db.plannedSessionDao()
    private val reviewDao = db.reviewQueueDao()
    private val checkInDao = db.dailyCheckInDao()
    private val errorNoteDao = db.errorNoteDao()
    private val settingsDao = db.settingsDao()
    private val memoryDao = db.studentMemoryDao()

    private val scheduler = AdaptiveStudyScheduler()

    // Flows for UI
    val profileFlow: Flow<StudentProfileEntity?> = profileDao.getProfileFlow()
    val activeSubjectsFlow: Flow<List<SubjectEntity>> = subjectDao.getAllActiveSubjects()
    val commitmentsFlow: Flow<List<FixedCommitmentEntity>> = commitmentDao.getAllCommitments()
    val pendingTasksFlow: Flow<List<StudyTaskEntity>> = taskDao.getPendingTasks()
    val examsFlow: Flow<List<ExamEntity>> = examDao.getAllExams()
    val errorNotesFlow: Flow<List<ErrorNoteEntity>> = errorNoteDao.getAllNotes()
    val settingsFlow: Flow<SettingsAndBackupEntity?> = settingsDao.getSettingsFlow()
    val memoriesFlow: Flow<List<StudentMemoryEntity>> = memoryDao.getAllMemoriesFlow()


    fun getSessionsForDate(date: String): Flow<List<PlannedSessionEntity>> = sessionDao.getSessionsForDate(date)
    fun getUnitsForSubject(subjectId: String): Flow<List<CurriculumUnitEntity>> = unitDao.getUnitsForSubject(subjectId)
    fun getCheckInFlow(date: String): Flow<DailyCheckInEntity?> = checkInDao.getCheckInFlow(date)

    suspend fun getProfile(): StudentProfileEntity? = profileDao.getProfile()

    suspend fun saveProfile(profile: StudentProfileEntity) {
        profileDao.insertOrUpdate(profile)
    }

    /**
     * Seeds curriculum subjects and units from local JSON assets during Onboarding.
     */
    suspend fun seedCurriculum(branch: Branch) = withContext(Dispatchers.IO) {
        val jsonFileName = if (branch == Branch.SCIENTIFIC) "curriculum_scientific.json" else "curriculum_literary.json"
        val jsonString = runCatching {
            context.assets.open(jsonFileName).bufferedReader().use { it.readText() }
        }.getOrNull() ?: return@withContext

        val jsonObject = JSONObject(jsonString)
        val curriculumVersion = jsonObject.optString("curriculumVersion", "2025-2026")
        val subjectsArray = jsonObject.getJSONArray("subjects")

        val subjectsToInsert = mutableListOf<SubjectEntity>()
        val unitsToInsert = mutableListOf<CurriculumUnitEntity>()

        for (i in 0 until subjectsArray.length()) {
            val sObj = subjectsArray.getJSONObject(i)
            val subId = sObj.getString("id")
            val subName = sObj.getString("name")
            val shortName = sObj.getString("shortName")
            val subTypeStr = sObj.getString("subjectType")
            val colorHex = sObj.getString("color")
            val iconName = sObj.getString("icon")
            val importance = sObj.getDouble("importanceWeight").toFloat()
            val weeklyMin = sObj.optInt("weeklyMinimumMinutes", 180)

            val subjectType = runCatching { SubjectType.valueOf(subTypeStr.uppercase()) }.getOrDefault(SubjectType.PROBLEM_SOLVING)

            subjectsToInsert.add(
                SubjectEntity(
                    id = subId,
                    name = subName,
                    shortName = shortName,
                    branch = branch,
                    curriculumVersion = curriculumVersion,
                    subjectType = subjectType,
                    colorHex = colorHex,
                    iconName = iconName,
                    importanceWeight = importance,
                    weeklyMinimumMinutes = weeklyMin,
                    weeklyTargetMinutes = weeklyMin + 120
                )
            )

            val unitsArray = sObj.getJSONArray("units")
            for (j in 0 until unitsArray.length()) {
                val uObj = unitsArray.getJSONObject(j)
                unitsToInsert.add(
                    CurriculumUnitEntity(
                        id = uObj.getString("id"),
                        subjectId = subId,
                        title = uObj.getString("title"),
                        orderIndex = j,
                        estimatedMinutes = uObj.optInt("estimatedMinutes", 180),
                        difficulty = uObj.optInt("difficulty", 3),
                        curriculumVersion = curriculumVersion
                    )
                )
            }
        }

        subjectDao.insertSubjects(subjectsToInsert)
        unitDao.insertUnits(unitsToInsert)
    }

    /**
     * Trigger scheduler re-generation for the next 7 days.
     */
    suspend fun triggerReSchedule(
        startDate: LocalDate = LocalDate.now(),
        dailyCheckIn: DailyCheckInEntity? = null
    ) = withContext(Dispatchers.IO) {
        val profile = profileDao.getProfile() ?: return@withContext
        val subjects = subjectDao.getAllSubjectsList()
        val units = unitDao.getAllUnits()
        val commitments = commitmentDao.getAllCommitmentsList()
        val pendingTasks = taskDao.getPendingTasksList()
        val exams = examDao.getAllExamsList()
        val todayStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val dueReviews = reviewDao.getDueReviewsList(todayStr)

        val result = scheduler.schedulePeriod(
            startDate = startDate,
            daysCount = 7,
            profile = profile,
            subjects = subjects,
            units = units,
            commitments = commitments,
            pendingTasks = pendingTasks,
            exams = exams,
            dueReviews = dueReviews,
            dailyCheckIn = dailyCheckIn
        )

        // Save new sessions
        sessionDao.clearFutureUncompletedSessions(todayStr)
        sessionDao.insertSessions(result.sessions)

        result
    }

    suspend fun saveCheckIn(checkIn: DailyCheckInEntity) = withContext(Dispatchers.IO) {
        checkInDao.insertCheckIn(checkIn)
        triggerReSchedule(dailyCheckIn = checkIn)
    }

    suspend fun addFixedCommitment(commitment: FixedCommitmentEntity) = withContext(Dispatchers.IO) {
        commitmentDao.insertCommitment(commitment)
        triggerReSchedule()
    }

    suspend fun deleteCommitment(commitment: FixedCommitmentEntity) = withContext(Dispatchers.IO) {
        commitmentDao.deleteCommitment(commitment)
        triggerReSchedule()
    }

    suspend fun addCustomTask(task: StudyTaskEntity) = withContext(Dispatchers.IO) {
        taskDao.insertTask(task)
        triggerReSchedule()
    }

    suspend fun addExam(exam: ExamEntity) = withContext(Dispatchers.IO) {
        val examId = examDao.insertExam(exam)
        val subject = subjectDao.getSubjectById(exam.subjectId)
        val units = unitDao.getUnitsListForSubject(exam.subjectId)
        val profile = profileDao.getProfile()

        if (subject != null && profile != null) {
            val generatedTasks = scheduler.planForExam(exam, subject, units, profile)
            for (t in generatedTasks) {
                taskDao.insertTask(t)
            }
        }
        triggerReSchedule()
    }

    suspend fun completeSession(
        session: PlannedSessionEntity,
        actualMinutes: Int,
        recallQuality: RecallQuality?
    ) = withContext(Dispatchers.IO) {
        val updatedSession = session.copy(
            status = SessionStatus.COMPLETED,
            actualMinutes = actualMinutes,
            recallQuality = recallQuality
        )
        sessionDao.updateSession(updatedSession)

        // Spaced Repetition queue update if unit is present
        if (session.taskId > 0) {
            val task = taskDao.getTaskById(session.taskId)
            if (task != null) {
                val remaining = max(0, task.remainingMinutes - actualMinutes)
                if (remaining == 0) {
                    taskDao.updateTask(task.copy(remainingMinutes = 0, status = TaskStatus.COMPLETED, completedAt = System.currentTimeMillis()))
                } else {
                    taskDao.updateTask(task.copy(remainingMinutes = remaining))
                }

                if (task.curriculumUnitId != null && recallQuality != null) {
                    val unit = unitDao.getUnitById(task.curriculumUnitId)
                    if (unit != null) {
                        val newMastery = when (recallQuality) {
                            RecallQuality.NOT_REMEMBERED -> max(0.1f, unit.mastery - 0.1f)
                            RecallQuality.HARD_REMEMBERED -> (unit.mastery + 0.1f).coerceIn(0.1f, 1.0f)
                            RecallQuality.MOSTLY_REMEMBERED -> (unit.mastery + 0.2f).coerceIn(0.1f, 1.0f)
                            RecallQuality.MASTERED -> (unit.mastery + 0.3f).coerceIn(0.1f, 1.0f)
                        }
                        unitDao.updateUnit(unit.copy(mastery = newMastery, lastStudiedAt = System.currentTimeMillis()))

                        val intervalDays = when (recallQuality) {
                            RecallQuality.NOT_REMEMBERED -> 1
                            RecallQuality.HARD_REMEMBERED -> 3
                            RecallQuality.MOSTLY_REMEMBERED -> 7
                            RecallQuality.MASTERED -> 14
                        }
                        val nextDate = LocalDate.now().plusDays(intervalDays.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE)
                        reviewDao.insertReview(
                            ReviewQueueEntity(
                                curriculumUnitId = unit.id,
                                dueDate = nextDate,
                                intervalDays = intervalDays,
                                recallQuality = recallQuality
                            )
                        )
                    }
                }
            }
        }
    }

    suspend fun addErrorNote(note: ErrorNoteEntity) = withContext(Dispatchers.IO) {
        errorNoteDao.insertNote(note)
    }

    suspend fun addMemory(memory: StudentMemoryEntity) = withContext(Dispatchers.IO) {
        memoryDao.insertMemory(memory)
    }

    suspend fun deleteMemory(memory: StudentMemoryEntity) = withContext(Dispatchers.IO) {
        memoryDao.deleteMemory(memory)
    }

    suspend fun updateSettings(settings: SettingsAndBackupEntity) = withContext(Dispatchers.IO) {

        settingsDao.insertOrUpdate(settings)
    }

    /**
     * Export database content into a structured JSON string for local backup.
     */
    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        val profile = profileDao.getProfile()
        val subjects = subjectDao.getAllSubjectsList()
        val commitments = commitmentDao.getAllCommitmentsList()
        val tasks = taskDao.getPendingTasksList()
        val exams = examDao.getAllExamsList()

        root.put("version", 1)
        root.put("exportTime", System.currentTimeMillis())

        if (profile != null) {
            val pObj = JSONObject()
            pObj.put("name", profile.name)
            pObj.put("branch", profile.branch.name)
            pObj.put("wakeTimeMinutes", profile.wakeTimeMinutes)
            pObj.put("sleepTimeMinutes", profile.sleepTimeMinutes)
            root.put("profile", pObj)
        }

        val subjArray = JSONArray()
        for (s in subjects) {
            val sObj = JSONObject()
            sObj.put("id", s.id)
            sObj.put("name", s.name)
            sObj.put("mastery", s.currentMastery)
            subjArray.put(sObj)
        }
        root.put("subjects", subjArray)

        root.toString(2)
    }
}
