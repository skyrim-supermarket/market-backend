package org.example

import org.example.com.supermarket.classes.*

fun main() {
    val caixa1 = Cashier(1, "Caixa 1", "cx1@ssm.com" , "123", "08-04-25", 0, 1)

    println("Sou o ${caixa1.getName()}")
}