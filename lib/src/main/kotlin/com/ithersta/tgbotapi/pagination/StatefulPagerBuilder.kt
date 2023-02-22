package com.ithersta.tgbotapi.pagination

import com.ithersta.tgbotapi.fsm.BaseStatefulContext

class StatefulPagerBuilder<BS : Any, BU : Any, S : BS, U : BU>(
    page: Int,
    offset: Int,
    limit: Int,
    id: String,
    private val statefulContext: BaseStatefulContext<BS, BU, S, U>
) : PagerBuilder(page, offset, limit, id), BaseStatefulContext<BS, BU, S, U> by statefulContext
