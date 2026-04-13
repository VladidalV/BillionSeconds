package com.example.billionseconds.navigation.command

import com.example.billionseconds.navigation.AppScreen
import com.example.billionseconds.navigation.MainTab

/**
 * Навигационные команды — единственный способ инициировать переход.
 * Feature-слой (Store) создаёт команды, AppNavigator их исполняет.
 * NavController и Compose-навигация скрыты за этим контрактом.
 */
sealed interface NavCommand {

    /** Открыть экран поверх текущего (добавить в back stack). */
    data class Forward(val screen: AppScreen) : NavCommand

    /** Заменить текущий экран без добавления в back stack. */
    data class Replace(val screen: AppScreen) : NavCommand

    /**
     * Очистить весь back stack и установить новый корневой экран.
     * Используется при переходе onboarding → main.
     */
    data class NewRoot(val screen: AppScreen) : NavCommand

    /** Вернуться на предыдущий экран. */
    data object Back : NavCommand

    /**
     * Вернуться до указанного экрана (по типу экрана).
     * [inclusive] = true → убрать и сам целевой экран из стека.
     */
    data class BackTo(val screenClass: kotlin.reflect.KClass<out AppScreen>, val inclusive: Boolean = false) : NavCommand

    /**
     * Переключить вкладку в MainScaffold.
     * Не добавляет новый экран в back stack — обновляет tab у текущего Main.
     */
    data class SwitchTab(val tab: MainTab) : NavCommand

    /**
     * Завершить текущий flow и вернуться к предыдущему экрану.
     * Семантически то же что Back, но явно сигнализирует о завершении flow.
     */
    data object FinishFlow : NavCommand
}
