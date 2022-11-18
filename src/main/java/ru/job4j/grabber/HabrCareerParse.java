package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.model.Post;
import ru.job4j.grabber.util.DateTimeParser;
import ru.job4j.grabber.util.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) {
        HabrCareerParse habrCareerParse = new HabrCareerParse(new HabrCareerDateTimeParser());
        for (int i = 1; i <= 5; i++) {
            String link = PAGE_LINK + i;
            List<Post> posts = habrCareerParse.list(link);
        }
    }

    @Override
    public List<Post> list(String link) {
        Connection connection = Jsoup.connect(link);
        List<Post> result = new ArrayList<>();
        try {
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                String title = titleElement.text();
                String jobLink = String.format("%s%s", SOURCE_LINK, titleElement.child(0).attr("href"));
                String description = retrieveDescription(jobLink);
                LocalDateTime date = dateTimeParser.parse(
                        row.select(".vacancy-card__date time").first().attr("datetime")
                );
                result.add(new Post(title, jobLink, description, date));
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private String retrieveDescription(String link) {
        Connection connection = Jsoup.connect(link);
        StringBuilder result = new StringBuilder();
        try {
            Document document = connection.get();
            Elements descSections = document.select("div.style-ugc *");
            descSections.forEach(row -> {
                if (!row.text().equals("")) {
                    result.append(row.text()).append('\n');
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result.toString();
    }
}