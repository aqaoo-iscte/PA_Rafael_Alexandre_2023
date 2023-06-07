package Fase2


import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*


class JSONEditorView(private val model: JSONObject) : JPanel() {

    private val observers: MutableList<JSONEditViewObserver> = mutableListOf()
    var clickedKey: String = ""

    init {
        layout = GridLayout(0, 1)
        model.properties.forEach {
            addElement(it.key, it.value)
        }

//            override fun visit(jsonArray: JSONArray2): Boolean {
//                jsonArray.elements.forEach { element ->
//                    try {
//                        val jsonObject = element as JSONObject2
//                        jsonObject.properties.forEach {
//                            addElement(it.key, it.value)
//                        }
//                    } catch (e: ClassCastException) {
//                        // Handle the case where the element is not of type JSONObject2
//                    }
//                }
//
//                return true
//            }


        model.addObserver(object : JSONElementObserver {
            override fun jsonElementAdded(
                clickedKey: String,
                jsonElement: Pair<String, JSONElement>,
                clickedComponent: Component
            ) =
                addNewElement(clickedKey, jsonElement.first, jsonElement.second, clickedComponent)

            override fun jsonElementRemoved(jsonElement: Pair<String, JSONElement>, clickedComponent: Component?) =
                removeElement(jsonElement.first, jsonElement.second, clickedComponent)

            override fun jsonElementReplaced(jsonElementOld: Pair<String, JSONElement>, jsonElementNew: JSONElement) =
                replaceElement(jsonElementOld, jsonElementNew)
        })

        model.properties.values.filterIsInstance<JSONArray>().forEach { jsonArray ->
            jsonArray.addObserver(object : JSONElementObserver {
                override fun jsonElementAdded(
                    clickedKey: String,
                    jsonElement: Pair<String, JSONElement>,
                    clickedComponent: Component
                ) =
                    addNewElement(clickedKey, jsonElement.first, jsonElement.second, clickedComponent)

                override fun jsonElementRemoved(jsonElement: Pair<String, JSONElement>, clickedComponent: Component?) =
                    removeElement(jsonElement.first, jsonElement.second, clickedComponent)

                override fun jsonElementReplaced(
                    jsonElementOld: Pair<String, JSONElement>,
                    jsonElementNew: JSONElement
                ) =
                    replaceElement(jsonElementOld, jsonElementNew)
            })
        }

        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    val clickedComponent = getComponentAt(e.point)
                    if (clickedComponent is ElementsInBoard) {
                        val clickedBox = clickedComponent
                        val clickedKey = clickedBox.key
                        val clickedValue = clickedBox.value
                        val menu = JPopupMenu("Message")
                        val add = JButton("add")
                        add.addActionListener {
                            val text = dualPrompt("New Values", "key", "value", "", "")!!
                            val i: JSONObject = when {
                                text.second.toIntOrNull() != null -> {
                                    JSONObject(mutableMapOf(text.first to JSONNumber(text.second.toInt())))
                                }

                                text.second.toDoubleOrNull() != null -> {
                                    JSONObject(mutableMapOf(text.first to JSONNumber(text.second.toDouble())))
                                }

                                text.second == "true" || text.second == "false" -> {
                                    JSONObject(mutableMapOf(text.first to JSONBoolean(text.second.toBoolean())))
                                }

                                text.second == "null" -> {
                                    JSONObject(mutableMapOf(text.first to JSONNull))
                                }

                                else -> JSONObject(mutableMapOf(text.first to JSONString(text.second)))
                            }
                            observers.forEach {
                                it.jsonElementAdded(
                                    clickedKey,
                                    i.properties.keys.first(),
                                    i.properties.values.first(),
                                    clickedBox
                                )
                            }
                            menu.isVisible = false
                            revalidate()
                        }
                        val del = JButton("delete")
                        del.addActionListener {
                            observers.forEach {
                                it.jsonElementRemove(clickedKey, clickedValue, clickedBox)
                            }
                        }
                        menu.add(add)
                        menu.add(del)
                        menu.show(e.component, e.x, e.y)
                    }
                }
            }
        })
    }

    fun addObserver(observer: JSONEditViewObserver) {
        observers.add(observer)
    }

    fun addElement(key: String, value: JSONElement, father: JSONArray? = null, index: Int? = -1) {
        add(ElementsInBoard(key, value, father, index))
        if (value is JSONArray) {
            var index = 0
            value.elements.forEach {
                add(JSONArrayInBoard(key, it, value, index))
                index++
            }

        }
        revalidate()
        repaint()
    }


    fun addNewElement(clickedKey: String, key: String, value: JSONElement, clickedComponent: Component) {
        val father = (clickedComponent as ElementsInBoard).father
        val index = clickedComponent.index
        val clickedElement = model.properties[clickedKey]
        if (clickedElement is JSONArray && father == null) {
            val lastIndex =
                components.indexOfLast { it is ElementsInBoard && it.father == clickedComponent.value }
                add(ElementsInBoard(key,(value as JSONObject).properties.values.first(), clickedElement, clickedElement.elements.size - 1), lastIndex + 1
            )
        } else if (father != null) {
            val lastIndex =
                components.indexOfFirst { it is ElementsInBoard && it.matchesHash(clickedComponent.hashCode()) }
            add(ElementsInBoard(key, value, father, index), lastIndex + 1)
        } else {
            val lastIndex =
                components.indexOfLast { it is ElementsInBoard && it.matchesHash(clickedComponent.hashCode()) }
            val insertIndex = lastIndex + 1
            add(ElementsInBoard(key, value, null), insertIndex)
        }
        revalidate()

    }


    fun replaceElement(old: Pair<String, JSONElement>, new: JSONElement) {
        val find = components.find { it is ElementsInBoard && it.matches(old.second) } as? ElementsInBoard
        find?.let { find.modify(new) }
        revalidate()
    }

    fun removeElement(clickedObjectKey: String, clickedObjectValue: JSONElement, clickedComponent: Component?) {
        val find =
            components.find { it is ElementsInBoard && clickedComponent.hashCode() == it.hashCode() } as? ElementsInBoard
        find?.let { find.remove() }
        if (find?.value is JSONArray) {
            val find2 =
                components.filter { it is ElementsInBoard && it.isFather(clickedObjectValue as JSONArray) } as List<ElementsInBoard?>
            find2.forEach {
                it?.remove()
            }
        }
        revalidate()
    }

    inner class JSONArrayInBoard(
        val key: String,
        private var value: JSONElement,
        private val father: JSONArray,
        private val index: Int
    ) :
        JPanel() {

        private val element = value

        init {
            when (element) {
                is JSONObject -> {
                    element.properties.forEach {
                        addElement(it.key, it.value, father, index)
                    }
                }
            }
        }

    }


    inner class ElementsInBoard(val key: String, var value: JSONElement, val father: JSONArray?, val index: Int? = -1) :
        JPanel() {
        var field: JComponent? = null
        private val element = value

        init {

            layout = BoxLayout(this, BoxLayout.X_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            alignmentY = Component.TOP_ALIGNMENT

            if (element !is JSONArray && element !is JSONObject) {
                add(JLabel(key))
            }

            when (element) {
                is JSONBoolean -> {
                    field = JCheckBox("", element.value)
                }

                is JSONString -> {
                    field = JFormattedTextField(element.value)
                }

                is JSONNumber -> {
                    field = JFormattedTextField(element.value)
                }

                is JSONNull -> {
                    field = JFormattedTextField(element.toString())
                }

                else -> {
                    val keyPanel = JPanel(FlowLayout(FlowLayout.CENTER))
                    keyPanel.add(JLabel(key))
                    add(keyPanel)
                }
            }

            if (field != null) {
                add(field)
            }

            field?.preferredSize = Dimension(200, 30)
            field?.addKeyListener(KeyClick(value))
        }


        fun modify(new: JSONElement) {
            if (field is JFormattedTextField && new is JSONBoolean) {
                val container = field!!.parent
                container.remove(field)
                field = JCheckBox("", new.value)
                (field as JCheckBox).addKeyListener(KeyClick(new))
                container.add(field)
                container.revalidate()
                container.repaint()
            }
            value = new
        }

        fun remove() {
            val parent = parent
            parent?.remove(this)

        }

        fun isFather(p: JSONArray): Boolean {
            return father == p
        }

        fun getHash() = value.hashCode()
        fun matches(p: JSONElement?) = p == value
        fun matchesKey(p: String?) = p == key
        fun matchesHash(p: Int) = p == this.hashCode()

        inner class KeyClick(private val value: JSONElement) : KeyAdapter() {
            override fun keyPressed(e: KeyEvent?) {
                if (e?.keyCode == KeyEvent.VK_ENTER) {
                    val newValue: Any = when (field) {
                        is JCheckBox -> (field as JCheckBox).isSelected
                        is JFormattedTextField -> (field as JFormattedTextField).text
                        else -> JSONNull
                    }
                    val newElement2: JSONElement = when (newValue) {
                        is String -> {
                            if (newValue.contains(".") || newValue.contains(",")) {
                                createModelFromObject(newValue.toDouble())
                            } else if (newValue.toIntOrNull() != null) {
                                createModelFromObject(newValue.toInt())
                            } else if (newValue == "true" || newValue == "false") {
                                createModelFromObject(newValue.toBoolean())
                            } else if (newValue == "null") {
                                JSONNull
                            } else {
                                createModelFromObject(newValue)
                            }
                        }

                        is Boolean -> createModelFromObject(newValue)

                        else -> JSONNull
                    }
                    newElement2.let {
                        observers.forEach {
                            it.jsonElementModified(clickedKey, Pair(key, value), newElement2, this@ElementsInBoard)
                        }
                    }
                }
            }
        }
    }
}

interface JSONEditViewObserver {
    fun jsonElementModified(
        clickedKey: String,
        old: Pair<String, JSONElement>,
        new: JSONElement,
        clickedComponent: Component?
    ) {
    }

    fun jsonElementAdded(clickedKey: String, key: String, value: JSONElement, clickedComponent: Component) {}
    fun jsonElementRemove(clickedObjectKey: String, clickedObjectValue: JSONElement, clickedComponent: Component?) {}
}