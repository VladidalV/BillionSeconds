package com.example.billionseconds.domain

import com.example.billionseconds.data.model.CapsuleGroup
import com.example.billionseconds.data.model.CapsuleUiItem

object CapsuleListGrouper {

    fun group(items: List<CapsuleUiItem>): List<CapsuleGroup> {
        val available = items.filter { it.status is CapsuleStatus.Available }
        val locked    = items.filter { it.status is CapsuleStatus.Locked }
        val opened    = items.filter { it.status is CapsuleStatus.Opened }
        val draft     = items.filter { it.status is CapsuleStatus.Draft }
        val invalid   = items.filter { it.status is CapsuleStatus.Invalid }

        return buildList {
            if (available.isNotEmpty()) add(CapsuleGroup("ДОСТУПНО", available))
            if (locked.isNotEmpty())    add(CapsuleGroup("ЗАПЕРТО", locked))
            if (opened.isNotEmpty())    add(CapsuleGroup("ОТКРЫТО", opened))
            if (draft.isNotEmpty())     add(CapsuleGroup("ЧЕРНОВИКИ", draft))
            if (invalid.isNotEmpty())   add(CapsuleGroup("НЕДОСТУПНО", invalid))
        }
    }
}
