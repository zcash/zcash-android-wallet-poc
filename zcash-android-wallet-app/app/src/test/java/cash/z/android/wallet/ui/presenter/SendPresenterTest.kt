package cash.z.android.wallet.ui.presenter

import cash.z.wallet.sdk.data.Synchronizer
import cash.z.wallet.sdk.ext.convertZatoshiToZecString
import cash.z.wallet.sdk.ext.safelyConvertToBigDecimal
import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.math.RoundingMode

@ExtendWith(MockitoExtension::class)
internal class SendPresenterTest {

    @Mock val view: SendPresenter.SendView = mock()
    lateinit var presenter: SendPresenter

    @BeforeEach
    fun setUp(@Mock synchronizer: Synchronizer) {
        presenter = SendPresenter(view, synchronizer)
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun headerUpdating_leadingZeros() {
        presenter.headerUpdating("007")
        verify(view).setSubheaderValue("0.14", true)
    }
    @Test
    fun headerUpdating_commas() {
        presenter.headerUpdating("1,000")
        verify(view).setSubheaderValue("20.38", true)
    }
    @Test
    fun headerUpdating_badInputCommas() {
        presenter.headerUpdating("34,5")
        assertTrue(presenter.sendUiModel.isUsdSelected)
        verify(view).setSubheaderValue("7.03", true)
    }
    @Test
    fun headerValidated_roundDown() {
        presenter.toggleCurrency()
        assertTrue(!presenter.sendUiModel.isUsdSelected, "zec should be selected to avoid testing conversions")
        presenter.headerValidated("1.1234561".safelyConvertToBigDecimal()!!)
        verify(view, atLeastOnce()).setHeaders(eq(false), eq("1.123456"), eq("55.13"))
    }
    @Test
    fun headerValidated_usdConversion() {
        assertTrue(presenter.sendUiModel.isUsdSelected, "expecting USD for this test")
        presenter.headerValidated("1000.045".safelyConvertToBigDecimal()!!)
        verify(view).setHeaders(eq(true), eq("1,000.04"), eq("20.379967"))
    }
    @Test
    fun headerValidated_roundUp() {
        presenter.toggleCurrency()
        assertTrue(!presenter.sendUiModel.isUsdSelected, "zec should be selected to avoid testing conversions")
        presenter.headerValidated("1.1234556".safelyConvertToBigDecimal()!!)
        verify(view).setHeaders(eq(false), eq("1.123456"), eq("55.13"))
    }
    @Test
    fun headerValidated_roundUpBankersRounding() {
        // banker's rounding follows odd up, even down
        // We'll encourage using that since it has good statistical properties and this rounding only happens in the UI
        presenter.toggleCurrency()
        assertTrue(!presenter.sendUiModel.isUsdSelected, "zec should be selected to avoid testing conversions")
        presenter.headerValidated("1.1234535".safelyConvertToBigDecimal()!!)
        assertEquals(112345350, presenter.sendUiModel.zecValue)
        assertEquals("1.123454", presenter.sendUiModel.zecValue.convertZatoshiToZecString(), "5 is odd, we should round up")

        presenter.headerValidated("1.1234565".safelyConvertToBigDecimal()!!)
        assertEquals(112345650, presenter.sendUiModel.zecValue)
        assertEquals("1.123456", presenter.sendUiModel.zecValue.convertZatoshiToZecString(), "6 is even, we should round down")
    }

    @Test
    fun parseSafely_commas() {
        assertEquals("3124", "3,124".safelyConvertToBigDecimal().toString())
    }

    @Test
    fun parseSafely_commasBad() {
        assertEquals("3124", ",3124".safelyConvertToBigDecimal().toString())
        assertEquals("3124", "3,124".safelyConvertToBigDecimal().toString())
        assertEquals("3124", "31,24".safelyConvertToBigDecimal().toString())
        assertEquals("3124", "312,4".safelyConvertToBigDecimal().toString())
        assertEquals("3124", "3124,".safelyConvertToBigDecimal().toString())
        assertEquals("3124", ",3,1,2,4,".safelyConvertToBigDecimal().toString())
    }

    @Test
    fun parseSafely_spaces() {
        assertEquals("3124", " 3124".safelyConvertToBigDecimal().toString())
        assertEquals("3124", "3 124".safelyConvertToBigDecimal().toString())
        assertEquals("3124", "31 24".safelyConvertToBigDecimal().toString())
        assertEquals("3124", "312 4".safelyConvertToBigDecimal().toString())
        assertEquals("3124", "3124 ".safelyConvertToBigDecimal().toString())
        assertEquals("3124", " 3 1 2 4 ".safelyConvertToBigDecimal().toString())
        assertEquals("3124", " 3    12 4 ".safelyConvertToBigDecimal().toString())
    }
}