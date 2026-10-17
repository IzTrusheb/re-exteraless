/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Material 3 Expressive shape definitions adapted from androidx.compose.material3.MaterialShapes.
 */
package app.exteraless.player

import android.graphics.Path

import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.TransformResult
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.rectangle
import androidx.graphics.shapes.star

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** The official Material 3 Expressive shape catalog, in normalized 0..1 coordinates. */
object Material3ExpressiveShapes {
    private val cornerRound15 = CornerRounding(0.15f)
    private val cornerRound20 = CornerRounding(0.2f)
    private val cornerRound50 = CornerRounding(0.5f)
    private val cornerRound100 = CornerRounding(1f)

    private val cachedPaths = HashMap<Int, Path>()

    @JvmStatic
    fun pathFor(shape: Int): Path = synchronized(cachedPaths) {
        cachedPaths.getOrPut(shape) { polygonFor(shape).toAndroidPath() }
    }

    private fun polygonFor(shape: Int): RoundedPolygon {
        val polygon = when (shape) {
            PlayerSettings.BUTTON_SHAPE_M3_CIRCLE -> RoundedPolygon.circle(numVertices = 10)
            PlayerSettings.BUTTON_SHAPE_M3_SLANTED -> slanted()
            PlayerSettings.BUTTON_SHAPE_M3_ARCH -> arch()
            PlayerSettings.BUTTON_SHAPE_M3_FAN -> fan()
            PlayerSettings.BUTTON_SHAPE_M3_ARROW -> arrow()
            PlayerSettings.BUTTON_SHAPE_M3_SEMICIRCLE -> semiCircle()
            PlayerSettings.BUTTON_SHAPE_M3_OVAL -> oval()
            PlayerSettings.BUTTON_SHAPE_M3_PILL -> expressivePill()
            PlayerSettings.BUTTON_SHAPE_M3_TRIANGLE -> triangle()
            PlayerSettings.BUTTON_SHAPE_M3_DIAMOND -> diamond()
            PlayerSettings.BUTTON_SHAPE_M3_CLAMSHELL -> clamShell()
            PlayerSettings.BUTTON_SHAPE_M3_PENTAGON -> pentagon()
            PlayerSettings.BUTTON_SHAPE_M3_GEM -> gem()
            PlayerSettings.BUTTON_SHAPE_M3_VERY_SUNNY -> verySunny()
            PlayerSettings.BUTTON_SHAPE_M3_SUNNY -> sunny()
            PlayerSettings.BUTTON_SHAPE_M3_COOKIE_4 -> cookie4()
            PlayerSettings.BUTTON_SHAPE_M3_COOKIE_6 -> cookie6()
            PlayerSettings.BUTTON_SHAPE_M3_COOKIE_7 -> cookie7()
            PlayerSettings.BUTTON_SHAPE_M3_COOKIE_9 -> cookie9()
            PlayerSettings.BUTTON_SHAPE_M3_COOKIE_12 -> cookie12()
            PlayerSettings.BUTTON_SHAPE_M3_GHOSTISH -> ghostish()
            PlayerSettings.BUTTON_SHAPE_M3_CLOVER_4 -> clover4()
            PlayerSettings.BUTTON_SHAPE_M3_CLOVER_8 -> clover8()
            PlayerSettings.BUTTON_SHAPE_M3_BURST -> burst()
            PlayerSettings.BUTTON_SHAPE_M3_SOFT_BURST -> softBurst()
            PlayerSettings.BUTTON_SHAPE_M3_BOOM -> boom()
            PlayerSettings.BUTTON_SHAPE_M3_SOFT_BOOM -> softBoom()
            PlayerSettings.BUTTON_SHAPE_M3_FLOWER -> flower()
            PlayerSettings.BUTTON_SHAPE_M3_PUFFY -> puffy()
            PlayerSettings.BUTTON_SHAPE_M3_PUFFY_DIAMOND -> puffyDiamond()
            PlayerSettings.BUTTON_SHAPE_M3_PIXEL_CIRCLE -> pixelCircle()
            PlayerSettings.BUTTON_SHAPE_M3_PIXEL_TRIANGLE -> pixelTriangle()
            PlayerSettings.BUTTON_SHAPE_M3_BUN -> bun()
            PlayerSettings.BUTTON_SHAPE_M3_HEART -> heart()
            else -> RoundedPolygon.circle()
        }
        return polygon.normalized()
    }

    private data class Point(val x: Float, val y: Float)

    private data class PointNRound(
        val point: Point,
        val rounding: CornerRounding = CornerRounding.Unrounded,
    )

    private fun slanted() = customPolygon(
        listOf(
            PointNRound(Point(0.926f, 0.970f), CornerRounding(0.189f, 0.811f)),
            PointNRound(Point(-0.021f, 0.967f), CornerRounding(0.187f, 0.057f)),
        ),
        reps = 2,
    )

    private fun arch() = RoundedPolygon(
        numVertices = 4,
        perVertexRounding = listOf(cornerRound100, cornerRound100, cornerRound20, cornerRound20),
    ).rotate(-135f)

    private fun fan() = customPolygon(
        listOf(
            PointNRound(Point(1.004f, 1.000f), CornerRounding(0.148f, 0.417f)),
            PointNRound(Point(0.000f, 1.000f), CornerRounding(0.151f)),
            PointNRound(Point(0.000f, -0.003f), CornerRounding(0.148f)),
            PointNRound(Point(0.978f, 0.020f), CornerRounding(0.803f)),
        ),
        reps = 1,
    )

    private fun arrow() = customPolygon(
        listOf(
            PointNRound(Point(0.500f, 0.892f), CornerRounding(0.313f)),
            PointNRound(Point(-0.216f, 1.050f), CornerRounding(0.207f)),
            PointNRound(Point(0.499f, -0.160f), CornerRounding(0.215f, 1.000f)),
            PointNRound(Point(1.225f, 1.060f), CornerRounding(0.211f)),
        ),
        reps = 1,
    )

    private fun semiCircle() = RoundedPolygon.rectangle(
        width = 1.6f,
        height = 1f,
        perVertexRounding = listOf(cornerRound20, cornerRound20, cornerRound100, cornerRound100),
    )

    private fun oval() = RoundedPolygon.circle()
        .scale(1f, 0.64f)
        .rotate(-45f)

    private fun expressivePill() = customPolygon(
        listOf(
            PointNRound(Point(0.961f, 0.039f), CornerRounding(0.426f)),
            PointNRound(Point(1.001f, 0.428f)),
            PointNRound(Point(1.000f, 0.609f), CornerRounding(1.000f)),
        ),
        reps = 2,
        mirroring = true,
    )

    private fun triangle() = RoundedPolygon(numVertices = 3, rounding = cornerRound20).rotate(-90f)

    private fun diamond() = customPolygon(
        listOf(
            PointNRound(Point(0.500f, 1.096f), CornerRounding(0.151f, 0.524f)),
            PointNRound(Point(0.040f, 0.500f), CornerRounding(0.159f)),
        ),
        reps = 2,
    )

    private fun clamShell() = customPolygon(
        listOf(
            PointNRound(Point(0.171f, 0.841f), CornerRounding(0.159f)),
            PointNRound(Point(-0.020f, 0.500f), CornerRounding(0.140f)),
            PointNRound(Point(0.170f, 0.159f), CornerRounding(0.159f)),
        ),
        reps = 2,
    )

    private fun pentagon() = customPolygon(
        listOf(
            PointNRound(Point(0.500f, -0.009f), CornerRounding(0.172f)),
            PointNRound(Point(1.030f, 0.365f), CornerRounding(0.164f)),
            PointNRound(Point(0.828f, 0.970f), CornerRounding(0.169f)),
        ),
        reps = 1,
        mirroring = true,
    )

    private fun gem() = customPolygon(
        listOf(
            PointNRound(Point(0.499f, 1.023f), CornerRounding(0.241f, 0.778f)),
            PointNRound(Point(-0.005f, 0.792f), CornerRounding(0.208f)),
            PointNRound(Point(0.073f, 0.258f), CornerRounding(0.228f)),
            PointNRound(Point(0.433f, -0.000f), CornerRounding(0.491f)),
        ),
        reps = 1,
        mirroring = true,
    )

    private fun sunny() = RoundedPolygon.star(
        numVerticesPerRadius = 8,
        innerRadius = 0.8f,
        rounding = cornerRound15,
    )

    private fun verySunny() = customPolygon(
        listOf(
            PointNRound(Point(0.500f, 1.080f), CornerRounding(0.085f)),
            PointNRound(Point(0.358f, 0.843f), CornerRounding(0.085f)),
        ),
        reps = 8,
    )

    private fun cookie4() = customPolygon(
        listOf(
            PointNRound(Point(1.237f, 1.236f), CornerRounding(0.258f)),
            PointNRound(Point(0.500f, 0.918f), CornerRounding(0.233f)),
        ),
        reps = 4,
    )

    private fun cookie6() = customPolygon(
        listOf(
            PointNRound(Point(0.723f, 0.884f), CornerRounding(0.394f)),
            PointNRound(Point(0.500f, 1.099f), CornerRounding(0.398f)),
        ),
        reps = 6,
    )

    private fun cookie7() = RoundedPolygon.star(
        numVerticesPerRadius = 7,
        innerRadius = 0.75f,
        rounding = cornerRound50,
    ).rotate(-90f)

    private fun cookie9() = RoundedPolygon.star(
        numVerticesPerRadius = 9,
        innerRadius = 0.8f,
        rounding = cornerRound50,
    ).rotate(-90f)

    private fun cookie12() = RoundedPolygon.star(
        numVerticesPerRadius = 12,
        innerRadius = 0.8f,
        rounding = cornerRound50,
    ).rotate(-90f)

    private fun ghostish() = customPolygon(
        listOf(
            PointNRound(Point(0.500f, 0f), CornerRounding(1.000f)),
            PointNRound(Point(1f, 0f), CornerRounding(1.000f)),
            PointNRound(Point(1f, 1.140f), CornerRounding(0.254f, 0.106f)),
            PointNRound(Point(0.575f, 0.906f), CornerRounding(0.253f)),
        ),
        reps = 1,
        mirroring = true,
    )

    private fun clover4() = customPolygon(
        listOf(
            PointNRound(Point(0.500f, 0.074f)),
            PointNRound(Point(0.725f, -0.099f), CornerRounding(0.476f)),
        ),
        reps = 4,
        mirroring = true,
    )

    private fun clover8() = customPolygon(
        listOf(
            PointNRound(Point(0.500f, 0.036f)),
            PointNRound(Point(0.758f, -0.101f), CornerRounding(0.209f)),
        ),
        reps = 8,
    )

    private fun burst() = customPolygon(
        listOf(
            PointNRound(Point(0.500f, -0.006f), CornerRounding(0.006f)),
            PointNRound(Point(0.592f, 0.158f), CornerRounding(0.006f)),
        ),
        reps = 12,
    )

    private fun softBurst() = customPolygon(
        listOf(
            PointNRound(Point(0.193f, 0.277f), CornerRounding(0.053f)),
            PointNRound(Point(0.176f, 0.055f), CornerRounding(0.053f)),
        ),
        reps = 10,
    )

    private fun boom() = customPolygon(
        listOf(
            PointNRound(Point(0.457f, 0.296f), CornerRounding(0.007f)),
            PointNRound(Point(0.500f, -0.051f), CornerRounding(0.007f)),
        ),
        reps = 15,
    )

    private fun softBoom() = customPolygon(
        listOf(
            PointNRound(Point(0.733f, 0.454f)),
            PointNRound(Point(0.839f, 0.437f), CornerRounding(0.532f)),
            PointNRound(Point(0.949f, 0.449f), CornerRounding(0.439f, 1.000f)),
            PointNRound(Point(0.998f, 0.478f), CornerRounding(0.174f)),
        ),
        reps = 16,
        mirroring = true,
    )

    private fun flower() = customPolygon(
        listOf(
            PointNRound(Point(0.370f, 0.187f)),
            PointNRound(Point(0.416f, 0.049f), CornerRounding(0.381f)),
            PointNRound(Point(0.479f, 0.001f), CornerRounding(0.095f)),
        ),
        reps = 8,
        mirroring = true,
    )

    private fun puffy(): RoundedPolygon = customPolygon(
        listOf(
            PointNRound(Point(0.500f, 0.053f)),
            PointNRound(Point(0.545f, -0.040f), CornerRounding(0.405f)),
            PointNRound(Point(0.670f, -0.035f), CornerRounding(0.426f)),
            PointNRound(Point(0.717f, 0.066f), CornerRounding(0.574f)),
            PointNRound(Point(0.722f, 0.128f)),
            PointNRound(Point(0.777f, 0.002f), CornerRounding(0.360f)),
            PointNRound(Point(0.914f, 0.149f), CornerRounding(0.660f)),
            PointNRound(Point(0.926f, 0.289f), CornerRounding(0.660f)),
            PointNRound(Point(0.881f, 0.346f)),
            PointNRound(Point(0.940f, 0.344f), CornerRounding(0.126f)),
            PointNRound(Point(1.003f, 0.437f), CornerRounding(0.255f)),
        ),
        reps = 2,
        mirroring = true,
    ).scale(1f, 0.742f)

    private fun puffyDiamond() = customPolygon(
        listOf(
            PointNRound(Point(0.870f, 0.130f), CornerRounding(0.146f)),
            PointNRound(Point(0.818f, 0.357f)),
            PointNRound(Point(1.000f, 0.332f), CornerRounding(0.853f)),
        ),
        reps = 4,
        mirroring = true,
    )

    private fun pixelCircle() = customPolygon(
        listOf(
            PointNRound(Point(0.500f, 0.000f)),
            PointNRound(Point(0.704f, 0.000f)),
            PointNRound(Point(0.704f, 0.065f)),
            PointNRound(Point(0.843f, 0.065f)),
            PointNRound(Point(0.843f, 0.148f)),
            PointNRound(Point(0.926f, 0.148f)),
            PointNRound(Point(0.926f, 0.296f)),
            PointNRound(Point(1.000f, 0.296f)),
        ),
        reps = 2,
        mirroring = true,
    )

    private fun pixelTriangle() = customPolygon(
        listOf(
            PointNRound(Point(0.110f, 0.500f)),
            PointNRound(Point(0.113f, 0.000f)),
            PointNRound(Point(0.287f, 0.000f)),
            PointNRound(Point(0.287f, 0.087f)),
            PointNRound(Point(0.421f, 0.087f)),
            PointNRound(Point(0.421f, 0.170f)),
            PointNRound(Point(0.560f, 0.170f)),
            PointNRound(Point(0.560f, 0.265f)),
            PointNRound(Point(0.674f, 0.265f)),
            PointNRound(Point(0.675f, 0.344f)),
            PointNRound(Point(0.789f, 0.344f)),
            PointNRound(Point(0.789f, 0.439f)),
            PointNRound(Point(0.888f, 0.439f)),
        ),
        reps = 1,
        mirroring = true,
    )

    private fun bun() = customPolygon(
        listOf(
            PointNRound(Point(0.796f, 0.500f)),
            PointNRound(Point(0.853f, 0.518f), CornerRounding(1f)),
            PointNRound(Point(0.992f, 0.631f), CornerRounding(1f)),
            PointNRound(Point(0.968f, 1.000f), CornerRounding(1f)),
        ),
        reps = 2,
        mirroring = true,
    )

    private fun heart() = customPolygon(
        listOf(
            PointNRound(Point(0.500f, 0.268f), CornerRounding(0.016f)),
            PointNRound(Point(0.792f, -0.066f), CornerRounding(0.958f)),
            PointNRound(Point(1.064f, 0.276f), CornerRounding(1.000f)),
            PointNRound(Point(0.501f, 0.946f), CornerRounding(0.129f)),
        ),
        reps = 1,
        mirroring = true,
    )

    private fun customPolygon(
        points: List<PointNRound>,
        reps: Int,
        centerX: Float = 0.5f,
        centerY: Float = 0.5f,
        mirroring: Boolean = false,
    ): RoundedPolygon {
        val repeated = repeatPoints(points, reps, centerX, centerY, mirroring)
        val vertices = FloatArray(repeated.size * 2)
        repeated.forEachIndexed { index, point ->
            vertices[index * 2] = point.point.x
            vertices[index * 2 + 1] = point.point.y
        }
        return RoundedPolygon(
            vertices = vertices,
            perVertexRounding = repeated.map { it.rounding },
            centerX = centerX,
            centerY = centerY,
        )
    }

    private fun repeatPoints(
        points: List<PointNRound>,
        reps: Int,
        centerX: Float,
        centerY: Float,
        mirroring: Boolean,
    ): List<PointNRound> {
        if (!mirroring) {
            return (0 until points.size * reps).map { index ->
                val point = points[index % points.size]
                val angle = ((index / points.size) * 360f / reps).toRadians()
                val dx = point.point.x - centerX
                val dy = point.point.y - centerY
                PointNRound(
                    Point(
                        dx * cos(angle) - dy * sin(angle) + centerX,
                        dx * sin(angle) + dy * cos(angle) + centerY,
                    ),
                    point.rounding,
                )
            }
        }

        val angles = points.map { point ->
            atan2(point.point.y - centerY, point.point.x - centerX) * 180f / PI.toFloat()
        }
        val distances = points.map { point ->
            val dx = point.point.x - centerX
            val dy = point.point.y - centerY
            sqrt(dx * dx + dy * dy)
        }
        val actualReps = reps * 2
        val sectionAngle = 360f / actualReps
        return buildList {
            repeat(actualReps) { section ->
                points.indices.forEach { index ->
                    val pointIndex = if (section % 2 == 0) index else points.lastIndex - index
                    if (pointIndex > 0 || section % 2 == 0) {
                        val angle = (
                            sectionAngle * section + if (section % 2 == 0) {
                                angles[pointIndex]
                            } else {
                                sectionAngle - angles[pointIndex] + 2 * angles[0]
                            }
                        ).toRadians()
                        add(
                            PointNRound(
                                Point(
                                    cos(angle) * distances[pointIndex] + centerX,
                                    sin(angle) * distances[pointIndex] + centerY,
                                ),
                                points[pointIndex].rounding,
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun RoundedPolygon.rotate(degrees: Float): RoundedPolygon {
        val angle = degrees.toRadians()
        val c = cos(angle)
        val s = sin(angle)
        return transformed { x, y -> TransformResult(x * c - y * s, x * s + y * c) }
    }

    private fun RoundedPolygon.scale(xScale: Float, yScale: Float): RoundedPolygon =
        transformed { x, y -> TransformResult(x * xScale, y * yScale) }

    private fun Float.toRadians(): Float = this / 360f * 2f * PI.toFloat()

    private fun RoundedPolygon.toAndroidPath(): Path {
        val path = Path()
        val curves = cubics
        if (curves.isNotEmpty()) {
            path.moveTo(curves.first().anchor0X, curves.first().anchor0Y)
            curves.forEach { curve ->
                path.cubicTo(
                    curve.control0X,
                    curve.control0Y,
                    curve.control1X,
                    curve.control1Y,
                    curve.anchor1X,
                    curve.anchor1Y,
                )
            }
            path.close()
        }
        return path
    }
}
