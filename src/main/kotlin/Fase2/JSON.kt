package Fase2

/** TIPOS DE DADOS JSON
 * Objeto JSON -> é um conjunto de pares chave/valor separados por vírgulas e delimitados por '{}'. A chave é uma String e o valor pode ser de qualquer tipo de dados
 * Array JSON -> é uma lista de valores separados por vírgulas e delimitados por '[]'
 * String JSON -> é uma sequência de caracteres delimitada por '"'
 * Boolean JSON -> é um valor boolean, true ou false
 * Número JSON -> é um valor numérico um inteiro
 * Float JSON -> é um valor numérico de ponto flutuante
 * Null JSON -> é um valor especial que representa a ausência de valor
 **/
import java.awt.Component
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KProperty
import kotlin.reflect.full.*

interface JSONElement {
    val observers: MutableList<JSONElementObserver>
        get() = mutableListOf()

    fun accept(visitor: Visitor)
}

interface JSONElementObserver {
    fun jsonElementAdded(clickedKey: String, jsonElement: Pair<String, JSONElement>, clickedComponent: Component)
    fun jsonElementRemoved(jsonElement: Pair<String, JSONElement>, clickedComponent: Component?)
    fun jsonElementReplaced(jsonElementOld: Pair<String, JSONElement>, jsonElementNew: JSONElement)
}

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

data class JSONObject(val properties: MutableMap<String, JSONElement>) : JSONElement {
    override val observers: MutableList<JSONElementObserver> = mutableListOf()

    override fun accept(visitor: Visitor) {
        if (visitor.visit(this))
            properties.forEach { (chave, valor) ->
                visitor.visitProperty(chave)
                valor.accept(visitor)
            }
        visitor.endVisit(this)
    }

    fun addObserver(observer: JSONElementObserver) {
        observers.add(observer)
    }

    fun removeObserver(observer: JSONElementObserver) {
        observers.remove(observer)
    }


    fun add(clickedKey: String, name: String, jsonElement: JSONElement, clickedComponent: Component) {
        if ((clickedComponent as JSONEditorView.ElementsInBoard).father != null) {
            clickedComponent.father?.add(clickedKey, name, jsonElement, clickedComponent)
        } else {
            addObject(clickedKey, name, jsonElement, clickedComponent)
        }

    }

    fun addObject(clickedKey: String, name: String, jsonElement: JSONElement, clickedComponent: Component) {
        val updatedProperties = mutableMapOf<String, JSONElement>()
        properties.forEach { (key, value) ->
            updatedProperties[key] = value
            if (key == clickedKey) {
                updatedProperties[name] = jsonElement
            }
        }
        properties.clear()
        properties.putAll(updatedProperties)
        observers.forEach { it.jsonElementAdded(clickedKey, Pair(name, jsonElement), clickedComponent) }
    }

    fun remove(name: String, jsonElement: JSONElement, clickedComponent: Component?) {
        if (clickedComponent != null && (clickedComponent as JSONEditorView.ElementsInBoard).father != null) {
            clickedComponent.father?.remove(name, jsonElement, clickedComponent)
        }

        if (properties.remove(name, jsonElement)) {
            observers.forEach {
                it.jsonElementRemoved(Pair(name, jsonElement), clickedComponent)
            }
        }
    }

    fun replace(jsonOld: Pair<String, JSONElement>, jsonNew: JSONElement, clickedComponent: Component?) {
        if (clickedComponent != null && clickedComponent is JSONEditorView.ElementsInBoard && clickedComponent.father != null) {
            clickedComponent.father.replace(jsonOld, jsonNew, clickedComponent)
        } else {
            replaceObject(jsonOld, jsonNew)
        }

    }

    fun replaceObject(json_old: Pair<String, JSONElement>, json_new: JSONElement) {

        properties[json_old.first] = json_new

        observers.forEach {
            it.jsonElementReplaced(json_old, json_new)
        }
    }
}

data class JSONArray(val elements: MutableList<JSONElement>) : JSONElement {
    override val observers: MutableList<JSONElementObserver> = mutableListOf()
    override fun accept(visitor: Visitor) {
        if (visitor.visit(this))
            elements.forEachIndexed { index, jsonElement ->
                visitor.visitElementInArray(index)
                jsonElement.accept(visitor)
            }
        visitor.endVisit(this)
    }

    fun addObserver(observer: JSONElementObserver) {
        observers.add(observer)
    }

    fun add(clickedKey: String, name: String, jsonElement: JSONElement, clickedComponent: Component) {
        if (jsonElement is JSONObject) {
            elements.add(this.elements.size, jsonElement)
        } else {
            (elements[(clickedComponent as JSONEditorView.ElementsInBoard).index!!] as JSONObject).addObject(
                clickedKey,
                name,
                jsonElement,
                clickedComponent
            )
        }

        observers.forEach {
            it.jsonElementAdded(clickedKey, Pair(name, jsonElement), clickedComponent)
        }
    }


    fun remove(name: String, jsonElement: JSONElement, clickedComponent: Component) {
        (elements[(clickedComponent as JSONEditorView.ElementsInBoard).index!!] as JSONObject).remove(
            name,
            jsonElement,
            null
        )
        observers.forEach { it.jsonElementRemoved(Pair(name, jsonElement), clickedComponent) }
    }

    fun replace(jsonOld: Pair<String, JSONElement>, jsonNew: JSONElement, clickedComponent: Component?) {

        (elements[(clickedComponent as JSONEditorView.ElementsInBoard).index!!] as JSONObject).replaceObject(
            jsonOld,
            jsonNew
        )
        observers.forEach { it.jsonElementReplaced(jsonOld, jsonNew) }

    }
}

fun JSONElement.isJSONArray(key: String): Boolean {
    if (this is JSONObject) {
        val value = this.properties[key]
        return value is JSONArray
    }
    return false
}

data class JSONString(var value: String) : JSONElement {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

data class JSONBoolean(var value: Boolean) : JSONElement {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

data class JSONNumber(var value: Number) : JSONElement {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}

object JSONNull : JSONElement {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }

    override fun toString(): String {
        return "null"
    }
}

fun JSONElement.getObjectsWithProperties(properties: List<String>): List<JSONObject> {
    val result = mutableListOf<JSONObject>()
    val visitor = object : Visitor {
        override fun visit(jsonObject: JSONObject): Boolean {
            if (properties.all { it in jsonObject.properties })
                result.add(jsonObject)
            return true
        }
    }
    accept(visitor)
    return result
}

fun JSONElement.getValuesOfProperty(property: String): List<JSONElement> {
    val values = mutableListOf<JSONElement>()
    val visitor = object : Visitor {
        override fun visit(jsonObject: JSONObject): Boolean {
            jsonObject.properties.forEach { if (it.key == property) values.add(it.value!!) }
            return true
        }

//        override fun visit(jsonArray: JSONArray): Boolean {
//            jsonArray.elements.forEach { if(it is JSONObject) visit(it) }
//            return true
//        }
    }
    accept(visitor)
    return values
}

fun JSONElement.validateNumeroProperty(): Boolean {
    var validate: Boolean = true
    val visitor = object : Visitor {
        override fun visit(jsonObject: JSONObject): Boolean {
            jsonObject.properties.forEach {
                if (it.key == "numero") {
                    if (it.value !is JSONNumber)
                        validate = false
                }
            }
            return true
        }

    }
    accept(visitor)
    return validate
}

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
                if (it.key == "inscritos") {
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

val <T : Any> KClass<T>.enumConstants: List<T>
    get() {
        require(isEnum) { "instance must be enum" }
        return java.enumConstants.toList()
    }

fun createModelFromObject(obj: Any): JSONElement {
    return when {
//        obj is KClass<*> -> {
//            if (obj.isEnum2) {
//                val values = obj.enumConstants2.map { it.toString() }
//                val properties = mutableMapOf("name" to JSONString2(obj?.simpleName), "values" to JSONArray2(values.map { JSONString2(it) }))
//                JSONObject2(properties)
//            } else {
//                JSONNull2()
//            }
//        }
        obj::class.isData -> {
            val keys = obj::class.dataClassFields.filter { !it.hasAnnotation<JSONIgnore>() }
                .map { if (it.hasAnnotation<JSONPersonalize>()) it.findAnnotation<JSONPersonalize>()!!.personalize else it.name }
            val values = obj::class.dataClassFields.filter { !it.hasAnnotation<JSONIgnore>() }
                .map {
                    if (it.hasAnnotation<JSONForceString>())
                        createModelFromObject(it.call(obj)!!.toString())
                    else
                        createModelFromObject(it.call(obj)!!)
                }
            val properties: MutableMap<String, JSONElement> = keys.zip(values).toMap().toMutableMap()
            JSONObject(properties)
        }

        obj is Collection<*> -> {
            val elements = obj.map { createModelFromObject(it!!) }.toMutableList()
            JSONArray(elements)
        }

        obj is Map<*, *> -> {
            val keys: List<String> = obj.keys.map { it.toString() }.toList()
            val value = obj.values.map { createModelFromObject(it!!) }
            JSONObject(keys.zip(value).toMap().toMutableMap())
        }

        obj is String -> JSONString(obj)
        obj is Boolean -> JSONBoolean(obj)
        obj is Number -> JSONNumber(obj)
        obj is Enum<*> -> JSONString(obj.name)
        else -> JSONNull
    }
}

fun main() {}

