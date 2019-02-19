package cash.z.android.wallet.extention

internal inline fun tryIgnore(block: () -> Unit) {
    try { block() } catch(ignored: Throwable) {}
}

internal inline fun <T> tryNull(block: () -> T): T? {
    return try { block() } catch(ignored: Throwable) { null }
}

internal inline fun String.truncate(): String {
    return "${substring(0..4)}...${substring(length-5, length)}"
}