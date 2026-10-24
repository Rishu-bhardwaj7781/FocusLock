package com.focuslock

/** blockAll = block every app except essentials; otherwise block only the listed packages. */
data class Mode(val key: String, val label: String, val blockAll: Boolean, val blocked: Set<String>)

val SOCIAL = setOf(
    "com.instagram.android", "com.snapchat.android", "com.twitter.android", "com.facebook.katana",
    "com.facebook.orca", "com.whatsapp", "org.telegram.messenger", "com.reddit.frontpage",
    "com.zhiliaoapp.musically", "com.pinterest", "com.discord"
)
val VIDEO = setOf(
    "com.google.android.youtube", "com.netflix.mediaclient", "in.startv.hotstar",
    "com.amazon.avod.thirdpartyclient", "com.mxtech.videoplayer.ad", "com.spotify.music"
)
val GAMES = setOf(
    "com.supercell.clashofclans", "com.supercell.brawlstars", "com.activision.callofduty.shooter",
    "com.tencent.ig", "com.pubg.imobile", "com.dts.freefireth", "com.roblox.client",
    "com.kiloo.subwaysurf", "com.king.candycrushsaga", "com.mojang.minecraftpe"
)

val MODES = listOf(
    Mode("study", "Study", false, SOCIAL + VIDEO + GAMES),
    Mode("reading", "Reading", false, SOCIAL + GAMES + setOf("com.google.android.youtube")),
    Mode("exam", "Exam Prep", true, emptySet()),
    Mode("deep", "Deep Work", true, emptySet()),
    Mode("custom", "Custom", false, emptySet())
)

/** Always blocked during a session so the lock cannot be switched off from Settings. */
val PROTECTED = setOf("com.android.settings", "com.google.android.packageinstaller", "com.android.packageinstaller")
