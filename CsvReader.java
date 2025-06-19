import java.io.*;
import java.util.*;

public class CsvReader {
    private Map<String, Map<Integer, Double>> cpiData = new HashMap<>();
    private Set<String> cities = new TreeSet<>();
    private Set<Integer> allYears = new TreeSet<>();

    public CsvReader(String filePath) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "MS949"))) {
            // ─ 헤더 처리 ─
            String header = br.readLine().replace("\uFEFF", ""); // BOM 제거
            String[] columns = header.split(",");

            List<Integer> years = new ArrayList<>();
            for (int i = 2; i < columns.length; i++) {
                try {
                    int y = Integer.parseInt(columns[i].trim());
                    years.add(y);
                    allYears.add(y);
                } catch (Exception e) {
                    // skip
                }
            }

            // ─ 본문 파싱 ─
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",", -1); // 공백 유지

                if (tokens.length < 3) continue;

                String city = tokens[0].trim().replaceAll("\"", "");
                String item = tokens[1].trim().replaceAll("\"", "");
                String key = city + "_" + item;


                cities.add(city); // 도시 등록

                Map<Integer, Double> yearMap = new HashMap<>();
                for (int i = 2; i < tokens.length; i++) {
                    String value = tokens[i].trim();
                    if (value.isEmpty() || value.equals("-")) continue;
                    try {
                        double cpi = Double.parseDouble(value);
                        yearMap.put(years.get(i - 2), cpi);
                    } catch (Exception e) {
                        // skip
                    }
                }

                if (!yearMap.isEmpty()) {
                    cpiData.put(key, yearMap);
                }
            }

            System.out.println("✅ CSV 로딩 완료!");
            System.out.println("도시 수: " + cities.size());
            System.out.println("데이터 키 예시:");
            cpiData.keySet().stream().limit(5).forEach(System.out::println);

        } catch (Exception e) {
            System.err.println("CSV 읽기 실패: " + e.getMessage());
        }
    }

    /** 특정 도시+품목+연도에 대한 CPI 값 반환 (가까운 연도 자동 대체) */
    public Double getCpi(String city, String item, int year) {
        String key = city.trim() + "_" + item.trim();
        Map<Integer, Double> yearMap = cpiData.get(key);
        if (yearMap == null || yearMap.isEmpty()) {
            System.err.println("❌ 존재하지 않는 키: " + key);
            return null;
        }

        // 정확히 해당 연도 데이터가 있을 경우
        if (yearMap.containsKey(year)) return yearMap.get(year);

        // 가장 가까운 연도 찾기
        int closestYear = -1;
        int minDiff = Integer.MAX_VALUE;

        for (Integer y : yearMap.keySet()) {
            int diff = Math.abs(y - year);
            if (diff < minDiff) {
                minDiff = diff;
                closestYear = y;
            }
        }

        if (closestYear != -1) {
            System.out.printf("⚠️ %s_%s의 %d년 데이터가 없어 %d년 데이터를 대신 사용합니다.%n",
                            city, item, year, closestYear);
            return yearMap.get(closestYear);
        }

        return null; // 정말 아무것도 없을 경우
    }
    
    public Map<Integer, Double> getYearToCpiMap(String city, String item) {
    String key = city + "_" + item;
    return cpiData.getOrDefault(key, new HashMap<>());
}


    /** 도시 목록 반환 (자동완성용) */
    public Set<String> getCities() {
        return cities;
    }

    /** 연도 목록 반환 */
    public Set<Integer> getAllYears() {
        return allYears;
    }

    /** 특정 도시의 모든 품목 종류 반환 */
    public Set<String> getItemsForCity(String city) {
        Set<String> items = new TreeSet<>();
        for (String key : cpiData.keySet()) {
            if (key.startsWith(city.trim() + "_")) {
                String item = key.substring((city.trim() + "_").length());
                items.add(item);
            }
        }
        return items;
    }
}
