import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LatteCheckerUI extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JTextField basePriceField;
    private JTextField nowPriceField;
    private JComboBox<Integer> baseYearBox;
    private JComboBox<Integer> nowYearBox;
    private JComboBox<String> categoryBox;
    private JTextArea resultArea;
    private JList<String> cityList;
    private JTextField citySearchField;
    private JPanel resultPanel;
    private JPanel insightButtonPanel;

    private static final Color PAPER_BG = new Color(255, 251, 239);
    private static final Font SERIF_FONT = new Font("Serif", Font.PLAIN, 17);

    private List<String> allCities;

    public LatteCheckerUI() {
        setTitle("Latte 물가 변환기");
        setSize(650, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        CsvReader reader = new CsvReader("inf.csv");
        allCities = new ArrayList<>(reader.getCities());

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 입력 화면
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        inputPanel.setBackground(PAPER_BG);

        JLabel title = new JLabel("🧾 Latte 물가 변환기", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 24));
        inputPanel.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setOpaque(false);

        formPanel.add(new JLabel("기준 가격 (원):", SwingConstants.RIGHT)).setFont(SERIF_FONT);
        basePriceField = new JTextField();
        basePriceField.setFont(SERIF_FONT);
        formPanel.add(basePriceField);

        formPanel.add(new JLabel("기준 연도:", SwingConstants.RIGHT)).setFont(SERIF_FONT);
        baseYearBox = new JComboBox<>();
        for (int year = 2007; year <= 2024; year++) baseYearBox.addItem(year);
        baseYearBox.setFont(SERIF_FONT);
        formPanel.add(baseYearBox);

        formPanel.add(new JLabel("비교 연도:", SwingConstants.RIGHT)).setFont(SERIF_FONT);
        nowYearBox = new JComboBox<>();
        for (int year = 2007; year <= 2024; year++) nowYearBox.addItem(year);
        nowYearBox.setSelectedItem(2024);
        nowYearBox.setFont(SERIF_FONT);
        formPanel.add(nowYearBox);

        formPanel.add(new JLabel("현재 가격 (원):", SwingConstants.RIGHT)).setFont(SERIF_FONT);
        nowPriceField = new JTextField();
        nowPriceField.setFont(SERIF_FONT);
        formPanel.add(nowPriceField);

        formPanel.add(new JLabel("도시 검색:", SwingConstants.RIGHT)).setFont(SERIF_FONT);
        citySearchField = new JTextField();
        citySearchField.setFont(SERIF_FONT);
        formPanel.add(citySearchField);

        formPanel.add(new JLabel("품목 성질:", SwingConstants.RIGHT)).setFont(SERIF_FONT);
        categoryBox = new JComboBox<>(new String[] {
            "총지수", "상품", "농축수산물", "공업제품", "전기 · 가스 · 수도",
            "서비스", "집세", "공공서비스", "개인서비스"
        });
        categoryBox.setFont(SERIF_FONT);
        formPanel.add(categoryBox);

        inputPanel.add(formPanel, BorderLayout.NORTH);

        cityList = new JList<>();
        cityList.setFont(SERIF_FONT);
        updateCityList("");
        JScrollPane cityScroll = new JScrollPane(cityList);
        cityScroll.setPreferredSize(new Dimension(250, 100));
        inputPanel.add(cityScroll, BorderLayout.CENTER);

        citySearchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String keyword = citySearchField.getText().trim();
                updateCityList(keyword);
            }
        });

        JButton checkButton = new JButton("확인하기");
        checkButton.setFont(SERIF_FONT);
        checkButton.addActionListener(e -> showResult());
        inputPanel.add(checkButton, BorderLayout.SOUTH);

        // 결과 화면
        resultPanel = new JPanel(new BorderLayout(10, 10));
        resultPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        resultPanel.setBackground(PAPER_BG);

        resultArea = new JTextArea();
        resultArea.setFont(SERIF_FONT);
        resultArea.setEditable(false);
        resultArea.setBackground(PAPER_BG);
        resultPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        JButton backButton = new JButton("↩ 돌아가기");
        backButton.setFont(SERIF_FONT);
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "input"));
        resultPanel.add(backButton, BorderLayout.SOUTH);

        mainPanel.add(inputPanel, "input");
        mainPanel.add(resultPanel, "result");

        add(mainPanel);
        cardLayout.show(mainPanel, "input");
    }

    private void updateCityList(String keyword) {
        List<String> filtered = allCities.stream()
                .filter(city -> city.contains(keyword))
                .collect(Collectors.toList());
        cityList.setListData(filtered.toArray(new String[0]));
    }

    private void showResult() {
    try {
        double base = Double.parseDouble(basePriceField.getText());
        double now = Double.parseDouble(nowPriceField.getText());
        int baseYear = (int) baseYearBox.getSelectedItem();
        int nowYear = (int) nowYearBox.getSelectedItem();
        String city = cityList.getSelectedValue();
        String category = (String) categoryBox.getSelectedItem();

        if (city == null) {
            resultArea.setText("⚠ 도시를 선택하세요.");
            cardLayout.show(mainPanel, "result");
            return;
        }

        CsvReader reader = new CsvReader("inf.csv");
        Double cpiThen = reader.getCpi(city, category, baseYear);
        Double cpiNow = reader.getCpi(city, category, nowYear);

        if (cpiThen == null || cpiNow == null) {
            resultArea.setText("❗ 데이터를 찾을 수 없습니다.");
        } else {
            double adjusted = PriceAdvisor.calculateAdjustedPrice(base, cpiThen, cpiNow);
            String judgment = PriceAdvisor.evaluate(now, adjusted);
            double diffRate = (now - adjusted) / adjusted * 100;

            resultArea.setText(String.format(
                "📌 기준가 (당시 가격): %.2f원\n" +
                "📏 적정가 (물가 보정): %.2f원\n" +
                "💸 현재가 (입력값): %.2f원\n" +
                "📈 차이율: %.2f%%\n" +
                "✅ 판단: %s",
                base, adjusted, now, diffRate, judgment
            ));

            resultPanel.removeAll();

            // 결과 텍스트 + 버튼 패널
            JPanel leftPanel = new JPanel(new BorderLayout());
            leftPanel.setBackground(PAPER_BG);
            leftPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);
            leftPanel.add(createInsightButtons(base, now, adjusted, baseYear, nowYear, city, category), BorderLayout.SOUTH);

            // 오른쪽 차트
            PriceChartPanel chart = new PriceChartPanel(base, now, adjusted);

            resultPanel.add(leftPanel, BorderLayout.CENTER);
            resultPanel.add(chart, BorderLayout.EAST);
        }

        cardLayout.show(mainPanel, "result");

    } catch (Exception ex) {
        resultArea.setText("⚠️ 숫자를 정확히 입력하세요.");
        cardLayout.show(mainPanel, "result");
    }
}

private JPanel createInsightButtons(double base, double now, double adjusted, int baseYear, int nowYear, String city, String category) {
    JPanel panel = new JPanel(new FlowLayout());
    panel.setBackground(PAPER_BG);
    panel.setBorder(new EmptyBorder(10, 5, 10, 5));

    JButton[] buttons = {
        new JButton("📦 유사 품목 추천"),
        new JButton("🌎 지역간 비교"),
        new JButton("📈 물가 추이"),
        new JButton("💰 실질 소득 변환"),
        new JButton("🕰 라떼 감성")
    };

    for (int i = 0; i < buttons.length; i++) {
        final int idx = i;
        buttons[i].setFont(SERIF_FONT);
        buttons[i].setFocusPainted(false);
        buttons[i].addActionListener(e -> showInsight(idx, base, now, adjusted, baseYear, nowYear, city, category));
        panel.add(buttons[i]);
    }

    return panel;
}

private Map<String, List<String>> getSimilarCategoryMap() {
    Map<String, List<String>> map = new HashMap<>();
    map.put("상품", Arrays.asList("상품", "공업제품", "농축수산물"));
    map.put("서비스", Arrays.asList("서비스", "개인서비스", "공공서비스"));
    map.put("전기 · 가스 · 수도", Arrays.asList("전기 · 가스 · 수도", "공공서비스"));
    map.put("집세", Arrays.asList("집세", "서비스"));

    return map;
}
private void showCpiTrendChart(String city, String category) {
    CsvReader reader = new CsvReader("inf.csv");
    Map<Integer, Double> yearToCpi = reader.getYearToCpiMap(city, category);
    
    if (yearToCpi == null || yearToCpi.isEmpty()) {
        JOptionPane.showMessageDialog(this, "❗ CPI 데이터를 불러올 수 없습니다.");
        return;
    }

    JPanel chartPanel = new JPanel() {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int width = getWidth(), height = getHeight();
            int margin = 50;
            int chartHeight = height - 2 * margin;
            int chartWidth = width - 2 * margin;
            int x = margin, y = margin;

            List<Integer> years = new ArrayList<>(yearToCpi.keySet());
            Collections.sort(years);
            double maxCpi = Collections.max(yearToCpi.values());

            int barWidth = chartWidth / years.size();
            int i = 0;
            for (int year : years) {
                double cpi = yearToCpi.get(year);
                int barHeight = (int)(cpi / maxCpi * chartHeight);
                g.setColor(new Color(173, 216, 230)); // 연한 파랑
                g.fillRect(x + i * barWidth, height - margin - barHeight, barWidth - 5, barHeight);
                g.setColor(Color.DARK_GRAY);
                g.drawString(String.valueOf(year), x + i * barWidth + 5, height - 10);
                i++;
            }
        }
    };
    chartPanel.setPreferredSize(new Dimension(500, 300));

    JOptionPane.showMessageDialog(this, chartPanel, "📈 " + city + " - " + category + " CPI 추이", JOptionPane.PLAIN_MESSAGE);
}

private void showInsight(int idx, double base, double now, double adjusted, int baseYear, int nowYear, String city, String category) {
    CsvReader reader = new CsvReader("inf.csv");

    switch (idx) {
        case 0:
    reader = new CsvReader("inf.csv");
    Set<String> allItems = reader.getItemsForCity(city);
    System.out.println("📌 " + city + "의 품목 목록: " + allItems);

    List<String> similarItems = new ArrayList<>();
    Map<String, List<String>> itemMap = getSimilarCategoryMap();
    System.out.println("🔍 현재 선택된 품목: " + category);

    for (Map.Entry<String, List<String>> entry : itemMap.entrySet()) {
        if (entry.getValue().contains(category)) {
            for (String candidate : entry.getValue()) {
                if (!candidate.equals(category)) {
                    boolean exists = allItems.contains(candidate);
                    System.out.printf("⛳ 후보: %s (존재? %b)\n", candidate, exists);
                    if (exists) similarItems.add(candidate);
                }
            }
            break;
        }
    }

    StringBuilder simSb = new StringBuilder();
    simSb.append(String.format("🔍 추천 유사 품목 (%s, %d~%d):\n", city, baseYear, nowYear));
    if (similarItems.isEmpty()) {
        simSb.append("❌ 유사 품목 데이터를 찾을 수 없습니다.");
    } else {
        for (String item : similarItems) {
            simSb.append("• ").append(item).append("\n");
        }
    }

    JOptionPane.showMessageDialog(this, simSb.toString(), "📦 유사 품목 추천", JOptionPane.INFORMATION_MESSAGE);
    break;


        case 1: {
    
    Set<String> cities = reader.getCities();
    List<String> topCities = new ArrayList<>();
    List<Double> changeRates = new ArrayList<>();

    for (String otherCity : cities) {
        if (otherCity.equals(city)) continue; // 현재 도시 제외
        Double cpiStart = reader.getCpi(otherCity, category, baseYear);
        Double cpiEnd = reader.getCpi(otherCity, category, nowYear);
        if (cpiStart == null || cpiEnd == null) continue;

        double rate = (cpiEnd - cpiStart) / cpiStart * 100;
        topCities.add(otherCity);
        changeRates.add(rate);
    }

    // 변화율 기준 상위 5개 도시 추출
    List<Integer> topIdx = new ArrayList<>();
    for (int i = 0; i < changeRates.size(); i++) topIdx.add(i);
    topIdx.sort((i, j) -> Double.compare(changeRates.get(j), changeRates.get(i)));

    StringBuilder sb = new StringBuilder("📊 " + category + " 물가 상승률 (단위: %)\n\n");
    for (int i = 0; i < Math.min(5, topIdx.size()); i++) {
        idx = topIdx.get(i);
        sb.append(String.format("%d위. %s: %.2f%%\n", i + 1, topCities.get(idx), changeRates.get(idx)));
    }

    JOptionPane.showMessageDialog(this, sb.toString(), "🌎 지역 간 비교", JOptionPane.INFORMATION_MESSAGE);
    break;
}

        case 2:
    showCpiTrendChart(city, category);
    break;
        case 3:
    try {
        String input = JOptionPane.showInputDialog(this,
            String.format("💰 %d년의 월급(소득)을 입력하세요 (원):", baseYear));
        if (input == null || input.trim().isEmpty()) break;

        double income = Double.parseDouble(input.replaceAll(",", "").trim());

        reader = new CsvReader("inf.csv");
        Double cpiThen = reader.getCpi(city, category, baseYear);
        Double cpiNow = reader.getCpi(city, category, nowYear);

        if (cpiThen == null || cpiNow == null) {
            JOptionPane.showMessageDialog(this, "❗ CPI 데이터를 찾을 수 없습니다.");
            break;
        }

        adjusted = income * (cpiNow / cpiThen);

        String msg = String.format(
            "📅 %d년 기준 월급 %.0f원은\n💰 %d년 기준 약 %.0f원의 가치입니다.",
            baseYear, income, nowYear, adjusted
        );

        JOptionPane.showMessageDialog(this, msg, "💰 실질 소득 환산", JOptionPane.INFORMATION_MESSAGE);

    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "⚠️ 숫자를 정확히 입력하세요.");
    }
    break;

        case 4:
    String mood = "";

    if (baseYear < 2010) {
        mood += "📻 그때는 스마트폰보다 피처폰을 쓰던 시절이었죠.\n";
    } else if (baseYear < 2015) {
        mood += "🎧 이어폰으로 들으며 MP3를 만지던 그 시절!\n";
    } else {
        mood += "📱 카카오톡보다 싸이월드가 익숙하던 마지막 세대였죠.\n";
    }

    if (category.contains("전기") || category.contains("가스")) {
        mood += "💡 '절전 모드'와 '가스 차단기'가 필수였던 시절...\n";
    } else if (category.contains("공업제품") || category.contains("상품")) {
        mood += "🛒 동네 마트 전단지를 모으며 할인 날짜를 외우던 기억 나세요?\n";
    } else if (category.contains("서비스") || category.contains("개인")) {
        mood += "💇‍♂️ 미용실에서 1만원 컷... 정말 라떼 감성 아닐까요?\n";
    }

    mood += String.format("\n🏙 %s에서의 %d년 소비는 어떤 기억이 떠오르시나요?", city, baseYear);

    JOptionPane.showMessageDialog(this, mood, "🕰 라떼 감성", JOptionPane.INFORMATION_MESSAGE);
    break;

    }
}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LatteCheckerUI ui = new LatteCheckerUI();
            ui.setVisible(true);
        });
    }
}
