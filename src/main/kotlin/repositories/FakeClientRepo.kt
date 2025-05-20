package com.mac350.repositories
import com.mac350.models.*

class FakeClientRepo : ClientRepo {
    private val clients = mutableListOf<Client>()

    override fun allClients(): List<Client> = clients

    override fun specialClients(vip: Boolean) = clients.filter {
        it.isSpecialClient == vip
    }

    override fun clientByEmail(email: String)= clients.find {
        it.email.equals(email, ignoreCase = true)
    }

    override fun addClient(client: Client) {
        if (clientByEmail(client.email) != null) {
            throw IllegalStateException("Email já registrado!")
        }
        clients.add(client)
    }

    override fun removeClient(email: String): Boolean {
        return clients.removeIf { it.email == email }
    }
}