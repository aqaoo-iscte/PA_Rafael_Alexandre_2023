
import Fase1.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class Tests {

    private val obj = JSONObject(
        mapOf(
            "uc" to JSONString("PA"),
            "ects" to JSONFloat(6.0f),
            "data-exame" to JSONNull,
            "inscritos" to JSONArray(
                listOf(
                    JSONObject(
                        mapOf(
                            "numero" to JSONNumber(101101),
                            "nome" to JSONString("Dave Farley"),
                            "internacional" to JSONBoolean(true)

                        )
                    ), JSONObject(
                        mapOf(
                            "numero" to JSONNumber(101102),
                            "nome" to JSONString("Martin Fowler"),
                            "internacional" to JSONBoolean(true)
                        )
                    ), JSONObject(
                        mapOf(
                            "numero" to JSONNumber(26503),
                            "nome" to JSONString("André Santos"),
                            "internacional" to JSONBoolean(false)
                        )
                    )
                )
            )
        )
    )

    @Test
    fun testeObterJsonTextual() {
        val expected = """{"uc" : "PA", "ects" : 6.0, "data-exame" : null, "inscritos" : [{"numero" : 101101, "nome" : "Dave Farley", "internacional" : true}, {"numero" : 101102, "nome" : "Martin Fowler", "internacional" : true}, {"numero" : 26503, "nome" : "André Santos", "internacional" : false}]}"""
        val visitor = JSONTextBuilder()
        obj.accept(visitor)
        val actual = visitor.getJsonString()
        assertEquals(expected, actual);
    }

    @Test
    fun testGetObjectsWithProperties() {
        val h = JSONObject(mapOf("inscritos" to JSONArray(listOf(JSONObject(mapOf("numero" to JSONNumber(101101), "nome" to JSONString("rafael"), "internacional" to JSONBoolean(true))), JSONObject(mapOf("Válido" to JSONBoolean(false), "nomenclatura" to JSONString("alexandre"), "internacional" to JSONBoolean(true)))))))
        val f = h.getObjectsWithProperties(listOf("nome")).toString()
        val expected = "[Fase1.JSONObject(properties={numero=Fase1.JSONNumber(value=101101), nome=Fase1.JSONString(value=rafael), internacional=Fase1.JSONBoolean(value=true)})]"
        assertEquals(expected, f)
    }

    @Test
    fun testGetValuesOfProperty() {
        val f = obj.getValuesOfProperty("numero")
        assertEquals(listOf(JSONNumber(101101), JSONNumber(101102), JSONNumber(26503)), f)
    }

    @Test
    fun testValidateNumeroProperty() {
        val f = obj.validateNumeroProperty()
        assertTrue(f)
    }

    @Test
    fun testValidateInscritosProperty() {
        val f = obj.validateInscritosProperty()
        assertTrue(f)
    }

}