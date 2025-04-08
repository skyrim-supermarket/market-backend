package org.example.com.supermarket.classes

abstract class Employee (
    id: Long,
    name: String,
    email: String,
    password: String,
    createdAt: String,
    private var totalCommissions: Long
) : Account(id, name, email, password, createdAt) {
    fun getTotalCommissions(): Long {
        return this.totalCommissions
    }

    fun setTotalCommissions(totalCommissions: Long) {
        this.totalCommissions = totalCommissions
    }
}