package com.example.a123

import kotlin.math.pow

object cropRegressionCalculator {

    // Function to calculate linear regression for pest data
    fun calculateLinearRegression(pestData: List<PestData>): Triple<Float, Float, Float> {
        val avgPestCount = pestData.map { it.pestCount }.average().toFloat()
        val avgTemperature = pestData.map { it.temperature }.average().toFloat()
        val avgPrecipitation = pestData.map { it.precipitation }.average().toFloat()

        var numeratorTemp = 0f
        var numeratorPrec = 0f
        var denominatorTemp = 0f
        var denominatorPrec = 0f

        for (data in pestData) {
            numeratorTemp += (data.temperature - avgTemperature) * (data.pestCount - avgPestCount)
            denominatorTemp += (data.temperature - avgTemperature).pow(2)

            numeratorPrec += (data.precipitation - avgPrecipitation) * (data.pestCount - avgPestCount)
            denominatorPrec += (data.precipitation - avgPrecipitation).pow(2)
        }

        val betaTemp = if (denominatorTemp != 0f) numeratorTemp / denominatorTemp else 0f
        val betaPrec = if (denominatorPrec != 0f) numeratorPrec / denominatorPrec else 0f
        val beta0 = avgPestCount - (betaTemp * avgTemperature) - (betaPrec * avgPrecipitation)

        return Triple(beta0, betaTemp, betaPrec)
    }
}
