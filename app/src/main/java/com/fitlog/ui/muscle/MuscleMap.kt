package com.fitlog.ui.muscle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fitlog.ui.screens.training.TrainingTarget
import com.fitlog.ui.theme.FitLogTheme
import kotlin.math.min

/** Male / female figure model for the muscle map. */
enum class BodyModel { MALE, FEMALE }

/** App-level trainable muscle regions (muscle-map handoff 1.0 keys). */
enum class MuscleRegion {
    CHEST, DELTOIDS, BICEPS, TRICEPS, FOREARM, ABS, OBLIQUES, TRAPEZIUS,
    UPPER_BACK, LOWER_BACK, GLUTES, ADDUCTORS, QUADRICEPS, HAMSTRINGS, CALVES, TIBIALIS,
}

/** Provided near the app root from the current user's saved gender. */
val LocalBodyModel = staticCompositionLocalOf { BodyModel.MALE }

fun genderToBodyModel(gender: String?): BodyModel =
    if (gender == "女") BodyModel.FEMALE else BodyModel.MALE

/** Source SVG slug -> app region. Non-training slugs (head/hands/feet…) map to null. */
private val slugToRegion: Map<String, MuscleRegion> = mapOf(
    "chest" to MuscleRegion.CHEST,
    "deltoids" to MuscleRegion.DELTOIDS,
    "biceps" to MuscleRegion.BICEPS,
    "triceps" to MuscleRegion.TRICEPS,
    "forearm" to MuscleRegion.FOREARM,
    "abs" to MuscleRegion.ABS,
    "obliques" to MuscleRegion.OBLIQUES,
    "trapezius" to MuscleRegion.TRAPEZIUS,
    "upper-back" to MuscleRegion.UPPER_BACK,
    "lower-back" to MuscleRegion.LOWER_BACK,
    "gluteal" to MuscleRegion.GLUTES,
    "adductors" to MuscleRegion.ADDUCTORS,
    "quadriceps" to MuscleRegion.QUADRICEPS,
    "hamstring" to MuscleRegion.HAMSTRINGS,
    "calves" to MuscleRegion.CALVES,
    "tibialis" to MuscleRegion.TIBIALIS,
)

/** Maps a training target to the muscle regions it primarily works. */
fun TrainingTarget.toMuscleRegions(): Set<MuscleRegion> = when (this) {
    TrainingTarget.CHEST -> setOf(MuscleRegion.CHEST)
    TrainingTarget.BACK -> setOf(MuscleRegion.UPPER_BACK, MuscleRegion.LOWER_BACK, MuscleRegion.TRAPEZIUS)
    TrainingTarget.SHOULDER -> setOf(MuscleRegion.DELTOIDS)
    TrainingTarget.ARM -> setOf(MuscleRegion.BICEPS, MuscleRegion.TRICEPS, MuscleRegion.FOREARM)
    TrainingTarget.ABS -> setOf(MuscleRegion.ABS, MuscleRegion.OBLIQUES)
    TrainingTarget.LEG -> setOf(MuscleRegion.QUADRICEPS, MuscleRegion.HAMSTRINGS, MuscleRegion.CALVES)
    TrainingTarget.GLUTE -> setOf(MuscleRegion.GLUTES)
    TrainingTarget.CARDIO, TrainingTarget.FREE -> emptySet()
}

private val DefaultFill = Color(0xFFEEF4F8)
private val MapOutline = Color(0xFFC8D8E3)
private val PrimaryFill = Color(0xFF2F80ED)
private val SecondaryFill = Color(0xFF8EC5FF)

private data class VB(val minX: Float, val minY: Float, val w: Float, val h: Float)

// viewBoxes differ per model; female must NOT reuse male offsets.
private fun frontVB(m: BodyModel): VB =
    if (m == BodyModel.MALE) VB(0f, 0f, 724f, 1448f) else VB(-50f, -40f, 734f, 1538f)

private fun backVB(m: BodyModel): VB =
    if (m == BodyModel.MALE) VB(724f, 0f, 724f, 1448f) else VB(756f, 0f, 774f, 1448f)

private fun parse(map: Map<String, List<String>>): Map<String, List<Path>> =
    map.mapValues { (_, list) -> list.map { PathParser().parsePathString(it).toPath() } }

/**
 * Local muscle response map drawn with Compose Canvas from static SVG paths (no WebView /
 * network). Front (and optional back) figure; primary regions deep blue, secondary light
 * blue, everything else pale. Male/female chosen via [bodyModel]. Adapted from
 * react-muscle-highlighter (MIT).
 */
@Composable
fun MuscleMap(
    primary: Set<MuscleRegion>,
    secondary: Set<MuscleRegion>,
    modifier: Modifier = Modifier,
    showBack: Boolean = true,
    bodyModel: BodyModel = LocalBodyModel.current,
) {
    val maleFront = remember { parse(MuscleMapPaths.maleFront) }
    val maleBack = remember { parse(MuscleMapPaths.maleBack) }
    val femaleFront = remember { parse(MuscleMapPaths.femaleFront) }
    val femaleBack = remember { parse(MuscleMapPaths.femaleBack) }

    val front = if (bodyModel == BodyModel.MALE) maleFront else femaleFront
    val back = if (bodyModel == BodyModel.MALE) maleBack else femaleBack
    val fvb = frontVB(bodyModel)
    val bvb = backVB(bodyModel)

    Canvas(modifier = modifier) {
        val cols = if (showBack) 2 else 1
        val gap = if (showBack) 20f else 0f
        val backW = if (showBack) bvb.w else 0f
        val maxH = if (showBack) maxOf(fvb.h, bvb.h) else fvb.h
        val scale = min(size.height / maxH, (size.width - gap * (cols - 1)) / (fvb.w + backW))
        val totalW = (fvb.w + backW) * scale + gap * (cols - 1)
        val left0 = (size.width - totalW) / 2f

        drawBody(front, primary, secondary, regionLeft = left0, topPx = (size.height - fvb.h * scale) / 2f, scale = scale, vb = fvb)
        if (showBack) {
            drawBody(back, primary, secondary, regionLeft = left0 + fvb.w * scale + gap, topPx = (size.height - bvb.h * scale) / 2f, scale = scale, vb = bvb)
        }
    }
}

private fun DrawScope.drawBody(
    paths: Map<String, List<Path>>,
    primary: Set<MuscleRegion>,
    secondary: Set<MuscleRegion>,
    regionLeft: Float,
    topPx: Float,
    scale: Float,
    vb: VB,
) {
    paths.forEach { (slug, pathList) ->
        val region = slugToRegion[slug]
        val fill = when {
            region != null && region in primary -> PrimaryFill
            region != null && region in secondary -> SecondaryFill
            else -> DefaultFill
        }
        pathList.forEach { p ->
            withTransform({
                translate(left = regionLeft, top = topPx)
                scale(scaleX = scale, scaleY = scale, pivot = Offset.Zero)
                translate(left = -vb.minX, top = -vb.minY)
            }) {
                drawPath(p, color = fill)
                drawPath(p, color = MapOutline, style = Stroke(width = 1.2f / scale))
            }
        }
    }
}

@Preview(name = "肌肉图 · 男 · 胸+三头", showBackground = true, widthDp = 320, heightDp = 260)
@Composable
private fun MuscleMapPreviewMale() {
    FitLogTheme {
        MuscleMap(
            primary = setOf(MuscleRegion.CHEST, MuscleRegion.TRICEPS),
            secondary = setOf(MuscleRegion.DELTOIDS),
            modifier = Modifier.fillMaxWidth().height(240.dp),
            bodyModel = BodyModel.MALE,
        )
    }
}

@Preview(name = "肌肉图 · 女 · 背+二头", showBackground = true, widthDp = 320, heightDp = 260)
@Composable
private fun MuscleMapPreviewFemale() {
    FitLogTheme {
        MuscleMap(
            primary = setOf(MuscleRegion.UPPER_BACK, MuscleRegion.LOWER_BACK),
            secondary = setOf(MuscleRegion.BICEPS, MuscleRegion.FOREARM),
            modifier = Modifier.fillMaxWidth().height(240.dp),
            bodyModel = BodyModel.FEMALE,
        )
    }
}

@Preview(name = "肌肉图 · 空", showBackground = true, widthDp = 320, heightDp = 260)
@Composable
private fun MuscleMapPreviewEmpty() {
    FitLogTheme {
        MuscleMap(primary = emptySet(), secondary = emptySet(), modifier = Modifier.fillMaxWidth().height(240.dp))
    }
}
