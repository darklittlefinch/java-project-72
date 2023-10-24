package hexlet.code.controller;

import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import hexlet.code.util.NormalizedData;
import hexlet.code.util.Time;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.sql.SQLException;

public final class UrlCheckController {
    public static void create(Context ctx) throws SQLException {
        var urlId = ctx.pathParamAsClass("id", Long.class).get();
        var url = UrlRepository.find(urlId)
                .orElseThrow(() -> new NotFoundResponse("Entity with id = " + urlId + " not found"));

        try {
            HttpResponse<String> response = Unirest.get(url.getName()).asString();
            Document doc = Jsoup.parse(response.getBody());

            var statusCode = response.getStatus();
            var title = doc.title();
            var h1 = NormalizedData.getNormalizedHtmlItem(doc, "h1");
            var description = NormalizedData.getNormalizedHtmlItem(doc, "meta[name=description]");
            var createdAt = Time.getCurrentTime();

            var urlCheck = new UrlCheck(statusCode, title, h1, description, urlId, createdAt);
            UrlCheckRepository.save(urlCheck);

            ctx.sessionAttribute("flash", "Page checked successfully");
            ctx.sessionAttribute("flash-type", "success");
        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Incorrect URL");
            ctx.sessionAttribute("flash-type", "danger");
        }

        ctx.redirect(NamedRoutes.urlPath(urlId));
    }
}
