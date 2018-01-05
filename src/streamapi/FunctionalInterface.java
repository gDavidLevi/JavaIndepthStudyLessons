package streamapi;

/**
 * Функциональный интерфейс
 * Вызов метода внутри метода
 */
public class FunctionalInterface {
    public static void main(String[] args) {
        // Пример вызова
        doSomething(new FuncInterface() {
            @Override
            public void doSomething() {
                System.out.println("123");
            }
        });
    }

    /**
     * Фунция вызывает метод из интерфейса
     *
     * @param funcInterface FuncInterface
     */
    private static void doSomething(FuncInterface funcInterface) {
        System.out.println("doSomething...");
        funcInterface.doSomething();
    }
}

/**
 * Функциональный интерфейс
 */
@java.lang.FunctionalInterface
interface FuncInterface {
    void doSomething();
}
