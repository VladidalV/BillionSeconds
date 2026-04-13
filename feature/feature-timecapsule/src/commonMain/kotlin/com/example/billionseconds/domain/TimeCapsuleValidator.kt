package com.example.billionseconds.domain

import com.example.billionseconds.ui.timecapsule.CapsuleFormDraft
import com.example.billionseconds.ui.timecapsule.ConditionType

object TimeCapsuleValidator {

    data class Result(
        val titleError: String?,
        val messageError: String?,
        val conditionError: String?
    ) {
        val isValid: Boolean get() = titleError == null && messageError == null && conditionError == null
    }

    fun validate(draft: CapsuleFormDraft, nowMs: Long): Result {
        val titleError = when {
            draft.title.isBlank()       -> "Заголовок не может быть пустым"
            draft.title.length > 80     -> "Максимум 80 символов"
            else                        -> null
        }

        val messageError = when {
            draft.message.isBlank()     -> "Сообщение не может быть пустым"
            draft.message.length > 2000 -> "Максимум 2000 символов"
            else                        -> null
        }

        val conditionError = when (draft.conditionType) {
            ConditionType.DATE -> {
                val ms = parseDateToMs(draft.selectedYear, draft.selectedMonth, draft.selectedDay, draft.selectedHour, draft.selectedMinute)
                when {
                    ms == null               -> "Укажите корректную дату"
                    ms <= nowMs + 3_600_000L -> "Дата должна быть в будущем (минимум через 1 час)"
                    else                     -> null
                }
            }
            ConditionType.BILLION_SECONDS_EVENT -> {
                if (draft.selectedProfileId == null) "Выберите профиль" else null
            }
        }

        return Result(titleError, messageError, conditionError)
    }

    fun parseDateToMs(year: String, month: String, day: String, hour: String, minute: String): Long? {
        val y  = year.toIntOrNull() ?: return null
        val mo = month.toIntOrNull()?.takeIf { it in 1..12 } ?: return null
        val d  = day.toIntOrNull()?.takeIf { it in 1..31 } ?: return null
        val h  = hour.toIntOrNull()?.takeIf { it in 0..23 } ?: return null
        val mi = minute.toIntOrNull()?.takeIf { it in 0..59 } ?: return null
        return try {
            val data = com.example.billionseconds.data.model.BirthdayData(y, mo, d, h, mi)
            BillionSecondsCalculator.birthEpochMs(data)
        } catch (e: Exception) {
            null
        }
    }
}
