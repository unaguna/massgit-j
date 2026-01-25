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
    }
}

private class IllegalGitConfigPairException(value: String) :
    MassgitException("illegal git config specification: $value")
