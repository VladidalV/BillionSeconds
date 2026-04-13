package com.example.billionseconds.domain

object MilestoneConfig {
    val definitions: List<MilestoneDefinition> = listOf(
        MilestoneDefinition(
            id               = "100m",
            secondsThreshold = 100_000_000L,
            title            = "100 миллионов секунд",
            shortTitle       = "100 млн",
            isPrimary        = false,
            isShareable      = true
        ),
        MilestoneDefinition(
            id               = "250m",
            secondsThreshold = 250_000_000L,
            title            = "250 миллионов секунд",
            shortTitle       = "250 млн",
            isPrimary        = false,
            isShareable      = true
        ),
        MilestoneDefinition(
            id               = "500m",
            secondsThreshold = 500_000_000L,
            title            = "Полмиллиарда секунд",
            shortTitle       = "500 млн",
            isPrimary        = false,
            isShareable      = true
        ),
        MilestoneDefinition(
            id               = "750m",
            secondsThreshold = 750_000_000L,
            title            = "750 миллионов секунд",
            shortTitle       = "750 млн",
            isPrimary        = false,
            isShareable      = true
        ),
        MilestoneDefinition(
            id               = "1b",
            secondsThreshold = 1_000_000_000L,
            title            = "Миллиард секунд",
            shortTitle       = "1 млрд",
            isPrimary        = true,
            isShareable      = true
        )
    )
}
