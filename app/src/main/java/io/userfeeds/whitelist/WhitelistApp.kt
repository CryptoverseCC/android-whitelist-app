package io.userfeeds.whitelist

import android.app.Application
import io.userfeeds.sdk.core.UserfeedsSdk

class WhitelistApp : Application() {

    override fun onCreate() {
        super.onCreate()
        UserfeedsSdk.initialize(
                apiKey = "59049c8fdfed920001508e2a94bad07aa8f846674ae92e8765bd926c",
                debug = BuildConfig.DEBUG)
    }
}