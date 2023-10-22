package hexlet.code;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;
import hexlet.code.controller.RootController;
import hexlet.code.controller.UrlController;
import hexlet.code.repository.BaseRepository;

import hexlet.code.util.NamedRoutes;
import io.javalin.rendering.template.JavalinJte;
import lombok.extern.slf4j.Slf4j;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.javalin.Javalin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import java.sql.SQLException;
import java.util.stream.Collectors;

@Slf4j
public final class App {

    private static final String PORT_DEFAULT = "7070";
    private static final String JDBC_URL_DEFAULT = "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1;";
    private static final String DATABASE_FILE_NAME = "schema.sql";

    public static void main(String[] args) throws IOException, SQLException {
        Javalin app = getApp();
        var port = getPort();
        app.start(port);
    }

    public static int getPort() {
        String port = System.getenv().getOrDefault("PORT", PORT_DEFAULT);
        return Integer.valueOf(port);
    }

    public static String getJdbcUrl() {
        String jdbcUrl = System.getenv().getOrDefault("JDBC_DATABASE_URL", JDBC_URL_DEFAULT);
        return jdbcUrl;
    }

    private static TemplateEngine createTemplateEngine() {
        ClassLoader classLoader = App.class.getClassLoader();
        ResourceCodeResolver codeResolver = new ResourceCodeResolver("templates", classLoader);
        TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);
        return templateEngine;
    }

    public static Javalin getApp() throws IOException, SQLException {

        var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(getJdbcUrl());

        var dataSource = new HikariDataSource(hikariConfig);
        var url = App.class.getClassLoader().getResource(DATABASE_FILE_NAME);
        var file = new File(url.getFile());
        var sql = Files.lines(file.toPath())
                .collect(Collectors.joining("\n"));

        log.info(sql);
        try (var connection = dataSource.getConnection();
                var statement = connection.createStatement()) {
            statement.execute(sql);
        }
        BaseRepository.dataSource = dataSource;

        var app = Javalin.create(config -> {
            config.plugins.enableDevLogging();
        });

        JavalinJte.init(createTemplateEngine());

        app.get(NamedRoutes.rootPath(), RootController::index);
        app.get(NamedRoutes.urlsPath(), UrlController::index);
        app.get(NamedRoutes.urlPath("{id}"), UrlController::show);
        app.post(NamedRoutes.urlsPath(), UrlController::create);

        return app;
    }
}
