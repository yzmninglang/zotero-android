package org.zotero.android.sync

import org.zotero.android.database.objects.AnnotationsConfig
import org.zotero.android.ktx.rounded

interface SplittablePathPoint {
    val x: Double
    val y: Double
}

object AnnotationSplitter {
    fun splitRectsIfNeeded(rects: List<CGRect>): List<List<CGRect>>? {
        if (rects.isEmpty()) {
            return null
        }
        val sortedRects = rects.sortedWith { lRect, rRect ->
            if (lRect.minY == rRect.minY) {
                return@sortedWith rRect.minX.compareTo(lRect.minX)
            }
            return@sortedWith lRect.minY.compareTo(rRect.minY)
        }

        var count = 2
        val splitRects = mutableListOf<List<CGRect>>()
        var currentRects = mutableListOf<CGRect>()

        for (rect in sortedRects) {
            val size =
                rect.minX.rounded(3).toString().length +
                        rect.minY.rounded(3).toString().length +
                        rect.maxX.rounded(3).toString().length +
                        rect.maxY.rounded(3)
                            .toString().length + 6

            if (count + size > AnnotationsConfig.positionSizeLimit) {
                if (currentRects.isNotEmpty()) {
                    splitRects.add(currentRects)
                    currentRects = mutableListOf()
                }
                count = 2
            }
            currentRects.add(rect)
            count += size
        }

        if (currentRects.isNotEmpty()) {
            splitRects.add(currentRects)
        }

        if (splitRects.size == 1) {
            return null
        }
        return splitRects
    }

    fun splitPathsIfNeeded(paths: List<List<SplittablePathPoint>>): List<List<List<SplittablePathPoint>>>? {
        if (paths.isEmpty()) {
            return null
        }

        var count = 2
        val splitPaths = mutableListOf<MutableList<MutableList<SplittablePathPoint>>>()
        var currentLines = mutableListOf<MutableList<SplittablePathPoint>>()
        var currentPoints = mutableListOf<SplittablePathPoint>()

        for (subpaths in paths) {
            if (count + 3 > AnnotationsConfig.positionSizeLimit) {
                if (currentPoints.isNotEmpty()) {
                    currentLines.add(currentPoints)
                    currentPoints = mutableListOf()
                }
                if (currentLines.isNotEmpty()) {
                    splitPaths.add(currentLines)
                    currentLines = mutableListOf()
                }
                count = 2
            }
            count += 3
            for (point in subpaths) {
                val size =
                    point.x.rounded(3).toString().length +
                            point.y.rounded(3).toString().length + 2

                if (count + size > AnnotationsConfig.positionSizeLimit) {
                    if (currentPoints.isNotEmpty()) {
                        currentLines.add(currentPoints)
                        currentPoints = mutableListOf()
                    }
                    if (currentLines.isNotEmpty()) {
                        splitPaths.add(currentLines)
                        currentLines = mutableListOf()
                    }
                    count = 5
                }
                count += size
                currentPoints.add(point)
            }

            currentLines.add(currentPoints)
            currentPoints = mutableListOf()
        }

        if (currentPoints.isNotEmpty()) {
            currentLines.add(currentPoints)
        }
        if (currentLines.isNotEmpty()) {
            splitPaths.add(currentLines)
        }
        if (splitPaths.size == 1) {
            return null
        }
        return splitPaths
    }
}