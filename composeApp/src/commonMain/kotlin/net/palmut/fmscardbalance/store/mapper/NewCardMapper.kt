package net.palmut.fmscardbalance.store.mapper

import net.palmut.fmscardbalance.component.entity.NewCardState
import net.palmut.fmscardbalance.store.entity.CardModel
import net.palmut.fmscardbalance.store.entity.NewCardModel

fun NewCardModel.map() =
    NewCardState (
        label = label,
        tail = tail
    )

fun NewCardModel.toCardModel() =
    CardModel(
        title = label,
        tail = tail
    )
