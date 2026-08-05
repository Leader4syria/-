package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.models.*

@Database(
    entities = [
        StudentProfileEntity::class,
        SubjectEntity::class,
        CurriculumUnitEntity::class,
        FixedCommitmentEntity::class,
        StudyTaskEntity::class,
        ExamEntity::class,
        PlannedSessionEntity::class,
        ReviewQueueEntity::class,
        DailyCheckInEntity::class,
        ErrorNoteEntity::class,
        SettingsAndBackupEntity::class,
        StudentMemoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MasarDatabase : RoomDatabase() {

    abstract fun studentProfileDao(): StudentProfileDao
    abstract fun subjectDao(): SubjectDao
    abstract fun curriculumUnitDao(): CurriculumUnitDao
    abstract fun fixedCommitmentDao(): FixedCommitmentDao
    abstract fun studyTaskDao(): StudyTaskDao
    abstract fun examDao(): ExamDao
    abstract fun plannedSessionDao(): PlannedSessionDao
    abstract fun reviewQueueDao(): ReviewQueueDao
    abstract fun dailyCheckInDao(): DailyCheckInDao
    abstract fun errorNoteDao(): ErrorNoteDao
    abstract fun settingsDao(): SettingsDao
    abstract fun studentMemoryDao(): StudentMemoryDao

    companion object {
        @Volatile
        private var INSTANCE: MasarDatabase? = null

        fun getDatabase(context: Context): MasarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MasarDatabase::class.java,
                    "masar_baccalaureate_db"
                )
                .fallbackToDestructiveMigration() // safe initial fallback, proper migrations supported for future schema changes
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
