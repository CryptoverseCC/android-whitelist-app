package io.userfeeds.whitelist

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class ContextsAdapter(private val contexts: List<String>) : RecyclerView.Adapter<ContextsAdapter.Holder>() {

    override fun getItemCount() = contexts.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false)
        return Holder(itemView)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val contextView = holder.itemView.findViewById(android.R.id.text1) as TextView
        contextView.text = contexts[position]
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
