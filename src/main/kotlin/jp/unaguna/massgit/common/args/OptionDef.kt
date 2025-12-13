package jp.unaguna.massgit.common.args

interface OptionDef {
    val representativeName: String
    fun sufficient(actualNum: Int): Boolean
}
