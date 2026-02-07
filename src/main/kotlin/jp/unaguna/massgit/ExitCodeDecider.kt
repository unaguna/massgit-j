package jp.unaguna.massgit

interface ExitCodeDecider<An> {
    fun decideExitCode(results: List<GitProcessResult<An>>): Int
}
