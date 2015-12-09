package ru.qatools.gridrouter.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public final class HttpUtils {

    private HttpUtils() {
    }

    public static <T> T executeSimpleGet(String url, Class<T> clazz) throws IOException {
        CloseableHttpResponse execute = HttpClientBuilder
                .create().build()
                .execute(new HttpGet(url));
        InputStream content = execute.getEntity().getContent();
        return new ObjectMapper().readValue(content, clazz);
    }
}
