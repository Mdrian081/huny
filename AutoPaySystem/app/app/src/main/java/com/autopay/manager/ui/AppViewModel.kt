package com.autopay.manager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.autopay.manager.data.BalanceInfo
import com.autopay.manager.data.FirebaseRepository
import com.autopay.manager.data.Transaction
import com.autopay.manager.data.TopUpOrder
import com.autopay.manager.data.WithdrawRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppViewModel(private val repository: FirebaseRepository) : ViewModel() {

    class Factory(private val repository: FirebaseRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AppViewModel(repository) as T
        }
    }

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _withdrawals = MutableStateFlow<List<WithdrawRequest>>(emptyList())
    val withdrawals: StateFlow<List<WithdrawRequest>> = _withdrawals.asStateFlow()

    private val _topups = MutableStateFlow<List<TopUpOrder>>(emptyList())
    val topups: StateFlow<List<TopUpOrder>> = _topups.asStateFlow()

    private val _balances = MutableStateFlow<List<BalanceInfo>>(emptyList())
    val balances: StateFlow<List<BalanceInfo>> = _balances.asStateFlow()

    init {
        viewModelScope.launch { repository.observeTransactions().collect { _transactions.value = it } }
        viewModelScope.launch { repository.observeWithdrawals().collect { _withdrawals.value = it } }
        viewModelScope.launch { repository.observeTopUpOrders().collect { _topups.value = it } }
        viewModelScope.launch { repository.observeBalances().collect { _balances.value = it } }
    }

    fun addManualTransaction(tx: Transaction) = viewModelScope.launch {
        repository.addTransaction(tx)
    }

    fun requestWithdraw(request: WithdrawRequest) = viewModelScope.launch {
        repository.requestWithdraw(request)
    }

    fun placeTopUpOrder(order: TopUpOrder) = viewModelScope.launch {
        repository.addTopUpOrder(order)
    }

    fun markTransactionUsed(id: String) = viewModelScope.launch {
        repository.markTransactionUsed(id)
    }

    fun setBalanceQuick(method: String, amount: Double) = viewModelScope.launch {
        repository.setBalance(BalanceInfo(method = method, currentBalance = amount))
    }

    private val _apiKey = MutableStateFlow<String?>(null)
    val apiKey: StateFlow<String?> = _apiKey.asStateFlow()

    init {
        viewModelScope.launch { _apiKey.value = repository.getApiKey() }
    }

    fun generateApiKey() = viewModelScope.launch {
        val newKey = (1..32)
            .map { "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".random() }
            .joinToString("")
        repository.setApiKey(newKey)
        _apiKey.value = newKey
    }
}
