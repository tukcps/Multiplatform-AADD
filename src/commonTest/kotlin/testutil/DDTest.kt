package testutil

import io.github.tukcps.aadd.DDBuilder

/**
 * Runs a test given as parameter lambda in the context of a DDBuilder
 */
fun ddTest( test: DDBuilder.() -> Unit ) {
    DDBuilder {
        test()
    }
}
