package hw.first.calcucator

import android.R.attr.label
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


const val BUNDLE_KEY1 = "MY_COUNT_KEY"
const val BUNDLE_KEY2 = "MY_STR_KEY"
const val BUNDLE_KEY3 = "MY_CUR_EXPRESSION_KEY"
const val BUNDLE_KEY4 = "MY_CUR_SIGN_KEY"
const val BUNDLE_KEY5 = "MY_HAS_MINUS_KEY"

class MainActivity : AppCompatActivity() {
    private val calc = Calculate()
    private lateinit var display1: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button = arrayOf(findViewById<Button>(R.id.button_0), findViewById(R.id.button_1), findViewById(R.id.button_2),
            findViewById(R.id.button_3), findViewById(R.id.button_4), findViewById(R.id.button_5),
            findViewById(R.id.button_6), findViewById(R.id.button_7), findViewById(R.id.button_8), findViewById(R.id.button_9))
        display1 = findViewById(R.id.disp)
        for (i in 0..9) {
            button[i].setOnClickListener {
                display1.text = calc.number(button[i].text.toString())
            }
        }
        val copy = findViewById<Button>(R.id.copy)
        copy.setOnClickListener {
            val clipboard: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("calc copy text", display1.text)
            clipboard.setPrimaryClip(clip)
        }
        val plus = findViewById<Button>(R.id.plus)
        plus.setOnClickListener {
            display1.text = calc.sum()
        }

        val minus = findViewById<Button>(R.id.minus)
        minus.setOnClickListener {
            display1.text = calc.sub()
        }

        val divide = findViewById<Button>(R.id.div)
        divide.setOnClickListener {
            display1.text = calc.div()
        }

        val multiply = findViewById<Button>(R.id.mult)
        multiply.setOnClickListener {
            display1.text = calc.multiply()
        }

        val delete = findViewById<Button>(R.id.del)
        delete.setOnClickListener {
            display1.text = calc.del()
        }

        val equal = findViewById<Button>(R.id.equal)
        equal.setOnClickListener {
            display1.text = calc.equal()
        }

        val point = findViewById<Button>(R.id.point)
        point.setOnClickListener {
            display1.text = calc.point()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putDouble(BUNDLE_KEY1, calc.count)
        outState.putString(BUNDLE_KEY2, calc.str)
        outState.putString(BUNDLE_KEY3, calc.curExpression)
        outState.putString(BUNDLE_KEY4, calc.curSign)
        outState.putBoolean(BUNDLE_KEY5, calc.hasMinus)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        calc.count = savedInstanceState.getDouble(BUNDLE_KEY1, calc.count)
        calc.str = savedInstanceState.getString(BUNDLE_KEY2, calc.str)
        calc.curExpression = savedInstanceState.getString(BUNDLE_KEY3, calc.curExpression)
        calc.curSign = savedInstanceState.getString(BUNDLE_KEY4, calc.curSign)
        calc.hasMinus = savedInstanceState.getBoolean(BUNDLE_KEY5, calc.hasMinus)
        display1.text = String.format(calc.str)
    }
}