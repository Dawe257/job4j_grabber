package ru.job4j.grabber;

import ru.job4j.grabber.model.Post;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {

    private Connection cnn;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
            cnn = DriverManager.getConnection(cfg.getProperty("jdbc.url"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void save(Post post) {
        String sql = "insert into posts(title, link, description, created) values (?, ?, ?, ?)";
        try (PreparedStatement statement = cnn.prepareStatement(sql)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getLink());
            statement.setString(3, post.getDescription());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> result = new ArrayList<>();
        String sql = "select * from posts";
        try (Statement statement = cnn.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                int id = resultSet.getInt(1);
                String title = resultSet.getString(2);
                String link = resultSet.getString(3);
                String description = resultSet.getString(4);
                LocalDateTime created = resultSet.getTimestamp(5).toLocalDateTime();
                result.add(new Post(id, title, link, description, created));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public Post findById(int id) {
        Post result = null;
        String sql = "select * from posts where id = ?";
        try (PreparedStatement statement = cnn.prepareStatement(sql)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int postId = resultSet.getInt(1);
                String title = resultSet.getString(2);
                String link = resultSet.getString(3);
                String description = resultSet.getString(4);
                LocalDateTime created = resultSet.getTimestamp(5).toLocalDateTime();
                result = new Post(postId, title, link, description, created);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }
}