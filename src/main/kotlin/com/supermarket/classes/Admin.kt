package org.example.com.supermarket.classes

class Admin (
    id: Long,
    name: String,
    email: String,
    password: String,
    createdAt: String,
) : Account(id, name, email, password, createdAt) {

}