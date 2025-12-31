package jp.unaguna.massgit.testcommon.assertion

fun assertNotContains(charSequence: CharSequence, substring: CharSequence) {
    assert(!charSequence.contains(substring)) {
        "Expected the char sequence NOT to contain the substring.\n" +
            "CharSequence <$charSequence>, substring <$substring>."
    }
}
