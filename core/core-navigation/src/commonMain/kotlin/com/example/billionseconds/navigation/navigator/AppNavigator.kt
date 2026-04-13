package com.example.billionseconds.navigation.navigator

import com.example.billionseconds.navigation.AppScreen
import com.example.billionseconds.navigation.MainTab
import com.example.billionseconds.navigation.command.NavCommand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Централизованный исполнитель навигационных команд.
 *
 * Принципы (Cicerone-adapted):
 * - Единственный владелец back stack.
 * - Feature-слой никогда не обращается сюда напрямую — только через NavCommand.
 * - App.kt создаёт AppNavigator, подписывается на [current] и рендерит экраны.
 */
class AppNavigator(initialScreen: AppScreen) {

    private val backStack: ArrayDeque<AppScreen> = ArrayDeque<AppScreen>().also { it.add(initialScreen) }

    private val _current = MutableStateFlow(initialScreen)
    val current: StateFlow<AppScreen> = _current.asStateFlow()

    /** Может ли пользователь вернуться назад (есть ли что в back stack). */
    val canGoBack: Boolean get() = backStack.size > 1

    fun execute(command: NavCommand) {
        when (command) {
            is NavCommand.Forward -> {
                backStack.addLast(command.screen)
                _current.value = command.screen
            }

            is NavCommand.Replace -> {
                if (backStack.isNotEmpty()) {
                    backStack[backStack.lastIndex] = command.screen
                }
                _current.value = command.screen
            }

            is NavCommand.NewRoot -> {
                backStack.clear()
                backStack.addLast(command.screen)
                _current.value = command.screen
            }

            is NavCommand.Back, is NavCommand.FinishFlow -> {
                if (backStack.size > 1) {
                    backStack.removeLast()
                    _current.value = backStack.last()
                }
            }

            is NavCommand.BackTo -> {
                val idx = backStack.indexOfFirst { it::class == command.screenClass }
                if (idx >= 0) {
                    val keepUntil = if (command.inclusive) idx else idx + 1
                    while (backStack.size > keepUntil) {
                        backStack.removeLast()
                    }
                    _current.value = backStack.lastOrNull() ?: backStack.first()
                }
            }

            is NavCommand.SwitchTab -> {
                val currentScreen = backStack.lastOrNull()
                if (currentScreen is AppScreen.Main && currentScreen.tab == command.tab) {
                    return
                }
                val updatedMain = AppScreen.Main(command.tab)
                if (currentScreen is AppScreen.Main) {
                    backStack[backStack.lastIndex] = updatedMain
                } else {
                    backStack.addLast(updatedMain)
                }
                _current.value = updatedMain
            }
        }
    }

    /** Текущий активный Tab (если мы на Main-экране). */
    val currentTab: MainTab?
        get() = (_current.value as? AppScreen.Main)?.tab
}
