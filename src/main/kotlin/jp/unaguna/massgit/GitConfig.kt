package jp.unaguna.massgit

import jp.unaguna.massgit.exception.MassgitException

data class GitConfig(val name: String, val value: String) {
    fun toStringAsArg() = "$name=$value"

    companion object {
        fun fromArg(arg: String): GitConfig {
            val parts = arg.split('=', limit = 2)
            if (parts.size != 2) {
                throw IllegalGitConfigPairException(arg)
            }

            return GitConfig(parts[0], parts[1])
        }

        fun mergeLists(vararg lists: List<GitConfig>): List<GitConfig> {
            val mergedMap = mutableMapOf<String, GitConfig>()
            for (list in lists.reversed()) {
                for (config in list) {
                    mergedMap[config.name] = config
                }
            }

            return mergedMap.asSequence()
                .sortedBy { it.key }
                .map { it.value }
                .toList()
        }
    }
}

private class IllegalGitConfigPairException(value: String) :
    MassgitException("illegal git config specification: $value")
