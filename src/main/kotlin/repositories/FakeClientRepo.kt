package com.mac350.repositories
import com.mac350.models.*

class FakeClientRepo : ClientRepo {
    private val clients = mutableListOf<Client>()

    override suspend fun allClients(): List<Client> = clients

    override suspend fun specialClients(vip: Boolean) = clients.filter {
        it.isSpecialClient == vip
    }

    override suspend fun clientByEmail(email: String)= clients.find {
        it.email.equals(email, ignoreCase = true)
    }

    override suspend fun addClient(client: Client) {
        if (clientByEmail(client.email) != null) {
            throw IllegalStateException("Email já registrado!")
        }
        clients.add(client)
    }

    override suspend fun removeClient(email: String): Boolean {
        return clients.removeIf { it.email == email }
    }
}