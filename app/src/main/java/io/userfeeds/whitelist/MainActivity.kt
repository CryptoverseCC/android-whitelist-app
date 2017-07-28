package io.userfeeds.whitelist

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        contextsView.layoutManager = LinearLayoutManager(this)
        fab.setOnClickListener { startActivity(Intent(this, AddContextActivity::class.java)) }
    }

    override fun onResume() {
        super.onResume()
        contextsView.adapter = ContextsAdapter(ContextsRepository(this).contexts)
    }
}
