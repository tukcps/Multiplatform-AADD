package examples.aaddtests

import com.github.tukcps.aadd.DDBuilder
import com.github.tukcps.aadd.dao.*
import com.github.tukcps.aadd.values.AffineForm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ToFromJsonTests {

    @Test
    fun afToJsonTest() {
        DDBuilder{
            this.config.noiseSymbolsFlag = true
            this.config.originalFormsFlag = true
            val a = AffineForm(this, 1.0..2.0, 1)
            val jsonStr = a.toDAO().toJson()
            val jObject: AffineFormDAO = json.decodeFromString(jsonStr)
            assertEquals(1.0, jObject.min)
            assertEquals(2.0, jObject.max)
            assertEquals(1, jObject.xi.size)
        }
    }


    @Test fun aaddToJsonLeaf() {
        DDBuilder {
            val a = real(1.0..2.0, 1.toString())
            val aStr = a.toDAO().toJson()
            val jObject = json.decodeFromString<AaddDAO>(string = aStr)
            assertEquals(1.0, jObject.value?.min)
            assertEquals(2.0, jObject.value?.max)
            assertEquals(1, jObject.value!!.xi.size)
        }
    }


    @Test
    // An AADD leaf with value +-Infinity or NaN from a json.
    fun aaddInfiniteFromJson() {
        DDBuilder {
            val a = Reals
            val aStr = a.toDAO().toJson()
            val jObject = json.decodeFromString<AaddDAO>(string = aStr)
            assertTrue(jObject.value!!.min.isInfinite())
            assertTrue(jObject.value.max.isInfinite())
            assertTrue(jObject.value.central.isNaN())
            assertTrue(jObject.value.r.isInfinite())
        }
    }



    @Test
    // An AADD *tree* from a json with a factory pre-existing.
    fun aaddTreeFromJson() {
        DDBuilder {
            val a = real(1.0..2.0, 1.toString())
            val b = (a greaterThanOrEquals real(1.5)).ite(real(3.0..4.0, 2.toString()), real(5.0..6.0, 3.toString()))
            val jsonString = b.toDAO().toJson()
            val jObject = json.decodeFromString<AaddDAO>(string = jsonString)
            assertEquals(3.0, jObject.T!!.value!!.min, 0.000001)
            assertEquals(6.0, jObject.F!!.value!!.max, 0.000001)
        }
    }

    /*



    @Test
    // Write Factory to Json string with all kind of entries
    // If this fails, check the next method that generates new json.
    // then, the hard-coded json might need update if changed.
    fun ddBuilderToJsonTest() {
        DDBuilder {
            range(1.0..2.0, "a")
            range(2.0..3.0)
            conds.newVariable("var1", this)
            conds.newConstraint(AF(1.0, 2.0, 2), "constr2")
            val str = toJson()
            val expected = "{\"conds\":{\"topIndex\":2,\"indexes\":{\"constr2\":2,\"var1\":1},\"x\":{\"1\":{\"type\":\"com.github.tukcps.jaadd.BDD.Leaf\"},\"2\":{\"type\":\"com.github.tukcps.jaadd.AADD.Leaf\",\"value\":{\"min\":1.0,\"max\":2.0,\"central\":1.5,\"xi\":{\"2\":0.5}}}}},\"noiseVars\":{\"maxIndex\":2,\"names\":{\"1\":\"a\"}}}"
            JSONAssert.assertEquals(expected, str, JSONCompareMode.LENIENT)
        }
    }

    @Test
    fun json2FactoryTest() {
        val json = "{\n" +
                "  \"conds\" : {\n" +
                "    \"x\" : {\n" +
                "      \"1\" : {\n" +
                "        \"type\" : \"com.github.tukcps.jaadd.BDD.Leaf\",\n" +
                "        \"value\": {\n" +
                "                    \"type\": \"com.github.tukcps.jaadd.values.XBoolImpl\",\n" +
                "                    \"xBoolEnum\": \"True\"\n" +
                "        }" +
                "      },\n" +
                "      \"2\" : {\n" +
                "        \"type\" : \"com.github.tukcps.jaadd.AADD.Leaf\",\n" +
                "        \"value\" : {\n" +
                "          \"min\" : 1.0,\n" +
                "          \"max\" : 2.0,\n" +
                "          \"central\" : 1.5,\n" +
                "          \"r\" : 0.0,\n" +
                "          \"xi\" : {\n" +
                "            \"2\" : 0.5\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"indexes\" : {\n" +
                "      \"constr2\" : 2,\n" +
                "      \"var1\" : 1\n" +
                "    },\n" +
                "    \"topIndex\" : 2,\n" +
                "    \"btmIndex\" : 0\n" +
                "  },\n" +
                "  \"noiseVars\" : {\n" +
                "    \"maxIndex\" : 2,\n" +
                "    \"names\" : {\n" +
                "      \"1\" : \"a\"\n" +
                "    }\n" +
                "  }\n" +
                "}\n"
        with(DDBuilder(json)) {
            Assertions.assertTrue(conds.indexes["constr2"] == 2)
        }
    }


    @Test // Restore first a factory, then an AADD.
    fun json2FactoryAADDTest() {
        val json = "{\n" +
                "  \"conds\" : {\n" +
                "    \"x\" : {\n" +
                "      \"1\" : {\n" +
                "        \"type\" : \"com.github.tukcps.jaadd.BDD.Leaf\",\n" +
                "        \"value\": {\n" +
                "                    \"type\": \"com.github.tukcps.jaadd.values.XBoolImpl\",\n" +
                "                    \"xBoolEnum\": \"True\"\n" +
                "                }" +
                "      },\n" +
                "      \"2\" : {\n" +
                "        \"type\" : \"com.github.tukcps.jaadd.AADD.Leaf\",\n" +
                "        \"value\" : {\n" +
                "          \"min\" : 1.0,\n" +
                "          \"max\" : 2.0,\n" +
                "          \"central\" : 1.5,\n" +
                "          \"r\" : 0.0,\n" +
                "          \"xi\" : {\n" +
                "            \"2\" : 0.5\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"indexes\" : {\n" +
                "      \"constr2\" : 2,\n" +
                "      \"var1\" : 1\n" +
                "    },\n" +
                "    \"topIndex\" : 2,\n" +
                "    \"btmIndex\" : 0\n" +
                "  },\n" +
                "  \"noiseVars\" : {\n" +
                "    \"maxIndex\" : 2,\n" +
                "    \"names\" : {\n" +
                "      \"1\" : \"a\"\n" +
                "    }\n" +
                "  }\n" +

                "}\n"
        with (DDBuilder(json)) {
            Assertions.assertTrue(conds.indexes["var1"] == 1)
            val c = aaddFromJson(
                "{\n" +
                        "  \"type\" : \"com.github.tukcps.jaadd.AADD.Internal\",\n" +
                        "  \"index\": 1,\n" +
                        "  \"T\": {\n" +
                        "    \"type\" : \"com.github.tukcps.jaadd.AADD.Leaf\",\n" +
                        "    \"value\": {\n" +
                        "      \"central\": 3.5,\n" +
                        "      \"r\": 5.551115123125784E-16,\n" +
                        "      \"xi\": {\n" +
                        "        \"2\": 0.5\n" +
                        "      },\n" +
                        "      \"min\": 3.0,\n" +
                        "      \"max\": 4.0\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"F\": {\n" +
                        "    \"type\" : \"com.github.tukcps.jaadd.AADD.Leaf\",\n" +
                        "    \"value\": {\n" +
                        "      \"central\": 5.5,\n" +
                        "      \"r\": 9.99200722162641E-16,\n" +
                        "      \"xi\": {\n" +
                        "        \"3\": 0.5\n" +
                        "      },\n" +
                        "      \"min\": 5.0,\n" +
                        "      \"max\": 6.0\n" +
                        "    }\n" +
                        "  }\n" +
                        "}"
            )
            val d = c + scalar(2.0)
            // println("d=" + d)
            // println(this)
            assertEquals(1, d.height())
            assertEquals(1, c.height())
            assertEquals(1, c.index)
        }
    }
    */
}
