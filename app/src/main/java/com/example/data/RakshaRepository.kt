package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RakshaRepository(private val scamDao: ScamDao) {

    val allHistory: Flow<List<ScamHistoryEntity>> = scamDao.getAllHistory()
    val allReports: Flow<List<ScamReportEntity>> = scamDao.getAllReports()
    val blacklist: Flow<List<BlacklistItemEntity>> = scamDao.getBlacklist()

    suspend fun insertHistory(item: ScamHistoryEntity): Long = withContext(Dispatchers.IO) {
        scamDao.insertHistory(item)
    }

    suspend fun deleteHistory(id: Int) = withContext(Dispatchers.IO) {
        scamDao.deleteHistoryById(id)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        scamDao.clearHistory()
    }

    suspend fun insertReport(item: ScamReportEntity): Long = withContext(Dispatchers.IO) {
        scamDao.insertReport(item)
    }

    suspend fun deleteReport(id: Int) = withContext(Dispatchers.IO) {
        scamDao.deleteReportById(id)
    }

    suspend fun checkLocalBlacklist(value: String): BlacklistItemEntity? = withContext(Dispatchers.IO) {
        scamDao.checkBlacklist(value)
    }

    suspend fun insertBlacklistItem(item: BlacklistItemEntity) = withContext(Dispatchers.IO) {
        scamDao.insertBlacklistItem(item)
    }

    suspend fun deleteBlacklist(id: Int) = withContext(Dispatchers.IO) {
        scamDao.deleteBlacklistById(id)
    }

    suspend fun prepopulateBlacklistIfEmpty() = withContext(Dispatchers.IO) {
        val currentList = scamDao.getBlacklist().firstOrNull()
        if (currentList.isNullOrEmpty()) {
            scamDao.insertBlacklistItem(
                BlacklistItemEntity(
                    type = "NUMBER",
                    value = "+91 98765 43210",
                    category = "Impersonation",
                    reason = "Fake Police Commissioner call requesting money to avoid arrest."
                )
            )
            scamDao.insertBlacklistItem(
                BlacklistItemEntity(
                    type = "NUMBER",
                    value = "+91 90123 45678",
                    category = "KYC Scam",
                    reason = "Fake SBI KYC Manager asking for OTP and online transaction approval."
                )
            )
            scamDao.insertBlacklistItem(
                BlacklistItemEntity(
                    type = "NUMBER",
                    value = "+91 88888 77777",
                    category = "Utility Bill Scam",
                    reason = "Fake Power Grid officer claiming electricity will be disconnected tonight."
                )
            )
            scamDao.insertBlacklistItem(
                BlacklistItemEntity(
                    type = "WEBSITE",
                    value = "http://sbi-kyc-verify.com",
                    category = "Phishing Website",
                    reason = "Fake banking portal designed to steal username, password, and transaction PIN."
                )
            )
            scamDao.insertBlacklistItem(
                BlacklistItemEntity(
                    type = "WEBSITE",
                    value = "https://verify-pan-income-tax.net",
                    category = "Phishing Website",
                    reason = "Fake Income Tax portal asking for credit card credentials to receive refunds."
                )
            )
            scamDao.insertBlacklistItem(
                BlacklistItemEntity(
                    type = "WEBSITE",
                    value = "http://parttimejobs-earn-now.in",
                    category = "Job Fraud",
                    reason = "Fake recruitment agency charging processing fees for non-existent virtual tasks."
                )
            )
        }
    }
}
