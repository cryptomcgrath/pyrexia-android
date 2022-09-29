package com.cryptomcgrath.pyrexia.util

interface DiffableItem {
    fun areContentsTheSame(other: DiffableItem): Boolean

    fun areItemsTheSame(other: DiffableItem): Boolean
}