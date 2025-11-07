package com.pedidos.domain.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.pedidos.domain.errors.InvalidMoneyException;
import com.pedidos.domain.errors.InvalidMoneyOperationException;

class MoneyTest {

    @Test
    void createWithNullAmountThrows() {
        assertThrows(InvalidMoneyException.class, () -> new Money(null, Currency.EUR()));
    }

    @Test
    void createWithNullCurrencyThrows() {
        assertThrows(InvalidMoneyException.class, () -> new Money(BigDecimal.valueOf(10), null));
    }

    @Test
    void createWithNegativeOrZeroAmountThrows() {
        assertThrows(InvalidMoneyException.class, () -> new Money(BigDecimal.ZERO, Currency.EUR()));
        assertThrows(InvalidMoneyException.class, () -> new Money(BigDecimal.valueOf(-1), Currency.EUR()));
    }

    @Test
    void addAndSubtractSameCurrency() {
        Money a = new Money(BigDecimal.valueOf(10), Currency.EUR());
        Money b = new Money(BigDecimal.valueOf(5), Currency.EUR());

        Money sum = a.add(b);
        assertEquals(new BigDecimal("15.00"), sum.getAmount());

        Money diff = a.subtract(b);
        assertEquals(new BigDecimal("5.00"), diff.getAmount());
    }

    @Test
    void operationsWithDifferentCurrencyThrow() {
        Money a = new Money(BigDecimal.valueOf(10), Currency.EUR());
        Money b = new Money(BigDecimal.valueOf(5), Currency.USD());

        assertThrows(InvalidMoneyOperationException.class, () -> a.add(b));
        assertThrows(InvalidMoneyOperationException.class, () -> a.subtract(b));
    }

    @Test
    void multiplyScalesCorrectly() {
        Money a = new Money(BigDecimal.valueOf(2), Currency.EUR());
        Money m = a.multiply(3);
        assertEquals(new BigDecimal("6.00"), m.getAmount());
    }
}
