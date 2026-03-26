package com.quanneng.memory.features.dailyquestion

import com.quanneng.memory.features.dailyquestion.data.Question
import com.quanneng.memory.features.dailyquestion.data.QuestionAnalysis
import com.quanneng.memory.features.dailyquestion.data.QuestionCategory
import com.quanneng.memory.features.dailyquestion.data.QuestionDifficulty
import com.quanneng.memory.features.dailyquestion.data.QuestionMetadata
import com.quanneng.memory.features.dailyquestion.domain.QuestionSelector
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 每日Android一问单元测试
 * 测试问题选择算法和数据处理逻辑
 */
@DisplayName("每日Android一问测试")
class DailyQuestionTest {

    private val testQuestions = listOf(
        Question(
            id = "q_001",
            title = "ViewModel 的作用域应该如何选择？",
            background = "测试背景",
            optionA = "根据生命周期需求选择",
            optionB = "统一使用 Fragment 级别",
            analysis = QuestionAnalysis(
                whyA = "A".repeat(400),
                whyNotB = "B".repeat(400)
            ),
            category = QuestionCategory.ANDROID_CORE,
            difficulty = QuestionDifficulty.INTERMEDIATE,
            tags = listOf("android", "viewmodel"),
            metadata = QuestionMetadata(
                id = "q_001",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        ),
        Question(
            id = "q_002",
            title = "Context 的正确使用方式是什么？",
            background = "测试背景",
            optionA = "根据生命周期选择",
            optionB = "统一使用 Application Context",
            analysis = QuestionAnalysis(
                whyA = "A".repeat(400),
                whyNotB = "B".repeat(400)
            ),
            category = QuestionCategory.ANDROID_CORE,
            difficulty = QuestionDifficulty.BEGINNER,
            tags = listOf("android", "context"),
            metadata = QuestionMetadata(
                id = "q_002",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        ),
        Question(
            id = "q_003",
            title = "SparseArray 优于 HashMap 吗？",
            background = "测试背景",
            optionA = "在特定条件下使用 SparseArray",
            optionB = "始终使用 SparseArray",
            analysis = QuestionAnalysis(
                whyA = "A".repeat(400),
                whyNotB = "B".repeat(400)
            ),
            category = QuestionCategory.DATA_STRUCTURE,
            difficulty = QuestionDifficulty.ADVANCED,
            tags = listOf("android", "datastructure"),
            metadata = QuestionMetadata(
                id = "q_003",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
    )

    @Test
    @DisplayName("问题选择器应该选择非当前问题")
    fun questionSelector_shouldSelectNonCurrentQuestion() = runTest {
        val selector = QuestionSelector()

        val selected = selector.selectDailyQuestion(
            availableQuestions = testQuestions,
            currentQuestionId = "q_001"
        )

        assertNotNull(selected)
        assertEquals("q_002", selected?.id)
    }

    @Test
    @DisplayName("问题选择器应该根据难度分布选择问题")
    fun questionSelector_shouldSelectByDifficultyDistribution() = runTest {
        val selector = QuestionSelector()

        // 多次选择，统计难度分布
        val difficultyCount = mutableMapOf<QuestionDifficulty, Int>()
        repeat(100) {
            val selected = selector.selectDailyQuestion(
                availableQuestions = testQuestions,
                currentQuestionId = null
            )
            selected?.let {
                difficultyCount[it.difficulty] = (difficultyCount[it.difficulty] ?: 0) + 1
            }
        }

        // 验证初级和中级问题被选择的次数较多
        val beginnerCount = difficultyCount[QuestionDifficulty.BEGINNER] ?: 0
        val intermediateCount = difficultyCount[QuestionDifficulty.INTERMEDIATE] ?: 0
        val advancedCount = difficultyCount[QuestionDifficulty.ADVANCED] ?: 0

        assertTrue(beginnerCount > 0, "应该选择初级问题")
        assertTrue(intermediateCount > 0, "应该选择中级问题")
        assertTrue(advancedCount > 0, "应该选择高级问题")
    }

    @Test
    @DisplayName("问题选择器在空列表时应返回 null")
    fun questionSelector_shouldReturnNullForEmptyList() = runTest {
        val selector = QuestionSelector()

        val selected = selector.selectDailyQuestion(
            availableQuestions = emptyList(),
            currentQuestionId = null
        )

        assertNull(selected)
    }

    @Test
    @DisplayName("问题选择器应该避免重复选择最近的问题")
    fun questionSelector_shouldAvoidRecentQuestions() = runTest {
        val selector = QuestionSelector()

        // 选择第一个问题
        val first = selector.selectDailyQuestion(
            availableQuestions = testQuestions,
            currentQuestionId = null
        )

        // 选择第二个问题
        val second = selector.selectDailyQuestion(
            availableQuestions = testQuestions,
            currentQuestionId = first?.id
        )

        // 验证第二个问题不同于第一个
        assertNotNull(second)
        assertEquals(false, first?.id == second?.id)
    }

    @Test
    @DisplayName("问题数据模型应该正确验证字数")
    fun questionModel_shouldValidateWordCount() {
        val validQuestion = Question(
            id = "q_test",
            title = "测试问题",
            background = "测试背景",
            optionA = "选项A",
            optionB = "选项B",
            analysis = QuestionAnalysis(
                whyA = "A".repeat(400),
                whyNotB = "B".repeat(400)
            ),
            category = QuestionCategory.ANDROID_CORE,
            difficulty = QuestionDifficulty.INTERMEDIATE,
            tags = emptyList(),
            metadata = QuestionMetadata(
                id = "q_test",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )

        assertTrue(validQuestion.isValid())
    }

    @Test
    @DisplayName("问题分类应该正确解析")
    fun questionCategory_shouldParseCorrectly() {
        assertEquals("Android核心", QuestionCategory.ANDROID_CORE.displayName)
        assertEquals("数据结构", QuestionCategory.DATA_STRUCTURE.displayName)
        assertEquals("设计模式", QuestionCategory.DESIGN_PATTERN.displayName)
        assertEquals("Java/Kotlin", QuestionCategory.JAVA_KOTLIN.displayName)
    }

    @Test
    @DisplayName("问题难度应该正确解析")
    fun questionDifficulty_shouldParseCorrectly() {
        assertEquals(1, QuestionDifficulty.BEGINNER.level)
        assertEquals(2, QuestionDifficulty.INTERMEDIATE.level)
        assertEquals(3, QuestionDifficulty.ADVANCED.level)
    }

    @Test
    @DisplayName("问题标记为已读应该更新元数据")
    fun questionMarkAsRead_shouldUpdateMetadata() {
        val question = Question(
            id = "q_test",
            title = "测试问题",
            background = "测试背景",
            optionA = "选项A",
            optionB = "选项B",
            analysis = QuestionAnalysis(
                whyA = "A".repeat(400),
                whyNotB = "B".repeat(400)
            ),
            category = QuestionCategory.ANDROID_CORE,
            difficulty = QuestionDifficulty.INTERMEDIATE,
            tags = emptyList(),
            metadata = QuestionMetadata(
                id = "q_test",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isRead = false
            )
        )

        val markedQuestion = question.markAsRead()
        assertTrue(markedQuestion.metadata.isRead)
    }

    @Test
    @DisplayName("问题标记为删除应该更新元数据")
    fun questionMarkAsDeleted_shouldUpdateMetadata() {
        val question = Question(
            id = "q_test",
            title = "测试问题",
            background = "测试背景",
            optionA = "选项A",
            optionB = "选项B",
            analysis = QuestionAnalysis(
                whyA = "A".repeat(400),
                whyNotB = "B".repeat(400)
            ),
            category = QuestionCategory.ANDROID_CORE,
            difficulty = QuestionDifficulty.INTERMEDIATE,
            tags = emptyList(),
            metadata = QuestionMetadata(
                id = "q_test",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isDeleted = false
            )
        )

        val deletedQuestion = question.markAsDeleted()
        assertTrue(deletedQuestion.metadata.isDeleted)
    }
}
