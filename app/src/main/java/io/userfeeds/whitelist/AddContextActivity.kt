package io.userfeeds.whitelist

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.add_context_activity.*

class AddContextActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_context_activity)
        submitView.setOnClickListener { submit() }
        scanQrCodeView.setOnClickListener { scanQrCode() }
    }

    private fun submit() {
        val context = contextView.text.toString().trim()
        if (context.isNotEmpty()) {
            addContext(context)
        }
    }

    private fun scanQrCode() {
        IntentIntegrator(this).initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (scanResult != null) {
            addContext(scanResult.contents)
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun addContext(context: String) {
        ContextsRepository(this).add(context)
        finish()
    }
}
