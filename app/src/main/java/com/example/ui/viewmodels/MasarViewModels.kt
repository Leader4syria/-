package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.*
import com.example.data.repository.MasarRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MasarViewModel(application: Application) : AndroidViewModel(application) {
    val repository = MasarRepository(application)

    val profileFlow: StateFlow<StudentProfileEntity?> = repository.profileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeSubjectsFlow: StateFlow<List<SubjectEntity>> = repository.activeSubjectsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val commitmentsFlow: StateFlow<List<FixedCommitmentEntity>> = repository.commitmentsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTasksFlow: StateFlow<List<StudyTaskEntity>> = repository.pendingTasksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val examsFlow: StateFlow<List<ExamEntity>> = repository.examsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settingsFlow: StateFlow<SettingsAndBackupEntity?> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val errorNotesFlow: StateFlow<List<ErrorNoteEntity>> = repository.errorNotesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memoriesFlow: StateFlow<List<StudentMemoryEntity>> = repository.memoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    private val _selectedDate = MutableStateFlow(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedDateSessionsFlow: StateFlow<List<PlannedSessionEntity>> = _selectedDate
        .flatMapLatest { repository.getSessionsForDate(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDateCheckIn = MutableStateFlow<DailyCheckInEntity?>(null)
    val selectedDateCheckIn: StateFlow<DailyCheckInEntity?> = _selectedDateCheckIn.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getCheckInFlow(_selectedDate.value).collect {
                _selectedDateCheckIn.value = it
            }
        }
    }

    fun setSelectedDate(dateStr: String) {
        _selectedDate.value = dateStr
    }

    fun submitDailyCheckIn(energyLevel: Int, mood: String = "normal") {
        viewModelScope.launch {
            val checkIn = DailyCheckInEntity(
                date = _selectedDate.value,
                availableEnergy = energyLevel,
                mood = mood
            )
            repository.saveCheckIn(checkIn)
        }
    }

    fun completeSession(session: PlannedSessionEntity, actualMinutes: Int, recallQuality: RecallQuality?) {
        viewModelScope.launch {
            repository.completeSession(session, actualMinutes, recallQuality)
        }
    }

    fun triggerReschedule() {
        viewModelScope.launch {
            repository.triggerReSchedule(LocalDate.parse(_selectedDate.value))
        }
    }

    fun addCustomTask(task: StudyTaskEntity) {
        viewModelScope.launch {
            repository.addCustomTask(task)
        }
    }

    fun addExam(exam: ExamEntity) {
        viewModelScope.launch {
            repository.addExam(exam)
        }
    }

    fun addCommitment(commitment: FixedCommitmentEntity) {
        viewModelScope.launch {
            repository.addFixedCommitment(commitment)
        }
    }

    fun deleteCommitment(commitment: FixedCommitmentEntity) {
        viewModelScope.launch {
            repository.deleteCommitment(commitment)
        }
    }

    fun addErrorNote(note: ErrorNoteEntity) {
        viewModelScope.launch {
            repository.addErrorNote(note)
        }
    }

    fun addStudentMemory(title: String, note: String, tag: String = "لحظة إنجاز") {
        viewModelScope.launch {
            val memory = StudentMemoryEntity(
                title = title,
                note = note,
                tag = tag,
                date = _selectedDate.value
            )
            repository.addMemory(memory)
        }
    }

    fun deleteStudentMemory(memory: StudentMemoryEntity) {
        viewModelScope.launch {
            repository.deleteMemory(memory)
        }
    }

    fun updateSettings(settings: SettingsAndBackupEntity) {

        viewModelScope.launch {
            repository.updateSettings(settings)
        }
    }
}

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MasarRepository(application)

    private val _currentStep = MutableStateFlow(1) // 1 to 5
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    var studentName = MutableStateFlow("")
    var selectedBranch = MutableStateFlow(Branch.SCIENTIFIC)
    var targetScore = MutableStateFlow(2400)

    var wakeTimeMinutes = MutableStateFlow(360) // 06:00
    var sleepTimeMinutes = MutableStateFlow(1380) // 23:00
    var dailyFreeMinutes = MutableStateFlow(120) // 2 hours
    var restDay = MutableStateFlow(5) // Friday

    var focusDuration = MutableStateFlow(45)
    var maxDailyStudy = MutableStateFlow(420) // 7 hours max

    val tempCommitments = MutableStateFlow<List<FixedCommitmentEntity>>(emptyList())
    val subjectRatings = MutableStateFlow<Map<String, Int>>(emptyMap()) // subId -> rating 1..5

    fun nextStep() {
        if (_currentStep.value < 5) {
            _currentStep.value++
        }
    }

    fun prevStep() {
        if (_currentStep.value > 1) {
            _currentStep.value--
        }
    }

    fun addTempCommitment(title: String, dayOfWeek: Int, startMin: Int, endMin: Int, type: CommitmentType) {
        val list = tempCommitments.value.toMutableList()
        list.add(
            FixedCommitmentEntity(
                title = title,
                dayOfWeek = dayOfWeek,
                startTimeMinutes = startMin,
                endTimeMinutes = endMin,
                type = type
            )
        )
        tempCommitments.value = list
    }

    fun setSubjectRating(subId: String, rating: Int) {
        val map = subjectRatings.value.toMutableMap()
        map[subId] = rating
        subjectRatings.value = map
    }

    fun finishOnboarding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // 1. Seed curriculum subjects
            repository.seedCurriculum(selectedBranch.value)

            // 2. Save profile
            val profile = StudentProfileEntity(
                id = 1,
                name = studentName.value,
                branch = selectedBranch.value,
                targetScore = targetScore.value,
                wakeTimeMinutes = wakeTimeMinutes.value,
                sleepTimeMinutes = sleepTimeMinutes.value,
                minimumDailyFreeMinutes = dailyFreeMinutes.value,
                weeklyRestDay = restDay.value,
                defaultFocusDurationMinutes = focusDuration.value,
                maximumDailyStudyMinutes = maxDailyStudy.value,
                dialectMode = true,
                isOnboardingCompleted = true
            )
            repository.saveProfile(profile)

            // 3. Save commitments
            for (c in tempCommitments.value) {
                repository.addFixedCommitment(c)
            }

            // 4. Initial schedule generation
            repository.triggerReSchedule()

            onSuccess()
        }
    }
}
