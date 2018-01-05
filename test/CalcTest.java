import junit.Calculator;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Тестирование
 */
public class CalcTest {
    private Calculator calculator;

    @Before
    public void init() {
        System.out.println("INIT");
        calculator = new Calculator();
    }

    @Test
    public void assertTest() {
        String string = "A";
        // assertThat = "утверждать, что"
        Assert.assertThat(string, new Matcher<>() {
            // совпадает
            public boolean matches(Object o) {
                return ((String) o).length() == 1; // Если строка длинной в 1 символ, то true
            }

            // не совпадает
            public void describeMismatch(Object o, Description description) {
            }

            public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {
            }

            public void describeTo(Description description) {
            }
        });
    }

    @Test
    public void addTest() {
        Assert.assertEquals(8, calculator.add(4, 4));
    }

    @Test
    public void addTest2() {
        Assert.assertEquals(10, calculator.add(4, 6));
    }

    // Проверка на исключение ArithmeticException
    @Test(expected = ArithmeticException.class)
    public void divTestBy0() {
        calculator.div(4, 0);
    }

    @After
    public void finish() {
        // тут можно закрывать соккеты, подключения
    }
}
