package com.example.landscapedesign.geometry

import com.example.landscapedesign.model.Point3D

object GeometryUtils {
    /**
     * Calculates the area of a polygon projected onto the XZ (ground) plane
     * using the Shoelace formula (Gauss's area formula).
     */
    fun shoelaceArea(vertices: List<Point3D>): Float {
        if (vertices.size < 3) return 0f
        var area = 0.0
        val n = vertices.size
        for (i in 0 until n) {
            val current = vertices[i]
            val next = vertices[(i + 1) % n]
            area += (current.x.toDouble() * next.z.toDouble()) - (next.x.toDouble() * current.z.toDouble())
        }
        return (kotlin.math.abs(area) / 2.0).toFloat()
    }
}
