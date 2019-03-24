package com.hatim.engine.data

import net.openhft.chronicle.wire.Marshallable

data class PricingRequest(var id: String? = null,
                          var destination: String? = null,
                          var message: String? = null,
                          var timestamp: Long = 0)
    : Marshallable {

    fun copyInto(other: PricingRequest) {
        id = other.id
        timestamp = other.timestamp
        destination = other.destination
        message = other.message
    }
}
