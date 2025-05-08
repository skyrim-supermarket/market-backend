package com.mac350.models

import kotlinx.serialization.Serializable

@Serializable
sealed class Employee () : Account() {
    abstract val totalCommissions: Long
}