package com.mac350.repositories
import com.mac350.models.*

interface ClientRepo {
    fun allClients(): List<Client>
    fun clientsByEmail(email: String): Client?
    fun clientsByAddress(address: String): List<Client>
    fun addClient(client: Client)
    fun removeClient(email: String): Boolean
}