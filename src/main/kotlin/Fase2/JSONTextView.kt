package Fase2

import java.awt.Component
import java.awt.GridLayout
import javax.swing.JLabel
import javax.swing.JTextArea

class JSONTextView(private val model: JSONObject) : JLabel() {

    private val textArea: JTextArea = JTextArea()

    init {

        layout = GridLayout(0, 1)

        refresh()
        model.addObserver(object : JSONElementObserver {
            override fun jsonElementAdded(
                clickedKey: String,
                jsonElement: Pair<String, JSONElement>,
                clickedComponent: Component
            ) = refresh()

            override fun jsonElementRemoved(jsonElement: Pair<String, JSONElement>, clickedComponent: Component?) =
                refresh()

            override fun jsonElementReplaced(jsonElementOld: Pair<String, JSONElement>, jsonElementNew: JSONElement) =
                refresh()
        })

        model.properties.values.filterIsInstance<JSONArray>().forEach { jsonArray ->
            jsonArray.addObserver(object : JSONElementObserver {
                override fun jsonElementAdded(
                    clickedKey: String,
                    jsonElement: Pair<String, JSONElement>,
                    clickedComponent: Component
                ) = refresh()

                override fun jsonElementRemoved(jsonElement: Pair<String, JSONElement>, clickedComponent: Component?) =
                    refresh()

                override fun jsonElementReplaced(
                    jsonElementOld: Pair<String, JSONElement>,
                    jsonElementNew: JSONElement
                ) = refresh()
            })
        }
    }

    private fun refresh() {
        val visitor = JSONTextBuilder()
        model.accept(visitor)
        val jsonText = visitor.getJsonString()

        if (textArea.parent != null) {
            remove(textArea)
        }

        textArea.text = jsonText
        add(textArea)
        revalidate()
        repaint()
    }
}