package hw.first.calcucator

import java.text.NumberFormat
import java.util.*

val f: NumberFormat = NumberFormat.getInstance(Locale("en"))

class Calculate {

    var count = 0.0
    var str = ""
    var curExpression = ""
    var curSign = ""
    var hasMinus = false

    fun number(number: String): String {
        curExpression = curExpression.plus(number)
        hasMinus = false
        str = str.plus(number)
        return str
    }

    fun sum(): String {
        if (curExpression != "" && curExpression != "-") {
            makeOperation(curSign)
            curSign = "+"
            curExpression = ""
            str = str.plus("+")
            hasMinus = false
        }
        return str
    }

    fun sub(): String {
        if (!hasMinus) {
            if (curExpression != "" && curExpression != "-") {
                makeOperation(curSign)
                curSign = "-"
                curExpression = ""
                str = str.plus("-")
            } else {
                curExpression = curExpression.plus("-")
                str = str.plus("-")
                hasMinus = true
            }
        }
        return str
    }

    fun div(): String {
        if (curExpression != "" && curExpression != "-") {
            makeOperation(curSign)
            curSign = "/"
            hasMinus = false
            curExpression = ""
            str = str.plus("/")
        }
        return str
    }

    fun multiply(): String {
        if (curExpression != "" && curExpression != "-") {
            makeOperation(curSign)
            curSign = "*"
            hasMinus = false
            curExpression = ""
            str = str.plus("*")
        }
        return str
    }

    fun del(): String {
        count = 0.0
        str = ""
        curExpression = ""
        curSign = ""
        return str
    }

    fun equal(): String {
        if (curSign != "" && curExpression != "") {
            if (makeOperation(curSign) == "ERROR") {
                del()
                return String.format("ERROR")
            } else {
                str = f.format(count).replace(",", "")
                curSign = ""
                hasMinus = false
                curExpression = str
            }
        }
        return  str
    }

    fun point(): String {
        if (curExpression == ""  || curExpression == "-") {
            curExpression = curExpression.plus("0").plus(".")
            str = str.plus("0").plus(".")
        } else {
            if (!curExpression.contains('.')) {
                curExpression = curExpression.plus(".")
                str = str.plus(".")
            }
        }
        return str
    }

    private fun makeOperation(cur_sign: String): String {
        f.maximumFractionDigits = 10
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