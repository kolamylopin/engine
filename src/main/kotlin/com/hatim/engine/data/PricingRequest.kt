package com.hatim.engine.data

import net.openhft.chronicle.wire.Marshallable

data class PricingRequest(var id: String? = null,
                          var destination: String? = null,
                          var message: String? = null) : Marshallable
