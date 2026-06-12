package com.disciplica.shared.user;

import jakarta.validation.constraints.Positive;

/**
 * Anfrage, einen Goldbetrag abzuziehen (z.&nbsp;B. ein Kauf im Avatar-Shop).
 *
 * @param amount der abzuziehende Betrag (muss positiv sein)
 */
public record SpendGoldRequest(
        @Positive int amount
) {
}
