package com.driverwallet.app.core.model

data class Category(
    val key: String,
    val label: String,
    val type: TransactionType,
    val iconName: String,
)

object Categories {
    val ORDER = Category("order", "Order", TransactionType.INCOME, "shopping_cart")
    val BONUS = Category("bonus", "Bonus", TransactionType.INCOME, "star")
    val TIPS = Category("tips", "Tips", TransactionType.INCOME, "favorite")
    val INCENTIVE = Category("incentive", "Insentif", TransactionType.INCOME, "emoji_events")
    val OTHER_INCOME = Category("other_income", "Lainnya", TransactionType.INCOME, "more_horiz")

    val FUEL = Category("fuel", "BBM", TransactionType.EXPENSE, "local_gas_station")
    val FOOD = Category("food", "Makan", TransactionType.EXPENSE, "restaurant")
    val CIGARETTE = Category("cigarette", "Rokok", TransactionType.EXPENSE, "smoking_rooms")
    val PHONE = Category("phone", "Pulsa/Data", TransactionType.EXPENSE, "phone_android")
    val PARKING = Category("parking", "Parkir", TransactionType.EXPENSE, "local_parking")
    val MAINTENANCE = Category("maintenance", "Servis", TransactionType.EXPENSE, "build")
    val TOLL = Category("toll", "Tol", TransactionType.EXPENSE, "toll")
    val OTHER_EXPENSE = Category("other_expense", "Lainnya", TransactionType.EXPENSE, "more_horiz")

    val DEBT_PAYMENT = Category("debt_payment", "Bayar Hutang", TransactionType.EXPENSE, "credit_card")

    val incomeCategories = listOf(ORDER, BONUS, TIPS, INCENTIVE, OTHER_INCOME)
    val expenseCategories = listOf(FUEL, FOOD, CIGARETTE, PHONE, PARKING, MAINTENANCE, TOLL, OTHER_EXPENSE)
    val allCategories = incomeCategories + expenseCategories + DEBT_PAYMENT

    private val categoryMap: Map<String, Category> = allCategories.associateBy { it.key }

    fun fromKey(key: String): Category? = categoryMap[key]

    fun getByType(type: TransactionType): List<Category> = when (type) {
        TransactionType.INCOME -> incomeCategories
        TransactionType.EXPENSE -> expenseCategories
    }
}
