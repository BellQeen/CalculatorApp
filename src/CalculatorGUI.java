import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CalculatorGUI {
    private JFrame frame;
    private JTextField textField;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new CalculatorGUI();
        });
    }

    public CalculatorGUI() {
        frame = new JFrame("Калькулятор");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 400);
        frame.setLayout(new BorderLayout());

        textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 24));
        frame.add(textField, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(4, 4, 10, 10));

        String[] buttonLabels = {
                "7", "8", "9", "/",
                "4", "5", "6", "*",
                "1", "2", "3", "-",
                "0", ".", "=", "+"
        };

        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.setFont(new Font("Arial", Font.PLAIN, 18));
            button.addActionListener(new ButtonClickListener());
            buttonPanel.add(button);
        }

        frame.add(buttonPanel, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    private class ButtonClickListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            JButton source = (JButton) event.getSource();
            String command = source.getText();

            switch (command) {
                case "=":
                    calculateResult();
                    break;
                case "C":
                    clear();
                    break;
                default:
                    updateTextField(command);
                    break;
            }
        }
    }

    private void calculateResult() {
        try {
            String expression = textField.getText();
            double result = evaluateExpression(expression);
            textField.setText(String.valueOf(result));
        } catch (NumberFormatException | ArithmeticException e) {
            textField.setText("Ошибка");
        }
    }

    private void updateTextField(String text) {
        String currentText = textField.getText();
        textField.setText(currentText + text);
    }

    private void clear() {
        textField.setText("");
    }

    private double evaluateExpression(String expression) {
        try {
            return new Object() {
                int pos = -1, ch;

                void nextChar() {
                    ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
                }

                boolean eat(int charToEat) {
                    while (Character.isWhitespace(ch)) nextChar();
                    if (ch == charToEat) {
                        nextChar();
                        return true;
                    }
                    return false;
                }

                double parse() {
                    nextChar();
                    double x = parseExpression();
                    if (pos < expression.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                    return x;
                }

                double parseExpression() {
                    double x = parseTerm();
                    for (;;) {
                        if (eat('+')) x += parseTerm(); // addition
                        else if (eat('-')) x -= parseTerm(); // subtraction
                        else return x;
                    }
                }

                double parseTerm() {
                    double x = parseFactor();
                    for (;;) {
                        if (eat('*')) x *= parseFactor(); // multiplication
                        else if (eat('/')) x /= parseFactor(); // division
                        else return x;
                    }
                }

                double parseFactor() {
                    if (eat('+')) return parseFactor(); // unary plus
                    if (eat('-')) return -parseFactor(); // unary minus

                    double x;
                    int startPos = this.pos;
                    if (eat('(')) { // parentheses
                        x = parseExpression();
                        eat(')');
                    } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                        while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                        x = Double.parseDouble(expression.substring(startPos, this.pos));
                    } else {
                        throw new RuntimeException("Unexpected: " + (char)ch);
                    }

                    return x;
                }
            }.parse();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка вычисления: " + e.getMessage());
        }
    }
}

