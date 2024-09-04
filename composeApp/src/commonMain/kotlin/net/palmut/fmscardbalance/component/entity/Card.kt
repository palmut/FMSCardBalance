package net.palmut.fmscardbalance.component.entity

import net.palmut.fmscardbalance.component.MainComponent.*


data class Card(
    val title: String,
    val availableAmount: String,
    val tail: String,
    val id: Int,
    val date: String,
    val status: Status = Status.SUCCESS
)