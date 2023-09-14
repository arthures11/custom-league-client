package com.hawolt.ui.chat.window;

import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.util.paint.PaintHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created: 09/08/2023 10:38
 * Author: Twitter @hawolt
 **/

public class ChatMessage extends ChildUIComponent {
    private final ChatPerspective perspective;
    private final Font font = new Font(Font.DIALOG, Font.PLAIN, 16);
    private String[] computed;
    private String message;
    private Color color;

    public ChatMessage(ChatPerspective perspective) {
        super(null);
        this.setBackground(ColorPalette.popupWindowColor);
        this.color = perspective == ChatPerspective.SELF ? ColorPalette.messageOut : ColorPalette.messageIn;
        this.perspective = perspective;
        ChildUIComponent test = new ChildUIComponent(null);
        add(test, BorderLayout.CENTER);
    }

    public ChatPerspective getPerspective() {
        return perspective;
    }

    public void append(String text) {
        if (this.message == null) {
            this.message = text;
        } else {
            this.message = this.message + text;
        }
        this.computed = compute();
        Graphics2D graphics2D = PaintHelper.getGraphics2D();
        graphics2D.setFont(font);
        FontMetrics metrics = graphics2D.getFontMetrics();

        int endY = 3 + (metrics.getAscent() + ((computed.length - 1) * (metrics.getAscent() + 5)));

        this.setPreferredSize(
                new Dimension(
                        getPreferredSize().width,
                        endY + 9
                )
        );
        this.setSize(getPreferredSize());
        this.revalidate();
        this.repaint();
    }

    private String[] compute() {
        List<String> history = new ArrayList<>();
        int width = getPreferredSize().width - 10;

        // don't compute if window isn't open
        if (width > 0) {
            Graphics2D graphics2D = PaintHelper.getGraphics2D();
            graphics2D.setFont(font);
            FontMetrics metrics = graphics2D.getFontMetrics();
            String[] messages = this.message.split("\n");
            for (String message : messages) {
                history.addAll(formatMessage(message, width, metrics));
            }
        }
        return history.toArray(String[]::new);
    }

    private List<String> formatMessage(String message, int width, FontMetrics metrics) {
        List<String> result = new ArrayList<>();

        message = message.trim();
        String[] words = message.split(" ");

        if (words.length > 0) {
            StringBuilder currentLine = new StringBuilder();
            String word = words[0];
            int wordWidth = metrics.stringWidth(word);
            int spaceWidth = metrics.charWidth(' ');

            if (wordWidth > width) {
                List<String> subwords = formatLongWord(word, width, metrics);
                ListIterator<String> iterator = subwords.listIterator();

                String subword = iterator.next();
                while (iterator.hasNext()) {
                    result.add(subword);
                    subword = iterator.next();
                }
                currentLine.append(subword);
            } else {
                currentLine.append(word);
            }
            for (int i = 1; i < words.length; i++) {
                word = words[i];
                wordWidth = metrics.stringWidth(word);
                int currentLineWidth = metrics.stringWidth(currentLine.toString());

                if (currentLineWidth + wordWidth + spaceWidth <= width) {
                    currentLine.append(' ').append(word);
                } else if (wordWidth > width) {
                    if (!currentLine.isEmpty()) {
                        result.add(currentLine.toString());
                        currentLine.setLength(0);
                    }

                    List<String> subwords = formatLongWord(word, width, metrics);
                    ListIterator<String> iterator = subwords.listIterator();

                    String subword = iterator.next();
                    while (iterator.hasNext()) {
                        result.add(subword);
                        subword = iterator.next();
                    }
                    currentLine.append(subword);
                } else {
                    result.add(currentLine.toString());
                    currentLine.setLength(0);

                    currentLine.append(word);
                }
            }
            result.add(currentLine.toString());
        }

        return result;
    }

    private List<String> formatLongWord(String word, int width, FontMetrics metrics) {
        List<String> result = new ArrayList<>();

        int currentWidth = metrics.charWidth(word.codePointAt(0));
        int lastSplit = 0;

        for (int i = 1; i < word.length(); i++) {
            int c = word.codePointAt(i);
            currentWidth += metrics.charWidth(c);

            if (currentWidth > width) {
                result.add(word.substring(lastSplit, i));

                currentWidth = metrics.charWidth(c);
                lastSplit = i;
            }
        }
        result.add(word.substring(lastSplit));

        return result;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (computed == null) return;
        Dimension dimension = getPreferredSize();
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setFont(font);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        FontMetrics metrics = graphics2D.getFontMetrics();
        int width = 0;
        for (String message : computed) {
            width = Math.max(width, metrics.stringWidth(message));
        }
        int startY = 3 + (metrics.getAscent());
        int endY = 3 + (metrics.getAscent() + ((computed.length - 1) * (metrics.getAscent() + 5)));
        graphics2D.setColor(color);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int startX = perspective == ChatPerspective.OTHER ? 5 : dimension.width - 5 - width;
        graphics2D.fillRoundRect(startX - 2, startY - metrics.getAscent() - 2, width + 8, endY + 7, 10, 10);
        graphics2D.setColor(Color.WHITE);
        for (int i = 0; i < computed.length; i++) {
            String message = computed[i];
            int x = perspective == ChatPerspective.OTHER ? 7 : dimension.width - 3 - metrics.stringWidth(message);
            int y = 3 + (metrics.getAscent() + (i * (metrics.getAscent() + 5)));
            graphics2D.drawString(message, x, y);
        }
    }
}
