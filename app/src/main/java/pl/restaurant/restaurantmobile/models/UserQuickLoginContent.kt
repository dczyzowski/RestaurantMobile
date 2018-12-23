package pl.restaurant.restaurantmobile.models

import java.util.ArrayList
import java.util.HashMap

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object UserQuickLoginContent {

    /**
     * An array of sample (dummy) items.
     */
    val ITEMS: MutableList<UserQuickLogin> = ArrayList()

    /**
     * A map of sample (dummy) items, by ID.
     */
    val ITEM_MAP: MutableMap<String, UserQuickLogin> = HashMap()

    private val COUNT = 25

    init {
        // Add some sample items.
        // Tu bede pobieeroal aktuualnych uzytkownikow z db
    }

    private fun addItem(item: UserQuickLogin) {
        ITEMS.add(item)
        ITEM_MAP.put(item.id, item)
    }

    /**
     * A dummy item representing a piece of content.
     */
    data class UserQuickLogin(val id: String, val content: String, val drawable: Int) {
        override fun toString(): String = content
    }
}
