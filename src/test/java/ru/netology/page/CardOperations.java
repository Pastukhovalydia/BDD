package ru.netology.page;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import ru.netology.data.DataHelper;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class CardOperations {
    private final SelenideElement heading = $("[data-test-id=dashboard]");

    public CardOperations() {
        heading.shouldBe(visible);
    }

    public void transferBetweenCards(String amount, DataHelper.CardInfo fromCard, DataHelper.CardInfo toCard) {
        var transferPage = selectCardToTransfer(fromCard);
        transferPage.makeValidTransfer(amount, toCard);
    }

    public int getCardBalance(DataHelper.CardInfo cardInfo) {
        var cards = $$(".list__item div");
        var text = cards.findBy(Condition.text(cardInfo.getCardNumber().substring(15))).getText();
        return extractBalance(text);
    }

    private TransferPage selectCardToTransfer(DataHelper.CardInfo cardInfo) {
        var cards = $$(".list__item div");
        cards.findBy(Condition.attribute("data-test-id", cardInfo.getTestId())).$("button").click();
        return new TransferPage();
    }

    private int extractBalance(String text) {
        var balanceStart = ", баланс: ";
        var balanceFinish = " р.";
        var start = text.indexOf(balanceStart);
        var finish = text.indexOf(balanceFinish);
        var value = text.substring(start + balanceStart.length(), finish);
        return Integer.parseInt(value);
    }

    public boolean isTransferErrorMessageVisible() {
        return $(".notification_visible .notification__content").exists();
    }
}
