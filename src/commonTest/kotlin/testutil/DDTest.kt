package testutil

import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.dd.DD

/**
 * Runs a test given as parameter lambda in the context of a DDBuilder
 */
fun ddTest( test: DDBuilder.() -> Unit ) {
    DDBuilder {
        test()
    }
}
