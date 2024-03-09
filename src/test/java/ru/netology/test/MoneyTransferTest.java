package ru.netology.test;

import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeOptions;
import ru.netology.page.DashboardPage;
import ru.netology.page.LoginPage;

import java.util.HashMap;
import java.util.Map;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.netology.data.DataHelper.*;

public class MoneyTransferTest {

    DashboardPage dashboardPage;
    CardInfo firstCardInfo;
    CardInfo secondCardInfo;
    int firstCardBalance;
    int secondCardBalance;

    @BeforeEach
    void setup() {
        // Отключение проверок паролей в Chrome
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("password_manager_enabled", false);

        options.setExperimentalOption("prefs", prefs);
        Configuration.browserCapabilities = options;

        // Открываем страницу логина
        var loginPage = open("http://localhost:9999", LoginPage.class);
        // Получаем данные для авторизации
        var authInfo = getAuthInfo();
        // Авторизуемся
        var verificationPage = loginPage.validLogin(authInfo);
        // Получаем код верификации
        var verificationCode = getVerificationCode();
        // Подтверждаем вход кодом верификации
        dashboardPage = verificationPage.validVerify(verificationCode);

        // Получаем информацию о картах
        firstCardInfo = getFirstCardInfo();
        secondCardInfo = getSecondCardInfo();
        // Получаем балансы карт
        firstCardBalance = dashboardPage.getCardBalance(firstCardInfo);
        secondCardBalance = dashboardPage.getCardBalance(secondCardInfo);
    }

    @Test
    void transferBetweenCardsShouldUpdateBalances() {
        //  Переводим случайную сумму с первой карты на вторую
        var amount = generateInvalidAmount(firstCardBalance);
        var expectedBalanceFirstCard = firstCardBalance - amount;
        var expectedBalanceSecondCard = secondCardBalance + amount;
        var transferPage = dashboardPage.selectCardToTransfer(secondCardInfo);
        //  Выполняем транзакцию
        dashboardPage = transferPage.makeValidTransfer(String.valueOf(amount), firstCardInfo);
        var actualBalanceFirstCard = dashboardPage.getCardBalance(firstCardInfo);
        var actualBalanceSecondCard = dashboardPage.getCardBalance(secondCardInfo);
        assertEquals(expectedBalanceFirstCard, actualBalanceFirstCard);
        assertEquals(expectedBalanceSecondCard, actualBalanceSecondCard);
    }

    @Test
    void transferZeroAmountBetweenCardsShouldNotChangeBalances() {
        // Переводим нулевую сумму с первой карты на вторую
        var amount = 0;

        var transferPage = dashboardPage.selectCardToTransfer(secondCardInfo);
        //  Выполняем транзакцию
        dashboardPage = transferPage.makeValidTransfer(String.valueOf(amount), firstCardInfo);

        //  Проверяем, что балансы остались неизменными
        var actualBalanceFirstCard = dashboardPage.getCardBalance(firstCardInfo);
        var actualBalanceSecondCard = dashboardPage.getCardBalance(secondCardInfo);

        assertEquals(firstCardBalance, actualBalanceFirstCard);
        assertEquals(secondCardBalance, actualBalanceSecondCard);
    }

    @Test
    void transferShouldGetErrorMessageIfAmountMoreBalance() {
        // Устанавливаем сумму, превышающую баланс второй карты
        var amount = generateInvalidAmount(secondCardBalance);
        var transferPage = dashboardPage.selectCardToTransfer(firstCardInfo);
        //  Пытаемся выполнить транзакцию с избыточной суммой
        transferPage.makeTransfer(String.valueOf(amount), secondCardInfo);
        //  Проверяем, что выводится ожидаемое сообщение об ошибке
        transferPage.findErrorMessage("Выполнена попытка перевода суммы, превышающей остаток на карте списания");
        //  Проверяем, что балансы не изменились
        var actualBalanceFirstCard = dashboardPage.getCardBalance(firstCardInfo);
        var actualBalanceSecondCard = dashboardPage.getCardBalance(secondCardInfo);
        assertEquals(firstCardBalance, actualBalanceFirstCard);
        assertEquals(secondCardBalance, actualBalanceSecondCard);
    }
}
