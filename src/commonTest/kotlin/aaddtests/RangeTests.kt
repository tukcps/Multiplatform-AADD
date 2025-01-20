package aaddtests

import com.github.tukcps.aadd.values.Range
import com.github.tukcps.aadd.values.ceil
import com.github.tukcps.aadd.values.floor
import kotlin.test.Test
import kotlin.test.assertEquals


class RangeTests {

    @Test
    fun toStringTest() {
        val r = Range(-1.2345678 .. 1.0 )
        assertEquals("-1.2345678..1.0", r.toString())
    }

    /**TODO: Test in the assertions instead of comparing to a string value if the min value is of the type -INF if it exists and INF if it exists*/
    // TODO commented out due to non availablity of from string function in MP
    /*
    @Test fun fromStringTest() {
        val r = Range.fromString("-*..*")
        assertEquals("Real", r.toString())
    }*/

    /** TODO : Test already exists in this form AADDTests so can potentially be deleted */
    @Test
    fun testNeuralNetworkExampleRun1() {
        val precision = 0.001
        val C = Range(40.0 .. 40.0) // variable to be changed
        val C_w = Range(101.0 .. 101.0)
        val K = Range(16.0 .. 16.0)
        val F = Range(3.0 .. 3.0)
        val s = Range(1.0 .. 1.0)
        val p = Range(0.0 .. 0.0) // padding, 0 = NOT enabled, 1 = enabled
        val C_wb: Range = floor (F / Range(2.0 .. 2.0))
        val C_w_hat: Range = C_w + p * Range(2.0 .. 2.0) * C_wb
        val a_w: Range = floor((C_w_hat - F) / s + 1.0)
        val C_wb_s_floor_arg: Range = (C_wb - Range(1.0 .. 1.0)) / s + 1.0
        val a_pb: Range = p * floor(C_wb_s_floor_arg)
        val sum_a_pb: Range = (a_pb - Range(1.0 .. 1.0)) * a_pb / Range(2.0 .. 2.0) // sum from 0 to a_pb - 1
        val MAC_notb: Range = (C_wb - s) * sum_a_pb // eq. 12
        val Fw: Range = s * (a_w - Range(1.0 .. 1.0)) + F
        val C_we: Range = Fw - C_w - C_wb
        val C_we_s_floor_arg: Range = (C_we - Range(1.0 .. 1.0)) / s + 1.0
        val a_pe: Range = p * floor(C_we_s_floor_arg)
        val sum_a_pe: Range = (a_pe - Range(1.0 .. 1.0)) * a_pe / Range(2.0 .. 2.0) // sum from 0 to a_pe - 1
        val C_wb_minus_C_we: Range = C_wb - C_we
        val MAC_note: Range = (C_we - s) * sum_a_pe - C_wb_minus_C_we
        //var MAC_note: Range = sum_i(0, a_pe - 1.0, C_wb - s * i) - C_wb_minus_C_we
        // var t_l: Range = Range(1.0 .. 1.0) + C / Range(8.0 .. 8.0) * K / Range(8.0 .. 8.0) * (a_w * F - MAC_notb - MAC_note)
        val t_l: Range = Range(1.0 .. 1.0) + ceil(C / Range(8.0 .. 8.0)) * ceil(K / Range(8.0 .. 8.0)) * (a_w * F - MAC_notb - MAC_note)
        assertEquals(1.0, C_wb.min, precision) // 1
        assertEquals(1.0, C_wb.max, precision) // 1
        assertEquals(100.99999999999999, C_w_hat.min, precision) // 101
        assertEquals(101.00000000000001, C_w_hat.max, precision) // 101
        assertEquals(98.0, a_w.min, precision) // 99
        assertEquals(99.0, a_w.max, precision) // 99
        assertEquals(0.9999999999999998, C_wb_s_floor_arg.min, precision)
        assertEquals(1.0000000000000002, C_wb_s_floor_arg.max, precision)
        assertEquals(-4.9E-324, a_pb.min, precision) // 1
        assertEquals(4.9E-324, a_pb.max, precision) // 1
        assertEquals(-1.0E-323, sum_a_pb.min, precision)
        assertEquals(1.0E-323, sum_a_pb.max, precision)
        assertEquals(-4.9E-324, MAC_notb.min, precision) // 1
        assertEquals(4.9E-324, MAC_notb.max, precision) // 1
        assertEquals(99.99999999999996, Fw.min, precision) // 101
        assertEquals(101.00000000000004, Fw.max, precision) // 101
        assertEquals(-2.000000000000043, C_we.min, precision) // -1
        assertEquals(-0.9999999999999573, C_we.max, precision) // -1
        assertEquals(-2.0000000000000444, C_we_s_floor_arg.min, precision)
        assertEquals(-0.9999999999999568, C_we_s_floor_arg.max, precision)
        assertEquals(-4.9E-324, a_pe.min, precision) // 0
        assertEquals(4.9E-324, a_pe.max, precision) // 0
        assertEquals(-1.0E-323, sum_a_pe.min, precision)
        assertEquals(1.0E-323, sum_a_pe.max, precision)
        assertEquals(1.9999999999999571, C_wb_minus_C_we.min, precision)
        assertEquals(3.0000000000000435, C_wb_minus_C_we.max, precision)
        assertEquals(-3.000000000000044, MAC_note.min, precision) // -2
        assertEquals(-1.999999999999957, MAC_note.max, precision) // -2
        assertEquals(2960.9999999999964, t_l.min, precision) // 2981
        assertEquals(5401.0000000000055, t_l.max, precision) // 2981
        //println("\nEnd of test Neural Network Example, Run 1\n")
    }

    /** TODO : Test already exists in this form AADDTests so can potentially be deleted */
    @Test
    fun testNeuralNetworkExampleRun2() {
        //println("\nBeginning test Neural Network Example, Run 2\n")
        val precision = 0.001
        val C = Range(16.0 .. 16.0) // variable to be changed
        val C_w = Range(99.0 .. 99.0)
        val K = Range(24.0 .. 24.0)
        val F = Range(9.0 .. 9.0)
        val s = Range(2.0 .. 2.0)
        val p = Range(1.0 .. 1.0) // padding, 0 = NOT enabled, 1 = enabled
        //var i = Range(1.0 .. 1.0)
        //var timeinyears = Range(10.0 .. 10.0)

        val C_wb: Range = floor (F / Range(2.0 .. 2.0))
        val C_w_hat: Range = C_w + p * Range(2.0 .. 2.0) * C_wb
        //var a_w: Range = (C_w_hat - F) / s + 1.0
        val a_w: Range = floor((C_w_hat - F) / s + 1.0)
        val C_wb_s_floor_arg: Range = (C_wb - Range(1.0 .. 1.0)) / s + 1.0
        val a_pb: Range = p * floor(C_wb_s_floor_arg)
        val sum_a_pb: Range = (a_pb - Range(1.0 .. 1.0)) * a_pb / Range(2.0 .. 2.0) // sum from 0 to a_pb - 1
        val MAC_notb: Range = (C_wb - s) * sum_a_pb // eq. 12
        //var MAC_notb: Range = sum_i(0, a_pb - 1.0, C_wb - s * i)
        val Fw: Range = s * (a_w - Range(1.0 .. 1.0)) + F
        val C_we: Range = Fw - C_w - C_wb
        val C_we_s_floor_arg: Range = (C_we - Range(1.0 .. 1.0)) / s + 1.0
        val a_pe: Range = p * floor(C_we_s_floor_arg)
        val sum_a_pe: Range = (a_pe - Range(1.0 .. 1.0)) * a_pe / Range(2.0 .. 2.0) // sum from 0 to a_pe - 1
        val C_wb_minus_C_we: Range = C_wb - C_we
        val MAC_note: Range = (C_we - s) * sum_a_pe - C_wb_minus_C_we
        //var MAC_note: Range = sum_i(0, a_pe - 1.0, C_wb - s * i) - C_wb_minus_C_we
        // var t_l: Range = Range(1.0 .. 1.0) + C / Range(8.0 .. 8.0) * K / Range(8.0 .. 8.0) * (a_w * F - MAC_notb - MAC_note)
        val t_l: Range = Range(1.0 .. 1.0) + ceil(C / Range(8.0 .. 8.0)) * ceil(K / Range(8.0 .. 8.0)) * (a_w * F - MAC_notb - MAC_note)
        // var runtimein: Range = t_l
        // var runtimeout: Range = runtimein / power2(0.5 * timeinyears) // power2 not defined here

        //println("C_wb = [" + C_wb.min + ", " + C_wb.max + "]")  // expected 1
        //println("C_w_hat = [" + C_w_hat.min + ", " + C_w_hat.max + "]") // expected 101
        //println("a_w = [" + a_w.min + ", " + a_w.max + "]") // expected 99
        //println("C_wb_s_floor_arg = [" + C_wb_s_floor_arg.min + ", " + C_wb_s_floor_arg.max + "]")
        //println("a_pb = [" + a_pb.min + ", " + a_pb.max + "]") // expected 1
        //println("sum_a_pb = [" + sum_a_pb.min + ", " + sum_a_pb.max + "]") // expected 1
        //println("MAC_notb = [" + MAC_notb.min + ", " + MAC_notb.max + "]")
        //println("Fw = [" + Fw.min + ", " + Fw.max + "]") // expected 101
        //println("C_we = [" + C_we.min + ", " + C_we.max + "]") // expected -1
        //println("C_we_s_floor_arg = [" + C_we_s_floor_arg.min + ", " + C_we_s_floor_arg.max + "]")
        //println("a_pe = [" + a_pe.min + ", " + a_pe.max + "]") // expected 0
        //println("sum_a_pe = [" + sum_a_pe.min + ", " + sum_a_pe.max + "]")
        //println("C_wb_minus_C_we = [" + C_wb_minus_C_we.min + ", " + C_wb_minus_C_we.max + "]") // expected -2
        //println("MAC_note = [" + MAC_note.min + ", " + MAC_note.max + "]")
        //println("t_l = [" + t_l.min + ", " + t_l.max + "]") // expected 2629
        //println("lit val = 2629")
        //println("Mathematica val = 2629")
        assertEquals(4.0, C_wb.min, precision) // 4
        assertEquals(4.0, C_wb.max, precision) // 4
        assertEquals(106.99999999999999, C_w_hat.min, precision) // 107
        assertEquals(107.00000000000001, C_w_hat.max, precision) // 107
        assertEquals(49.0, a_w.min, precision) // 50
        assertEquals(50.0, a_w.max, precision) // 50
        assertEquals(2.499999999999999, C_wb_s_floor_arg.min, precision)
        assertEquals(2.500000000000001, C_wb_s_floor_arg.max, precision)
        assertEquals(1.9999999999999996, a_pb.min, precision) // 2.5
        assertEquals(2.0000000000000004, a_pb.max, precision) // 2.5
        assertEquals(0.999999999999999, sum_a_pb.min, precision)
        assertEquals(1.0000000000000013, sum_a_pb.max, precision)
        assertEquals(1.9999999999999973, MAC_notb.min, precision) // 6
        assertEquals(2.0000000000000036, MAC_notb.max, precision) // 6
        assertEquals(104.99999999999996, Fw.min, precision) // 107
        assertEquals(107.00000000000004, Fw.max, precision) // 107
        assertEquals(1.9999999999999563, C_we.min, precision) // 4
        assertEquals(4.00000000000004, C_we.max, precision) // 4
        assertEquals(1.4999999999999778, C_we_s_floor_arg.min, precision)
        assertEquals(2.5000000000000235, C_we_s_floor_arg.max, precision)
        assertEquals(0.9999999999999998, a_pe.min, precision) // 2
        assertEquals(2.0000000000000004, a_pe.max, precision) // 2
        assertEquals(-2.220446049250315E-16, sum_a_pe.min, precision)
        assertEquals(1.0000000000000013, sum_a_pe.max, precision)
        assertEquals(-4.5297099404706393E-14, C_wb_minus_C_we.min, precision)
        assertEquals(2.000000000000044, C_wb_minus_C_we.max, precision)
        assertEquals(-2.0000000000000884, MAC_note.min, precision) // 6
        assertEquals(2.0000000000000946, MAC_note.max, precision) // 6
        assertEquals(2622.999999999997, t_l.min, precision) // 2629
        assertEquals(5401.0000000000055, t_l.max, precision) // 2629
        //println("\nEnd of test Neural Network Example, Run 2\n")
    }
}