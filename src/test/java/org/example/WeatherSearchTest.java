package org.example;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class WeatherSearchTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private String location = "Miami,Florida";
    private String apiKey = "ca888afc447e8897364d3bd6ee738b92";
    private HttpClient httpClient = HttpClient.newHttpClient();
    private JsonParser parser = new JsonParser();

    @Before
    public void setUp() {
        // Set ChromeDriver path
        System.setProperty("webdriver.chrome.driver", "src/resources/chromedriver.exe");
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, 10);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(options);
    }

    @Test
    public void testWeatherSearch() throws URISyntaxException {
        //1. Open Google homepage using Selenium
        //2. Enter the search "Weather in a selected location" in the search bar
        //3. Submit the search and wait for the results page to load
        //4. Extract the Temperature in the selected location from the results and store the value in an object
        driver.get("https://www.google.com/");
        WebElement searchBox = driver.findElement(By.name("q"));
        searchBox.sendKeys("Weather in " + location);
        searchBox.submit();
        //wait.until(ExpectedConditions.titleContains(location));
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        String searchResultTemp = driver.findElement(By.cssSelector(".wob_t")).getText();
        driver.quit();

        //5. Make a call to the OpenWeatherMap API to retrieve weather data for the same specific location
        // and deserialize the result into an object
        double apiResultTemp;
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.openweathermap.org/data/2.5/weather?q=" + location + "&appid=" + apiKey))
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonElement jsonElement = parser.parse(response.body());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonObject main = jsonObject.getAsJsonObject("main");
            apiResultTemp = main.get("temp").getAsDouble();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //6. Print the temperature difference between the results in 4 and 5
        double apiCelsius = apiResultTemp - 273;
        double searchResultCelsius = Double.parseDouble(searchResultTemp);
        System.out.println("Step 4 t is: " + searchResultCelsius + " Step 5 result is: " + apiCelsius);
        double difference = searchResultCelsius - apiCelsius;
        System.out.println("Difference between step 4 and 5: " + difference) ;
    }
}
