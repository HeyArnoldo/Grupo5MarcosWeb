package pe.edu.utp.Paway.service;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import pe.edu.utp.Paway.dto.QuoteForm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuoteServiceTests {
    private final QuoteService service = new QuoteService();

    private QuoteForm form(String weight, int quantity, String category) {
        QuoteForm form = new QuoteForm();
        form.setProvince("lima");
        form.setDistrict("Miraflores");
        form.setWeight(new BigDecimal(weight));
        form.setQuantity(quantity);
        form.setCategory(category);
        return form;
    }

    @Test
    void includesTwoKilogramsAndChargesFractionalOverweightPerPackage() {
        assertThat(service.calculate(form("2", 1, "textil")).total()).isEqualByComparingTo("10.00");
        QuoteService.Quote quote = service.calculate(form("2.5", 3, "tecnologia"));
        assertThat(quote.overweight()).isEqualByComparingTo("1.50");
        assertThat(quote.unit()).isEqualByComparingTo("16.50");
        assertThat(quote.total()).isEqualByComparingTo("49.50");
    }

    @Test
    void preservesJewelrySurchargeAndOriginalDestinationTariff() {
        QuoteForm form = form("1", 2, "joyeria");
        form.setProvince("canete");
        form.setDistrict("Asia");
        assertThat(service.calculate(form).total()).isEqualByComparingTo("70.00");
    }

    @Test
    void rejectsDistrictFromAnotherProvinceAndUnknownCategory() {
        QuoteForm form = form("1", 1, "textil");
        form.setDistrict("Asia");
        assertThatThrownBy(() -> service.calculate(form)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.calculate(form("1", 1, "unknown")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsInvalidWeightsAndCountsEvenWhenCalledOutsideMvc() {
        for (String weight : new String[]{"0", "-1", "1001", "1.234"}) {
            assertThatThrownBy(() -> service.calculate(form(weight, 1, "textil")))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        assertThatThrownBy(() -> service.calculate(form("1", 0, "textil")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.calculate(form("1", 101, "textil")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
