package cash.z.android.wallet.sample

import cash.z.android.qrecycler.QScanner

class SampleQrScanner : QScanner {
    override fun scanBarcode(callback: (Result<String>) -> Unit) {
        callback(Result.success("sampleqrcode_scan_success"))
    }
}