package hw.first.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView


class MainActivity : AppCompatActivity() {
    private lateinit var err : TextView
    var id = 0

    companion object {
        private const val BUNDLE_KEY = "MY_ERROR_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val log = findViewById<Button>(R.id.login)
        val name = findViewById<EditText>(R.id.enter)
        val pass1 = findViewById<EditText>(R.id.password1)
        err = findViewById(R.id.error)
        log.setOnClickListener {
            if (name.text.isEmpty()) {
                if (pass1.text.isEmpty()) {
                    err.text = getString(R.string.emptyLP)
                    id = R.string.emptyLP
                } else {
                    err.text = getString(R.string.emptyL)
                    id = R.string.emptyL
                }
            } else {
                if (pass1.text.isEmpty()) {
                    err.text = getString(R.string.emptyP)
                    id = R.string.emptyP
                } else {
                    err.text = getString(R.string.errLP)
                    id = R.string.errLP
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(BUNDLE_KEY, id)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        id = savedInstanceState.getInt(BUNDLE_KEY, id)
        if (id != 0) {
            err.text = getString(id)
        } else {
            err.text = ""
        }
    }
}
