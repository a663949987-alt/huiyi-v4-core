package com.huiyi.v4.domain.simulation

import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.TacticalDecisionType
import com.huiyi.v4.domain.model.VisualBounds

object AccessibilityNodeFixtureCatalog {
    fun requiredFixtures(): List<AccessibilityNodeFixture> = listOf(
        liaoqiLastOtherPass(),
        liaoqiLastMeWait(),
        liaoqiPostPanelContaminated(),
        liaoqiReadReceiptStatus(),
        genericTimeMetadataTrap(),
        unsupportedAppNoChatRows()
    )

    private fun liaoqiLastOtherPass(): AccessibilityNodeFixture = fixture(
        category = FixtureCategory.LIAOQI_LAST_OTHER_PASS,
        name = "liaoqi_last_other_pass",
        appPackage = "com.bajiao.im.liaoqi",
        windowTitle = "Liaoqi chat",
        expectedLastSpeaker = Speaker.OTHER,
        expectedDecisionType = TacticalDecisionType.NORMAL_REPLY,
        expectedRouteCount = 5,
        expectedPanelState = PanelState.ROUTE_PANEL,
        rows = listOf(
            center("title", "\u767d\u4e91\u84dd\u5929"),
            center("time1", "10:56"),
            left("o1", "Today was a little heavy."),
            right("m1", "I am listening."),
            left("o2", "I just want someone steady to hear me."),
            right("m2", "Take your time."),
            left("o3", "Then can you stay with this topic for a bit?"),
            right("m3", "I can stay with it."),
            left("o4", "That makes me feel a little safer.")
        )
    )

    private fun liaoqiLastMeWait(): AccessibilityNodeFixture = fixture(
        category = FixtureCategory.LIAOQI_LAST_ME_WAIT,
        name = "liaoqi_last_me_wait",
        appPackage = "com.bajiao.im.liaoqi",
        windowTitle = "Liaoqi chat",
        expectedLastSpeaker = Speaker.ME,
        expectedDecisionType = TacticalDecisionType.WAIT,
        expectedRouteCount = 0,
        expectedPanelState = PanelState.WAIT_PANEL,
        rows = listOf(
            center("time1", "11:00"),
            left("o1", "I am busy today."),
            right("m1", "No rush, finish your work first."),
            left("o2", "Maybe later."),
            right("m2", "Okay, I will wait for you.")
        )
    )

    private fun liaoqiPostPanelContaminated(): AccessibilityNodeFixture = fixture(
        category = FixtureCategory.LIAOQI_POST_PANEL_CONTAMINATED,
        name = "liaoqi_post_panel_contaminated",
        appPackage = "com.bajiao.im.liaoqi",
        windowTitle = "Huiyi Radar: last message is me, wait panel, send to GPT",
        expectedLastSpeaker = null,
        expectedDecisionType = TacticalDecisionType.PRE_ANALYSIS_CONTAMINATED,
        expectedRouteCount = 0,
        expectedPanelState = PanelState.CONTROLLED_FAIL_PANEL,
        rows = listOf(
            center("overlay-title", "Huiyi Radar"),
            center("overlay-copy", "last message is me, wait"),
            left("o1", "Did you get home?"),
            right("m1", "Yes, just arrived.")
        ),
        notes = "window title intentionally contains Huiyi overlay text"
    )

    private fun liaoqiReadReceiptStatus(): AccessibilityNodeFixture = fixture(
        category = FixtureCategory.LIAOQI_READ_RECEIPT_STATUS,
        name = "liaoqi_read_receipt_status",
        appPackage = "com.bajiao.im.liaoqi",
        windowTitle = "Liaoqi chat",
        expectedLastSpeaker = Speaker.ME,
        expectedDecisionType = TacticalDecisionType.WAIT,
        expectedRouteCount = 0,
        expectedPanelState = PanelState.WAIT_PANEL,
        rows = listOf(
            center("time1", "11:08"),
            left("o1", "I will be offline for a while."),
            right("m1", "Okay, I will not keep pushing."),
            rightSmall("status-read", "\u5df2\u8bfb"),
            rightSmall("status-check", "\u2713\u2713")
        )
    )

    private fun genericTimeMetadataTrap(): AccessibilityNodeFixture = fixture(
        category = FixtureCategory.GENERIC_TIME_METADATA_TRAP,
        name = "generic_time_metadata_trap",
        appPackage = "com.huiyi.mockchat",
        windowTitle = "Generic chat",
        expectedLastSpeaker = Speaker.OTHER,
        expectedDecisionType = TacticalDecisionType.CONTEXT_REQUIRED,
        expectedRouteCount = 0,
        expectedPanelState = PanelState.CONTEXT_REQUIRED_PANEL,
        rows = listOf(
            center("header", "\u767d\u4e91\u84dd\u5929"),
            center("status", "\u4e0a\u6b21\u5728\u7ebf\u65f6\u95f407-02 18:06"),
            center("date1", "07-02"),
            center("time1", "10:56"),
            left("o1", "I was quiet because the day was messy."),
            right("m1", "I understand."),
            center("time2", "11:00"),
            left("o2", "I still want to talk, just slowly."),
            center("time-bottom", "11:02")
        )
    )

    private fun unsupportedAppNoChatRows(): AccessibilityNodeFixture = AccessibilityNodeFixture(
        category = FixtureCategory.UNSUPPORTED_APP_NO_CHAT_ROWS,
        name = "unsupported_app_no_chat_rows",
        appPackage = "com.example.unsupported",
        windowTitle = "Unsupported page",
        screenWidth = 1080,
        screenHeight = 2400,
        nodes = emptyList(),
        expectedLastSpeaker = null,
        expectedDecisionType = TacticalDecisionType.CONTEXT_REQUIRED,
        expectedRouteCount = 0,
        expectedPanelState = PanelState.UNSUPPORTED_APP_PANEL,
        notes = "no visible chat rows"
    )

    private fun fixture(
        category: FixtureCategory,
        name: String,
        appPackage: String,
        windowTitle: String,
        expectedLastSpeaker: Speaker?,
        expectedDecisionType: TacticalDecisionType?,
        expectedRouteCount: Int?,
        expectedPanelState: PanelState,
        rows: List<RowSpec>,
        notes: String = ""
    ): AccessibilityNodeFixture {
        return AccessibilityNodeFixture(
            category = category,
            name = name,
            appPackage = appPackage,
            windowTitle = windowTitle,
            screenWidth = 1080,
            screenHeight = 2400,
            nodes = rows.mapIndexed { index, row -> row.toNode(index) },
            expectedLastSpeaker = expectedLastSpeaker,
            expectedDecisionType = expectedDecisionType,
            expectedRouteCount = expectedRouteCount,
            expectedPanelState = expectedPanelState,
            notes = notes
        )
    }

    private fun left(id: String, text: String): RowSpec = RowSpec(id, text, VisualBounds(72, 0, 510, 76), VisualBounds(0, 0, 680, 86))
    private fun right(id: String, text: String): RowSpec = RowSpec(id, text, VisualBounds(590, 0, 1018, 76), VisualBounds(400, 0, 1080, 86))
    private fun rightSmall(id: String, text: String): RowSpec = RowSpec(id, text, VisualBounds(842, 0, 1018, 42), VisualBounds(400, 0, 1080, 52))
    private fun center(id: String, text: String): RowSpec = RowSpec(id, text, VisualBounds(430, 0, 650, 42), VisualBounds(0, 0, 1080, 52))

    private data class RowSpec(
        val id: String,
        val text: String,
        val textBoundsTemplate: VisualBounds,
        val rowBoundsTemplate: VisualBounds
    ) {
        fun toNode(index: Int): FixtureNode {
            val top = 120 + index * 104
            val textBounds = textBoundsTemplate.shiftTop(top)
            val rowBounds = rowBoundsTemplate.shiftTop(top - 5)
            return FixtureNode(
                id = id,
                text = text,
                bounds = textBounds,
                parentBounds = rowBounds,
                ancestorBoundsChain = listOf(
                    VisualBounds(0, 0, 1080, 2400),
                    rowBounds,
                    textBounds
                )
            )
        }

        private fun VisualBounds.shiftTop(top: Int): VisualBounds {
            val height = bottom - this.top
            return VisualBounds(left, top, right, top + height)
        }
    }
}
