package ru.mail.airenea;

/**
 * Created by a_iri on 10.07.2018.
 */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Класс, который хранит списки типа LinkedList всех найденных пар URL-глубина,
 * всех еще не обработанных пар URL-глубина, список типа ArrayList пар URL-глубина, которые
 * уже были просмотренны.
 * Класс управляет синхронизацией и доступом потоков к ним.
 *
 */

public class URLPool {

    // Содержит все проверенные на данный момент ссылки
    public LinkedList<URLDepthPair> usedURLs;

    // Включает еще не просмотренные ссылки, когда список оказывается пуст, работа закончена
    public LinkedList<URLDepthPair> unusedURLs;

    // Список просмотренных URL, для сравнения
    LinkedList <String> seenURLs = new LinkedList<String>();

    // Количество запущенных ожидающих потоков
    public int waitingThreads;

    //Максимальная глубина поиска
    public int maxDepth;

    // Конструктор класса
    public URLPool(int depth) {
        maxDepth = depth;
        waitingThreads = 0;
        usedURLs	= new LinkedList<URLDepthPair>();
        unusedURLs = new LinkedList<URLDepthPair>();
    }


    //Синхронизированный метод получения количества проверенных пар URL-глубина
    public synchronized int getUsedSize() {
        return usedURLs.size();
    }

    // Возвращает количество ссылок в списке необработанных пар URL-глубина
    public synchronized int getUnusedSize() {
        return unusedURLs.size();
    }

    // Метод, который добавляет полученную ссылку в список необработанных пар URL-глубина,
    // если ее глубина меньше максимально заданной.
    // Иначе, добавляет полученную ссылку в список просмотренных пар URL-глубина
    public synchronized boolean input (URLDepthPair newPair) {

        // Флажок добавления в список
        boolean addFlag = false;

        if(newPair.getDepth() < maxDepth) {
            unusedURLs.addLast(newPair);
            addFlag = true;

            // В список вписали ссылку, запускаем поток, уменьшаем на единицу количество ожидающих потоков
            this.notify();

        }
        else {
            seenURLs.add(newPair.getURL());
            usedURLs.add(newPair);
        }
        return addFlag;
    }

    // Достает пару URL-глубина из списка неиспользованных пар, в процессе удаляет ее оттуда
    // и добавляет в списки просмотренных ссылок и всех известных пар
    public synchronized URLDepthPair getPair() {

        // Инициализируем переменную URLDepthPair нулем
        URLDepthPair pair = null;

        // Если список необработанных ссылок пуст - ставим поток в ожидание
        if (unusedURLs.isEmpty() == true) {

            // Увеличиваем количество ждущих потоков
            waitingThreads++;

            try {
                this.wait();
            }
            catch (InterruptedException e) {
                System.out.println(e.getMessage());
                return null;
            }

            // Как только поток проснулся, уменьшаем количество ждущих потоков
            waitingThreads--;
        }

        // Как только поток дождался получения данных берем пару из непроверенных и добавляем в просмотренные и проверенные
        Iterator<URLDepthPair> iterator = unusedURLs.iterator();

        // Дополнительная проверка на случай, если в некоторый момент произойдет обращение к пустому перечню
        if (!iterator.hasNext()) {
            return null;
        }

        pair = unusedURLs.removeFirst();

        // Проверка на повторение уже встретившихся сайтов - в этом случае не добавляем сайт к просмотренным
        for (int i = 0; i < usedURLs.size(); ++i) {
            if (pair.getURL().equals(seenURLs.get(i))) {
                return null;
            }
        }

        seenURLs.add(pair.getURL());
        usedURLs.add(pair);
        return pair;
    }

    // Возвращает количество запущенных ожидающих потоков
    public synchronized int getWaitThreads() {
        return waitingThreads;
    }

    // Возвращает список просмотренных ссылок
    public synchronized LinkedList<String> getseenURLsList () {
        return seenURLs;
    }

    //Синхронизированный метод убирает последнюю сохраненную ссылку (вызывается в случе неверного формата ссылки)
    public synchronized void removeUsed() {
        usedURLs.removeLast();
    }

    //Синхронизированный метод возвращает список просмотренных сайтов
    public synchronized LinkedList<URLDepthPair> getusedList() {
        return usedURLs;
    }
}


