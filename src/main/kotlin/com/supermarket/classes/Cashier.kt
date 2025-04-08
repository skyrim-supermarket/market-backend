package org.example.com.supermarket.classes

class Cashier (
    id: Long,
    name: String,
    email: String,
    password: String,
    createdAt: String,
    totalCommissions: Long,
    private var section: Long,
) : Employee(id, name, email, password, createdAt, totalCommissions) {
    fun getSection(): Long {
        return this.section
    }

    fun setSection(section: Long) {
        this.section = section
    }
}