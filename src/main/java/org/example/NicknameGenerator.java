package org.example;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class NicknameGenerator {

    private static final String LETTERS = "abc";
    private static final int TEXT_COUNT = 100_000;
    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 5;

    private static final AtomicInteger count3 = new AtomicInteger(0);
    private static final AtomicInteger count4 = new AtomicInteger(0);
    private static final AtomicInteger count5 = new AtomicInteger(0);

    private final Random random = new Random(); // Random как поле экземпляра

    public String generateText(int length) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(LETTERS.charAt(random.nextInt(LETTERS.length())));
        }
        return text.toString();
    }

    public static void main(String[] args) throws InterruptedException {
        NicknameGenerator generator = new NicknameGenerator(); // Создаем экземпляр класса
        String[] texts = generator.generateTexts(TEXT_COUNT, MIN_LENGTH, MAX_LENGTH);

        // Используем ExecutorService для управления потоками
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Отправляем задачи на выполнение
        executor.submit(() -> generator.countMatchingTexts(texts, NicknameGenerator::isPalindrome));
        executor.submit(() -> generator.countMatchingTexts(texts, NicknameGenerator::isSameLetter));
        executor.submit(() -> generator.countMatchingTexts(texts, NicknameGenerator::isAscending));

        // Завершаем ExecutorService и ждем завершения всех задач
        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);

        printResults();
    }

    // DRY: выносим общую логику подсчета в отдельный метод
    public void countMatchingTexts(String[] texts, Predicate<String> criteria) {
        for (String text : texts) {
            if (criteria.test(text)) {
                incrementCounter(text.length());
            }
        }
    }

    // DRY: выносим логику генерации массива текстов в отдельный метод
    private String[] generateTexts(int count, int minLength, int maxLength) {
        String[] texts = new String[count];
        for (int i = 0; i < texts.length; i++) {
            int length = minLength + random.nextInt(maxLength - minLength + 1);
            texts[i] = generateText(length);
        }
        return texts;
    }

    public static boolean isPalindrome(String text) {
        String reversed = new StringBuilder(text).reverse().toString();
        return text.equals(reversed);
    }

    public static boolean isSameLetter(String text) {
        if (text.length() == 0) return true;
        char firstChar = text.charAt(0);
        for (int i = 1; i < text.length(); i++) {
            if (text.charAt(i) != firstChar) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAscending(String text) {
        if (text.length() == 0) return true;
        for (int i = 1; i < text.length(); i++) {
            if (text.charAt(i) < text.charAt(i - 1)) {
                return false;
            }
        }
        return true;
    }

    // DRY: выносим логику инкремента счетчика в отдельный метод
    private static void incrementCounter(int length) {
        switch (length) {
            case 3:
                count3.incrementAndGet();
                break;
            case 4:
                count4.incrementAndGet();
                break;
            case 5:
                count5.incrementAndGet();
                break;
        }
    }

    // DRY: выносим логику вывода результатов в отдельный метод
    public static void printResults() {
        try {
            PrintStream out = new PrintStream(System.out, true, "UTF-8");
            System.setOut(out); // Перенаправляем стандартный вывод

            System.out.println("Результаты:");
            System.out.println("  Слов длиной 3, соответствующих критериям: " + count3.get() + " шт.");
            System.out.println("  Слов длиной 4, соответствующих критериям: " + count4.get() + " шт.");
            System.out.println("  Слов длиной 5, соответствующих критериям: " + count5.get() + " шт.");
        } catch (UnsupportedEncodingException e) {
            System.err.println("Ошибка при установке кодировки UTF-8: " + e.getMessage());
            // В случае ошибки выводим результаты со стандартной кодировкой
            System.out.println("Результаты:");
            System.out.println("  Слов длиной 3, соответствующих критериям: " + count3.get() + " шт.");
            System.out.println("  Слов длиной 4, соответствующих критериям: " + count4.get() + " шт.");
            System.out.println("  Слов длиной 5, соответствующих критериям: " + count5.get() + " шт.");
        }
    }
}