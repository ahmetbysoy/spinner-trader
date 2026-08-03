package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CryptoDatabase
import com.example.data.model.QuantStrategy
import com.example.data.repository.MarketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MarketViewModel(application: Application) : AndroidViewModel(application) {

    private val db = CryptoDatabase.getDatabase(application)
    val repository = MarketRepository(
        backtestDao = db.backtestDao(),
        alertDao = db.alertDao(),
        externalScope = viewModelScope
    )

    val tickers = repository.tickers
    val selectedSymbol = repository.selectedSymbol
    val orderBook = repository.orderBook
    val trades = repository.trades
    val cvdHistory = repository.cvdHistory
    val strategies = repository.strategies

    val savedBacktests = repository.savedBacktests.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val whaleAlerts = repository.whaleAlerts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Backtest Running State
    private val _isBacktesting = MutableStateFlow(false)
    val isBacktesting: StateFlow<Boolean> = _isBacktesting.asStateFlow()

    private val _lastBacktestResult = MutableStateFlow<String?>(null)
    val lastBacktestResult: StateFlow<String?> = _lastBacktestResult.asStateFlow()

    fun selectSymbol(symbol: String) {
        repository.selectTicker(symbol)
    }

    fun runBacktest(strategy: QuantStrategy) {
        viewModelScope.launch {
            _isBacktesting.value = true
            val result = repository.runStrategyBacktest(strategy, selectedSymbol.value)
            _lastBacktestResult.value = "${result.verdict}: Net ${result.netProfitPercent}% | Win ${result.winRatePercent}% | p99 ${result.p99LatencyMs}ms"
            _isBacktesting.value = false
        }
    }

    fun clearAlerts() {
        viewModelScope.launch {
            repository.clearAlerts()
        }
    }

    fun clearBacktests() {
        viewModelScope.launch {
            repository.clearBacktests()
        }
    }
}
