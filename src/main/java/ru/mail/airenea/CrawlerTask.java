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
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * Класс, который который реализует интерфейс Runnable.
 * Имеет ссылку на единственный экземпляр класса URLPool.
 * Берет пару URL-глубина из списка непросмотренных ссылок из класса URLPool и посещает страницу,
 * ищет на ней все ссылки и, если они еще не просмотренны, добавляет соответсвующую пару URL-глубина в
 * список непросмотренных ссылок класса URLPool.
 */

public class CrawlerTask implements Runnable {

    // Констанда, содержащая префикс URL,
    // нужный для того чтобы определить имеет ли URL правильный формат
    public static final String URL_PREFIX = "a href=\"";

    // Константа, содержащая постфикс URL
    public static final char URL_POSTEFIX = '"';

    //Время ожидания ответа
    public static final int TIMEOUT = 5000;

    // Переменная для хранения полученной пары URL-глубина из класса URLPool
    public URLDepthPair pair;

    // Переменная для хранения экземпляра класса URLPool
    public URLPool pool;

    // Конструктор класса, определяет экземпляра класса URLPool
    public CrawlerTask(URLPool gotPool) {
        pool = gotPool;
    }

    // Переопределяем метод запуска потока
    @Override
    public void run() {

        // Текущая глубина
        int currentDepth = 0;

        // Флаг оповещает о том, достигнута ли максимальная глубина поиска (true если нет)
        boolean notGotToMaxDepth = true;

        pair = null;

        // Пока размер пула не стал равен 0 или не была достигнута максимальная глубина поиска, исследуем страницу
        while (pool.getUnusedSize() != 0 || notGotToMaxDepth == true) {

            try {
                // Берем ссылку из списка
                pair = pool.getPair();

                // Если oldPair пуст, то не выполняем действия (пустой oldPair может говорить о том, что ссылка уже встречалась или сработала проверка на возможные исключительный случаи
                if (pair != null) {
                    currentDepth = pair.getDepth();

                    // Открываем соединение при помощи Socket
                    Socket socket;

                    socket = new Socket(pair.getHost(), 80);

                    // Задаем время ожидания
                    socket.setSoTimeout(TIMEOUT);


                    // Создаем поток ввода
                    OutputStream output;


                    output = socket.getOutputStream();


                    //Создаем PrintWriter и отправляем запрос на соединение
                    PrintWriter outputWriter = new PrintWriter(output, true);

                    outputWriter.println("GET " + pair.getPath() + " HTTP/1.1");
                    outputWriter.println("Host: " + pair.getHost());
                    outputWriter.println("Connection: close");
                    outputWriter.println();

                    // Создаем поток для получения информации со страницы
                    InputStream input;

                    input = socket.getInputStream();

                    // Создаем экземпляр класса InputStreamReader для преобразования байтов из входного потока в символы
                    InputStreamReader inputReader = new InputStreamReader(input);

                    // Создаем экземпляр класса BufferedReader для считывания символов из потока в буфер
                    BufferedReader bufferReader = new BufferedReader(inputReader);

                    System.out.println("Идет проверка URL: " + pair.getURL() + " , с глубиной: " + pair.getDepth());

                    // Строка в которую будут считываться строки html документа
                    String newLine;

                    // Считаем первую строку

                    newLine = bufferReader.readLine();

                    while (newLine != null) {
                        // Индексы нужны для хранения информации о положении найденной ссылки в данной строке
                        int beginIndex = 0;
                        int endIndex = 0;
                        int index = 0;

                        while (true) {

                            // Ищем вхождениев строку подстроки вида "a href="" , обозначающую начало ссылки
                            index = newLine.indexOf(URL_PREFIX, index);
                            if (index < 0) {
                                // Если начало не найдено - выходим из данного блока while что бы считать новую строку
                                break;
                            }

                            // Если нашли "a href="" то сдвигаемся на его длину и сохраняем индекс начала ссылки
                            index = index + URL_PREFIX.length();
                            beginIndex = index;

                            // Ищем символ ", обозначающий завершение ссылки
                            if (newLine.indexOf(URL_POSTEFIX, index) >= 0) {
                                endIndex = newLine.indexOf(URL_POSTEFIX, index);
                                index = endIndex;
                            }
                            else {
                                // Если завершение не найдено - выходим из данного блока while что бы считать новую строку
                                break;
                            }

                            URLDepthPair foundPair = new URLDepthPair(newLine.substring(beginIndex, endIndex), currentDepth + 1);
                            // Проверяем, что считанная ссылка верна, если не верна - не добавляем её в список
                            if (foundPair.getHost() != null && foundPair.getPath() != null) {
                                notGotToMaxDepth = pool.input(foundPair);
                            }
                        }
                        // Продолжаем считывать строки
                        newLine = bufferReader.readLine();
                    }

                    // Прежде чем переходить на новую страницу, закроем все ппотоки и Socket для данной страницы
                    output.close();
                    input.close();
                    socket.close();
                }
            }


            catch (NoSuchElementException e) {
                // Ошибка может быть вызвана при обращении к пустому пулу
                //System.out.println("Ошибка при обращении к пулу");
            }
            catch (SocketTimeoutException e) {
                // Отображаем соответствующую ошибку, если время ожидания соединения было превышено
                //System.err.println(pair.getURL() + ": Время ожидания вышло, не удалось подключиться!");
            }
            catch (IOException exeption) {
                // Удаляем поcледнее добавленное из проверенных пар, если было вызвано исключение, связанное с неверным форматом
                //System.out.println("Найдена ссылка с неверным форматом!");
                pool.removeUsed();
            }
            catch (Exception exeption) {
                // Обработка других исключений, связанных с созданием соединения и передачей данных
                //System.err.println("Ошибка при передаче данных!");
            }
        }
    }
}


