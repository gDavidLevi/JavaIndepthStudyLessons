# StreamAPI

После добавления методов по умолчанию в интерфейсы, в Java 8 появилась возможность работать со StreamAPI.

## Виды потоков

В Java существует 2 вида потока в рамках процесса (на уровне ОС каждая программа - это процесс): 
- stream (поток) - чтение и запись; об этом в конспекте
- thread (нить) - параллельный поток, см. Thread

```java 
public class StreamApiEdu {
    private static Collection<Integer> collection = Arrays.asList(1, 2, 3, 4, 5, 6);

    public static void main(String[] args) {
        method();
        methodStreamApi();
        methodParallelStream();
    }
    /*
    Код
     */
    private static void method() {
        Integer sum = 0;
        for (Integer i : collection) if (i % 2 != 0) sum += i;
    }

    /*
    Используя StreamAPI можно код переписать
     */
    private static void methodStreamApi() {
        Integer sum = collection.stream().filter(o -> o % 2 != 0).reduce((s1, s2) -> s1 + s2).orElse(0);
    }

    /*
    Stream Api позволяет решать задачу параллельно лишь изменив stream() на parallelStream() без всякого лишнего кода.
    Уже делает код параллельным, без всяких семафоров, синхронизаций, рисков взаимных блокировок и т.п.
     */
    private static void methodParallelStream() {
        Integer sumOdd = collection.parallelStream().filter(o -> o % 2 != 0).reduce((s1, s2) -> s1 + s2).orElse(0);
    }
}
```


## I. Способы создания стримов

### Классический: Создание стрима из коллекции
- collection.stream()
```java 
Collection<String> collection = Arrays.asList("a1", "a2", "a3");
Stream<String> streamFromCollection = collection.stream();
```

### Создание стрима из значений
- Stream.of(значение1,… значениеN)
```java 
Stream<String> streamFromValues = Stream.of("a1", "a2", "a3");
```

### Создание стрима из массива
- Arrays.stream(массив)
```java 
String[] array = {"a1","a2","a3"};                     
Stream<String> streamFromArrays = Arrays.stream(array);
```

### Создание стрима из файла (каждая строка в файле будет отдельным элементом в стриме)
- Files.lines(путь_к_файлу)
```java 
Stream<String> streamFromFiles = Files.lines(Paths.get("file.txt"));
```

### Создание стрима из строки
- «строка».chars()
```java 
IntStream streamFromString = "123".chars();
```

### С помощью Stream.builder
- Stream.builder().add(...)....build()
```java 
Stream.builder().add("a1").add("a2").add("a3").build();
```

### Создание параллельного стрима
- collection.parallelStream()
```java 
Stream<String> stream = collection.parallelStream();
```

### Создание бесконечных стрима с помощью Stream.iterate 
- Stream.iterate(начальное_условие, выражение_генерации)
```java 
Stream<Integer> streamFromIterate = Stream.iterate(1, n -> n + 1);
```

### Создание бесконечных стрима с помощью Stream.generate
- Stream.generate(выражение_генерации)
```java 
Stream<String> streamFromGenerate = Stream.generate(() -> "a1");
```
В принципе, кроме последних двух способов создания стрима, все не отличается от обычных способов создания коллекций. Последние два способа служат для генерации бесконечных стримов, в iterate задается начальное условие и выражение получение следующего значения из предыдущего, то есть Stream.iterate(1, n -> n + 1) будет выдавать значения 1, 2, 3, 4,… N. Stream.generate служит для генерации константных и случайных значений, он просто выдает значения соответствующие выражению, в данном примере, он будет выдавать бесконечное количество значений «a1».

## II. Методы работы со стримами

Java Stream API предлагает два вида методов: 
- Конвейерные — возвращают другой stream, то есть работают как builder, 
- Терминальные — возвращают другой объект, такой как коллекция, примитивы, объекты, Optional и т.д.

Общее правило: у stream'a может быть сколько угодно вызовов конвейерных вызовов и в конце один терминальный, при этом все конвейерные методы выполняются лениво и пока не будет вызван терминальный метод никаких действий на самом деле не происходит, так же как создать объект Thread или Runnable, но не вызвать у него start.

В целом, этот механизм похож на конструирования SQL запросов, может быть сколько угодно вложенных Select'ов и только один результат в итоге. 
Например, в выражении collection.stream().filter((s) -> s.contains(«1»)).skip(2).findFirst(), filter и skip — конвейерные, а findFirst — терминальный, он возвращает объект Optional и это заканчивает работу со stream'ом.

### 2.1 Краткое описание конвейерных методов работы со стримами

#### filter
Отфильтровывает записи, возвращает только записи, соответствующие условию
```java 
collection.stream().filter(«a1»::equals).count();
```

#### skip
Позволяет пропустить N первых элементов
```java 
collection.stream().skip(collection.size() — 1).findFirst().orElse(«1»);
```


#### distinct
Возвращает стрим без дубликатов (для метода equals)
```java 
collection.stream().distinct().collect(Collectors.toList());
```

#### peek
Возвращает тот же стрим, но применяет функцию к каждому элементу стрима
```java 
collection.stream().map(String::toUpperCase).peek((e) -> System.out.print("," + e)).collect(Collectors.toList());
```


#### limit
Позволяет ограничить выборку определенным количеством первых элементов
```java 
collection.stream().limit(2).collect(Collectors.toList());
```


#### sorted
Позволяет сортировать значения либо в натуральном порядке, либо задавая Comparator
```java 
collection.stream().sorted().collect(Collectors.toList());
```


#### map
Преобразует каждый элемент стрима
```java 
collection.stream().map((s) -> s + "_1").collect(Collectors.toList());
```


#### mapToInt, mapToDouble, mapToLong
Аналог map, но возвращает числовой стрим (то есть стрим из числовых примитивов)
```java 
collection.stream().mapToInt((s) -> Integer.parseInt(s)).toArray();
```


#### flatMap, flatMapToInt, flatMapToDouble, flatMapToLong
Похоже на map, но может создавать из одного элемента несколько
```java 
collection.stream().flatMap((p) -> Arrays.asList(p.split(",")).stream()).toArray(String[]::new);
```


### 2.2 Краткое описание терминальных методов работы со стримами


#### findFirst
Возвращает первый элемент из стрима (возвращает Optional)
```java 
collection.stream().findFirst().orElse(«1»);
```


#### findAny
Возвращает любой подходящий элемент из стрима (возвращает Optional)
```java 
collection.stream().findAny().orElse(«1»);
```


#### collect
Представление результатов в виде коллекций и других структур данных
```java 
collection.stream().filter((s) -> s.contains(«1»)).collect(Collectors.toList());
```


#### count
Возвращает количество элементов в стриме
```java 
collection.stream().filter(«a1»::equals).count();
```


#### anyMatch
Возвращает true, если условие выполняется хотя бы для одного элемента
```java 
collection.stream().anyMatch(«a1»::equals);
```


#### noneMatch
Возвращает true, если условие не выполняется ни для одного элемента
```java 
collection.stream().noneMatch(«a8»::equals);
```


#### allMatch
Возвращает true, если условие выполняется для всех элементов
```java 
collection.stream().allMatch((s) -> s.contains(«1»));
```


#### min
Возвращает минимальный элемент, в качестве условия использует компаратор
```java 
collection.stream().min(String::compareTo).get();
```


#### max
Возвращает максимальный элемент, в качестве условия использует компаратор
```java 
collection.stream().max(String::compareTo).get();
```

#### forEach
Применяет функцию к каждому объекту стрима, порядок при параллельном выполнении не гарантируется
```java 
set.stream().forEach((p) -> p.append("_1"));
```

#### forEachOrdered
Применяет функцию к каждому объекту стрима, сохранение порядка элементов гарантирует
```java 
list.stream().forEachOrdered((p) -> p.append("_new"));
```

#### toArray
Возвращает массив значений стрима
```java 
collection.stream().map(String::toUpperCase).toArray(String[]::new);
```

#### reduce
Позволяет выполнять агрегатные функции на всей коллекцией и возвращать один результат
```java 
collection.stream().reduce((s1, s2) -> s1 + s2).orElse(0);
```
Обратите внимание методы findFirst, findAny, anyMatch это short-circuiting методы, то есть обход стримов организуется таким образом чтобы найти подходящий элемент максимально быстро, а не обходить весь изначальный стрим.

## 2.3 Краткое описание дополнительных методов у числовых стримов
#### sum
Возвращает сумму всех чисел
```java 
collection.stream().mapToInt((s) -> Integer.parseInt(s)).sum();
```

#### average
Возвращает среднее арифметическое всех чисел
```java 
collection.stream().mapToInt((s) -> Integer.parseInt(s)).average();
```

#### mapToObj
Преобразует числовой стрим обратно в объектный
```java 
intStream.mapToObj((id) -> new Key(id)).toArray();
```


## 2.4 Несколько других полезных методов стримов

#### isParallel
- Узнать является ли стрим параллельным

#### parallel
- Вернуть параллельный стрим, если стрим уже параллельный, то может вернуть самого себя

#### sequential
- Вернуть последовательный стрим, если стрим уже последовательный, то может вернуть самого себя

С помощью, методов parallel и sequential можно определять какие операции могут быть параллельными, а какие только последовательными. Так же из любого последовательного стрима можно сделать параллельный и наоборот, то есть: 
```java 
collection.stream().
peek(...). // операция последовательна
parallel().
map(...). // операция может выполняться параллельно,
sequential().
reduce(...) // операция снова последовательна 
```
Внимание: крайне не рекомендуется использовать параллельные стримы для сколько-нибудь долгих операций (получение данных из базы, сетевых соединений), так как все параллельные стримы работают c одним пулом fork/join и такие долгие операции могут остановить работу всех параллельных стримов в JVM из-за того отсутствия доступных потоков в пуле, т.е. параллельные стримы стоит использовать лишь для коротких операций, где счет идет на миллисекунды, но не для тех где счет может идти на секунды и минуты. 

## III. Примеры работы с методами стримов
### 3.1 Примеры использования filter, findFirst, findAny, skip, limit и count

Имеется класс:
```java 
package com.github.vedenin.rus.stream_api;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * Примеры работы методов Stream Api
 *
 * Created by vedenin on 17.10.15.
 */
public class LimitAndSkipTests {
    // Метод Limit позволяет ограничить выборку определенным количеством первых элементов
    private static void testLimit() {
        System.out.println();
        System.out.println("Test limit start");
        Collection<String> collection = Arrays.asList("a1", "a2", "a3", "a1");

        // Вернуть первые два элемента
        List<String> limit = collection.stream().limit(2).collect(Collectors.toList());
        System.out.println("limit = " + limit); // напечатает limit = [a1, a2]

        // Вернуть два элемента начиная со второго
        List<String> fromTo = collection.stream().skip(1).limit(2).collect(Collectors.toList());
        System.out.println("fromTo = " + fromTo); // напечатает fromTo = [a2, a3]

        // вернуть последний элемент коллекции
        String last = collection.stream().skip(collection.size() - 1).findAny().orElse("1");
        System.out.println("last = " + last ); // напечатает last = a1
    }

    public static void main(String[] args)  throws Exception {
        testLimit();
    }
}
```

Условие: дана коллекция строк,
```java 
Collection<String> collection = Arrays.asList(«a1», «a2», «a3», «a1»);
```
GGосмотрим как её можно обрабатывать используя методы filter, findFirst, findAny, skip и count:

* Вернуть количество вхождений объекта «a1»
```java 
collection.stream().filter(«a1»::equals).count(); // 2
```

* Вернуть первый элемент коллекции или 0, если коллекция пуста
```java 
collection.stream().findFirst().orElse(«0»); // a1
```

* Вернуть последний элемент коллекции или «empty», если коллекция пуста
```java 
collection.stream().skip(collection.size() — 1).findAny().orElse(«empty»); // a1
```

* Найти элемент в коллекции равный «a3» или кинуть ошибку 
```java 
collection.stream().filter(«a3»::equals).findFirst().get(); // a3
```

* Вернуть третий элемент коллекции по порядку
```java 
collection.stream().skip(2).findFirst().get(); // a3
```

* Вернуть два элемента начиная со второго
```java 
collection.stream().skip(1).limit(2).toArray(); // [a2, a3]
```

* Выбрать все элементы по шаблону
```java 
collection.stream().filter((s) -> s.contains(«1»)).collect(Collectors.toList()); // [a1, a1]
```

Методы findFirst и findAny возвращают новый тип Optional, появившийся в Java 8, для того чтобы избежать NullPointerException. Метод filter удобно использовать для выборки лишь определенного множества значений, а метод skip позволяет пропускать определенное количество элементов.

Выражение «a3»::equals это аналог 
```java 
boolean func(s) { 
    return «a3».equals(s);
}
``` 
а (s) -> s.contains(«1») это аналог
```java 
boolean func(s) { 
    return s.contains(«1»);
}
``` 
обернутых в анонимный класс.



Имеется класс:
```java 
package com.github.vedenin.rus.stream_api;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * Примеры работы методов Stream Api
 *
 * Created by vedenin on 17.10.15.
 */
public class FiterAndCountTests {
    // filter - возвращает stream, в котором есть только элементы, соответствующие условию фильтра
    // count - возвращает количество элементов в стриме
    // collect - преобразует stream в коллекцию или другую структуру данных
    // mapToInt - преобразовать объект в числовой стрим (стрим, содержащий числа)
    private static void testFilterAndCount() {
        System.out.println("Test filter and count start");

        // ************ Работа со строками
        Collection<String> collection = Arrays.asList("a1", "a2", "a3", "a1");

        // Вернуть количество вхождений объекта
        long count = collection.stream().filter("a1"::equals).count();
        System.out.println("count = " + count); // напечатает count = 2

        // Выбрать все элементы по шаблону
        List<String> select = collection.stream().filter((s) -> s.contains("1")).collect(Collectors.toList());
        System.out.println("select = " + select); // напечатает select = [a1, a1]

        // ************ Работа со сложными объектами

        // Зададим коллекцию людей
        Collection<People> peoples = Arrays.asList(
                new People("Вася", 16, Sex.MAN),
                new People("Петя", 23, Sex.MAN),
                new People("Елена", 42, Sex.WOMEN),
                new People("Иван Иванович", 69, Sex.MAN)
        );

        // Выбрать мужчин-военообязанных
        List<People> militaryService = peoples.stream().filter((p)-> p.getAge() >= 18 && p.getAge() < 27
                && p.getSex() == Sex.MAN).collect(Collectors.toList());
        System.out.println("militaryService = " + militaryService); // напечатает militaryService = [{name='Петя', age=23, sex=MAN}]

        // Найти средний возраст среди мужчин
        double manAverageAge = peoples.stream().filter((p) -> p.getSex() == Sex.MAN).
                mapToInt(People::getAge).average().getAsDouble();
        System.out.println("manAverageAge = " + manAverageAge); // напечатает manAverageAge = 36.0

        // Найти кол-во потенциально работоспосбных людей в выборке (т.е. от 18 лет и учитывая что женщины выходят в 55 лет, а мужчина в 60)
        long peopleHowCanWork = peoples.stream().filter((p) -> p.getAge() >= 18).filter(
                (p) -> (p.getSex() == Sex.WOMEN && p.getAge() < 55) || (p.getSex() == Sex.MAN && p.getAge() < 60)).count();
        System.out.println("peopleHowCanWork = " + peopleHowCanWork); // напечатает manAverageAge = 2

    }

    private enum Sex {
        MAN,
        WOMEN
    }

    private static class People {
        private final String name;
        private final Integer age;
        private final Sex sex;

        public People(String name, Integer age, Sex sex) {
            this.name = name;
            this.age = age;
            this.sex = sex;
        }

        public String getName() {
            return name;
        }

        public Integer getAge() {
            return age;
        }

        public Sex getSex() {
            return sex;
        }

        @Override
        public String toString() {
            return "{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    ", sex=" + sex +
                    '}';
        }
    }

    public static void main(String[] args)  throws Exception {
        testFilterAndCount();
    }
}
``` 

Условие: дана коллекция класс People (с полями name — имя, age — возраст, sex — пол), вида  
```java 
Collection<People> collection = Arrays.asList( new People(«Вася», 16, Sex.MAN), new People(«Петя», 23, Sex.MAN), new People(«Елена», 42, Sex.WOMEN), new People(«Иван Иванович», 69, Sex.MAN));
```

##### Примеры как работать с таким классом: 

* Выбрать мужчин-военнообязанных (от 18 до 27 лет)
```java 
peoples.stream().filter((p)-> p.getAge() >= 18 && p.getAge() < 27 && p.getSex() == Sex.MAN).collect(Collectors.toList());
// [{name='Петя', age=23, sex=MAN}]
```

* Найти средний возраст среди мужчин
```java 
peoples.stream().filter((p) -> p.getSex() == Sex.MAN).mapToInt(People::getAge).average().getAsDouble();
// 36.0
```

* Найти кол-во потенциально работоспособных людей в выборке (т.е. от 18 лет и учитывая что женщины выходят в 55 лет, а мужчина в 60)
```java 
peoples.stream().filter((p) -> p.getAge() >= 18).filter((p) -> (p.getSex() == Sex.WOMEN && p.getAge() < 55) || (p.getSex() == Sex.MAN && p.getAge() < 60)).count();
// 2
```

## 3.2 Примеры использования distinct

Метод distinct возвращает stream без дубликатов, при этом для упорядоченного стрима (например, коллекция на основе list) порядок стабилен, для неупорядоченного — порядок не гарантируется. Рассмотрим результаты работы над коллекцией Collection ordered = Arrays.asList(«a1», «a2», «a2», «a3», «a1», «a2», «a2») и Collection nonOrdered = new HashSet<>(ordered).

* Получение коллекции без дубликатов из неупорядоченного стрима
```java 
nonOrdered.stream().distinct().collect(Collectors.toList());
// [a1, a2, a3] — порядок не гарантируется
```

* Получение коллекции без дубликатов из упорядоченного стрима
```java 
ordered.stream().distinct().collect(Collectors.toList());
// [a1, a2, a3] — порядок гарантируется
```
 
Обратите внимание:
1. Если вы используете distinct с классом, у которого переопределен equals, обязательно так же корректно переопределить hashCode в соответствие с контрактом equals/hashCode (самое главное чтобы hashCode для всех equals объектов, возвращал одинаковое значение), иначе distinct может не удалить дубликаты (аналогично, как при использовании HashSet/HashMap),
2. Если вы используете параллельные стримы и вам не важен порядок элементов после удаления дубликатов — намного лучше для производительности сделать сначала стрим неупорядоченным с помощь unordered(), а уже потом применять distinct(), так как подержание стабильности сортировки при параллельном стриме довольно затратно по ресурсам и distinct() на упорядоченным стриме будет выполнятся значительно дольше чем при неупорядоченном, 

детальный пример:
```java 
// Метод distinct возвращает stream без дубликатов, при этом для упорядоченного стрима (например, коллекция на основе list) порядок стабилен , для неупорядоченного - порядок не гарантируется
// Метод collect преобразует stream в коллекцию или другую структуру данных
private static void testDistinct() {
    System.out.println();
    System.out.println("Test distinct start");
    Collection<String> ordered = Arrays.asList("a1", "a2", "a2", "a3", "a1", "a2", "a2");
    Collection<String> nonOrdered = new HashSet<>(ordered);

    // Получение коллекции без дубликатов
    List<String> distinct = nonOrdered.stream().distinct().collect(Collectors.toList());
    System.out.println("distinct = " + distinct); // напечатает distinct = [a1, a2, a3] - порядок не гарантируется

    List<String> distinctOrdered = ordered.stream().distinct().collect(Collectors.toList());
    System.out.println("distinctOrdered = " + distinctOrdered); // напечатает distinct = [a1, a2, a3] - порядок гарантируется
}
```

## 3.3 Примеры использования Match функций (anyMatch, allMatch, noneMatch)

Условие: дана коллекция строк Arrays.asList(«a1», «a2», «a3», «a1»), давайте посмотрим, как её можно обрабатывать используя Match функции 

* Найти существуют ли хоть один «a1» элемент в коллекции
```java 
collection.stream().anyMatch(«a1»::equals);
// true
```

* Найти существуют ли хоть один «a8» элемент в коллекции
```java 
collection.stream().anyMatch(«a8»::equals);
// false
```

* Найти есть ли символ «1» у всех элементов коллекции
```java 
collection.stream().allMatch((s) -> s.contains(«1»));
// false
```

* Проверить что не существуют ни одного «a7» элемента в коллекции
```java 
collection.stream().noneMatch(«a7»::equals);
// true
```

детальный пример:
```java 
// Метод anyMatch - возвращает true, если условие выполняется хотя бы для одного элемента
// Метод noneMatch - возвращает true, если условие не выполняется ни для одного элемента
// Метод allMatch - возвращает true, если условие выполняется для всех элементов
private static void testMatch() {
    System.out.println();
    System.out.println("Test anyMatch, allMatch, noneMatch  start");
    Collection<String> collection = Arrays.asList("a1", "a2", "a3", "a1");

    // найти существуют ли хоть одно совпадение с шаблоном в коллекции
    boolean isAnyOneTrue = collection.stream().anyMatch("a1"::equals);
    System.out.println("anyOneTrue " + isAnyOneTrue); // напечатает true
    boolean isAnyOneFalse = collection.stream().anyMatch("a8"::equals);
    System.out.println("anyOneFlase " + isAnyOneFalse); // напечатает false

    // найти существуют ли все совпадения с шаблоном в коллекции
    boolean isAll = collection.stream().allMatch((s) -> s.contains("1"));
    System.out.println("isAll " + isAll); // напечатает false

    // сравнение на неравенство
    boolean isNotEquals = collection.stream().noneMatch("a7"::equals);
    System.out.println("isNotEquals " + isNotEquals); // напечатает true
}
```

## 3.4 Примеры использования Map функций (map, mapToInt, FlatMap, FlatMapToInt)

Условие: даны две коллекции collection1 = Arrays.asList(«a1», «a2», «a3», «a1») и collection2 = Arrays.asList(«1,2,0», «4,5»), давайте посмотрим как её можно обрабатывать используя различные map функции

* Добавить "_1" к каждому элементу первой коллекции
```java 
collection1.stream().map((s) -> s + "_1").collect(Collectors.toList());
// [a1_1, a2_1, a3_1, a1_1]
```

* В первой коллекции убрать первый символ и вернуть массив чисел (int[])
```java 
collection1.stream().mapToInt((s) -> Integer.parseInt(s.substring(1))).toArray();
// [1, 2, 3, 1]
```

* Из второй коллекции получить все числа, перечисленные через запятую из всех элементов
```java 
collection2.stream().flatMap((p) -> Arrays.asList(p.split(",")).stream()).toArray(String[]::new);
// [1, 2, 0, 4, 5]
```

* Из второй коллекции получить сумму всех чисел, перечисленных через запятую
```java 
collection2.stream().flatMapToInt((p) -> Arrays.asList(p.split(",")).stream().mapToInt(Integer::parseInt)).sum();
// 12
```

Обратите внимание: все map функции могут вернуть объект другого типа (класса), то есть map может работать со стримом строк, а на выходе дать Stream из значений Integer или получать класс людей People, а возвращать класс Office, где эти люди работают и т.п., flatMap (flatMapToInt и т.п.) на выходе должны возвращать стрим с одним, несколькими или ни одним элементов для каждого элемента входящего стрима (см. последние два примера). 

```java 
// Метод Map изменяет выборку по определенному правилу, возвращает stream с новой выборкой
private static void testMap() {
    System.out.println();
    System.out.println("Test map start");
    Collection<String> collection = Arrays.asList("a1", "a2", "a3", "a1");
    // Изменение всех элементов коллекции
    List<String> transform = collection.stream().map((s) -> s + "_1").collect(Collectors.toList());
    System.out.println("transform = " + transform); // напечатает transform = [a1_1, a2_1, a3_1, a1_1]

    // убрать первый символ и вернуть числа
    List<Integer> number = collection.stream().map((s) -> Integer.parseInt(s.substring(1))).collect(Collectors.toList());
    System.out.println("number = " + number); // напечатает transform = [1, 2, 3, 1]

}

// Метод MapToInt - изменяет выборку по определенному правилу, возвращает stream с новой числовой выборкой
private static void testMapToInt() {
    System.out.println();
    System.out.println("Test mapToInt start");
    Collection<String> collection = Arrays.asList("a1", "a2", "a3", "a1");
    // убрать первый символ и вернуть числа
    int[] number = collection.stream().mapToInt((s) -> Integer.parseInt(s.substring(1))).toArray();
    System.out.println("number = " + Arrays.toString(number)); // напечатает number = [1, 2, 3, 1]

}

// Метод FlatMap - похоже на Map - только вместо одного значения, он возвращает целый stream значений
private static void testFlatMap() {
    System.out.println();
    System.out.println("Test flat map start");
    Collection<String> collection = Arrays.asList("1,2,0", "4,5");
    // получить все числовые значения, которые хранятся через запятую в collection
    String[] number = collection.stream().flatMap((p) -> Arrays.asList(p.split(",")).stream()).toArray(String[]::new);
    System.out.println("number = " + Arrays.toString(number)); // напечатает number = [1, 2, 0, 4, 5]
}

// Метод FlatMapToInt - похоже на MapToInt - только вместо одного значения, он возвращает целый stream значений
private static void testFlatMapToInt() {
    System.out.println();
    System.out.println("Test flat map start");
    Collection<String> collection = Arrays.asList("1,2,0", "4,5");
    // получить сумму всех числовые значения, которые хранятся через запятую в collection
    int sum = collection.stream().flatMapToInt((p) -> Arrays.asList(p.split(",")).stream().mapToInt(Integer::parseInt)).sum();
    System.out.println("sum = " + sum); // напечатает sum = 12
}
```

## 3.5 Примеры использования Sorted функции

Условие: даны две коллекции коллекция строк Arrays.asList(«a1», «a4», «a3», «a2», «a1», «a4») и коллекция людей класса People (с полями name — имя, age — возраст, sex — пол), вида Arrays.asList( new People(«Вася», 16, Sex.MAN), new People(«Петя», 23, Sex.MAN), new People(«Елена», 42, Sex.WOMEN), new People(«Иван Иванович», 69, Sex.MAN)). Давайте посмотрим примеры как их можно сортировать: 

* Отсортировать коллекцию строк по алфавиту
```java 
collection.stream().sorted().collect(Collectors.toList())
// [a1, a1, a2, a3, a4, a4]
```

* Отсортировать коллекцию строк по алфавиту в обратном порядке
```java 
collection.stream().sorted((o1, o2) -> -o1.compareTo(o2)).collect(Collectors.toList())
// [a4, a4, a3, a2, a1, a1]
```

* Отсортировать коллекцию строк по алфавиту и убрать дубликаты
```java 
collection.stream().sorted().
                    distinct().
                    collect(Collectors.toList())
// [a1, a2, a3, a4]
```

* Отсортировать коллекцию строк по алфавиту в обратном порядке и убрать дубликаты
```java 
collection.stream().sorted((o1, o2) -> -o1.compareTo(o2)).
                distinct().
                collect(Collectors.toList())
// [a4, a3, a2, a1]
```

* Отсортировать коллекцию людей по имени в обратном алфавитном порядке
```java 
peoples.stream().sorted((o1,o2) -> -o1.getName().
                compareTo(o2.getName())).
                collect(Collectors.toList())
// [{'Петя'}, {'Иван Иванович'}, {'Елена'}, {'Вася'}]
```

* Отсортировать коллекцию людей сначала по полу, а потом по возрасту
```java 
peoples.stream().sorted((o1, o2) -> o1.getSex() != o2.getSex()? o1.getSex().
                compareTo(o2.getSex()): o1.getAge().compareTo(o2.getAge())).
                collect(Collectors.toList());
// [{'Вася'}, {'Петя'}, {'Иван Иванович'}, {'Елена'}]
```

подробный примемр:
```java 
// Метод Sorted позволяет сортировать значения либо в натуральном порядке, либо задавая Comparator
private static void testSorted() {
    System.out.println();
    System.out.println("Test sorted start");

    // ************ Работа со строками
    Collection<String> collection = Arrays.asList("a1", "a4", "a3", "a2", "a1", "a4");

    // отсортировать значения по алфавиту
    List<String> sorted = collection.stream().sorted().collect(Collectors.toList());
    System.out.println("sorted = " + sorted); // напечатает sorted = [a1, a1, a2, a3, a4, a4]

    // отсортировать значения по алфавиту и убрать дубликаты
    List<String> sortedDistinct = collection.stream().sorted().distinct().collect(Collectors.toList());
    System.out.println("sortedDistinct = " + sortedDistinct); // напечатает sortedDistinct = [a1, a2, a3, a4]

    // отсортировать значения по алфавиту в обратном порядке
    List<String> sortedReverse = collection.stream().sorted((o1, o2) -> -o1.compareTo(o2)).collect(Collectors.toList());
    System.out.println("sortedReverse = " + sortedReverse); // напечатает sortedReverse = [a4, a4, a3, a2, a1, a1]

    // отсортировать значения по алфавиту в обратном порядке  и убрать дубликаты
    List<String> distinctReverse = collection.stream().sorted((o1, o2) -> -o1.compareTo(o2)).distinct().collect(Collectors.toList());
    System.out.println("distinctReverse = " + distinctReverse); // напечатает sortedReverse = [a4, a3, a2, a1]

    // ************ Работа с объектами
    // Зададим коллекцию людей
    Collection<People> peoples = Arrays.asList(
            new People("Вася", 16, Sex.MAN),
            new People("Петя", 23, Sex.MAN),
            new People("Елена", 42, Sex.WOMEN),
            new People("Иван Иванович", 69, Sex.MAN)
    );

    // Отсортировать по имени в обратном алфавитном порядке
    Collection<People> byName = peoples.stream().sorted((o1,o2) -> -o1.getName().compareTo(o2.getName())).collect(Collectors.toList());
    System.out.println("byName = " + byName); // byName = [{name='Петя', age=23, sex=MAN}, {name='Иван Иванович', age=69, sex=MAN}, {name='Елена', age=42, sex=WOMEN}, {name='Вася', age=16, sex=MAN}]

    // Отсортировать сначала по полу, а потом по возрасту
    Collection<People> bySexAndAge = peoples.stream().sorted((o1, o2) -> o1.getSex() != o2.getSex() ? o1.getSex().
            compareTo(o2.getSex()) : o1.getAge().compareTo(o2.getAge())).collect(Collectors.toList());
    System.out.println("bySexAndAge = " + bySexAndAge); // bySexAndAge = [{name='Вася', age=16, sex=MAN}, {name='Петя', age=23, sex=MAN}, {name='Иван Иванович', age=69, sex=MAN}, {name='Елена', age=42, sex=WOMEN}]
}

private enum Sex {
    MAN,
    WOMEN
}

private static class People {
    private final String name;
    private final Integer age;
    private final Sex sex;

    public People(String name, Integer age, Sex sex) {
        this.name = name;
        this.age = age;
        this.sex = sex;
    }

    public String getName() {
        return name;
    }

    public Integer getAge() {
        return age;
    }

    public Sex getSex() {
        return sex;
    }

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", sex=" + sex +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof People)) return false;
        People people = (People) o;
        return Objects.equals(name, people.name) &&
                Objects.equals(age, people.age) &&
                Objects.equals(sex, people.sex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age, sex);
    }
}
```

## 3.6 Примеры использования Max и Min функций

Условие: дана коллекция строк Arrays.asList(«a1», «a2», «a3», «a1»), и коллекция класса Peoples из прошлых примеров про Sorted и Filter функции.

* Найти максимальное значение среди коллекции строк
```java 
collection.stream().max(String::compareTo).get()
// a3
```

* Найти минимальное значение среди коллекции строк 
```java 
collection.stream().min(String::compareTo).
                get()
// a1
```

* Найдем человека с максимальным возрастом
```java 
peoples.stream().max((p1, p2) -> p1.getAge().
                compareTo(p2.getAge())).
                get()
// {name='Иван Иванович', age=69, sex=MAN}
```

* Найдем человека с минимальным возрастом
```java 
peoples.stream().min((p1, p2) -> p1.getAge().
                compareTo(p2.getAge())).
                get()
// {name='Вася', age=16, sex=MAN}
```
подробный пример:
```java 
// Метод max вернет максимальный элемент, в качестве условия использует компаратор
// Метод min вернет минимальный элемент, в качестве условия использует компаратор
private static void testMinMax() {
    System.out.println();
    System.out.println("Test min and max start");
    // ************ Работа со строками
    Collection<String> collection = Arrays.asList("a1", "a2", "a3", "a1");

    // найти максимальное значение
    String max = collection.stream().max(String::compareTo).get();
    System.out.println("max " + max); // напечатает a3

    // найти минимальное значение
    String min = collection.stream().min(String::compareTo).get();
    System.out.println("min " + min); // напечатает a1

    // ************ Работа со сложными объектами

    // Зададим коллекцию людей
    Collection<People> peoples = Arrays.asList(
            new People("Вася", 16, Sex.MAN),
            new People("Петя", 23, Sex.MAN),
            new People("Елена", 42, Sex.WOMEN),
            new People("Иван Иванович", 69, Sex.MAN)
    );

    // найти человека с максимальным возрастом
    People older = peoples.stream().max((p1, p2) -> p1.getAge().compareTo(p2.getAge())).get();
    System.out.println("older " + older); // напечатает {name='Иван Иванович', age=69, sex=MAN}

    // найти человека с минимальным возрастом
    People younger = peoples.stream().min((p1, p2) -> p1.getAge().compareTo(p2.getAge())).get();
    System.out.println("younger " + younger); // напечатает {name='Вася', age=16, sex=MAN}
}


private enum Sex {
    MAN,
    WOMEN
}

private static class People {
    private final String name;
    private final Integer age;
    private final Sex sex;

    public People(String name, Integer age, Sex sex) {
        this.name = name;
        this.age = age;
        this.sex = sex;
    }

    public String getName() {
        return name;
    }

    public Integer getAge() {
        return age;
    }

    public Sex getSex() {
        return sex;
    }

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", sex=" + sex +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof People)) return false;
        People people = (People) o;
        return Objects.equals(name, people.name) &&
                Objects.equals(age, people.age) &&
                Objects.equals(sex, people.sex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age, sex);
    }
}  
```

## 3.7 Примеры использования ForEach и Peek функций

Обе ForEach и Peek по сути делают одно и тоже, меняют свойства объектов в стриме, единственная разница между ними в том что ForEach терминальная и она заканчивает работу со стримом, в то время как Peek конвейерная и работа со стримом продолжается. Например, есть коллекция:
```java 
Collection<StringBuilder> list = Arrays.asList(new StringBuilder("a1"), new StringBuilder("a2"), new StringBuilder("a3"));
```
И нужно добавить к каждому элементу "_new", то для ForEach код будет 
```java 
list.stream().forEachOrdered((p) -> p.append("_new")); // list - содержит [a1_new, a2_new, a3_new]
```
а для peek код будет 
```java 
List<StringBuilder> newList = list.stream().peek((p) -> p.append("_new")).collect(Collectors.toList()); // и list и newList содержат [a1_new, a2_new, a3_new]
```

подробный пример:
```java 
// Метод ForEach применяет указанный метод к каждому элементу стрима и заканчивает работу со стримом
private static void testForEach() {
    System.out.println();
    System.out.println("For each start");
    Collection<String> collection = Arrays.asList("a1", "a2", "a3", "a1");
    // Напечатать отладочную информацию по каждому элементу стрима
    System.out.print("forEach = ");
    collection.stream().map(String::toUpperCase).forEach((e) -> System.out.print(e + ",")); // напечатает forEach = A1,A2,A3,A1,
    System.out.println();

    Collection<StringBuilder> list = Arrays.asList(new StringBuilder("a1"), new StringBuilder("a2"), new StringBuilder("a3"));
    list.stream().forEachOrdered((p) -> p.append("_new"));
    System.out.println("forEachOrdered = " + list); // напечатает forEachOrdered = [a1_new, a2_new, a3_new]
}

// Метод Peek возвращает тот же стрим, но при этом применяет указанный метод к каждому элементу стрима
private static void testPeek() {
    System.out.println();
    System.out.println("Test peek start");
    Collection<String> collection = Arrays.asList("a1", "a2", "a3", "a1");
    // Напечатать отладочную информацию по каждому элементу стрима
    System.out.print("peak1 = ");
    List<String> peek = collection.stream().map(String::toUpperCase).peek((e) -> System.out.print(e + ",")).
            collect(Collectors.toList());
    System.out.println(); // напечатает peak1 = A1,A2,A3,A1,
    System.out.println("peek2 = " + peek); // напечатает peek2 = [A1, A2, A3, A1]

    Collection<StringBuilder> list = Arrays.asList(new StringBuilder("a1"), new StringBuilder("a2"), new StringBuilder("a3"));
    List<StringBuilder> newList = list.stream().peek((p) -> p.append("_new")).collect(Collectors.toList());
    System.out.println("newList = " + newList); // напечатает newList = [a1_new, a2_new, a3_new]
}
```

## 3.8 Примеры использования Reduce функции

Метод reduce позволяет выполнять агрегатные функции на всей коллекцией (такие как сумма, нахождение минимального или максимального значение и т.п.), он возвращает одно значение для стрима, функция получает два аргумента — значение полученное на прошлых шагах и текущее значение.

Условие: Дана коллекция чисел Arrays.asList(1, 2, 3, 4, 2) выполним над ними несколько действий используя reduce.


* Получить сумму чисел или вернуть 0
```java 
collection.stream().reduce((s1, s2) -> s1 + s2).orElse(0);
// 12
```

* Вернуть максимум или -1
```java 
collection.stream().reduce(Integer::max).orElse(-1);
// 4
```

* Вернуть сумму нечетных чисел или 0
```java 
collection.stream().filter(o -> o % 2 != 0).reduce((s1, s2) -> s1 + s2).orElse(0);
// 4
```

## 3.9 Примеры использования toArray и collect функции

Если с toArray все просто, можно либо вызвать toArray() получить Object[], либо toArray(T[]::new) — получив массив типа T, то collect позволяет много возможностей преобразовать значение в коллекцию, map'у или любой другой тип. Для этого используются статические методы из Collectors, например преобразование в List будет stream.collect(Collectors.toList()). 

Давайте рассмотрим статические методы из Collectors: 

* toList, toCollection, toSet - представляют стрим в виде списка, коллекции или множества
* toConcurrentMap, toMap - позволяют преобразовать стрим в map
* averagingInt, averagingDouble, averagingLong - возвращают среднее значение
* summingInt, summingDouble, summingLong - возвращает сумму
* summarizingInt, summarizingDouble, summarizingLong - возвращают SummaryStatistics с разными агрегатными значениями
* partitioningBy - разделяет коллекцию на две части по соответствию условию и возвращает их как Map<Boolean, List>
* groupingBy - разделяет коллекцию на несколько частей и возвращает Map<N, List<T>>
* mapping - дополнительные преобразования значений для сложных Collector'ов

Теперь давайте рассмотрим работу с collect и toArray на примерах: 
Условие: Дана коллекция чисел Arrays.asList(1, 2, 3, 4), рассмотрим работу collect и toArray с ней

* Получить сумму нечетных чисел
```java 
numbers.stream().collect(Collectors.summingInt(((p) -> p % 2 == 1? p: 0)));
// 4
```

* Вычесть от каждого элемента 1 и получить среднее
```java 
numbers.stream().collect(Collectors.averagingInt((p) -> p - 1));
// 1.5
```

* Прибавить к числам 3 и получить статистику
```java 
numbers.stream().collect(Collectors.summarizingInt((p) -> p + 3));
// IntSummaryStatistics{count=4, sum=22, min=4, average=5.5, max=7}
```

* Разделить числа на четные и нечетные
```java 
numbers.stream().collect(Collectors.partitioningBy((p) -> p % 2 == 0));
// {false=[1, 3], true=[2, 4]}
```

Условие: Дана коллекция строк Arrays.asList(«a1», «b2», «c3», «a1»), рассмотрим работу collect и toArray с ней

* Получение списка без дубликатов
```java 
strings.stream().distinct().collect(Collectors.toList())
// [a1, b2, c3]
```

* Получить массив строк без дубликатов и в верхнем регистре
```java 
strings.stream().distinct().map(String::toUpperCase).toArray(String[]::new)
// {A1, B2, C3}
```

* Объединить все элементы в одну строку через разделитель: и обернуть тегами <b>… </b>
```java 
strings.stream().collect(Collectors.joining(": ", "<b> ", " </b>"))
// <b> a1: b2: c3: a1 </b>
```

* Преобразовать в map, где первый символ ключ, второй символ значение
```java 
strings.stream().distinct().collect(Collectors.toMap((p) -> p.substring(0, 1), (p) -> p.substring(1, 2)))
// {a=1, b=2, c=3}
```

* Преобразовать в map, сгруппировав по первому символу строки
```java 
strings.stream().collect(Collectors.groupingBy((p) -> p.substring(0, 1)))
// {a=[a1, a1], b=[b2], c=[c3]}
```

* Преобразовать в map, сгруппировав по первому символу строки и объединим вторые символы через ":"
```java 
strings.stream().collect(Collectors.groupingBy((p) -> p.substring(0, 1), Collectors.mapping((p) -> p.substring(1, 2), Collectors.joining(":"))));
// {a=1:1, b=2, c=3}
```
детальный пример:

```java 
// Метод collect преобразует stream в коллекцию или другую структуру данных
// Полезные статические методы из Collectors:
// toList, toCollection, toSet - представляют стрим в виде списка, коллекции или множества
// toConcurrentMap, toMap - позволяют преобразовать стрим в map, используя указанные функции
// averagingInt, averagingDouble, averagingLong - возвращают среднее значение
// summingInt, summingDouble, summingLong - возвращает сумму
// summarizingInt, summarizingDouble, summarizingLong - возвращают SummaryStatistics с разными агрегатными значениями
// partitioningBy - разделяет коллекцию на две части по соответствию условию и возвращает их как Map<Boolean, List>
// groupingBy - разделить коллекцию по условию и вернуть Map<N, List<T>>, где T - тип последнего стрима, N - значение разделителя
// mapping - дополнительные преобразования значений для сложных Collector'ов
private static void testCollect() {
    System.out.println();
    System.out.println("Test distinct start");

    // ******** Работа со строками
    Collection<String> strings = Arrays.asList("a1", "b2", "c3", "a1");

    // Получение списка из коллекции строк без дубликатов
    List<String> distinct = strings.stream().distinct().collect(Collectors.toList());
    System.out.println("distinct = " + distinct); // напечатает distinct = [a1, b2, c3]

    // Получение массива уникальных значений из коллекции строк
    String[] array = strings.stream().distinct().map(String::toUpperCase).toArray(String[]::new);
    System.out.println("array = " + Arrays.asList(array)); // напечатает array = [A1, B2, C3]

    // Объединить все элементы в одну строку через разделитель : и обернуть тегами <b> ... </b>
    String join = strings.stream().collect(Collectors.joining(" : ", "<b> ", " </b>"));
    System.out.println("join = " + join); // напечатает <b> a1 : b2 : c3 : a1 </b>

    // Преобразовать в map, где первый символ ключ, второй символ значение
    Map<String, String> map = strings.stream().distinct().collect(Collectors.toMap((p) -> p.substring(0, 1), (p) -> p.substring(1, 2)));
    System.out.println("map = " + map); // напечатает map = {a=1, b=2, c=3}

    // Преобразовать в map, сгруппировав по первому символу строки
    Map<String, List<String>> groups = strings.stream().collect(Collectors.groupingBy((p) -> p.substring(0, 1)));
    System.out.println("groups = " + groups); // напечатает groups = {a=[a1, a1], b=[b2], c=[c3]}

    // Преобразовать в map, сгруппировав по первому символу строки и в качестве значения взять второй символ объединим через :
    Map<String, String> groupJoin = strings.stream().collect(Collectors.groupingBy((p) -> p.substring(0, 1), Collectors.mapping((p) -> p.substring(1, 2), Collectors.joining(":"))));
    System.out.println("groupJoin = " + groupJoin); // напечатает groupJoin = groupJoin = {a=1/1, b=2, c=3}

    // ******** Работа с числами
    Collection<Integer> numbers = Arrays.asList(1, 2, 3, 4);

    // Получить сумму нечетных чисел
    long sumOdd = numbers.stream().collect(Collectors.summingInt(((p) -> p % 2 == 1 ? p : 0)));
    System.out.println("sumOdd = " + sumOdd); // напечатает sumEven = 4

    // Вычесть к каждого элемента 1 и получить среднее
    double average = numbers.stream().collect(Collectors.averagingInt((p) -> p - 1));
    System.out.println("average = " + average); // напечатает average = 1.5

    // Прибавить к числам 3 и получить статистику
    IntSummaryStatistics statistics = numbers.stream().collect(Collectors.summarizingInt((p) -> p + 3));
    System.out.println("statistics = " + statistics); // напечатает statistics = IntSummaryStatistics{count=4, sum=22, min=4, average=5.500000, max=7}

    // Получить сумму четных чисел через IntSummaryStatistics
    long sumEven = numbers.stream().collect(Collectors.summarizingInt((p) -> p % 2 == 0 ? p : 0)).getSum();
    System.out.println("sumEven = " + sumEven); // напечатает sumEven = 6

    // Разделить числа на четные и нечетные
    Map<Boolean, List<Integer>> parts = numbers.stream().collect(Collectors.partitioningBy((p) -> p % 2 == 0));
    System.out.println("parts = " + parts); // напечатает parts = {false=[1, 3], true=[2, 4]}
    }
```

## 3.10 Пример создания собственного Collector'a

Кроме Collector'ов уже определенных в Collectors можно так же создать собственный Collector, Давайте рассмотрим пример как его можно создать. 

Метод определения пользовательского Collector'a: 
```java 
Collector<Тип_источника, Тип_аккумулятора, Тип_результата> сollector =  Collector.of(
                метод_инициализации_аккумулятора,
                метод_обработки_каждого_элемента,
                метод_соединения_двух_аккумуляторов,
                [метод_последней_обработки_аккумулятора]
        );
```

Как видно из кода выше, для реализации своего Collector'a нужно определить три или четыре метода (метод_последней_обработки_аккумулятора не обязателен). Рассмотрим следующий кода, который мы писали до Java 8, чтобы объединить все строки коллекции: 
```java 
StringBuilder b = new StringBuilder(); // метод_инициализации_аккумулятора
for(String s: strings) {
    b.append(s).append(" , "); // метод_обработки_каждого_элемента,
}
String joinBuilderOld = b.toString(); // метод_последней_обработки_аккумулятора
```

И аналогичный код, который будет написан в Java 8
```java 
String joinBuilder = strings.stream().collect(
   Collector.of(
                StringBuilder::new, // метод_инициализации_аккумулятора
                (b ,s) -> b.append(s).append(" , "), // метод_обработки_каждого_элемента,
                (b1, b2) -> b1.append(b2).append(" , "), // метод_соединения_двух_аккумуляторов
                StringBuilder::toString // метод_последней_обработки_аккумулятора
        )
);
```

В общем-то, три метода легко понять из кода выше, их мы писали практически при каждой обработки коллекций, но вот что такое метод_соединения_двух_аккумуляторов? Это метод который нужен для параллельной обработки Collector'a, в данном случае при параллельном стриме коллекция может быть разделенной на две части (или больше частей), в каждой из которых будет свой аккумулятор StringBuilder и потом необходимо будет их объединить, то код до Java 8 при 2 потоках будет таким:
```java 
StringBuilder b1 = new StringBuilder(); // метод_инициализации_аккумулятора_1        
for(String s: stringsPart1) { //  stringsPart1 - первая часть коллекции strings
    b1.append(s).append(" , "); // метод_обработки_каждого_элемента,
}

StringBuilder b2 = new StringBuilder(); // метод_инициализации_аккумулятора_2        
for(String s: stringsPart2) { //  stringsPart2 - вторая часть коллекции strings
    b2.append(s).append(" , "); // метод_обработки_каждого_элемента,
}

StringBuilder b = b1.append(b2).append(" , "), // метод_соединения_двух_аккумуляторов

String joinBuilderOld = b.toString(); // метод_последней_обработки_аккумулятора
```

Напишем свой аналог Collectors.toList() для работы со строковым стримом:
```java 
 // Напишем свой аналог toList 
        Collector<String, List<String>, List<String>> toList = Collector.of(
                ArrayList::new, // метод инициализации аккумулятора
                List::add, // метод обработки каждого элемента
                (l1, l2) -> { l1.addAll(l2); return l1; } // метод соединения двух аккумуляторов при параллельном выполнении
        );
// Используем его для получение списка строк без дубликатов из стрима
        List<String> distinct1 = strings.stream().distinct().collect(toList);
```

детальный пример:
```java 
// Напишем собственный Collector, который будет выполнять объединение строк с помощью StringBuilder
Collector<String,StringBuilder, String> stringBuilderCollector =  Collector.of(
        StringBuilder::new, // метод инициализации аккумулятора
        (b ,s) -> b.append(s).append(" , "), // метод обработки каждого элемента
        (b1, b2) -> b1.append(b2).append(" , "), // метод соединения двух аккумуляторов при параллельном выполнении
        StringBuilder::toString // метод, выполняющийся в самом конце
);
String joinBuilder = strings.stream().collect(stringBuilderCollector);
System.out.println("joinBuilder = " + joinBuilder); // напечатает joinBuilder = a1 , b2 , c3 , a1 ,

// Аналог Collector'а выше стилем JDK7 и ниже
StringBuilder b = new StringBuilder(); // метод инициализации аккумулятора
for(String s: strings) {
    b.append(s).append(" , "); // метод обработки каждого элемента
}
String joinBuilderOld = b.toString(); // метод, выполняющийся в самом конце
System.out.println("joinBuilderOld = " + joinBuilderOld); // напечатает joinBuilderOld = a1 , b2 , c3 , a1 ,

// Напишем свой аналог toList для получение списка из коллекции строк без дубликатов
Collector<String, List<String>, List<String>> toList = Collector.of(
        ArrayList::new, // метод инициализации аккумулятора
        List::add, // метод обработки каждого элемента
        (l1, l2) -> { l1.addAll(l2); return l1; } // метод соединения двух аккумуляторов при параллельном выполнении
);
List<String> distinct1 = strings.stream().distinct().collect(toList);
System.out.println("distinct1 = " + distinct1); // напечатает distinct1 = [a1, b2, c3]
```

## Ссылки
- Processing Data with Java SE 8 Streams, Part 1 от Oracle, http://www.oracle.com/technetwork/articles/java/ma14-java-se-8-streams-2177646.html 
- Processing Data with Java SE 8 Streams, Part 1 от Oracle, http://www.oracle.com/technetwork/articles/java/architect-streams-pt2-2227132.html 
- Полное руководство по Java 8 Stream http://javadevblog.com/polnoe-rukovodstvo-po-java-8-stream.html

