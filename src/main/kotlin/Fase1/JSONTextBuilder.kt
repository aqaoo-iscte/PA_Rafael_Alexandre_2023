package Fase1

class JSONTextBuilder: Visitor {
    private val stringBuilder = StringBuilder()
    private var commas: Boolean = true

    override fun visit(jsonObject: JSONObject): Boolean {
        stringBuilder.append("{")
        return true
    }

    override fun visit(jsonArray: JSONArray): Boolean {
        stringBuilder.append("[")
        commas = true
        return true
    }

    override fun visit(jsonString: JSONString) {
        stringBuilder.append("\"${jsonString.value}\"")
    }

    override fun visit(jsonBoolean: JSONBoolean) {
        stringBuilder.append(jsonBoolean.value)
    }

    override fun visit(jsonNumber: JSONNumber) {
        stringBuilder.append(jsonNumber.value)
    }


    override fun visit(jsonNull: JSONNull) {
        stringBuilder.append("null")
    }

    override fun visitProperty(propertyName: String) {
        if (commas) {
            commas = false
            stringBuilder.append("\"$propertyName\" : ")
        } else {
            stringBuilder.append(", \"$propertyName\" : ")
        }
    }

    override fun visitElementInArray(index: Int) {
        if(index != 0)
            stringBuilder.append(", ")
    }

    override fun endVisit(jsonObject: JSONObject) {
        stringBuilder.append("}")
        commas = true
    }

    override fun endVisit(jsonArray: JSONArray) {
        stringBuilder.append("]")
        commas = true
    }

    fun getJsonString(): String {
        return stringBuilder.toString()
    }
}