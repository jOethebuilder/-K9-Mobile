package com.joethebuilder.k9.network

data class SlotAssignment(val slot: Int, val spoolId: Int?)

interface SlotSpoolAdapter {
    val slotCount: Int
    fun readSlots(): List<SlotAssignment>
    fun assignSpool(slot: Int, spoolId: Int): Boolean
}
