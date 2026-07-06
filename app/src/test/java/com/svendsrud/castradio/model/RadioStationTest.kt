package com.svendsrud.castradio.model

import org.junit.Assert.assertTrue
import org.junit.Test

class RadioStationTest {

    private val stations = StationRepository.stations

    @Test
    fun `station list is not empty`() {
        assertTrue(stations.isNotEmpty())
    }

    @Test
    fun `station names are unique`() {
        val names = stations.map { it.name }
        assertTrue("Duplicate station names: $names", names.size == names.toSet().size)
    }

    @Test
    fun `stream urls are unique and use https`() {
        val urls = stations.map { it.streamUrl }
        assertTrue("Duplicate stream URLs: $urls", urls.size == urls.toSet().size)
        stations.forEach { station ->
            assertTrue("${station.name} URL is not https: ${station.streamUrl}", station.streamUrl.startsWith("https://"))
        }
    }

    @Test
    fun `accent colors are valid 6-digit hex`() {
        val hexPattern = Regex("^#[0-9A-Fa-f]{6}$")
        stations.forEach { station ->
            assertTrue(
                "${station.name} has invalid accentColor: ${station.accentColor}",
                hexPattern.matches(station.accentColor)
            )
        }
    }

    @Test
    fun `content types are audio`() {
        stations.forEach { station ->
            assertTrue(
                "${station.name} has non-audio contentType: ${station.contentType}",
                station.contentType.startsWith("audio/")
            )
        }
    }
}
