package Fase2

import java.awt.Component

interface Command {
    fun run()
    fun undo() {}
}

class ReplaceValueInModel(
    private val clickedKey: String,
    private val model: JSONObject,
    val old: Pair<String, JSONElement>,
    private val new: JSONElement,
    private val clickedComponent: Component?
) : Command {
    override fun run() {
        if (model.isJSONArray(clickedKey)) {
            (model.properties[clickedKey] as JSONArray).replace(old, new, clickedComponent)
        }
        model.replace(old, new, clickedComponent)
    }
}

class AddValueInModel(
    private val clickedKey: String,
    private val model: JSONObject,
    private val new: Pair<String, JSONElement>,
    private val clickedComponent: Component
) : Command {

    override fun run() {
        if (model.isJSONArray(clickedKey)) {
            val newObject = JSONObject(mutableMapOf(new.first to new.second))
            (model.properties[clickedKey] as JSONArray).add(clickedKey, new.first, newObject, clickedComponent)

        } else {
            model.add(clickedKey, new.first, new.second, clickedComponent)

        }

    }


}

class RemoveValueInModel(
    private val model: JSONObject,
    val new: Pair<String, JSONElement>,
    val clickedComponent: Component?
) : Command {
    override fun run() {
        model.remove(new.first, new.second, clickedComponent)
    }
}