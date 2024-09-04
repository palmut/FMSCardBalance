package net.palmut.fmscardbalance.component.mapper

import net.palmut.fmscardbalance.component.entity.NewCardState
import net.palmut.fmscardbalance.store.entity.NewCardModel

fun NewCardState.map() =
    NewCardModel (
        label = label,
        tail = tail
    )