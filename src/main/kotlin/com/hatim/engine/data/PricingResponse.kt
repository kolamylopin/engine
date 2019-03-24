package com.hatim.engine.data

import net.openhft.chronicle.wire.Marshallable

data class PricingResponse(val id: String? = null,
                           val result: Any? = null,
                           val timestamp: Long = 0,
                           val requestTimeStamp: Long = 0) : Marshallable