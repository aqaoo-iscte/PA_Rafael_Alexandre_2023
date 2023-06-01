package Fase1

/** TIPOS DE DADOS JSON
 * Objeto JSON -> é um conjunto de pares chave/valor separados por vírgulas e delimitados por '{}'. A chave é uma String e o valor pode ser de qualquer tipo de dados
 * Array JSON -> é uma lista de valores separados por vírgulas e delimitados por '[]'
 * String JSON -> é uma sequência de caracteres delimitada por '"'
 * Boolean JSON -> é um valor boolean, true ou false
 * Número JSON -> é um valor numérico um inteiro
 * Float JSON -> é um valor numérico de ponto flutuante
 * Null JSON -> é um valor especial que representa a ausência de valor
 **/

import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KProperty
import kotlin.reflect.full.*


/**
 * JSONElement interface represents a JSON element that can be visited by a Visitor.
 *
 * @constructor Create empty J s o n element
 */
interface JSONElement{
    fun accept(visitor: Visitor)
}


/**
 * Visitor interface defines methods to visit different types of JSON elements.
 *
 * @constructor Create empty Visitor
 */
interface Visitor {
    fun visit(jsonObject: JSONObject): Boolean = true
    fun visitProperty(propertyName: String) {}
    fun visit(jsonArray: JSONArray): Boolean = true
    fun visitElementInArray(index: Int) {}
    fun visit(jsonString: JSONString) {}
    fun visit(jsonBoolean: JSONBoolean) {}
    fun visit(jsonNumber: JSONNumber) {}
    fun visit(jsonNull: JSONNull) {}
    fun endVisit(jsonObject: JSONObject) {}
    fun endVisit(jsonArray: JSONArray) {}
}


/**
 * JSONObject represents a JSON object that contains properties.
 *
 * @property properties The properties of the JSON object. Each property consists of a name and an associated element
 * @constructor Create empty Jsonobject
 */
data class JSONObject(val properties: Map<String, JSONElement>) : JSONElement {
    override fun accept(visitor: Visitor) {
        if(visitor.visit(this))
            properties.forEach { (chave, valor) ->
                visitor.visitProperty(chave)
                valor.accept(visitor)}
        visitor.endVisit(this)
    }
}


/**
 * JSONArray represents a JSON array that contains elements.
 *
 * @property elements The elements of the JSON array. It is an array of JSON elements.
 * @constructor Create empty Jsonarray
 */
data class JSONArray(val elements: List<JSONElement>) : JSONElement {
    override fun accept(visitor: Visitor) {
        if(visitor.visit(this))
            elements.forEachIndexed { index, jsonElement ->
                visitor.visitElementInArray(index)
                jsonElement.accept(visitor)
            }
        visitor.endVisit(this)
    }
}


/**
 * JSONString represents a JSON string value.
 *
 * @property value The string value.
 * @constructor Create empty Jsonstring
 */
data class JSONString(val value: String) : JSONElement {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}



/**
 * JSONBoolean represents a JSON boolean value.
 *
 * @property value The boolean value.
 * @constructor Create empty Jsonboolean
 */
data class JSONBoolean(val value: Boolean) : JSONElement {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}


/**
 * JSONNumber represents a JSON number value.
 *
 * @property value The number value.
 * @constructor Create empty Jsonnumber
 */
data class JSONNumber(val value: Number): JSONElement {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}


/**
 * JSONNull represents a JSON null value.
 *
 * @constructor Create empty Jsonnull
 */
object JSONNull : JSONElement {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}


/**
 * JSONElement extension function to get objects with specified properties.
 *
 * @param properties
 * @return
 */
fun JSONElement.getObjectsWithProperties(properties: List<String>): List<JSONObject> {
    val result = mutableListOf<JSONObject>()
    val visitor = object : Visitor {
        override fun visit(jsonObject: JSONObject): Boolean {
            if(properties.all { it in jsonObject.properties })
                result.add(jsonObject)
            return true
        }
    }
    accept(visitor)
    return result
}


/**
 * JSONElement extension function to get values of a specified property.
 *
 * @param property
 * @return
 */
fun JSONElement.getValuesOfProperty(property: String): List<JSONElement> {
    val values = mutableListOf<JSONElement>()
    val visitor = object : Visitor {
        override fun visit(jsonObject: JSONObject): Boolean {
            jsonObject.properties.forEach { if(it.key == property) values.add(it.value) }
            return true
        }
    }
    accept(visitor)
    return values
}



/**
 * JSONElement extension function to validate the presence of "numero" property as JSONNumber.
 *
 * @return
 */
fun JSONElement.validateNumeroProperty(): Boolean {
    var validate: Boolean = true
    val visitor = object : Visitor {
        override fun visit(jsonObject: JSONObject): Boolean {
            jsonObject.properties.forEach {
                if(it.key == "numero") {
                    if(it.value !is JSONNumber)
                        validate = false
                }
            }
            return true
        }
    }
    accept(visitor)
    return validate
}


/**
 * JSONElement extension function to validate the "inscritos" property as JSONArray with consistent properties.
 *
 * @return
 */
fun JSONElement.validateInscritosProperty(): Boolean {
    var validate: Boolean = true

    fun checkInscritos(jsonArray: JSONArray): Boolean {
        var expectedProps: Set<String>? = null
        var firstElement = true
        jsonArray.elements.forEach { element ->
            if (element is JSONObject) {
                if (firstElement) {
                    expectedProps = element.properties.keys
                    firstElement = false
                } else {
                    if (expectedProps != element.properties.keys) {
                        return false
                    }
                }
            }
        }
        return true
    }
    val visitor = object : Visitor {
        override fun visit(jsonObject: JSONObject): Boolean {
            jsonObject.properties.forEach {
                if(it.key == "inscritos") {
                    validate = checkInscritos(it.value as JSONArray)
                }
            }
            return true
        }
    }

    accept(visitor)
    return validate
}

val KClass<*>.dataClassFields: List<KProperty<*>>
    get() {
        require(isData) { "instance must be data class" }
        return primaryConstructor!!.parameters.map { p ->
            declaredMemberProperties.find { it.name == p.name }!!
        }
    }

val KClassifier?.isEnum: Boolean
    get() = this is KClass<*> && this.isSubclassOf(Enum::class)

val <T : Any> KClass<T>.enumConstants: List<T> get() {
    require(isEnum) { "instance must be enum" }
    return java.enumConstants.toList()
}

/**
 * Function to create a JSONElement model from an object.
 *
 * @param obj
 * @return
 */
fun createModelFromObject(obj: Any): JSONElement {
    return when {
        obj is KClass<*> -> {
            if (obj.isEnum) {
                val values = obj.enumConstants.map { it.toString() }
                val properties = mapOf("name" to JSONString(obj.simpleName!!), "values" to JSONArray(values.map { JSONString(it) }))
                JSONObject(properties)
            } else {
                JSONNull
            }
        }
        obj::class.isData -> {
            val keys = obj::class.dataClassFields.filter { !it.hasAnnotation<JSONIgnore>() }
                .map { if(it.hasAnnotation<JSONPersonalize>()) it.findAnnotation<JSONPersonalize>()!!.personalize else it.name }
            val values = obj::class.dataClassFields.filter { !it.hasAnnotation<JSONIgnore>() }
                .map { if(it.hasAnnotation<JSONForceString>())
                    createModelFromObject(it.call(obj)!!.toString())
                else
                    createModelFromObject(it.call(obj)!!) }
            val properties: Map<String, JSONElement> = keys.zip(values).toMap()
            JSONObject(properties)
        }
        obj is Collection<*> -> {
            val elements = obj.map { createModelFromObject(it!!) }
            JSONArray(elements)
        }
        obj is Map<*, *> -> {
            val keys: List<String> = obj.keys.map { it.toString() }.toList()
            val value = obj.values.map { createModelFromObject(it!!) }
            JSONObject(keys.zip(value).toMap())
        }
        obj is String -> JSONString(obj)
        obj is Boolean -> JSONBoolean(obj)
        obj is Number -> JSONNumber(obj)
        obj is Enum<*> -> JSONString(obj.name)
        else -> JSONNull
    }
}

/**
 * Student
 *
 * @property number
 * @property name
 * @property type
 * @property year_failed
 * @constructor Create empty Student
 */
data class Student(

    @JSONForceString
    val number: Int,

    @JSONPersonalize("nome estudante")
    val name: String,

    @JSONIgnore
    val type: StudentType? = null,

    val year_failed: Boolean
)

/**
 * Student type
 *
 * @constructor Create empty Student type
 */
enum class StudentType {
    Bachelor, Master, Doctoral
}

/**
 * Main
 *
 */

fun main() {

//    val visitor = JSONTextBuilder()
//    val estudante = Student(7,"rafael", StudentType.Master, true)
//    val model = createModelFromObject(estudante)
//    model.accept(visitor)
//    val jsonText = visitor.getJsonString()
//    println(jsonText)

//    val map = mapOf(1 to "x", 2 to "y", -1 to "zz")
//    val set: Set<Any> = setOf(2,6,4,29,5,"rafael","alexandre")
//    val inteiro = 1
//
//    val model = createModelFromObject(p)
//    model.accept(visitor)
//    val jsonText = visitor.getJsonString()
//
//    println(model)
//    println(jsonText)

//    val visitor = JSONTextBuilder()
//    val jsonObject = JSONObject(
//        mapOf(
//            "nome" to JSONString("Rafael"),
//            "idade" to JSONNumber(21),
//            "amigos" to JSONArray(
//                listOf(
//                    JSONString("Alexandre"),
//                    JSONString("Luís"),
//                    JSONString("João")
//                )
//            )
//        )
//    )
//    jsonObject.accept(visitor)
//    val jsonText = visitor.getJsonString()
//    println(jsonText)
}
