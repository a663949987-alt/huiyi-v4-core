package com.huiyi.v4

import com.huiyi.v4.update.UpdateRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateManifestParseTest {
    @Test
    fun latestJsonCanParse() {
        val raw = """{"versionName":"4.0-core","versionCode":1,"apkUrl":"https://example.com/app.apk","releaseNotes":"init","forceUpdate":false,"sha256":"abc","createdAt":1}"""

        val manifest = UpdateRepository("").parseLatest(raw)

        assertEquals("4.0-core", manifest.versionName)
        assertEquals(1, manifest.versionCode)
    }
}
