package com.example.data.models

enum class Branch {
    SCIENTIFIC, // فرع علمي
    LITERARY    // فرع أدبي
}

enum class SubjectType {
    PROBLEM_SOLVING, // رياضيات، فيزياء، كيمياء
    MEMORIZATION,    // علوم، تاريخ، جغرافيا، ديانة، وطنية، فلسفة
    LANGUAGE,        // عربي، إنكليزي، فرنسي
    MIXED
}

enum class UnitStatus {
    NOT_STARTED,
    LEARNING,
    REVIEWING,
    MASTERED
}

enum class CommitmentType {
    SCHOOL,         // مدرسة
    INSTITUTE,      // معهد
    PRIVATE_LESSON, // درس خصوصي
    COMMUTE,        // طريق/مواصلات
    FAMILY,         // عائلة
    SPORT,          // رياضة
    PERSONAL,       // شخصي
    OTHER
}

enum class TaskType {
    NEW_LESSON,    // درس جديد
    REVIEW,        // مراجعة
    ACTIVE_RECALL, // استرجاع نشط
    EXERCISES,     // حل مسائل وتمارين
    MOCK_EXAM,     // اختبار تجريبي
    HOMEWORK,      // وظيفة/واجب
    MEMORIZATION,  // حفظ
    ERROR_REVIEW   // مراجعة الأخطاء
}

enum class TaskStatus {
    PLANNED,
    IN_PROGRESS,
    COMPLETED,
    SKIPPED,
    RESCHEDULED
}

enum class ExamType {
    SCHOOL_QUIZ,        // مسبار/مذاكرة مدرسة
    INSTITUTE_EXAM,     // امتحان معهد
    MONTHLY_EXAM,       // امتحان شهري
    MOCK_BACCALAUREATE, // امتحان تجريبي
    OFFICIAL_EXAM,      // امتحان وزاري نهایی
    CUSTOM
}

enum class SessionStatus {
    PLANNED,
    STARTED,
    COMPLETED,
    PARTIALLY_COMPLETED,
    MISSED,
    SKIPPED,
    RESCHEDULED
}

enum class PreferredWorkload {
    LIGHT,     // خفيف (طاقة منخفضة)
    NORMAL,    // عادي
    INTENSIVE  // مكثف (طاقة عالية)
}

enum class RecallQuality {
    NOT_REMEMBERED,      // ما تذكّرت
    HARD_REMEMBERED,     // تذكّرت بصعوبة
    MOSTLY_REMEMBERED,   // تذكّرت أغلبه
    MASTERED             // صار ثابت عندي
}
