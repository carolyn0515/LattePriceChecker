import java.awt.*;
import javax.swing.*;

public class PriceChartPanel extends JPanel {
    private double base, now, adjusted;

    public PriceChartPanel(double base, double now, double adjusted) {
        this.base = base;
        this.now = now;
        this.adjusted = adjusted;
        setPreferredSize(new Dimension(400, 220));
        setBackground(new Color(255, 251, 239));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int width = getWidth();
        int height = getHeight();
        int barWidth = 60;
        int gap = 40;
        int originX = 40;
        int originY = height - 60;

        double max = Math.max(Math.max(base, now), adjusted);
        double scale = max > 0 ? 120.0 / max : 1;

        int hBase = (int) (base * scale);
        int hAdjusted = (int) (adjusted * scale);
        int hNow = (int) (now * scale);

        Font labelFont = new Font("Serif", Font.PLAIN, 14);
        g2.setFont(labelFont);

        // Draw baseline
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawLine(originX, originY, originX + 3 * (barWidth + gap), originY);

        // 기준가
        g2.setColor(new Color(160, 160, 160));
        g2.fillRoundRect(originX, originY - hBase, barWidth, hBase, 10, 10);
        g2.setColor(Color.BLACK);
        g2.drawString("기준가", originX + 5, originY + 20);

        // 적정가
        g2.setColor(new Color(100, 150, 255));
        g2.fillRoundRect(originX + (barWidth + gap), originY - hAdjusted, barWidth, hAdjusted, 10, 10);
        g2.setColor(Color.BLACK);
        g2.drawString("적정가", originX + (barWidth + gap) + 5, originY + 20);

        // 현재가
        if (now > adjusted * 1.05) {
            g2.setColor(new Color(255, 153, 179)); 
        } else if (now < adjusted * 0.95) {
            g2.setColor(new Color(153, 255, 230)); 
        } else {
            g2.setColor(new Color(255, 230, 153)); 
        }
        g2.fillRoundRect(originX + 2 * (barWidth + gap), originY - hNow, barWidth, hNow, 10, 10);
        g2.setColor(Color.BLACK);
        g2.drawString("현재가", originX + 2 * (barWidth + gap) + 5, originY + 20);

        // 수치
        g2.setFont(new Font("Serif", Font.BOLD, 12));
        g2.drawString(String.format("%.0f원", base), originX + 5, originY - hBase - 5);
        g2.drawString(String.format("%.0f원", adjusted), originX + (barWidth + gap) + 5, originY - hAdjusted - 5);
        g2.drawString(String.format("%.0f원", now), originX + 2 * (barWidth + gap) + 5, originY - hNow - 5);
    }
}
