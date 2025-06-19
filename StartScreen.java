import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class StartScreen extends JFrame {
    public StartScreen() {
        setTitle("라떼물가 변환기");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(255, 251, 239)); // 오래된 문서 느낌

        // 제목 라벨
        JLabel titleLabel = new JLabel("라떼는 말이야…", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 50));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        titleLabel.setForeground(new Color(80, 40, 20));

        // 설명 라벨
        JTextArea descLabel = new JTextArea(
                "📌 당신이 기억하는 그 시절 가격,\n"
              + "지금도 비싼 걸까요?\n\n"
              + "라떼물가 앱은 도시, 품목, 연도, 가격을 입력하면\n"
              + "현재 물가 기준으로 얼마나 차이가 나는지 계산해줍니다!\n"
              + "소비자물가지수를 기반으로 분석해드릴게요 💸\n");
        descLabel.setFont(new Font("Serif", Font.PLAIN, 20));
        descLabel.setEditable(false);
        descLabel.setOpaque(false);
        descLabel.setForeground(new Color(60, 30, 10));
        descLabel.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 40));
        descLabel.setLineWrap(true);
        descLabel.setWrapStyleWord(true);



        // 시작 버튼
        JButton startButton = new JButton("시작하기 🐴");
        startButton.setFont(new Font("Serif", Font.BOLD, 18));
        startButton.setBackground(new Color(230, 200, 150));
        startButton.setFocusPainted(false);
        startButton.setForeground(Color.DARK_GRAY);
        startButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // ✅ 여기서 setVisible(true) 추가!
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(() -> {
                    LatteCheckerUI ui = new LatteCheckerUI();
                    ui.setVisible(true);  // 💡 새 창이 보여야 함!
                });
                dispose(); // 시작 화면 닫기
            }
        });

        // 중앙 패널
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(descLabel, BorderLayout.CENTER);

        // 전체 구성
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(startButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StartScreen());
    }
}
