package io.userfeeds.whitelist

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.userfeeds.sdk.core.UserfeedsService
import io.userfeeds.sdk.core.algorithm.Algorithm
import io.userfeeds.sdk.core.ranking.RankingItem
import io.userfeeds.sdk.core.storage.Signature
import kotlinx.android.synthetic.main.whitelist_activity.*
import kotlin.LazyThreadSafetyMode.NONE

class WhitelistActivity : AppCompatActivity() {

    private val userfeedId by lazy(NONE) { intent.getStringExtra("userfeedId") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.whitelist_activity)
        requestPublicKeyFromIdentity()
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
        val whitelistedIds: Set<String> = whitelisted.map { it.id!! }.toSet()
        return all.map {
            WhitelistedRankingItem(
                    id = it.id!!,
                    target = it.target,
                    score = it.score,
                    total = it.total!!,
                    title = it.title,
                    summary = it.summary,
                    whitelisted = it.id!! in whitelistedIds)
        }
    }

    private fun onSuccess(whitelist: List<WhitelistedRankingItem>) {
        whitelistedLinksView.layoutManager = LinearLayoutManager(this)
        whitelistedLinksView.adapter = WhitelistAdapter(whitelist)
    }

    private fun onError(error: Throwable) {
        Log.e("TAG", "error", error)
    }

    companion object {

        private const val PUBLIC_KEY_REQUEST_CODE = 1001

        fun start(context: Context, userfeedId: String) {
            val intent = Intent(context, WhitelistActivity::class.java)
            intent.putExtra("userfeedId", userfeedId)
            context.startActivity(intent)
        }
    }
}
