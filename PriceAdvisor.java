public class PriceAdvisor {
    public static double calculateAdjustedPrice(double basePrice, double baseCpi, double nowCpi) {
        return basePrice * (nowCpi / baseCpi);
    }

    public static String evaluate(double actual, double expected) {
        double diff = (actual - expected) / expected * 100;
        if (diff < -5) return "👍 예전보다 저렴합니다!";
        else if (diff <= 5) return "😐 물가 상승 수준입니다.";
        else return "👎 실질적으로 비쌉니다!";
    }
}
