package com.movtery.zalithlauncher.game.account

import org.junit.Assert.assertEquals
import org.junit.Test

class PassportServerIdTest {
    @Test fun removesTrailingSlashesFromPassportServerIds() {
        assertEquals("012345abcdef", getPassportServerId("https://auth.mc-user.com:233/012345abcdef/"))
        assertEquals("012345abcdef", getPassportServerId("https://auth.mc-user.com:233/012345abcdef///"))
        assertEquals("012345abcdef", getPassportServerId("https://auth.mc-user.com:233/012345abcdef"))
    }
}
