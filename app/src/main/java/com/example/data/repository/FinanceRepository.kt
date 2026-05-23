package com.example.data.repository

import com.example.data.local.FinanceDao
import com.example.data.model.Budget
import com.example.data.model.Expense
import com.example.data.model.SyncLog
import com.example.data.model.UserAccount
import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val financeDao: FinanceDao) {

    // --- USERS ---
    suspend fun insertUser(user: UserAccount) = financeDao.insertUser(user)

    suspend fun getUserByEmail(email: String): UserAccount? = financeDao.getUserByEmail(email)

    // --- EXPENSES ---
    fun getExpensesFlow(userEmail: String): Flow<List<Expense>> = financeDao.getAllExpensesFlow(userEmail)

    suspend fun getAllExpensesDirect(userEmail: String): List<Expense> = financeDao.getAllExpensesDirect(userEmail)

    suspend fun insertExpense(expense: Expense) = financeDao.insertExpense(expense)

    suspend fun insertExpensesList(expenses: List<Expense>) = financeDao.insertExpensesList(expenses)

    suspend fun deleteExpense(expense: Expense) = financeDao.deleteExpense(expense)

    suspend fun clearExpenses(userEmail: String) = financeDao.clearAllExpensesForUser(userEmail)


    // --- BUDGETS ---
    fun getBudgetsFlow(userEmail: String): Flow<List<Budget>> = financeDao.getAllBudgetsFlow(userEmail)

    suspend fun getAllBudgetsDirect(userEmail: String): List<Budget> = financeDao.getAllBudgetsDirect(userEmail)

    suspend fun insertBudget(budget: Budget) = financeDao.insertBudget(budget)

    suspend fun insertBudgetsList(budgets: List<Budget>) = financeDao.insertBudgetsList(budgets)

    suspend fun deleteBudget(budget: Budget) = financeDao.deleteBudget(budget)

    suspend fun clearBudgets(userEmail: String) = financeDao.clearAllBudgetsForUser(userEmail)


    // --- SYNC LOGS ---
    fun getSyncLogsFlow(email: String): Flow<List<SyncLog>> = financeDao.getAllSyncLogsFlow(email)

    suspend fun insertSyncLog(syncLog: SyncLog) = financeDao.insertSyncLog(syncLog)

    suspend fun deleteSyncLog(syncLog: SyncLog) = financeDao.deleteSyncLog(syncLog)

    suspend fun clearSyncLogs(email: String) = financeDao.clearAllSyncLogsForUser(email)
}
