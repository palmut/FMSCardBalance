package net.palmut.fmscardbalance.store.mapper

import net.palmut.fmscardbalance.component.entity.Card
import net.palmut.fmscardbalance.component.map
import net.palmut.fmscardbalance.store.entity.CardModel

fun CardModel.map() =
    Card(
        title = title,
        availableAmount = availableAmount,
        tail = tail,
        status = status.map(),
        id = id,
        date = date
    )