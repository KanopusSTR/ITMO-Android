package hw.first.calcucator

import org.junit.Assert
import org.junit.Before
import org.junit.Test

class MyTestUI {
    private lateinit var calc: Calculate
    @Before
    fun myTestUI() {
        calc = Calculate()
    }

    @Test
    fun add_int() {
        calc.number("1")
        calc.sum()
        calc.number("3")
        val answer = calc.equal()
        Assert.assertEquals("4", answer)
    }
    @Test
    fun add_double_to_int() {
        calc.number("1")
        calc.point()
        calc.number("5")
        calc.sum()
        calc.number("3")
        calc.point()
        calc.number("5")
        val answer = calc.equal()
        Assert.assertEquals("5", answer)
    }
    @Test
    fun add_double() {
        calc.number("1")
        calc.point()
        calc.number("9")
        calc.sum()
        calc.number("3")
        calc.point()
        calc.number("6")
        val answer = calc.equal()
        Assert.assertEquals("5.5", answer)
    }

    @Test
    fun unary_minus_int() {
        calc.sub()
        val answer = calc.number("5")
        Assert.assertEquals("-5", answer)
    }
    @Test
    fun unary_minus_double() {
        calc.sub()
        calc.number("5")
        calc.point()
        calc.number("3")
        val answer = calc.equal()
        Assert.assertEquals("-5.3", answer)
    }
    @Test
    fun unary_minus_sub() {
        calc.number("10")
        calc.sub()
        calc.sub()
        calc.number("5")
        calc.point()
        calc.number("3")
        val answer = calc.equal()
        Assert.assertEquals("15.3", answer)
    }

    @Test
    fun sub_int() {
        calc.number("5")
        calc.sub()
        calc.number("2")
        val answer = calc.equal()
        Assert.assertEquals("3", answer)
    }
    @Test
    fun sub_double_equal_after_point() {
        calc.number("2.2")
        calc.sub()
        calc.number("1.2")
        val answer = calc.equal()
        Assert.assertEquals("1", answer)
    }
    @Test
    fun sub_double() {
        calc.number("8.7")
        calc.sub()
        calc.number("6.4")
        val answer = calc.equal()
        Assert.assertEquals("2.3", answer)
    }

    @Test
    fun multiply_int() {
        calc.number("5")
        calc.multiply()
        calc.number("2")
        val answer = calc.equal()
        Assert.assertEquals("10", answer)
    }
    @Test
    fun multiply_double_to_int() {
        calc.number("1")
        calc.point()
        calc.number("1")
        calc.multiply()
        calc.number("10")
        val answer = calc.equal()
        Assert.assertEquals("11", answer)
    }
    @Test
    fun multiply_double() {
        calc.number("1")
        calc.point()
        calc.number("1")
        calc.multiply()
        calc.number("9")
        calc.point()
        calc.number("9")
        val answer = calc.equal()
        Assert.assertEquals("10.89", answer)
    }

    @Test
    fun divide_int() {
        calc.number("10")
        calc.div()
        calc.number("2")
        val answer = calc.equal()
        Assert.assertEquals("5", answer)
        calc.del()
    }
    @Test
    fun divide_int_to_double() {
        calc.number("7")
        calc.div()
        calc.number("3")
        val answer = calc.equal()
        Assert.assertEquals("2.3333333333", answer)
        calc.del()
    }
    @Test
    fun divide_double() {
        calc.number("2")
        calc.point()
        calc.number("2")
        calc.div()
        calc.number("1")
        calc.point()
        calc.number("1")
        val answer = calc.equal()
        Assert.assertEquals("2", answer)
    }
    @Test
    fun divide_on_zero() {
        calc.number("2")
        calc.div()
        calc.number("0")
        val answer = calc.equal()
        Assert.assertEquals("ERROR", answer)
    }
    @Test
    fun divide_zero_on_zero() {
        calc.number("0")
        calc.div()
        calc.number("0")
        val answer = calc.equal()
        Assert.assertEquals("ERROR", answer)
    }

    @Test
    fun double_point1() {
        calc.number("2")
        calc.multiply()
        calc.number("1")
        calc.point()
        calc.number("1")
        calc.equal()
        calc.point()
        val answer = calc.equal()
        Assert.assertEquals("2.2", answer)
    }
    @Test
    fun double_point2() {
        calc.number("2")
        calc.point()
        calc.point()
        calc.number("2")
        val answer = calc.equal()
        Assert.assertEquals("2.2", answer)
    }

    @Test
    fun delete() {
        calc.number("2")
        calc.point()
        calc.number("2")
        calc.div()
        calc.number("2")
        calc.multiply()
        calc.number("2")
        calc.sub()
        calc.number("2")
        calc.del()
        val answer = calc.equal()
        Assert.assertEquals("", answer)
    }

    @Test
    fun big_evaluate() {
        calc.number("2436345633243224")
        calc.point()
        calc.number("2")
        calc.div()
        calc.number("35252")
        calc.multiply()
        calc.number("1")
        calc.point()
        calc.number("2")
        calc.sub()
        calc.number("23623535")
        calc.sum()
        calc.number("974538276829814")
        val answer = calc.equal()
        Assert.assertEquals("974621187926575.5", answer)
    }
}