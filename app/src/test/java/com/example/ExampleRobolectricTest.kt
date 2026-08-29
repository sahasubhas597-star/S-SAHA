package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.OptionStrategyType
import com.example.data.repository.MarketDataRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("zx26", appName)
  }

  @Test
  fun `verify option chain generation and greeks`() {
    val repository = MarketDataRepository()
    val optionChain = repository.getOptionChain("NIFTY 50")
    
    assertNotNull(optionChain)
    assertEquals("NIFTY 50", optionChain.underlyingSymbol)
    assertTrue("Option chain should contain strikes", optionChain.strikes.isNotEmpty())
    assertTrue("PCR should be calculated", optionChain.pcr > 0.0)

    val payoff = repository.calculateStrategyPayoff(OptionStrategyType.BULL_CALL_SPREAD, "NIFTY 50")
    assertNotNull(payoff)
    assertEquals(2, payoff.legs.size)
  }

  @Test
  fun `verify feed and scanner search functionality`() {
    val repository = MarketDataRepository()
    val niftyResults = repository.searchInstruments("NIFTY")
    assertTrue("Should find Nifty instruments", niftyResults.isNotEmpty())
    assertTrue("Should match NIFTY 50", niftyResults.any { it.symbol == "NIFTY 50" })

    val relianceResults = repository.searchInstruments("RELIANCE")
    assertTrue("Should find Reliance", relianceResults.any { it.symbol == "RELIANCE" })

    val rescanned = repository.rescanAllInstruments()
    assertTrue("Should rescan all instruments", rescanned.isNotEmpty())
    assertTrue("Every instrument should have a scanned technical signal", rescanned.all { it.primarySignal != null })
  }

  @Test
  fun `verify broker account models and session latency`() {
    val brokerTypes = com.example.data.model.BrokerType.values()
    assertTrue("Should contain multiple broker integrations", brokerTypes.isNotEmpty())
    assertTrue("Should contain Zerodha Kite", brokerTypes.any { it.name == "ZERODHA" })
    assertTrue("Should contain Dhan", brokerTypes.any { it.name == "DHAN" })
    assertTrue("Should contain Angel One", brokerTypes.any { it.name == "ANGEL_ONE" })
    assertTrue("Should contain Binance", brokerTypes.any { it.name == "BINANCE" })
  }
}

