
import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class NumberGuessingGame extends JFrame {

    private final Random random = new Random();

    private JComboBox<String> difficultyBox;
    private JTextField guessField;
    private JButton guessButton;
    private JButton playAgainButton;
    private JLabel messageLabel;
    private JLabel attemptsLabel;
    private JLabel roundLabel;
    private JLabel scoreLabel;
    private JTextArea historyArea;

    private int secretNumber;
    private int maxAttempts;
    private int attempts;
    private int range;
    private int round = 0;
    private int wins = 0;
    private int totalAttempts = 0;
    private boolean gameOver = false;

    public NumberGuessingGame() {
        setTitle("Number Guessing Game");
        setSize(620, 590);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(240, 248, 247));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel titleLabel = new JLabel(
                "NUMBER GUESSING GAME", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 25));
        titleLabel.setForeground(new Color(20, 105, 95));

        JLabel subtitleLabel = new JLabel(
                "Guess the secret number to win!",
                SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JPanel header = new JPanel(new GridLayout(2, 1, 5, 5));
        header.setOpaque(false);
        header.add(titleLabel);
        header.add(subtitleLabel);

        mainPanel.add(header, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 230, 225)),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)));

        JPanel difficultyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        difficultyPanel.setOpaque(false);
        difficultyPanel.add(new JLabel("Difficulty:"));

        difficultyBox = new JComboBox<>(new String[]{
                "Easy (1-50)",
                "Medium (1-100)",
                "Hard (1-200)"
        });
        difficultyBox.setFont(new Font("Arial", Font.PLAIN, 14));
        difficultyPanel.add(difficultyBox);

        roundLabel = new JLabel("Round: 0", SwingConstants.CENTER);
        roundLabel.setFont(new Font("Arial", Font.BOLD, 15));
        roundLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        messageLabel = new JLabel(
                "Choose a difficulty and start guessing!",
                SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 15));
        messageLabel.setForeground(new Color(20, 105, 95));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        guessField = new JTextField();
        guessField.setFont(new Font("Arial", Font.PLAIN, 18));
        guessField.setHorizontalAlignment(JTextField.CENTER);
        guessField.setMaximumSize(new Dimension(220, 40));
        guessField.setAlignmentX(Component.CENTER_ALIGNMENT);

        guessButton = new JButton("Guess");
        styleButton(guessButton, new Color(20, 125, 110));

        playAgainButton = new JButton("Play Again");
        styleButton(playAgainButton, new Color(50, 105, 175));
        playAgainButton.setEnabled(false);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 5));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(guessButton);
        buttonsPanel.add(playAgainButton);

        attemptsLabel = new JLabel("Attempts: 0 / 7", SwingConstants.CENTER);
        attemptsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        scoreLabel = new JLabel("Rounds won: 0 | Total guesses: 0",
                SwingConstants.CENTER);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(difficultyPanel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(roundLabel);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(messageLabel);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(guessField);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(buttonsPanel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(attemptsLabel);
        centerPanel.add(Box.createVerticalStrut(8));
        centerPanel.add(scoreLabel);

        JPanel historyPanel = new JPanel(new BorderLayout(5, 5));
        historyPanel.setOpaque(false);
        historyPanel.setBorder(
                BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JLabel historyTitle = new JLabel("Round History");
        historyTitle.setFont(new Font("Arial", Font.BOLD, 15));

        historyArea = new JTextArea(5, 30);
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        historyArea.setLineWrap(true);
        historyArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(historyArea);

        historyPanel.add(historyTitle, BorderLayout.NORTH);
        historyPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setOpaque(false);
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        contentPanel.add(historyPanel, BorderLayout.SOUTH);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);

        guessButton.addActionListener(e -> checkGuess());

        guessField.addActionListener(e -> checkGuess());

        playAgainButton.addActionListener(e -> startRound());

        difficultyBox.addActionListener(e -> {
            if (!gameOver && round > 0) {
                messageLabel.setText(
                        "Difficulty changed. Start a new round to apply it.");
            }
        });

        startRound();
    }

    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(125, 38));
    }

    private void startRound() {
        String difficulty = (String) difficultyBox.getSelectedItem();

        if (difficulty.startsWith("Easy")) {
            range = 50;
            maxAttempts = 10;
        } else if (difficulty.startsWith("Hard")) {
            range = 200;
            maxAttempts = 5;
        } else {
            range = 100;
            maxAttempts = 7;
        }

        secretNumber = random.nextInt(range) + 1;
        attempts = 0;
        gameOver = false;
        round++;

        roundLabel.setText("Round: " + round);
        messageLabel.setText("Guess a number between 1 and " + range);
        messageLabel.setForeground(new Color(20, 105, 95));

        attemptsLabel.setText("Attempts: 0 / " + maxAttempts);
        guessField.setText("");
        guessField.setEnabled(true);
        guessButton.setEnabled(true);
        playAgainButton.setEnabled(false);
        difficultyBox.setEnabled(false);

        guessField.requestFocusInWindow();
    }

    private void checkGuess() {
        if (gameOver) {
            return;
        }

        String input = guessField.getText().trim();

        if (input.isEmpty()) {
            messageLabel.setText("Please enter a number!");
            return;
        }

        int guess;

        try {
            guess = Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            messageLabel.setText("Enter a valid whole number!");
            guessField.selectAll();
            return;
        }

        if (guess < 1 || guess > range) {
            messageLabel.setText("Enter a number from 1 to " + range);
            guessField.selectAll();
            return;
        }

        attempts++;
        totalAttempts++;

        if (guess > secretNumber) {
            messageLabel.setText("Too High! Try a smaller number.");
            messageLabel.setForeground(new Color(190, 90, 35));
        } else if (guess < secretNumber) {
            messageLabel.setText("Too Low! Try a bigger number.");
            messageLabel.setForeground(new Color(50, 105, 175));
        } else {
            wins++;
            finishRound(true);
            return;
        }

        attemptsLabel.setText(
                "Attempts: " + attempts + " / " + maxAttempts
                + " | Remaining: " + (maxAttempts - attempts));

        scoreLabel.setText(
                "Rounds won: " + wins + " | Total guesses: " + totalAttempts);

        guessField.setText("");
        guessField.requestFocusInWindow();

        if (attempts >= maxAttempts) {
            finishRound(false);
        }
    }

    private void finishRound(boolean won) {
        gameOver = true;
        guessField.setEnabled(false);
        guessButton.setEnabled(false);
        playAgainButton.setEnabled(true);
        difficultyBox.setEnabled(true);

        scoreLabel.setText(
                "Rounds won: " + wins + " | Total guesses: " + totalAttempts);

        if (won) {
            messageLabel.setText(
                    "Correct! You won in " + attempts + " attempts!");
            messageLabel.setForeground(new Color(20, 140, 75));

            historyArea.append("Round " + round + " - WON in "
                    + attempts + " attempts.\n");
        } else {
            messageLabel.setText(
                    "You Lost! The number was " + secretNumber);
            messageLabel.setForeground(Color.RED);

            historyArea.append("Round " + round + " - LOST. Number: "
                    + secretNumber + ", attempts: " + attempts + ".\n");
        }

        attemptsLabel.setText(
                "Attempts used: " + attempts + " / " + maxAttempts);
        historyArea.setCaretPosition(historyArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            NumberGuessingGame game = new NumberGuessingGame();
            game.setVisible(true);
        });
    }
}