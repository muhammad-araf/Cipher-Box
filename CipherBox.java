import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class Manager {
    private String[] passwords = new String[15];
    private String username;
    private String name;
    private String pin;
    private String personalQuestion;
    private String personalAnswer;
    private boolean create = false;
    public int count = 0;

    boolean authenticate(String inpUsername, String inpPin) {
        if (username == null || pin == null) {
            JOptionPane.showMessageDialog(null, "No account exists. Please create an account first.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (inpUsername.equals(username) && inpPin.equals(pin)) {
            return true;
        } else {
            JOptionPane.showMessageDialog(null, "Invalid Username or Pin", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    boolean create(String subname, String subusername, String subpin, String subpin2, String ageStr, String question, String answer) {
        if (create) {
            JOptionPane.showMessageDialog(null, "In Free Trial You Can Create Only One Account", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!subpin.equals(subpin2)) {
            JOptionPane.showMessageDialog(null, "Pin Does Not Match", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            int age = Integer.parseInt(ageStr);
            if (age < 16) {
                JOptionPane.showMessageDialog(null, "Access Denied! You must be 16 or older to use this application.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid age format. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        name = subname;
        username = subusername;
        pin = subpin;
        personalQuestion = question;
        personalAnswer = answer;
        create = true;
        JOptionPane.showMessageDialog(null, "Your account has been successfully created.", "Success", JOptionPane.INFORMATION_MESSAGE);
        return true;
    }

    void show(String subpin) {
        if (!subpin.equals(pin)) {
            JOptionPane.showMessageDialog(null, "Invalid Pin", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (count == 0) {
            JOptionPane.showMessageDialog(null, "No password has been saved.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder passwordList = new StringBuilder("Your Saved Passwords:\n");
        for (int i = 0; i < count; i++) {
            passwordList.append(i).append(". ").append(passwords[i]).append("\n");
        }
        JTextArea textArea = new JTextArea(passwordList.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Arial", Font.PLAIN, 14));
        textArea.setBackground(new Color(62, 62, 62));
        textArea.setForeground(new Color(224, 224, 224));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(300, 200));

        int choice = JOptionPane.showConfirmDialog(null, scrollPane, "Saved Passwords", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            String serialStr = JOptionPane.showInputDialog(null, "Enter the Serial number of the password you want to see:", "Decrypt Password", JOptionPane.QUESTION_MESSAGE);
            try {
                int serial = Integer.parseInt(serialStr);
                if (serial >= 0 && serial < count) {
                    String keyStr = JOptionPane.showInputDialog(null, "Enter the key for the password " + passwords[serial] + ":", "Decrypt Key", JOptionPane.QUESTION_MESSAGE);
                    int key = Integer.parseInt(keyStr);
                    StringBuilder decryptedText = new StringBuilder();
                    for (int i = 0; i < passwords[serial].length(); i++) {
                        char a = (char) (passwords[serial].charAt(i) - key);
                        decryptedText.append(a);
                    }
                    JOptionPane.showMessageDialog(null, "Decrypted Password: " + decryptedText, "Decrypted Password", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid Serial Number", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid input. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    void decryption(String subpassword, String keyStr) {
        try {
            int key = Integer.parseInt(keyStr);
            StringBuilder decryptedText = new StringBuilder();
            for (int i = 0; i < subpassword.length(); i++) {
                char a = (char) (subpassword.charAt(i) - key);
                decryptedText.append(a);
            }
            JOptionPane.showMessageDialog(null, "According to your key Decrypted Password: " + decryptedText, "Decrypted Password", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid key. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void save(String password) {
        if (count >= passwords.length) {
            JOptionPane.showMessageDialog(null, "Storage limit reached. Cannot save more passwords.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int key = (int) (Math.random() * 8) + 1;
        StringBuilder encryptedText = new StringBuilder();
        for (int i = 0; i < password.length(); i++) {
            char a = (char) (password.charAt(i) + key);
            encryptedText.append(a);
        }
        passwords[count] = encryptedText.toString();
        count++;
        JOptionPane.showMessageDialog(null, "Your password has been saved securely.\nRemember the key for decryption: " + key, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    void forget(String subAnswer, String newPin, String confirmPin) {
        if (!subAnswer.equals(personalAnswer)) {
            JOptionPane.showMessageDialog(null, "You Are Not The Owner", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!newPin.equals(confirmPin)) {
            JOptionPane.showMessageDialog(null, "Pin Does Not Match", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        pin = newPin;
        JOptionPane.showMessageDialog(null, "Your Pin Has Been Changed", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    String getPersonalQuestion() {
        return personalQuestion;
    }
}

public class CipherBox {
    private Manager manager;
    private JFrame loginFrame;
    private JFrame mainFrame;
    private static final Color BACKGROUND_COLOR = new Color(47, 47, 47); // Charcoal Gray
    private static final Color PANEL_COLOR = new Color(62, 62, 62); // Dark Slate Gray
    private static final Color TEXT_COLOR = new Color(224, 224, 224); // Light Gray
    private static final Color SECONDARY_TEXT_COLOR = new Color(176, 176, 176); // Muted Gray
    private static final Color BORDER_COLOR = new Color(90, 90, 90); // Medium Gray
    private static final Color GLOW_COLOR = new Color(70, 130, 180); // Steel Blue

    // Button Colors
    private static final Color CREATE_BUTTON_COLOR = new Color(46, 139, 87); // Sea Green
    private static final Color CREATE_BUTTON_HOVER = new Color(62, 191, 119); // Lighter Sea Green
    private static final Color SIGNIN_BUTTON_COLOR = new Color(106, 90, 205); // Slate Blue
    private static final Color SIGNIN_BUTTON_HOVER = new Color(138, 118, 255); // Lighter Slate Blue
    private static final Color EXIT_BUTTON_COLOR = new Color(205, 92, 92); // Indian Red
    private static final Color EXIT_BUTTON_HOVER = new Color(255, 120, 120); // Lighter Indian Red
    private static final Color DECRYPT_BUTTON_COLOR = new Color(218, 165, 32); // Goldenrod
    private static final Color DECRYPT_BUTTON_HOVER = new Color(255, 195, 50); // Lighter Goldenrod
    private static final Color FORGET_BUTTON_COLOR = new Color(138, 43, 226); // Blue Violet
    private static final Color FORGET_BUTTON_HOVER = new Color(170, 75, 255); // Lighter Blue Violet

    public CipherBox() {
        Locale.setDefault(Locale.ENGLISH);
        manager = new Manager();
        manager.count = 0;
        showLoginScreen();
    }

    private void showLoginScreen() {
        loginFrame = new JFrame("Password Management System - Login");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(650, 500);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setLayout(new BorderLayout());
        loginFrame.getContentPane().setBackground(BACKGROUND_COLOR);

        // Header Panel with Gradient
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, new Color(72, 61, 139), 0, getHeight(), BACKGROUND_COLOR));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        JLabel welcomeLabel = new JLabel("Password Management System");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 30));
        welcomeLabel.setForeground(TEXT_COLOR);
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(welcomeLabel, BorderLayout.CENTER);

        // Button Panel with Gradient
        JPanel buttonPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, PANEL_COLOR, 0, getHeight(), new Color(74, 74, 74)));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 2),
            new EmptyBorder(40, 40, 40, 40)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JButton createButton = createStyledButton("Create an Account", CREATE_BUTTON_COLOR, CREATE_BUTTON_HOVER, "Creates a new user account.");
        JButton signInButton = createStyledButton("Sign In", SIGNIN_BUTTON_COLOR, SIGNIN_BUTTON_HOVER, "Log in to your existing account.");
        JButton exitButton = createStyledButton("Exit", EXIT_BUTTON_COLOR, EXIT_BUTTON_HOVER, "Close the application.");

        createButton.addActionListener(e -> showCreateAccountDialog());
        signInButton.addActionListener(e -> showSignInDialog());
        exitButton.addActionListener(e -> System.exit(0));

        gbc.gridy = 0;
        buttonPanel.add(createButton, gbc);
        gbc.gridy = 1;
        buttonPanel.add(signInButton, gbc);
        gbc.gridy = 2;
        buttonPanel.add(exitButton, gbc);

        // Status Bar
        JPanel statusPanel = new JPanel();
        statusPanel.setBackground(BACKGROUND_COLOR);
        statusPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        JLabel statusLabel = new JLabel(getCurrentTime());
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(SECONDARY_TEXT_COLOR);
        statusPanel.add(statusLabel);

        loginFrame.add(headerPanel, BorderLayout.NORTH);
        loginFrame.add(buttonPanel, BorderLayout.CENTER);
        loginFrame.add(statusPanel, BorderLayout.SOUTH);
        loginFrame.setVisible(true);

        // Update time every second
        new Timer(1000, e -> statusLabel.setText(getCurrentTime())).start();
    }

    private JButton createStyledButton(String text, Color baseColor, Color hoverColor, String tooltip) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(baseColor);
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(12, 24, 12, 24)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setToolTipText(tooltip);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(GLOW_COLOR, 2),
                    BorderFactory.createEmptyBorder(11, 23, 11, 23)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(baseColor);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(12, 24, 12, 24)
                ));
            }
        });

        return button;
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField textField = new JTextField(15);
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setForeground(Color.LIGHT_GRAY);
        textField.setText(placeholder);
        textField.setBackground(PANEL_COLOR);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(TEXT_COLOR);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setForeground(Color.LIGHT_GRAY);
                    textField.setText(placeholder);
                }
            }
        });
        return textField;
    }

    private JPasswordField createStyledPasswordField(String placeholder) {
        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setForeground(Color.LIGHT_GRAY);
        passwordField.setText(placeholder);
        passwordField.setBackground(PANEL_COLOR);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        passwordField.setEchoChar((char) 0);
        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                String currentText = new String(passwordField.getPassword());
                if (currentText.equals(placeholder)) {
                    passwordField.setText("");
                    passwordField.setForeground(TEXT_COLOR);
                    passwordField.setEchoChar('•');
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                String currentText = new String(passwordField.getPassword());
                if (currentText.isEmpty()) {
                    passwordField.setForeground(Color.LIGHT_GRAY);
                    passwordField.setText(placeholder);
                    passwordField.setEchoChar((char) 0);
                }
            }
        });
        return passwordField;
    }

    private void showCreateAccountDialog() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, PANEL_COLOR, 0, getHeight(), new Color(74, 74, 74)));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        nameLabel.setForeground(TEXT_COLOR);
        JTextField nameField = createStyledTextField("Enter your full name");

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        usernameLabel.setForeground(TEXT_COLOR);
        JTextField usernameField = createStyledTextField("Enter your username");

        JLabel pinLabel = new JLabel("Pin:");
        pinLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        pinLabel.setForeground(TEXT_COLOR);
        JPasswordField pinField = createStyledPasswordField("Enter your pin");

        JLabel confirmPinLabel = new JLabel("Confirm Pin:");
        confirmPinLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        confirmPinLabel.setForeground(TEXT_COLOR);
        JPasswordField confirmPinField = createStyledPasswordField("Confirm your pin");

        JLabel ageLabel = new JLabel("Age:");
        ageLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        ageLabel.setForeground(TEXT_COLOR);
        JTextField ageField = createStyledTextField("Enter your age");

        JLabel questionLabel = new JLabel("Security Question:");
        questionLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        questionLabel.setForeground(TEXT_COLOR);
        JTextField questionField = createStyledTextField("Enter your security question");

        JLabel answerLabel = new JLabel("Answer:");
        answerLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        answerLabel.setForeground(TEXT_COLOR);
        JTextField answerField = createStyledTextField("Enter your answer");

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(nameLabel, gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(usernameLabel, gbc);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(pinLabel, gbc);
        gbc.gridx = 1;
        panel.add(pinField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(confirmPinLabel, gbc);
        gbc.gridx = 1;
        panel.add(confirmPinField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(ageLabel, gbc);
        gbc.gridx = 1;
        panel.add(ageField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(questionLabel, gbc);
        gbc.gridx = 1;
        panel.add(questionField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        panel.add(answerLabel, gbc);
        gbc.gridx = 1;
        panel.add(answerField, gbc);

        int result = JOptionPane.showConfirmDialog(loginFrame, panel, "Create Account", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().equals("Enter your full name") ? "" : nameField.getText();
            String username = usernameField.getText().equals("Enter your username") ? "" : usernameField.getText();
            String pin = new String(pinField.getPassword()).equals("Enter your pin") ? "" : new String(pinField.getPassword());
            String confirmPin = new String(confirmPinField.getPassword()).equals("Confirm your pin") ? "" : new String(confirmPinField.getPassword());
            String age = ageField.getText().equals("Enter your age") ? "" : ageField.getText();
            String question = questionField.getText().equals("Enter your security question") ? "" : questionField.getText();
            String answer = answerField.getText().equals("Enter your answer") ? "" : answerField.getText();

            if (name.isEmpty() || username.isEmpty() || pin.isEmpty() || confirmPin.isEmpty() || age.isEmpty() || question.isEmpty() || answer.isEmpty()) {
                JOptionPane.showMessageDialog(loginFrame, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (manager.create(name, username, pin, confirmPin, age, question, answer)) {
                loginFrame.dispose();
                showMainMenu();
            }
        }
    }

    private void showSignInDialog() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, PANEL_COLOR, 0, getHeight(), new Color(74, 74, 74)));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        usernameLabel.setForeground(TEXT_COLOR);
        JTextField usernameField = createStyledTextField("Enter your username");

        JLabel pinLabel = new JLabel("Pin:");
        pinLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        pinLabel.setForeground(TEXT_COLOR);
        JPasswordField pinField = createStyledPasswordField("Enter your pin");

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(usernameLabel, gbc);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(pinLabel, gbc);
        gbc.gridx = 1;
        panel.add(pinField, gbc);

        int result = JOptionPane.showConfirmDialog(loginFrame, panel, "Sign In", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().equals("Enter your username") ? "" : usernameField.getText();
            String pin = new String(pinField.getPassword()).equals("Enter your pin") ? "" : new String(pinField.getPassword());

            if (username.isEmpty() || pin.isEmpty()) {
                JOptionPane.showMessageDialog(loginFrame, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (manager.authenticate(username, pin)) {
                loginFrame.dispose();
                showMainMenu();
            }
        }
    }

    private void showMainMenu() {
        mainFrame = new JFrame("Password Management System");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(650, 600);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.getContentPane().setBackground(BACKGROUND_COLOR);

        // Header Panel with Gradient
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, new Color(72, 61, 139), 0, getHeight(), BACKGROUND_COLOR));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        JLabel titleLabel = new JLabel("Password Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 30));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Button Panel with Gradient
        JPanel buttonPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, PANEL_COLOR, 0, getHeight(), new Color(74, 74, 74)));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 2),
            new EmptyBorder(40, 40, 40, 40)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JButton showButton = createStyledButton("Show Saved Passwords", SIGNIN_BUTTON_COLOR, SIGNIN_BUTTON_HOVER, "View and decrypt saved passwords.");
        JButton saveButton = createStyledButton("Save Password", CREATE_BUTTON_COLOR, CREATE_BUTTON_HOVER, "Store a new password securely.");
        JButton decryptButton = createStyledButton("Decrypt Password", DECRYPT_BUTTON_COLOR, DECRYPT_BUTTON_HOVER, "Decrypt a password using a key.");
        JButton forgetButton = createStyledButton("Forget Pin", FORGET_BUTTON_COLOR, FORGET_BUTTON_HOVER, "Reset your pin with the security answer.");
        JButton exitButton = createStyledButton("Exit", EXIT_BUTTON_COLOR, EXIT_BUTTON_HOVER, "Close the application.");

        showButton.addActionListener(e -> showPasswordsDialog());
        saveButton.addActionListener(e -> savePasswordDialog());
        decryptButton.addActionListener(e -> decryptPasswordDialog());
        forgetButton.addActionListener(e -> forgetPinDialog());
        exitButton.addActionListener(e -> showLoginScreen());

        gbc.gridy = 0;
        buttonPanel.add(showButton, gbc);
        gbc.gridy = 1;
        buttonPanel.add(saveButton, gbc);
        gbc.gridy = 2;
        buttonPanel.add(decryptButton, gbc);
        gbc.gridy = 3;
        buttonPanel.add(forgetButton, gbc);
        gbc.gridy = 4;
        buttonPanel.add(exitButton, gbc);

        // Status Bar
        JPanel statusPanel = new JPanel();
        statusPanel.setBackground(BACKGROUND_COLOR);
        statusPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        JLabel statusLabel = new JLabel(getCurrentTime());
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(SECONDARY_TEXT_COLOR);
        statusPanel.add(statusLabel);

        mainFrame.add(headerPanel, BorderLayout.NORTH);
        mainFrame.add(buttonPanel, BorderLayout.CENTER);
        mainFrame.add(statusPanel, BorderLayout.SOUTH);
        mainFrame.setVisible(true);

        // Update time every second
        new Timer(1000, e -> statusLabel.setText(getCurrentTime())).start();
    }

    private void showPasswordsDialog() {
        JPasswordField pinField = createStyledPasswordField("Enter your pin");
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, PANEL_COLOR, 0, getHeight(), new Color(74, 74, 74)));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel pinLabel = new JLabel("Pin:");
        pinLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        pinLabel.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(pinLabel, gbc);
        gbc.gridx = 1;
        panel.add(pinField, gbc);

        int result = JOptionPane.showConfirmDialog(mainFrame, panel, "Verify Pin", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
        if (result == JOptionPane.OK_OPTION) {
            String pin = new String(pinField.getPassword()).equals("Enter your pin") ? "" : new String(pinField.getPassword());
            if (pin.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "Pin is required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            manager.show(pin);
        }
    }

    private void savePasswordDialog() {
        JTextField passwordField = createStyledTextField("Enter your password");
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, PANEL_COLOR, 0, getHeight(), new Color(74, 74, 74)));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordLabel.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        int result = JOptionPane.showConfirmDialog(mainFrame, panel, "Save Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
        if (result == JOptionPane.OK_OPTION) {
            String password = passwordField.getText().equals("Enter your password") ? "" : passwordField.getText();
            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "Password is required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            manager.save(password);
        }
    }

    private void decryptPasswordDialog() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, PANEL_COLOR, 0, getHeight(), new Color(74, 74, 74)));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel passwordLabel = new JLabel("Password to decrypt:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordLabel.setForeground(TEXT_COLOR);
        JTextField passwordField = createStyledTextField("Enter password to decrypt");

        JLabel keyLabel = new JLabel("Key:");
        keyLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        keyLabel.setForeground(TEXT_COLOR);
        JTextField keyField = createStyledTextField("Enter decryption key");

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(keyLabel, gbc);
        gbc.gridx = 1;
        panel.add(keyField, gbc);

        int result = JOptionPane.showConfirmDialog(mainFrame, panel, "Decrypt Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
        if (result == JOptionPane.OK_OPTION) {
            String password = passwordField.getText().equals("Enter password to decrypt") ? "" : passwordField.getText();
            String key = keyField.getText().equals("Enter decryption key") ? "" : keyField.getText();
            if (password.isEmpty() || key.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            manager.decryption(password, key);
        }
    }

    private void forgetPinDialog() {
        JTextField answerField = createStyledTextField("Enter your answer");
        JPanel answerPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, PANEL_COLOR, 0, getHeight(), new Color(74, 74, 74)));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        answerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel answerLabel = new JLabel("Answer the security question:\nQ: " + manager.getPersonalQuestion());
        answerLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        answerLabel.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        answerPanel.add(answerLabel, gbc);
        gbc.gridx = 1;
        answerPanel.add(answerField, gbc);

        int answerResult = JOptionPane.showConfirmDialog(mainFrame, answerPanel, "Forget Pin", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
        if (answerResult != JOptionPane.OK_OPTION) {
            return;
        }

        String answer = answerField.getText().equals("Enter your answer") ? "" : answerField.getText();
        if (answer.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "Answer is required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JPanel pinPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, PANEL_COLOR, 0, getHeight(), new Color(74, 74, 74)));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        pinPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel newPinLabel = new JLabel("New Pin:");
        newPinLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        newPinLabel.setForeground(TEXT_COLOR);
        JPasswordField newPinField = createStyledPasswordField("Enter new pin");

        JLabel confirmPinLabel = new JLabel("Confirm Pin:");
        confirmPinLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        confirmPinLabel.setForeground(TEXT_COLOR);
        JPasswordField confirmPinField = createStyledPasswordField("Confirm new pin");

        gbc.gridx = 0;
        gbc.gridy = 0;
        pinPanel.add(newPinLabel, gbc);
        gbc.gridx = 1;
        pinPanel.add(newPinField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        pinPanel.add(confirmPinLabel, gbc);
        gbc.gridx = 1;
        pinPanel.add(confirmPinField, gbc);

        int pinResult = JOptionPane.showConfirmDialog(mainFrame, pinPanel, "Change Pin", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
        if (pinResult == JOptionPane.OK_OPTION) {
            String newPin = new String(newPinField.getPassword()).equals("Enter new pin") ? "" : new String(newPinField.getPassword());
            String confirmPin = new String(confirmPinField.getPassword()).equals("Confirm new pin") ? "" : new String(confirmPinField.getPassword());
            if (newPin.isEmpty() || confirmPin.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            manager.forget(answer, newPin, confirmPin);
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a zzz, MMMM dd, yyyy");
        return sdf.format(new Date()) + " (PKT)";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CipherBox());
    }
}