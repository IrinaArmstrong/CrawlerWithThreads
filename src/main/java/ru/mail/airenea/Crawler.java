package ru.mail.airenea;

/**
 * Created by a_iri on 10.07.2018.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Класс,который реализует основной функционал приложения.
 * Принимает в качетве аргументов три значения типа String: URL, глубину поиска,
 * которую в последствии преобразовывает в переменную типа int и количество потоков.
 * Сохраняет первые два значения как объект класса URLDepthPair.
 **/

public class Crawler {

    // Константа, хранящая ссылку по умолчанию
    static final String  DEFAULTURL = "http://www.cs.caltech.edu/courses/cs11";

    public static void main(String[] args) {


        // Переменная, для хранения max глубины поиска
        int  firstDepth = 0;
        // Переменная для количества потоков
        int numberOfThreads = 0;
        // Переменная, для хранения изначальной ссылки, введенной пользователем
        String firstUrl = "";

        //Ввод данных с клавиатуры
        try {
            Scanner in = new Scanner(System.in);
            System.out.println("Введите ссылку: ");
            firstUrl = in.nextLine();
            System.out.println("Введите глубину поиска (целое положительное число): ");
            firstDepth = in.nextInt();
            System.out.println("Введите количество потоков (целое положительное число): ");
            numberOfThreads = in.nextInt();
        }
        catch (Exception ex) {
            System.out.println("Не верный формат ввода данных!");
            System.exit(1);
        }

        if (firstUrl.equals("")) {
            firstUrl = DEFAULTURL;
        }

        if(firstDepth < 0) {
            // Если глубина не положительное число, то выходим из программы
            System.out.println("Не верный формат ввода данных!");
            System.exit(1);
        }
        if(numberOfThreads < 0) {
            // Если количество потоков не положительное число, то выходим из программы
            System.out.println("Не верный формат ввода данных! Количество потоков не может быть отрицательным числом.");
            System.exit(1);
        }

/*        // Проверяем правильность ввода данных: количество аргументов и значение для глубины поиска
        if(args.length != 3) {
            System.out.println("Не верный формат ввода данных!");
            System.exit(1);
        }
        else {
            // Глубина поиска и количество потоков - положительные целые числа
            try {
                firstDepth = Integer.parseInt(args[1]);
                numberOfThreads = Integer.parseInt(args[2]);

                if((firstDepth < 0) && (numberOfThreads < 0)) {
                    // Если глубина или количество потоков не положительные числа,
                    // то выходим из программы с кодом ошибки = 1
                    System.out.println("Не верный формат ввода данных!");
                    System.exit(1);
                }
            }
            catch(NumberFormatException e) {
                // Если глубина или количество потоков не преобразовываются в целочисленный тип,
                // то выходим из программы с кодом ошибки = 1
                System.out.println("Не верный формат ввода данных!");
                System.exit(1);
            }
        }*/

        // Переменная, в которой хранится пара URL-глубина, которую ввел пользователь
        URLDepthPair gotURLDepthPair = new URLDepthPair(firstUrl, 0);

        //  Создаем хранилище адресов и включаем туда пару URL-глубина, которую ввел пользователь
        URLPool pool = new URLPool(firstDepth);
        pool.input(gotURLDepthPair);

        // Переменная, хранящая количество активных в данный момент потоков
        int activeThreads = Thread.activeCount();

        // Выполняем цикл, пока количество ожидающих потоков не сравнялось
        // с общим количеством потоков, установленным пользователем,
        // Если всего потоков меньше, чем запрашивал пользователь,
        // то увеличиваем их количество до требуемого.
        // Иначе - поток засыпает.
        while (pool.getWaitThreads() != numberOfThreads) {
            if (Thread.activeCount() - activeThreads < numberOfThreads) {
                CrawlerTask crawler = new CrawlerTask(pool);
                Thread t = new Thread(crawler);
                t.start();
            }
            else {
                try {
                    Thread.sleep(1000);

                }
                catch (InterruptedException e) {
                    // System.out.println("Caught unexpected " +
                    //                    "InterruptedException, ignoring...");
                }
            }
        }

        // Когда количество ожидающих потоков сравнялось с общим количеством потоков,
        // то выводим список посещенных ссылок на экран и заканчиваем работу программы.

        // Проверка на верность введенного URL
        if (pool.getUsedSize() != 0) {
            System.out.println("Проверка завершена!");

            //Выводим на экран весь список URL со значением глубины
            LinkedList<URLDepthPair> forPrint = pool.getusedList();
            for (int i = 0; i < forPrint.size(); ++i) {
                URLDepthPair pairForPrint = forPrint.get(i);
                System.out.println("№ " + (i+1) + " " + pairForPrint.toString());
            }
        }
        // Если функция вернула нулевой спискок, то это означает,
        // что для самого первого URL было вызвано исключение IOExecption, связанное с неверным форматом URL,
        // и список был очищен
        else {
            System.out.println("Такого начального адреса не существует!");
        }
        System.exit(0);

    }
}


