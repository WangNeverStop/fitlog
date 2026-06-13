package com.fitlog.data.local

import com.fitlog.data.local.entity.MuscleGroup

/**
 * Built-in seed content inserted on first launch. Kept small and data-only; the exact
 * list and colours are placeholders to be refined later, not final product copy.
 */
object SeedData {

    /** Common training parts with a distinct colour for calendar coding. */
    val muscleGroups = listOf(
        MuscleGroup(name = "胸 Chest", colorHex = "#E57373"),
        MuscleGroup(name = "背 Back", colorHex = "#64B5F6"),
        MuscleGroup(name = "腿 Legs", colorHex = "#81C784"),
        MuscleGroup(name = "肩 Shoulders", colorHex = "#FFB74D"),
        MuscleGroup(name = "手臂 Arms", colorHex = "#BA68C8"),
        MuscleGroup(name = "核心 Core", colorHex = "#4DB6AC"),
    )

    /**
     * A few starter exercises per part, keyed by the muscle group NAME so the seeder
     * can resolve the generated ids after inserting the groups.
     * Pair(exerciseName, instructions).
     */
    val exercisesByGroupName: Map<String, List<Pair<String, String>>> = mapOf(
        "胸 Chest" to listOf(
            "杠铃卧推 Barbell Bench Press" to "平躺，肩胛收紧，杠铃下放至胸部中线后推起。",
            "哑铃飞鸟 Dumbbell Fly" to "微屈肘，沿弧线打开再收拢，感受胸部拉伸与收缩。",
        ),
        "背 Back" to listOf(
            "引体向上 Pull-up" to "正握略宽于肩，背部发力将下巴拉过横杆。",
            "杠铃划船 Barbell Row" to "屈髋俯身，背部收紧将杠铃拉向腹部。",
        ),
        "腿 Legs" to listOf(
            "深蹲 Barbell Squat" to "脚与肩同宽，屈髋下蹲至大腿平行后蹬起。",
            "腿举 Leg Press" to "脚掌踏稳，控制下放再蹬起，膝盖不锁死。",
        ),
        "肩 Shoulders" to listOf(
            "站姿推举 Overhead Press" to "核心收紧，将杠铃由肩上推至头顶。",
            "侧平举 Lateral Raise" to "微屈肘将哑铃侧向抬起至与肩平行。",
        ),
        "手臂 Arms" to listOf(
            "杠铃弯举 Barbell Curl" to "大臂固定，前臂发力将杠铃弯举至顶峰收缩。",
            "绳索下压 Triceps Pushdown" to "大臂贴身，前臂下压伸直后缓慢回放。",
        ),
        "核心 Core" to listOf(
            "平板支撑 Plank" to "肘撑地面，保持身体一条直线，核心收紧。",
            "卷腹 Crunch" to "下背贴地，腹部发力卷起上半身。",
        ),
    )
}
