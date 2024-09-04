package net.palmut.fmscardbalance.component.mapper

import net.palmut.fmscardbalance.component.entity.Card
import net.palmut.fmscardbalance.store.entity.CardModel

fun Card.map() =
    CardModel(
        title = title,
        availableAmount = availableAmount,
        tail = tail
    ).also {
        it.id = id
        it.date = date
    }