package com.bk.bk1.utilities

import com.squareup.otto.Bus

class BusProvider {

    companion object {

        @Volatile
        private var INSTANCE: Bus? = null
        fun getEventBus(): Bus {
            return INSTANCE ?: synchronized(this) {
                val instance = Bus()
                INSTANCE = instance
                instance
            }
        }
    }
}