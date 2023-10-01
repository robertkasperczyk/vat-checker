package rk.vatchecker.vies;

public record VatNumber(String countryCode, String number) {

    public VatNumber(String vatNumber) {
        this(vatNumber.substring(0, 2), vatNumber.substring(2));
    }

    @Override
    public String toString() {
        return countryCode + number;
    }
}
