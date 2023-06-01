package Fase2

import java.awt.Component
import java.awt.Dimension
import java.util.*
import javax.swing.JScrollPane

val model = JSONObject(
    mutableMapOf(
        "uc" to JSONString("PA"),
        "ects" to JSONNumber(6.0f),
        "data-exame" to JSONNull,
        "inscritos" to JSONArray(
            mutableListOf(
                JSONObject(
                    mutableMapOf(
                        "numero" to JSONNumber(101101),
                        "nome" to JSONString("Dave Farley"),
                        "internacional" to JSONBoolean(true)

                    )
                ), JSONObject(
                    mutableMapOf(
                        "numero" to JSONNumber(101102),
                        "nome" to JSONString("Martin Fowler"),
                        "internacional" to JSONBoolean(true)
                    )
                ), JSONObject(
                    mutableMapOf(
                        "numero" to JSONNumber(26503),
                        "nome" to JSONString("André Santos"),
                        "internacional" to JSONBoolean(false)
                    )
                )
            )
        )
    )
)

fun main() {
    val commands = Stack<Command>()
    fun runCommand(command: Command) {
        commands.push(command)
        command.run()
    }

    val textViewv = JSONTextView(model)

    val editorView = JSONEditorView(model)
    editorView.addObserver(object : JSONEditViewObserver {
        override fun jsonElementModified(
            clickedKey: String,
            old: Pair<String, JSONElement>,
            new: JSONElement,
            clickedComponent: Component?
        ) {
            runCommand(ReplaceValueInModel(clickedKey, model, old, new, clickedComponent))
        }

        override fun jsonElementAdded(
            clickedKey: String,
            key: String,
            value: JSONElement,
            clickedComponent: Component
        ) {
            runCommand(AddValueInModel(clickedKey, model, Pair(key, value), clickedComponent))

        }

        override fun jsonElementRemove(
            clickedObjectKey: String,
            clickedObjectValue: JSONElement,
            clickedComponent: Component?
        ) {
            runCommand(RemoveValueInModel(model, Pair(clickedObjectKey, clickedObjectValue), clickedComponent))
        }

    })

    val scrollPane = JScrollPane(editorView).apply {
        verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
    }

    val view = window {
        title = "Josue - JSON Object Editor"
        size = Dimension(600, 600)

        content {
            columns = 2
            +panel {
                +scrollPane
            }
            +textViewv
        }

    }
    view.open()

}
