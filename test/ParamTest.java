import junit.Calculator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * Параметризированный тест
 */
@RunWith(Parameterized.class)
public class ParamTest {
    /**
     * Параметры
     *
     * @return Collection
     */
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                // a, b, result
                {0, 0, 0},
                {1, 1, 3},
                {2, 2, 4},
                {4, 4, 9},
                {16, 16, 32},
        });
    }

    private int a, b, result;

    /* Конструктор для параметров */
    public ParamTest(int a, int b, int result) {
        this.a = a;
        this.b = b;
        this.result = result;
    }

    private Calculator calculator;

    @Before
    public void init() {
        calculator = new Calculator();
    }

    @Test
    public void massTestAdd() {
        Assert.assertEquals(result, calculator.add(a, b));
    }
}
