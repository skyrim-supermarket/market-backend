package com.mac350.repositories
import com.mac350.models.*

interface ClientRepo {
    suspend fun allClients(): List<Client>
    suspend fun clientByEmail(email: String): Client?
    suspend fun specialClients(vip: Boolean): List<Client>
    suspend fun addClient(client: Client)
    suspend fun removeClient(email: String): Boolean
}