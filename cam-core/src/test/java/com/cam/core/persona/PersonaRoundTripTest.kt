package com.cam.core.persona

import kotlin.test.*

class PersonaRoundTripTest {

    @Test
    fun `round trip preserves all fields`() {
        val p = Persona(
            personaId = "nl-test-001",
            deviceModel = "SM-A546B",
            manufacturer = "samsung",
            androidVersion = 34,
            securityPatch = "2024-06-01",
            feedImagePath = "personas/nl-test-001/p.jpg"
        )
        val json = PersonaLoader.toJson(p)
        val p2 = PersonaLoader.fromJson(json)
        assertEquals(p, p2)
    }

    @Test
    fun `loader fails fast on garbage`() {
        assertFailsWith<Exception> { PersonaLoader.fromJson("{ not json") }
    }

    @Test
    fun `validator rejects blank model`() {
        val p = Persona("x", "", "", 34, "", feedImagePath = "a.jpg")
        assertTrue(PersonaValidator.validate(p).isNotEmpty())
    }

    @Test
    fun `validator enforces exactly one feed source`() {
        val neither = Persona("x", "M", "m", 34, "2024-06-01")
        assertTrue(PersonaValidator.validate(neither).isNotEmpty())

        val both = Persona("x", "M", "m", 34, "2024-06-01",
            feedVideoPath = "v.mp4", feedImagePath = "i.jpg")
        assertTrue(PersonaValidator.validate(both).isNotEmpty())
    }

    @Test
    fun `validator rejects bad orientation`() {
        val p = Persona("x", "M", "m", 34, "2024-06-01",
            cameraSensorOrientation = 45, feedImagePath = "i.jpg")
        assertTrue(PersonaValidator.validate(p).isNotEmpty())
    }

    @Test
    fun `valid persona passes`() {
        val p = Persona("x", "SM-A546B", "samsung", 34, "2024-06-01",
            feedImagePath = "i.jpg")
        assertTrue(PersonaValidator.isValid(p))
    }
}
