package io.userfeeds.whitelist

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.Toast
import com.squareup.moshi.Moshi
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.userfeeds.sdk.core.UserfeedsService
import io.userfeeds.sdk.core.algorithm.Algorithm
import io.userfeeds.sdk.core.ranking.RankingItem
import io.userfeeds.sdk.core.storage.Claim
import io.userfeeds.sdk.core.storage.ClaimWrapper
import io.userfeeds.sdk.core.storage.Signature
import kotlinx.android.synthetic.main.whitelist_activity.*
import kotlin.LazyThreadSafetyMode.NONE

class WhitelistActivity : AppCompatActivity() {

    private val userfeedId by lazy(NONE) { intent.getStringExtra("userfeedId") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.whitelist_activity)
        requestPublicKeyFromIdentity()
        showBlacklisted.setOnCheckedChangeListener { buttonView, isChecked ->
            val adapter = whitelistedLinksView.adapter
            if (adapter is WhitelistAdapter) {
                adapter.showBlacklisted = isChecked
            }
        }
    }

    private fun requestPublicKeyFromIdentity() {
        val intent = Intent("io.userfeeds.identity.SIGN_MESSAGE")
        intent.putExtra("io.userfeeds.identity.message", "Please select 'don't ask again'.")
        startActivityForResult(intent, PUBLIC_KEY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PUBLIC_KEY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val whitelist = Signature.fromIntentData(data!!).creator
            callRankingWithAndWithoutWhitelist(whitelist)
            return
        } else if (requestCode == SIGN_REQUEST_CODE && resultCode == RESULT_OK) {
            val signature = Signature.fromIntentData(data!!)
            val requestId = data.getStringExtra("io.userfeeds.identity.requestId")
            sendClaim(requestId, signature)
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun callRankingWithAndWithoutWhitelist(whitelist: String) {
        Single.zip(
                UserfeedsService.get().getRanking(userfeedId, Algorithm("links", ""), null),
                UserfeedsService.get().getRanking(userfeedId, Algorithm("links", ""), whitelist),
                BiFunction(this::zipToSingleList))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onSuccess, this::onError)
    }

    private fun zipToSingleList(all: List<RankingItem>, whitelisted: List<RankingItem>): List<WhitelistedRankingItem> {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val blacklistedIds = prefs.getStringSet("blacklist", emptySet())
        val whitelistedIds: Set<String> = whitelisted.map { it.id!! }.toSet()
        return all.map {
            val state = when (it.id!!) {
                in whitelistedIds -> State.whitelisted
                in blacklistedIds -> State.blacklisted
                else -> State.unknown
            }
            WhitelistedRankingItem(
                    id = it.id!!,
                    target = it.target,
                    score = it.score,
                    total = it.total!!,
                    title = it.title,
                    summary = it.summary,
                    state = state)
        }
    }

    private fun onSuccess(whitelist: List<WhitelistedRankingItem>) {
        whitelistedLinksView.layoutManager = LinearLayoutManager(this)
        whitelistedLinksView.adapter = WhitelistAdapter(whitelist, showBlacklisted.isChecked, this::onWhitelist, this::onBlacklist)
    }

    private fun onWhitelist(item: WhitelistedRankingItem) {
        val intent = Intent("io.userfeeds.identity.SIGN_MESSAGE")
                .putExtra("io.userfeeds.identity.message", createClaimWrapper(item.id).toJson())
                .putExtra("io.userfeeds.identity.requestId", item.id)
        startActivityForResult(intent, SIGN_REQUEST_CODE)
    }

    private fun createClaimWrapper(itemId: String) = ClaimWrapper.create(
            context = userfeedId,
            type = emptyList(),
            claim = Claim(
                    target = itemId
            ),
            clientId = "android:io.userfeeds.whitelist")

    private inline fun <reified T> T.toJson(): String {
        return Moshi.Builder()
                .build()
                .adapter(T::class.java)
                .toJson(this)
    }

    private fun sendClaim(itemId: String, signature: Signature) {
        UserfeedsService.get().putClaim(createClaimWrapper(itemId), signature)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onSuccess, this::onError)
    }

    private fun onSuccess() {
        Toast.makeText(this, "Whitelisted!", Toast.LENGTH_SHORT).show()
        Log.i("WhitelistActivity", "share success")
    }

    private fun onError2(error: Throwable) {
        Toast.makeText(this, "Error while whitelisting!", Toast.LENGTH_SHORT).show()
        Log.e("WhitelistActivity", "error", error)
    }

    private fun onBlacklist(item: WhitelistedRankingItem) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val blacklist = prefs.getStringSet("blacklist", emptySet())
        val newBlacklist = blacklist + item.id
        prefs.edit()
                .putStringSet("blacklist", newBlacklist)
                .apply()
    }

    private fun onError(error: Throwable) {
        Toast.makeText(this, "Error loading ranking!", Toast.LENGTH_SHORT).show()
        Log.e("TAG", "error", error)
    }

    companion object {

        private const val PUBLIC_KEY_REQUEST_CODE = 1001
        private const val SIGN_REQUEST_CODE = 1002

        fun start(context: Context, userfeedId: String) {
            val intent = Intent(context, WhitelistActivity::class.java)
            intent.putExtra("userfeedId", userfeedId)
            context.startActivity(intent)
        }
    }
}
