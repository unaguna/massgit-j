package jp.unaguna.massgit.common.args

interface Options<D : OptionDef> : Map<D, List<Option<D>>> {
    /**
     * Returns the specified option argument instance.
     *
     * @return an option instance. If the option is not used, returns empty list.
     */
    fun of(key: D): List<Option<D>>

    /**
     * Returns the specified option argument instance.
     *
     * @return only one option instance
     * @throws IllegalArgumentException if zero or more two options specified
     */
    fun getOne(key: D): Option<D> {
        val result = of(key)
        return if (result.size == 1) result[0] else throw IllegalArgumentException(key.representativeName)
    }

    /**
     * Returns the specified option argument instance.
     *
     * @return only one option instance
     * @throws IllegalArgumentException if zero or more two options specified
     */
    fun getOneOrNull(key: D): Option<D>? {
        val result = of(key)
        return if (result.size == 1) result[0] else null
    }

    companion object {
        fun <D : OptionDef> build(
            args: List<String>,
            optionDefProvider: OptionDefProvider<D>
        ): Pair<Options<D>, List<String>> {
            val mainOptions = OptionsImpl(optionDefProvider)
            val remainingArgs = mutableListOf<String>()

            var processingOption: OptionImpl<D>? = null
            var stillOption = true
            val argsBuffer = ArrayDeque(args)
            while (stillOption && argsBuffer.isNotEmpty()) {
                // Determine whether optional arguments continue
                if (processingOption != null) {
                    // If it has already received a sufficient number of arguments,
                    // it stops accepting arguments for that option.
                    if (processingOption.argsNumberIsSufficient()) {
                        processingOption = null
                    }
                }

                val arg = argsBuffer.removeFirst()
                if (arg.first() != '-') {
                    if (processingOption != null) {
                        processingOption.push(arg)
                    } else {
                        remainingArgs.add(arg)
                        stillOption = false
                    }
                } else {
                    processingOption = mainOptions.addOption(arg)
                }
            }
            remainingArgs.addAll(argsBuffer)

            return Pair(mainOptions, remainingArgs)
        }
    }
}

class OptionsImpl<D : OptionDef> private constructor(
    private val optionDefProvider: OptionDefProvider<D>,
    private val optionMap: MutableMap<D, MutableList<Option<D>>>,
) : Map<D, List<Option<D>>> by optionMap, Options<D> {
    constructor(optionDefProvider: OptionDefProvider<D>) : this(optionDefProvider, mutableMapOf())

    fun addOption(optionDef: D): OptionImpl<D> {
        val options = optionMap.getOrPut(optionDef) { mutableListOf() }
        val option = OptionImpl(optionDef)
        options.add(option)
        return option
    }

    fun addOption(optionSpec: String): OptionImpl<D> {
        val optionParts = optionSpec.split("=", limit = 2)
        val optionName = optionParts[0]
        val optionValue = optionParts.getOrNull(1)

        val optionDef = optionDefProvider.getOptionDef(optionName)

        val option = addOption(optionDef)
        if (optionValue != null) {
            option.push(optionValue)
        }
        return option
    }

    override fun of(key: D): List<Option<D>> {
        return this[key] ?: emptyList()
    }

    override fun toString(): String {
        return optionMap.toString()
    }
}

interface OptionDefProvider<D : OptionDef> {
    fun getOptionDef(name: String): D
}
