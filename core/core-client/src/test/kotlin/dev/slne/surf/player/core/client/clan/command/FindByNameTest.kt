package dev.slne.surf.player.core.client.clan.command

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class FindByNameTest {
    private val members = listOf(1 to "DevAdmin", 2 to "DevP2a", 3 to "af2be922-3e41-3a18-bc08-d144d875737e")

    @Test
    fun `matches a member name ignoring case`() {
        assertEquals(2, findByName(members, "DevP2a"))
        assertEquals(2, findByName(members, "devp2a"))
        assertEquals(1, findByName(members, "DEVADMIN"))
    }

    @Test
    fun `an unknown name matches nothing`() {
        assertNull(findByName(members, "DevPlayer1"))
        assertNull(findByName(members, "DevP2"))
    }
}
