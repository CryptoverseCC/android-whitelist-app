package io.userfeeds.whitelist

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.math.BigDecimal
import kotlin.properties.Delegates

class WhitelistAdapter(
        private val whitelist: List<WhitelistedRankingItem>,
        showBlacklisted: Boolean,
        private val onWhitelist: (WhitelistedRankingItem) -> Unit,
        private val onBlacklist: (WhitelistedRankingItem) -> Unit) : RecyclerView.Adapter<WhitelistAdapter.Holder>() {

    var showBlacklisted by Delegates.observable(showBlacklisted) { _, _, _ -> notifyDataSetChanged() }

    override fun getItemCount() = if (showBlacklisted) whitelist.size else whitelist.count { it.state != State.blacklisted }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.whitelist_item, parent, false)
        return Holder(itemView)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val items = if (showBlacklisted) whitelist else whitelist.filter { it.state != State.blacklisted }
        val item = items[position]
        val titleView = holder.itemView.findViewById(R.id.titleView) as TextView
        titleView.text = item.title
        val targetView = holder.itemView.findViewById(R.id.targetView) as TextView
        targetView.text = item.target
        val totalView = holder.itemView.findViewById(R.id.totalView) as TextView
        totalView.text = item.total.toPlainString()
        val whitelistButton = holder.itemView.findViewById(R.id.whitelistButton)
        whitelistButton.setOnClickListener { whitelist(position, item) }
        when (item.state) {
            State.unknown -> whitelistButton.visibility = View.VISIBLE
            State.whitelisted -> whitelistButton.visibility = View.INVISIBLE
            State.blacklisted -> whitelistButton.visibility = View.VISIBLE
        }
        val blacklistButton = holder.itemView.findViewById(R.id.blacklistButton)
        blacklistButton.setOnClickListener { blacklist(position, item) }
        when (item.state) {
            State.unknown -> blacklistButton.visibility = View.VISIBLE
            State.whitelisted -> blacklistButton.visibility = View.INVISIBLE
            State.blacklisted -> blacklistButton.visibility = View.INVISIBLE
        }
        val color = when {
            item.state == State.unknown && item.total > BigDecimal.ZERO -> 0xFFCCCCCC.toInt()
            item.state == State.whitelisted -> 0xFFFFFFFF.toInt()
            item.state == State.blacklisted || item.state == State.unknown -> 0xFFFF0000.toInt()
            else -> error("error")
        }
        holder.itemView.setBackgroundColor(color)
    }

    private fun whitelist(position: Int, item: WhitelistedRankingItem) {
        item.state = State.whitelisted
        notifyItemChanged(position)
        onWhitelist(item)
    }

    private fun blacklist(position: Int, item: WhitelistedRankingItem) {
        item.state = State.blacklisted
        notifyItemChanged(position)
        onBlacklist(item)
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
