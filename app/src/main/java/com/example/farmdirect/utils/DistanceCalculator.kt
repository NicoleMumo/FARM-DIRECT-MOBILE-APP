package com.example.farmdirect.utils

import kotlin.math.*

object DistanceCalculator {
    // Nairobi center coordinates (default warehouse/farm location)
    private const val WAREHOUSE_LAT = -1.2921
    private const val WAREHOUSE_LON = 36.8219
    
    /**
     * Calculate distance between two points using Haversine formula
     * Returns distance in kilometers
     */
    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double = WAREHOUSE_LAT,
        lon2: Double = WAREHOUSE_LON
    ): Double {
        val earthRadius = 6371.0 // Earth radius in kilometers
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return earthRadius * c
    }
    
    /**
     * Calculate delivery fee based on distance
     * Base fee: 50 KSh
     * Additional: 10 KSh per km after first 5km
     */
    fun calculateDeliveryFee(distanceKm: Double): Double {
        val baseFee = 50.0
        val freeDistance = 5.0 // First 5km included in base fee
        
        return if (distanceKm <= freeDistance) {
            baseFee
        } else {
            val additionalKm = distanceKm - freeDistance
            baseFee + (additionalKm * 10.0)
        }
    }
    
    /**
     * Get coordinates for a location name (simple lookup for common Nairobi areas)
     * In production, use Geocoding API
     */
    fun getCoordinatesForLocation(locationName: String): Pair<Double, Double> {
        // Simple lookup for common Nairobi areas
        val locations = mapOf(
            "kilimani" to Pair(-1.2921, 36.8219),
            "westlands" to Pair(-1.2634, 36.8025),
            "karen" to Pair(-1.3194, 36.7078),
            "lavington" to Pair(-1.2756, 36.7731),
            "parklands" to Pair(-1.2608, 36.8083),
            "westlands" to Pair(-1.2634, 36.8025),
            "runda" to Pair(-1.2500, 36.7800),
            "kileleshwa" to Pair(-1.2833, 36.7917),
            "hurlingham" to Pair(-1.3000, 36.7833),
            "nairobi" to Pair(-1.2921, 36.8219)
        )
        
        val lowerName = locationName.lowercase()
        return locations.entries.firstOrNull { 
            lowerName.contains(it.key, ignoreCase = true) 
        }?.value ?: Pair(WAREHOUSE_LAT, WAREHOUSE_LON)
    }
}

