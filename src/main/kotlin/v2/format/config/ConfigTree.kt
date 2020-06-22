package v2.format.config

class ConfigTree {
    private val root = ConfigNode(FormatOptions(), HashMap())

    fun insert(levels: MutableList<String>, optionsLayer: FormatOptionsLayer) {
        insert(root, root.options!!, levels, optionsLayer)
    }

    private fun insert(
        node: ConfigNode,
        currOption: FormatOptions,
        levels: MutableList<String>,
        optionsLayer: FormatOptionsLayer
    ) {
        if (levels.isEmpty()) {
            node.options = optionsLayer.apply(currOption)
        } else {
            val key = levels.removeAt(0)
            val newOption = node.options ?: currOption

            if (node.hasKey(key)) {
                insert(node.children.getValue(key), newOption, levels, optionsLayer)
            } else {
                val child = ConfigNode(null, HashMap())
                node.children[key] = child

                insert(child, newOption, levels, optionsLayer)
            }
        }
    }

    operator fun get(levels: List<String>): FormatOptions {
        var curr = root
        var options = curr.options!!

        for (key in levels) {
            if (curr.hasKey(key)) {
                curr = curr.children.getValue(key)
                if (curr.options != null) {
                    options = curr.options!!
                }
            } else {
                break
            }
        }

        return options
    }
}

class ConfigNode(var options: FormatOptions?, val children: MutableMap<String, ConfigNode>) {
    fun hasKey(key: String) = children.containsKey(key)
}
