package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);

    public static void main(String[] args) throws IOException {
        for (int i = 1; i <= 5; i++) {
            String pageLink = PAGE_LINK + i;
            Connection connection = Jsoup.connect(pageLink);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                Element dateElement = row.select(".vacancy-card__date time").first();
                String vacancyName = titleElement.text();
                String date = dateElement.attr("datetime");
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                System.out.printf("%s %s %s%n", date, vacancyName, link);
            });
        }
    }

    private static String retrieveDescription(String link) {
        Connection connection = Jsoup.connect(link);
        StringBuilder result = new StringBuilder();
        try {
            Document document = connection.get();
            Elements rows = document.select(".style-ugc *");
            rows.forEach(row -> result.append(row.ownText()).append('\n'));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result.toString();
    }
}