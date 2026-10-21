package com.cigcounter.app.util

data class HabitTask(
    val id: String,
    val title: String,
    val description: String,
    val difficultyLevel: String, // "Gentle Micro-Habit", "Moderate Action", "Active Commitment"
    val category: String
)

/**
 * Habit cultivation engine.
 * Philosophy: Inverse Difficulty Rule.
 * The higher the daily count, the EASIER and lower-friction the task is.
 * When consumption is high, self-criticism is highest; imposing hard tasks creates avoidance.
 * Micro-habits like drinking a glass of water or taking 5 breaths restore self-agency without guilt.
 */
object HabitTasks {

    val DONATE_UPI_ID = "puneetfsn@ybl"
    val DONATE_CAMPAIGN_TITLE = "Schizophrenia Awareness & Developer Support"

    fun getSuggestedTasks(todayCount: Int): List<HabitTask> {
        return when {
            // High count (8+): Make tasks effortless, soothing, and zero-stress
            todayCount >= 8 -> listOf(
                HabitTask(
                    id = "water_glass",
                    title = "Drink 1 tall glass of cool water",
                    description = "Cool water resets your palate and triggers a mild calming reflex.",
                    difficultyLevel = "Super Easy (Micro-Habit)",
                    category = "Hydration"
                ),
                HabitTask(
                    id = "deep_breathing",
                    title = "Practice 5 slow deep breaths",
                    description = "Inhale for 4s, pause, exhale for 6s. Tells your nervous system you are safe.",
                    difficultyLevel = "Super Easy (Micro-Habit)",
                    category = "Mindfulness"
                ),
                HabitTask(
                    id = "shoulder_release",
                    title = "Drop your shoulders and unclench your jaw",
                    description = "Release physical tension held in your neck and face for 60 seconds.",
                    difficultyLevel = "Super Easy (Micro-Habit)",
                    category = "Relaxation"
                ),
                HabitTask(
                    id = "sky_gaze",
                    title = "Look out a window at the sky for 2 minutes",
                    description = "Give your eyes and thoughts a restful panoramic break.",
                    difficultyLevel = "Super Easy (Micro-Habit)",
                    category = "Rest"
                )
            )

            // Moderate count (4 to 7): Balanced, rewarding positive habits
            todayCount in 4..7 -> listOf(
                HabitTask(
                    id = "healthy_snack",
                    title = "Eat something healthy (fruit or nuts)",
                    description = "Crisp apple slices, almonds, or berries satisfy oral sensory craving.",
                    difficultyLevel = "Easy & Rewarding",
                    category = "Nutrition"
                ),
                HabitTask(
                    id = "help_someone",
                    title = "Help someone in need or do an unexpected kindness",
                    description = "Send a comforting text, hold a door, or listen to a colleague.",
                    difficultyLevel = "Easy & Rewarding",
                    category = "Kindness"
                ),
                HabitTask(
                    id = "donate_cause",
                    title = "Donate 1 unit ($1) to schizophrenia awareness",
                    description = "Support mental health awareness and app maintenance (UPI: $DONATE_UPI_ID).",
                    difficultyLevel = "Easy & Rewarding",
                    category = "Giving"
                ),
                HabitTask(
                    id = "walk_outside",
                    title = "Take a 5-minute outdoor stroll",
                    description = "Change your physical scene and get fresh air into your lungs.",
                    difficultyLevel = "Easy & Rewarding",
                    category = "Movement"
                )
            )

            // Low count (0 to 3): High willpower, energizing, connection-building tasks
            else -> listOf(
                HabitTask(
                    id = "call_mom_friend",
                    title = "Call your Mom or a close friend",
                    description = "Spend 5-10 minutes checking in with someone who values you.",
                    difficultyLevel = "Engaging & Meaningful",
                    category = "Connection"
                ),
                HabitTask(
                    id = "exercise_10m",
                    title = "10 minutes of light exercise or stretching",
                    description = "Brisk jumping jacks, bodyweight squats, or yoga stretches.",
                    difficultyLevel = "Engaging & Meaningful",
                    category = "Physical"
                ),
                HabitTask(
                    id = "veg_juice",
                    title = "Have a fresh vegetable or fruit juice",
                    description = "Drink a freshly squeezed carrot, beetroot, or cucumber juice.",
                    difficultyLevel = "Engaging & Meaningful",
                    category = "Nutrition"
                ),
                HabitTask(
                    id = "gift_partner",
                    title = "Gift your wife or loved one something thoughtful",
                    description = "A favourite snack, fresh flowers, or a handwritten appreciation note.",
                    difficultyLevel = "Engaging & Meaningful",
                    category = "Affection"
                ),
                HabitTask(
                    id = "clean_room",
                    title = "Tidy up your room or workspace",
                    description = "Spend 8 minutes organizing your desk or making your bed.",
                    difficultyLevel = "Engaging & Meaningful",
                    category = "Environment"
                )
            )
        }
    }
}
