package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.model.Post;
import ru.job4j.grabber.util.DateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final int PAGES_COUNTER = 5;
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> list(String link) {
        List<Post> result = new ArrayList<>();
        for (int i = 1; i <= PAGES_COUNTER; i++) {
            Connection connection = Jsoup.connect(link + i);
            try {
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> result.add(parsePost(row)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    private Post parsePost(Element row) {
        Element titleElement = row.select(".vacancy-card__title").first();
        String title = titleElement.text();
        String jobLink = String.format("%s%s", SOURCE_LINK, titleElement.child(0).attr("href"));
        String description = retrieveDescription(jobLink);
        LocalDateTime date = dateTimeParser.parse(
                row.select(".vacancy-card__date time").first().attr("datetime")
        );
        return new Post(title, jobLink, description, date);
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