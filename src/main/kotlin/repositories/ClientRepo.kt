package com.mac350.repositories
import com.mac350.models.*

interface ClientRepo {
    fun allClients(): List<Client>
    fun clientByEmail(email: String): Client?
    fun specialClients(vip: Boolean): List<Client>
    fun addClient(client: Client)
    fun removeClient(email: String): Boolean
}