package org.example.com.supermarket.classes

class CarrocaBoy (
    id: Long,
    name: String,
    email: String,
    password: String,
    createdAt: String,
    totalCommissions: Long,
) : Employee(id, name, email, password, createdAt, totalCommissions) {

}