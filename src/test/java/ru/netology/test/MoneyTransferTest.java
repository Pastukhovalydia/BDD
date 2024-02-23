package ru.netology.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.netology.page.CardOperations;
import ru.netology.page.LoginPage;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.*;
import static ru.netology.data.DataHelper.*;

public class MoneyTransferTest {
    CardOperations cardOperations;
    CardInfo firstCardInfo;
    CardInfo secondCardInfo;
    int firstCardBalance;
    int secondCardBalance;

    @BeforeEach
    void setup() {
        // Открываем страницу логина
        var loginPage = open("http://localhost:9999", LoginPage.class);
        // Получаем данные для авторизации
        var authInfo = getAuthInfo();
        // Авторизуемся
        var verificationPage = loginPage.validLogin(authInfo);
        // Получаем код верификации
        var verificationCode = getVerificationCode();
        // Подтверждаем вход кодом верификации
        var dashboardPage = verificationPage.validVerify(verificationCode);

        // Инициализируем объект для выполнения операций с картами
        cardOperations = new CardOperations();
        // Получаем информацию о картах
        firstCardInfo = getFirstCardInfo();
        secondCardInfo = getSecondCardInfo();
        // Получаем балансы карт
        firstCardBalance = cardOperations.getCardBalance(firstCardInfo);
        secondCardBalance = cardOperations.getCardBalance(secondCardInfo);
    }

    @Test
    void transferBetweenCardsShouldUpdateBalances() {
        // Переводим случайную сумму с первой карты на вторую
        var amount = generateInvalidAmount(firstCardBalance);
        var expectedBalanceFirstCard = firstCardBalance + amount;
        var expectedBalanceSecondCard = secondCardBalance - amount;
        cardOperations.transferBetweenCards(String.valueOf(amount), firstCardInfo, secondCardInfo);
        // Проверяем, что балансы обновились
        var actualBalanceFirstCard = cardOperations.getCardBalance(firstCardInfo);
        var actualBalanceSecondCard = cardOperations.getCardBalance(secondCardInfo);
        assertEquals(expectedBalanceFirstCard, actualBalanceFirstCard);
        assertEquals(expectedBalanceSecondCard, actualBalanceSecondCard);
    }

    @Test
    void transferWithExceedingAmountShouldShowErrorMessage() throws InterruptedException {
        // Переводим случайную сумму, превышающую баланс карты
        var amount = generateInvalidAmount(secondCardBalance);
        cardOperations.transferBetweenCards(String.valueOf(amount), secondCardInfo, firstCardInfo);
        // Проверяем, что балансы не изменились
        var actualBalanceFirstCard = cardOperations.getCardBalance(firstCardInfo);
        var actualBalanceSecondCard = cardOperations.getCardBalance(secondCardInfo);
        assertEquals(firstCardBalance, actualBalanceFirstCard);
        assertEquals(secondCardBalance, actualBalanceSecondCard);
    }

    @Test
    void transferZeroAmountBetweenCardsShouldNotChangeBalances() throws InterruptedException {
        // Переводим 0 RUB с первой карты на вторую
        var amount = 0;
        cardOperations.transferBetweenCards(String.valueOf(amount), firstCardInfo, secondCardInfo);
        // Проверяем, что балансы не изменились
        var actualBalanceFirstCard = cardOperations.getCardBalance(firstCardInfo);
        var actualBalanceSecondCard = cardOperations.getCardBalance(secondCardInfo);
        assertEquals(firstCardBalance, actualBalanceFirstCard);
        assertEquals(secondCardBalance, actualBalanceSecondCard);
    }

}