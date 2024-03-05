package hw.first.calcucator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import java.text.NumberFormat
import java.util.Locale

const val BUNDLE_KEY1 = "MY_COUNT_KEY"
const val BUNDLE_KEY2 = "MY_STR_KEY"
const val BUNDLE_KEY3 = "MY_CUR_EXPRESSION_KEY"
const val BUNDLE_KEY4 = "MY_CUR_SIGN_KEY"
const val BUNDLE_KEY5 = "MY_HAS_MUNIS_KEY"


val f = NumberFormat.getInstance(Locale("en"))

class MainActivity : AppCompatActivity() {
    private var count = 0.0
    private var str = ""
    private var curExpression = ""
    private var curSign = ""
    var has_minus = false
    private lateinit var display1: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        f.maximumFractionDigits = 10
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button = arrayOf(findViewById<Button>(R.id.button_0), findViewById(R.id.button_1), findViewById(R.id.button_2),
            findViewById(R.id.button_3), findViewById(R.id.button_4), findViewById(R.id.button_5),
            findViewById(R.id.button_6), findViewById(R.id.button_7), findViewById(R.id.button_8), findViewById(R.id.button_9))
        display1 = findViewById(R.id.disp)
        for (i in 0..9) {
            button[i].setOnClickListener {
                curExpression = curExpression.plus(button[i].text.toString())
                str = str.plus(button[i].text.toString())
                has_minus = false
                display1.text = str
            }
        }
        val plus = findViewById<Button>(R.id.plus)
        plus.setOnClickListener {
            if (curExpression != "" && curExpression != "-") {
                makeOperation(curSign)
                curSign = "+"
                curExpression = ""
                str = str.plus("+")
                display1.text = str
                has_minus = false
            }
        }

        val minus = findViewById<Button>(R.id.minus)
        minus.setOnClickListener {
            if (!has_minus) {
                if (curExpression != "" && curExpression != "-") {
                    makeOperation(curSign)
                    curSign = "-"
                    curExpression = ""
                    str = str.plus("-")
                    display1.text = str
                } else {
                    curExpression = curExpression.plus("-")
                    str = str.plus("-")
                    display1.text = str
                    has_minus = true
                }
            }
        }

        val divide = findViewById<Button>(R.id.div)
        divide.setOnClickListener {
            if (curExpression != "" && curExpression != "-") {
                makeOperation(curSign)
                curSign = "/"
                has_minus = false
                curExpression = ""
                str = str.plus("/")
                display1.text = str
            }
        }

        val multiply = findViewById<Button>(R.id.mult)
        multiply.setOnClickListener {
            if (curExpression != "" && curExpression != "-") {
                makeOperation(curSign)
                curSign = "*"
                has_minus = false
                curExpression = ""
                str = str.plus("*")
                display1.text = str
            }
        }

        val delete = findViewById<Button>(R.id.del)
        delete.setOnClickListener {
            count = 0.0
            str = ""
            curExpression = ""
            curSign = ""
            has_minus = false
            display1.text = ""
        }

        val equal = findViewById<Button>(R.id.equal)
        equal.setOnClickListener {
            if (curSign != "" && curExpression != "") {
                if (makeOperation(curSign) == "ERROR") {
                    del()
                    display1.text = String.format("ERROR")
                } else {
                    str = f.format(count).replace(",", "")
                    curSign = ""
                    has_minus = false
                    curExpression = str
                    display1.text = str
                }
            }
        }

        val point = findViewById<Button>(R.id.point)
        point.setOnClickListener {
            if (curExpression == ""  || curExpression == "-") {
                curExpression = curExpression.plus("0").plus(point.text.toString())
                str = str.plus("0").plus(point.text.toString())
            } else {
                if (!curExpression.contains('.')) {
                    curExpression = curExpression.plus(point.text.toString())
                    str = str.plus(point.text.toString())
                }
            }
            display1.text = str
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putDouble(BUNDLE_KEY1, count)
        outState.putString(BUNDLE_KEY2, str)
        outState.putString(BUNDLE_KEY3, curExpression)
        outState.putString(BUNDLE_KEY4, curSign)
        outState.putBoolean(BUNDLE_KEY5, has_minus)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        count = savedInstanceState.getDouble(BUNDLE_KEY1, count)
        str = savedInstanceState.getString(BUNDLE_KEY2, str)
        curExpression = savedInstanceState.getString(BUNDLE_KEY3, curExpression)
        curSign = savedInstanceState.getString(BUNDLE_KEY4, curSign)
        has_minus = savedInstanceState.getBoolean(BUNDLE_KEY5, has_minus)
        display1.text = String.format(str)
    }

    private fun del() {
        count = 0.0
        str = ""
        curExpression = ""
        curSign = ""
        display1.text = ""
    }

    private fun makeOperation(cur_sign: String): String {
        when (cur_sign) {
            "+" -> count += curExpression.toDouble()
            "-" -> count -= curExpression.toDouble()
            "*" -> count *= curExpression.toDouble()
            "/" -> {
                if (curExpression.toDouble() == 0.0) {
                    return "ERROR"
                } else {
                    count /= curExpression.toDouble()
                }
            }
            else -> count = curExpression.toDouble()
        }
        return ""
    }
}