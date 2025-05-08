package com.mac350.repositories
import com.mac350.models.*

class FakeClientRepo : ClientRepo {
    private val clients = mutableListOf(
        Client(1, "a", "a@gmail.com", "a", "8/5/2025", "8/5/2025", false, "8/5/2025", "aHouse")
    )

    override fun allClients(): List<Client> = clients

    override fun clientsByAddress(address: String) = clients.filter {
        it.address == address
    }

    override fun clientsByEmail(email: String)= clients.find {
        it.email.equals(email, ignoreCase = true)
    }

    override fun addClient(client: Client) {
        if (clientsByEmail(client.email) != null) {
            throw IllegalStateException("Email já registrado!")
        }
        clients.add(client)
    }

    override fun removeClient(email: String): Boolean {
        return clients.removeIf { it.email == email }
    }
}