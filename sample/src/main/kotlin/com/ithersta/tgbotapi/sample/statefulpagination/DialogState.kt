package com.ithersta.tgbotapi.sample.statefulpagination

import com.ithersta.tgbotapi.pagination.PagerState

interface DialogState

object EmptyState : DialogState

data class NumbersState(
    val pagerState: PagerState,
    val startWith: Int
) : DialogState
