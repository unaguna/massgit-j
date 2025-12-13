package jp.unaguna.massgit

import jp.unaguna.massgit.common.args.Option
import jp.unaguna.massgit.common.args.Options

class MassgitOptions(
    private val options: Options<MassgitOptionsDef>,
) : Map<MassgitOptionsDef, List<Option<MassgitOptionsDef>>> by options {
    fun getRepSuffix() = options.getOneOrNull(MassgitOptionsDef.REP_SUFFIX)?.getOneArg()
}
