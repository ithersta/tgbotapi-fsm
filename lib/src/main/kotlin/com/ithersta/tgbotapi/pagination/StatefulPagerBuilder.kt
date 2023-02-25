package com.ithersta.tgbotapi.pagination

import com.ithersta.tgbotapi.fsm.BaseStatefulContext

class StatefulPagerBuilder<BS : Any, BU : Any, S : BS, U : BU>(
    page: Int,
    offset: Int,
    limit: Int,
    id: String,
    private val statefulContext: BaseStatefulContext<BS, BU, S, U>
) : PagerBuilder<Unit>(page, offset, limit, Unit, Unit::class, id), BaseStatefulContext<BS, BU, S, U> by statefulContext
