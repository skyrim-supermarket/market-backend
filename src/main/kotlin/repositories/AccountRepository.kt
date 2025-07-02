package com.mac350.repositories

import com.mac350.plugins.suspendTransaction
import at.favre.lib.crypto.bcrypt.BCrypt
import com.mac350.models.Admin
import com.mac350.models.CarrocaBoy
import com.mac350.models.Cashier
import com.mac350.models.Client
import com.mac350.tables.*
import io.ktor.util.*
import kotlin.text.toCharArray

class AccountRepository {
    companion object {
        val reqFields = mapOf(
            "Admins" to listOf("username", "email", "password", "root"),
            "Cashiers" to listOf("username", "email", "password", "root", "section"),
            "Carrocaboys" to listOf("username", "email", "password", "root")
        )

        fun hashPw(pw: String): String {
            return BCrypt.withDefaults().hashToString(12, pw.toCharArray())
        }

        fun checkPw(pw: String, hashPw: String): Boolean {
            return BCrypt.verifyer().verify(pw.toCharArray(), hashPw).verified
        }

        suspend fun getAdmins(): List<Admin> = suspendTransaction {
            AdminDAO.all().map(::daoToAdmin)
        }

        suspend fun getCarrocaBoys(): List<CarrocaBoy> = suspendTransaction {
            CarrocaBoyDAO.all().map(::daoToCarrocaBoy)
        }

        suspend fun getCashiers(): List<Cashier> = suspendTransaction {
            CashierDAO.all().map(::daoToCashier)
        }

        suspend fun getClients(): List<Client> = suspendTransaction {
            ClientDAO.all().map(::daoToClient)
        }

        suspend fun getAccountByEmail(email: String): AccountDAO? = suspendTransaction {
            AccountDAO.find { AccountT.email eq email }.firstOrNull()
        }

        suspend fun getAdminById(accountId: Int): AdminDAO? = suspendTransaction {
            val account = AccountDAO.find { AccountT.id eq accountId }.firstOrNull()
            account?.let { acc ->
                AdminDAO.find { AdminT.account eq acc.id }.firstOrNull()
            }
        }

        suspend fun getAdminByEmail(email: String): AdminDAO? = suspendTransaction {
            val account = AccountDAO.find { AccountT.email eq email }.firstOrNull()
            account?.let { acc ->
                AdminDAO.find { AdminT.account eq acc.id }.firstOrNull()
            }
        }

        suspend fun getClientById(accountId: Int): ClientDAO? = suspendTransaction {
            val account = AccountDAO.find { AccountT.id eq accountId }.firstOrNull()
            account?.let { acc ->
                ClientDAO.find { ClientT.account eq acc.id }.firstOrNull()
            }
        }

        suspend fun getClientByEmail(email: String): ClientDAO? = suspendTransaction {
            val account = AccountDAO.find { AccountT.email eq email }.firstOrNull()
            account?.let { acc ->
                ClientDAO.find { ClientT.account eq acc.id }.firstOrNull()
            }
        }

        suspend fun getCashierById(accountId: Int): CashierDAO? = suspendTransaction {
            val account = AccountDAO.find { AccountT.id eq accountId }.firstOrNull()
            account?.let { acc ->
                CashierDAO.find { CashierT.account eq acc.id }.firstOrNull()
            }
        }

        suspend fun getCashierByEmail(email: String): CashierDAO? = suspendTransaction {
            val account = AccountDAO.find { AccountT.email eq email }.firstOrNull()
            account?.let { acc ->
                CashierDAO.find { CashierT.account eq acc.id }.firstOrNull()
            }
        }

        suspend fun getCarrocaBoyById(accountId: Int): CarrocaBoyDAO? = suspendTransaction {
            val account = AccountDAO.find { AccountT.id eq accountId }.firstOrNull()
            account?.let { acc ->
                CarrocaBoyDAO.find { CarrocaBoyT.account eq acc.id }.firstOrNull()
            }
        }

        suspend fun getCarrocaBoyByEmail(email: String): CarrocaBoyDAO? = suspendTransaction {
            val account = AccountDAO.find { AccountT.email eq email }.firstOrNull()
            account?.let { acc ->
                CarrocaBoyDAO.find { CarrocaBoyT.account eq acc.id }.firstOrNull()
            }
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

        suspend fun newAdmin(account: AccountDAO, root: Boolean): AdminDAO = suspendTransaction {
            AdminDAO.new {
                this.account = account
                this.root = root
            }
        }

        suspend fun newCarrocaBoy(account: AccountDAO): CarrocaBoyDAO = suspendTransaction {
            CarrocaBoyDAO.new {
                this.account = account
                this.totalCommissions = 0
            }
        }

        suspend fun newCashier(account: AccountDAO, section: Long): CashierDAO = suspendTransaction {
            CashierDAO.new {
                this.account = account
                this.totalCommissions = 0
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

        suspend fun setLastRun(account: AccountDAO, date: String) = suspendTransaction {
            account.lastRun = date
        }

        suspend fun addCommissionToEmployee(employee: Any, totalPrice: Long, date: String) = suspendTransaction {
            when(employee) {
                is CashierDAO -> {
                    employee.totalCommissions += totalPrice/10
                    employee.account.updatedAt = date
                }
                is CarrocaBoyDAO -> {
                    employee.totalCommissions += totalPrice/10
                    employee.account.updatedAt = date
                }
                else -> null
            }
        }
    }
}