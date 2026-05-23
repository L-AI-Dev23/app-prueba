package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Budget
import com.example.data.model.Expense
import com.example.data.model.SyncLog
import com.example.data.model.UserAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {

    // --- USERS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccount)

    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserAccount?

    // --- EXPENSES ---
    @Query("SELECT * FROM expenses WHERE userEmail = :userEmail ORDER BY dateMillis DESC")
    fun getAllExpensesFlow(userEmail: String): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE userEmail = :userEmail")
    suspend fun getAllExpensesDirect(userEmail: String): List<Expense>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpensesList(expenses: List<Expense>)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expenses WHERE userEmail = :userEmail")
    suspend fun clearAllExpensesForUser(userEmail: String)


    // --- BUDGETS ---
    @Query("SELECT * FROM budgets WHERE userEmail = :userEmail")
    fun getAllBudgetsFlow(userEmail: String): Flow<List<Budget>>

    @Query("SELECT * FROM budgets WHERE userEmail = :userEmail")
    suspend fun getAllBudgetsDirect(userEmail: String): List<Budget>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetsList(budgets: List<Budget>)

    @Delete
    suspend fun deleteBudget(budget: Budget)

    @Query("DELETE FROM budgets WHERE userEmail = :userEmail")
    suspend fun clearAllBudgetsForUser(userEmail: String)


    // --- SYNC LOGS ---
    @Query("SELECT * FROM sync_logs WHERE email = :email ORDER BY timestamp DESC")
    fun getAllSyncLogsFlow(email: String): Flow<List<SyncLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncLog(syncLog: SyncLog)

    @Delete
    suspend fun deleteSyncLog(syncLog: SyncLog)

    @Query("DELETE FROM sync_logs WHERE email = :email")
    suspend fun clearAllSyncLogsForUser(email: String)
}
