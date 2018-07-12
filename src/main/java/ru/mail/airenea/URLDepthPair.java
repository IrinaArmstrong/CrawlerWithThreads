package ru.mail.airenea;

/**
 * Created by a_iri on 10.07.2018.
 */
import java.net.MalformedURLException;
import java.net.URL;

// Класс, содержащий пару значений [URL, глубина].
public class URLDepthPair {


    // Строка, содержащая URL страницы с которой начинается поиск
    private String url;

    // Положительное целое число – максимальная глубина поиска
    private int depth;

    //Конструктор, принимает пару значений [URL, глубина]
    public 	URLDepthPair(String newUrl, int newDepth) {
        url = newUrl;
        depth = newDepth;
    }

    // Метод, который печатает пару значений [URL, глубина]на экране
    public String toString() {
        String depthStr = Integer.toString(depth);
        return "[ " + url + ", " + depthStr + " ]";
    }

    // Метод, возвращающий URL
    public String getURL() {
        return url;
    }

    // Метод, возвращающий глубину
    public int getDepth() {
        return depth;
    }

    // Метод, возвращающий имя хоста из URL. Может вызвать и обработать MalformedURLException
    public String getHost() {
        try {
            URL tempURL = new URL(url);
            return tempURL.getHost();
        }
        catch (MalformedURLException e) {
            //System.out.println("MalformedURLException in hostname: " + e.getMessage());
            return null;
        }
    }

    // Метод, возвращающий путь к веб странице на сервере. Может вызвать и обработать MalformedURLException
    public String getPath() {
        try {
            URL tempURL = new URL(url);
            return tempURL.getPath();
        }
        catch (MalformedURLException e) {
            //System.out.println("MalformedURLException in path: " + e.getMessage());
            return null;
        }
    }
}


