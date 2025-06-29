package com.mac350.repositories

import com.mac350.plugins.suspendTransaction
import at.favre.lib.crypto.bcrypt.BCrypt
import com.mac350.tables.*
import io.ktor.util.*
import kotlin.text.toCharArray

class AccountRepository {
    companion object {
        fun hashPw(pw: String): String {
            return BCrypt.withDefaults().hashToString(12, pw.toCharArray())
        }

        fun checkPw(pw: String, hashPw: String): Boolean {
            return BCrypt.verifyer().verify(pw.toCharArray(), hashPw).verified
        }

        suspend fun getAccountByEmail(email: String): AccountDAO? = suspendTransaction {
            AccountDAO.find { AccountT.email eq email }.firstOrNull()
        }

        suspend fun newAccount(
            username: String,
            email: String,
            password: String,
            type: String,
            date: String
        ): AccountDAO = suspendTransaction {
            val hashed = hashPw(password)
            AccountDAO.new {
                this.username = username
                this.email = email
                this.password = hashed
                this.type = type
                this.createdAt = date
                this.updatedAt = date
                this.lastRun = date
            }
        }

        suspend fun newAdmin(account: AccountDAO): AdminDAO = suspendTransaction {
            AdminDAO.new {
                this.account = account
                this.root = false
            }
        }

        suspend fun newCarrocaBoy(account: AccountDAO): CarrocaBoyDAO = suspendTransaction {
            CarrocaBoyDAO.new {
                this.account = account
                this.totalComissions = 0
            }
        }

        suspend fun newCashier(account: AccountDAO, section: Long): CashierDAO = suspendTransaction {
            CashierDAO.new {
                this.account = account
                this.totalComissions = 0
                this.section = section
            }
        }

        suspend fun newClient(account: AccountDAO, address: String): ClientDAO = suspendTransaction {
            ClientDAO.new {
                this.account = account
                this.isSpecialClient = false
                this.address = address
            }
        }
    }
}