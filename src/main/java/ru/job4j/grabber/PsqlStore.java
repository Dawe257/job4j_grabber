package ru.job4j.grabber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.grabber.model.Post;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {

    private Connection cnn;
    Logger logger = LoggerFactory.getLogger(PsqlStore.class.getName());

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
            cnn = DriverManager.getConnection(
                    cfg.getProperty("jdbc.url"),
                    cfg.getProperty("jdbc.user"),
                    cfg.getProperty("jdbc.password"));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void save(Post post) {
        String sql = "insert into posts(title, link, description, created) values (?, ?, ?, ?) "
                + "on conflict (link) do nothing";
        try (PreparedStatement statement = cnn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getLink());
            statement.setString(3, post.getDescription());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                post.setId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> result = new ArrayList<>();
        String sql = "select * from posts";
        try (Statement statement = cnn.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                result.add(getPostFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
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
                result = getPostFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    private Post getPostFromResultSet(ResultSet set) throws SQLException {
        int postId = set.getInt("id");
        String title = set.getString("title");
        String link = set.getString("link");
        String description = set.getString("description");
        LocalDateTime created = set.getTimestamp("created").toLocalDateTime();
        return new Post(postId, title, link, description, created);
    }
}