import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;

class ModernBorder implements Border {
    private int radius;
    private Color color;

    public ModernBorder(int radius, Color color) {
        this.radius = radius;
        this.color = color;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(color);
        g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        g2d.dispose();
    }

    public Insets getBorderInsets(Component c) {
        return new Insets(radius / 2 + 4, radius / 2 + 4, radius / 2 + 4, radius / 2 + 4);
    }

    public boolean isBorderOpaque() {
        return false;
    }
}

class ModernGradientPanel extends JPanel {
    private Color startColor;
    private Color endColor;

    public ModernGradientPanel(Color startColor, Color endColor) {
        this.startColor = startColor;
        this.endColor = endColor;
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(0, 0, startColor, 0, getHeight(), endColor);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
    }
}

class Manager {
    private String[] passwords = new String[15];
    private String[] passwordNames = new String[15];
    private String username;
    private String name;
    private String pin;
    private String personalQuestion;
    private String personalAnswer;
    private String email;
    private int age;
    private boolean create = false;
    public int count = 0;
    private List<String> activityLog = new ArrayList<>();
    private static final String FILE_PATH = "passwords.txt";

    void addActivity(String action) {
        String timestamp = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss").format(new Date());
        activityLog.add(0, timestamp + ": " + action);
        if (activityLog.size() > 5) {
            activityLog.remove(activityLog.size() - 1);
        }
    }

    List<String> getRecentActivities() {
        return new ArrayList<>(activityLog);
    }

    boolean authenticate(String inpUsername, String inpPin) {
        if (username == null || pin == null) {
            JOptionPane.showMessageDialog(null, "No account exists. Please create an account first.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (inpUsername.equals(username) && inpPin.equals(pin)) {
            addActivity("Successful login for " + username);
            return true;
        }
        JOptionPane.showMessageDialog(null, "Invalid Username or PIN", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
    }

    boolean create(String subname, String subusername, String subpin, String subpin2, String ageStr, String email, String question, String answer) {
        if (create) {
            JOptionPane.showMessageDialog(null, "In Free Trial, You Can Create Only One Account", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!subpin.equals(subpin2)) {
            JOptionPane.showMessageDialog(null, "PINs Do Not Match", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (subpin.length() < 4 || subpin.length() > 8) {
            JOptionPane.showMessageDialog(null, "PIN must be between 4 and 8 characters.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!subusername.matches("^[a-zA-Z0-9_]+$")) {
            JOptionPane.showMessageDialog(null, "Username can only contain letters, numbers, and underscores.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            int age = Integer.parseInt(ageStr);
            if (age < 16) {
                JOptionPane.showMessageDialog(null, "Access Denied! You must be 16 or older.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            this.age = age;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid age format. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(null, "Invalid email format.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        name = subname.trim();
        username = subusername.trim();
        pin = subpin.trim();
        personalQuestion = question.trim();
        personalAnswer = answer.trim();
        this.email = email.trim();
        create = true;
        try {
            saveToFile(subpin);
            addActivity("Account created for " + username);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to save account: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        JOptionPane.showMessageDialog(null, "Account created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        return true;
    }

    boolean validatePinAndCheckPasswords(String subpin) {
        if (!subpin.equals(pin)) {
            JOptionPane.showMessageDialog(null, "Invalid PIN", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (count == 0) {
            JOptionPane.showMessageDialog(null, "No passwords saved.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        return true;
    }

    String decryptPassword(int index, String keyStr) {
        try {
            int key = Integer.parseInt(keyStr.trim());
            StringBuilder decryptedText = new StringBuilder();
            for (int i = 0; i < passwords[index].length(); i++) {
                char a = (char) (passwords[index].charAt(i) - key);
                decryptedText.append(a);
            }
            addActivity("Password '" + passwordNames[index] + "' decrypted");
            return "Decrypted Password for '" + passwordNames[index] + "': " + decryptedText.toString();
        } catch (NumberFormatException e) {
            return "Invalid key. Please enter a valid number.";
        } catch (Exception e) {
            return "Error during decryption.";
        }
    }

    String[] getPasswordNames() {
        return passwordNames;
    }

    String[] getPasswords() {
        return passwords;
    }

    void saveToFile(String subpin) throws IOException {
        if (!subpin.equals(pin)) {
            JOptionPane.showMessageDialog(null, "Invalid PIN", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File file = new File(FILE_PATH);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                JOptionPane.showMessageDialog(null, "Failed to create directory.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("[User]\n");
            writer.write("username:" + (username != null ? username : "") + "\n");
            writer.write("pin:" + (pin != null ? pin : "") + "\n");
            writer.write("full_name:" + (name != null ? name : "") + "\n");
            writer.write("age:" + age + "\n");
            writer.write("email:" + (email != null ? email : "") + "\n");
            writer.write("question:" + (personalQuestion != null ? personalQuestion : "") + "\n");
            writer.write("answer:" + (personalAnswer != null ? personalAnswer : "") + "\n");
            writer.write("[Passwords]\n");
            for (int i = 0; i < count; i++) {
                writer.write(passwordNames[i] + ":" + passwords[i] + "\n");
            }
            addActivity("Data saved to file");
            JOptionPane.showMessageDialog(null, "Data saved to " + FILE_PATH, "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to save: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }

    void loadFromFile(String subpin) throws IOException {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return; // No file exists, so no data to load
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean inUserSection = false;
            boolean inPasswordSection = false;
            count = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.equals("[User]")) {
                    inUserSection = true;
                    inPasswordSection = false;
                    continue;
                } else if (line.equals("[Passwords]")) {
                    inUserSection = false;
                    inPasswordSection = true;
                    continue;
                }

                if (inUserSection && !create) {
                    if (line.startsWith("username:")) {
                        username = line.substring("username:".length()).trim();
                    } else if (line.startsWith("pin:")) {
                        pin = line.substring("pin:".length()).trim();
                    } else if (line.startsWith("full_name:")) {
                        name = line.substring("full_name:".length()).trim();
                    } else if (line.startsWith("age:")) {
                        try {
                            age = Integer.parseInt(line.substring("age:".length()).trim());
                        } catch (NumberFormatException e) {
                            // Skip invalid age
                        }
                    } else if (line.startsWith("email:")) {
                        email = line.substring("email:".length()).trim();
                    } else if (line.startsWith("question:")) {
                        personalQuestion = line.substring("question:".length()).trim();
                    } else if (line.startsWith("answer:")) {
                        personalAnswer = line.substring("answer:".length()).trim();
                        create = true;
                    }
                } else if (inPasswordSection && count < passwords.length) {
                    if (line.contains(":")) {
                        String[] parts = line.split(":", 2);
                        if (parts.length == 2) {
                            passwordNames[count] = parts[0].trim();
                            passwords[count] = parts[1].trim();
                            count++;
                        }
                    }
                }
            }

            if (create && subpin != null && !subpin.equals(pin)) {
                JOptionPane.showMessageDialog(null, "Invalid PIN", "Error", JOptionPane.ERROR_MESSAGE);
                username = null;
                pin = null;
                name = null;
                email = null;
                personalQuestion = null;
                personalAnswer = null;
                age = 0;
                create = false;
                count = 0;
                for (int i = 0; i < passwords.length; i++) {
                    passwords[i] = null;
                    passwordNames[i] = null;
                }
                return;
            }

            if (create) {
                addActivity("Loaded " + count + " passwords from file");
                JOptionPane.showMessageDialog(null, "Loaded user data and " + count + " passwords from " + FILE_PATH, "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to load: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }

    void deletePassword(int index) {
        if (index < 0 || index >= count) {
            JOptionPane.showMessageDialog(null, "Invalid password index.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String deletedName = passwordNames[index];
        for (int i = index; i < count - 1; i++) {
            passwords[i] = passwords[i + 1];
            passwordNames[i] = passwordNames[i + 1];
        }
        passwords[count - 1] = null;
        passwordNames[count - 1] = null;
        count--;
        try {
            saveToFile(pin);
            addActivity("Password '" + deletedName + "' deleted");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to update file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        JOptionPane.showMessageDialog(null, "Password deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    void clearAllPasswords(String subpin) {
        if (!subpin.equals(pin)) {
            JOptionPane.showMessageDialog(null, "Invalid PIN", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        for (int i = 0; i < count; i++) {
            passwords[i] = null;
            passwordNames[i] = null;
        }
        count = 0;
        try {
            saveToFile(subpin);
            addActivity("All passwords cleared");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to update file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        JOptionPane.showMessageDialog(null, "All passwords cleared.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    void decryption(String subpassword, String keyStr) {
        try {
            int key = Integer.parseInt(keyStr.trim());
            StringBuilder decryptedText = new StringBuilder();
            for (int i = 0; i < subpassword.length(); i++) {
                char a = (char) (subpassword.charAt(i) - key);
                decryptedText.append(a);
            }
            addActivity("Custom password decrypted");
            JOptionPane.showMessageDialog(null, "Decrypted Password: " + decryptedText, "Decrypted Password", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid key. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    String evaluatePasswordStrength(String password) {
        int score = 0;
        if (password.length() >= 8) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) score++;
        return switch (score) {
            case 5 -> "Strong";
            case 4 -> "Good";
            case 3 -> "Moderate";
            case 2 -> "Weak";
            default -> "Very Weak";
        };
    }

    void save(String passwordName, String password) {
        if (count >= passwords.length) {
            JOptionPane.showMessageDialog(null, "Storage limit reached.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int key = (int) (Math.random() * 8) + 1;
        StringBuilder encryptedText = new StringBuilder();
        for (int i = 0; i < password.length(); i++) {
            char a = (char) (password.charAt(i) + key);
            encryptedText.append(a);
        }
        passwords[count] = encryptedText.toString();
        passwordNames[count] = passwordName.trim();
        count++;
        try {
            saveToFile(pin);
            addActivity("Password '" + passwordName + "' saved");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to save password: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        JOptionPane.showMessageDialog(null, "Password '" + passwordName + "' saved.\nDecryption key: " + key, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    void forget(String subAnswer, String newPin, String confirmPin) {
        if (!subAnswer.trim().equalsIgnoreCase(personalAnswer)) {
            JOptionPane.showMessageDialog(null, "Incorrect Answer.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!newPin.equals(confirmPin)) {
            JOptionPane.showMessageDialog(null, "PINs do not match.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (newPin.length() < 4 || newPin.length() > 8) {
            JOptionPane.showMessageDialog(null, "PIN must be 4-8 characters.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        pin = newPin.trim();
        try {
            saveToFile(pin);
            addActivity("PIN reset");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to update file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        JOptionPane.showMessageDialog(null, "PIN changed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    String getPersonalQuestion() {
        return personalQuestion != null ? personalQuestion : "Not set";
    }

    String getPin() {
        return pin;
    }

    String getName() {
        return name != null ? name : "User";
    }
}

public class CipherBox {
    private Manager manager;
    private JFrame loginFrame;
    private JFrame mainFrame;

    private static final Color PRIMARY_COLOR = new Color(25, 118, 210);
    private static final Color SECONDARY_COLOR = new Color(100, 181, 246);
    private static final Color BACKGROUND_START = new Color(240, 242, 245);
    private static final Color BACKGROUND_END = new Color(220, 225, 235);
    private static final Color TEXT_COLOR = new Color(18, 18, 18);
    private static final Color SECONDARY_TEXT_COLOR = new Color(97, 97, 97);
    private static final Color BORDER_COLOR = new Color(169, 175, 179);
    private static final Color SUCCESS_COLOR = new Color(46, 125, 50);
    private static final Color DANGER_COLOR = new Color(211, 47, 47);
    private static final Color SIDEBAR_COLOR = new Color(248, 250, 252);
    private static final Color CARD_COLOR = new Color(255, 255, 255);
    private static final Color HOVER_COLOR = new Color(230, 240, 255);
    private static final Font PRIMARY_FONT = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font MONOSPACE_FONT = new Font("Consolas", Font.PLAIN, 15);

    public CipherBox() {
        Locale.setDefault(Locale.ENGLISH);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Button.font", PRIMARY_FONT);
            UIManager.put("Label.font", PRIMARY_FONT);
            UIManager.put("TextField.font", PRIMARY_FONT);
            UIManager.put("PasswordField.font", PRIMARY_FONT);
            UIManager.put("ToolTip.background", new Color(255, 255, 255));
            UIManager.put("ToolTip.border", new ModernBorder(8, BORDER_COLOR));
            UIManager.put("ToolTip.font", PRIMARY_FONT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        manager = new Manager();
        manager.count = 0;
        // Load saved data on startup
        try {
            manager.loadFromFile(null); // Pass null to load without PIN validation initially
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to load saved data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        showLoginScreen();
    }

    private void showLoginScreen() {
        if (mainFrame != null) {
            mainFrame.dispose();
        }
        loginFrame = new JFrame("CipherBox - Login");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(450, 600);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setResizable(false);
        loginFrame.setLayout(new BorderLayout());

        ModernGradientPanel mainPanel = new ModernGradientPanel(BACKGROUND_START, BACKGROUND_END);
        mainPanel.setLayout(new BorderLayout());
        loginFrame.setContentPane(mainPanel);

        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(40, 40, 20, 40));
        JLabel headerLabel = new JLabel("CipherBox");
        headerLabel.setFont(TITLE_FONT);
        headerLabel.setForeground(TEXT_COLOR);
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(headerLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 40, 20, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(PRIMARY_FONT);
        usernameLabel.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(usernameLabel, gbc);

        JTextField usernameField = new JTextField(20);
        usernameField.setFont(PRIMARY_FONT);
        usernameField.setBorder(new ModernBorder(8, BORDER_COLOR));
        usernameField.setBackground(Color.WHITE);
        usernameField.setForeground(TEXT_COLOR);
        gbc.gridy = 1;
        formPanel.add(usernameField, gbc);
        usernameLabel.setLabelFor(usernameField);

        JLabel pinLabel = new JLabel("PIN");
        pinLabel.setFont(PRIMARY_FONT);
        pinLabel.setForeground(TEXT_COLOR);
        gbc.gridy = 2;
        formPanel.add(pinLabel, gbc);

        JPasswordField pinField = new JPasswordField(20);
        pinField.setFont(PRIMARY_FONT);
        pinField.setBorder(new ModernBorder(8, BORDER_COLOR));
        pinField.setBackground(Color.WHITE);
        pinField.setForeground(TEXT_COLOR);
        gbc.gridy = 3;
        formPanel.add(pinField, gbc);
        pinLabel.setLabelFor(pinField);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setOpaque(false);
        JButton loginButton = createButton("Sign In", PRIMARY_COLOR);
        JButton createAcctButton = createButton("Create Account", SUCCESS_COLOR);

        buttonPanel.add(createAcctButton);
        buttonPanel.add(loginButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String pin = new String(pinField.getPassword()).trim();
            if (username.isEmpty() || pin.isEmpty()) {
                JOptionPane.showMessageDialog(loginFrame, "Both fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (manager.authenticate(username, pin)) {
                loginFrame.dispose();
                showMainMenu();
            }
        });

        createAcctButton.addActionListener(e -> showCreateAccountDialog());

        loginFrame.getRootPane().setDefaultButton(loginButton);
        loginFrame.setVisible(true);
    }

    private JButton createButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(isEnabled() ? getBackground() : getBackground().darker());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g2d);
                g2d.dispose();
            }
        };
        button.setFont(PRIMARY_FONT);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 24, 12, 24));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setPreferredSize(new Dimension(140, 38));
        button.setMinimumSize(new Dimension(140, 38));
        button.setMaximumSize(new Dimension(140, 38));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(HOVER_COLOR);
                    button.setForeground(TEXT_COLOR);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(bgColor);
                    button.setForeground(Color.WHITE);
                }
            }
        });

        return button;
    }

    private JButton createSidebarButton(String text, Color bgColor, Icon icon) {
        JButton button = new JButton(text, icon) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(isEnabled() ? getBackground() : getBackground().darker());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g2d);
                g2d.dispose();
            }
        };
        button.setFont(PRIMARY_FONT);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 15, 10, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setPreferredSize(new Dimension(200, 40));
        button.setMinimumSize(new Dimension(200, 40));
        button.setMaximumSize(new Dimension(200, 40));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(HOVER_COLOR);
                    button.setForeground(TEXT_COLOR);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(bgColor);
                    button.setForeground(Color.WHITE);
                }
            }
        });

        return button;
    }

    private void showCreateAccountDialog() {
        JDialog dialog = new JDialog(loginFrame, "Create Account", true);
        dialog.setSize(500, 650);
        dialog.setLocationRelativeTo(loginFrame);
        dialog.setResizable(false);
        dialog.setLayout(new BorderLayout());

        ModernGradientPanel mainPanel = new ModernGradientPanel(BACKGROUND_START, BACKGROUND_END);
        mainPanel.setLayout(new BorderLayout());
        dialog.setContentPane(mainPanel);

        JLabel title = new JLabel("Create Account");
        title.setFont(HEADER_FONT);
        title.setForeground(TEXT_COLOR);
        title.setBorder(new EmptyBorder(20, 20, 20, 20));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        String[] labels = {"Full Name", "Username", "PIN", "Confirm PIN", "Age", "Email", "Security Question", "Answer"};
        JComponent[] inputs = {
            new JTextField(25), new JTextField(25), new JPasswordField(25), new JPasswordField(25),
            new JTextField(25), new JTextField(25), new JTextField(25), new JTextField(25)
        };

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i * 2;
            JLabel label = new JLabel(labels[i]);
            label.setFont(PRIMARY_FONT);
            label.setForeground(TEXT_COLOR);
            formPanel.add(label, gbc);
            label.setLabelFor(inputs[i]);

            gbc.gridy = i * 2 + 1;
            inputs[i].setFont(PRIMARY_FONT);
            inputs[i].setBorder(new ModernBorder(8, BORDER_COLOR));
            inputs[i].setBackground(Color.WHITE);
            inputs[i].setForeground(TEXT_COLOR);
            formPanel.add(inputs[i], gbc);
        }

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setOpaque(false);
        JButton cancelBtn = createButton("Cancel", DANGER_COLOR);
        JButton createBtn = createButton("Create", SUCCESS_COLOR);

        buttonPanel.add(cancelBtn);
        buttonPanel.add(createBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        cancelBtn.addActionListener(e -> dialog.dispose());
        createBtn.addActionListener(e -> {
            String name = ((JTextField) inputs[0]).getText().trim();
            String username = ((JTextField) inputs[1]).getText().trim();
            String pin = new String(((JPasswordField) inputs[2]).getPassword()).trim();
            String confirmPin = new String(((JPasswordField) inputs[3]).getPassword()).trim();
            String age = ((JTextField) inputs[4]).getText().trim();
            String email = ((JTextField) inputs[5]).getText().trim();
            String question = ((JTextField) inputs[6]).getText().trim();
            String answer = ((JTextField) inputs[7]).getText().trim();

            if (name.isEmpty() || username.isEmpty() || pin.isEmpty() || confirmPin.isEmpty() || age.isEmpty() || email.isEmpty() || question.isEmpty() || answer.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (manager.create(name, username, pin, confirmPin, age, email, question, answer)) {
                dialog.dispose();
                loginFrame.dispose();
                showMainMenu();
            }
        });

        dialog.getRootPane().setDefaultButton(createBtn);
        dialog.setVisible(true);
    }

    private JLabel statsContentLabel; // To update Password Stats dynamically
    private JLabel activityContentLabel; // To update Recent Activity dynamically
    private JLabel statusMessageLabel; // For dynamic status messages

    private void showMainMenu() {
        if (loginFrame != null) {
            loginFrame.dispose();
        }
        mainFrame = new JFrame("CipherBox - Dashboard");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1000, 700);
        mainFrame.setMinimumSize(new Dimension(900, 600));
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setLayout(new BorderLayout());

        ModernGradientPanel mainPanel = new ModernGradientPanel(BACKGROUND_START, BACKGROUND_END);
        mainPanel.setLayout(new BorderLayout());
        mainFrame.setContentPane(mainPanel);

        // Sidebar
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setBackground(SIDEBAR_COLOR);
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        sidebarPanel.setPreferredSize(new Dimension(220, 0));
        sidebarPanel.setMaximumSize(new Dimension(220, Integer.MAX_VALUE));

        JLabel sidebarHeader = new JLabel("CipherBox");
        sidebarHeader.setFont(HEADER_FONT);
        sidebarHeader.setForeground(TEXT_COLOR);
        sidebarHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebarHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        sidebarPanel.add(sidebarHeader);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Sidebar buttons with icons
        String[] buttonLabels = {
            "Show Passwords", "Save Password", "Generate Password", "Delete Password",
            "Clear All", "Decrypt Password", "Export Passwords", "Import Passwords",
            "Forget PIN", "About", "Logout"
        };
        String[] tooltips = {
            "View and manage saved passwords",
            "Save a new password",
            "Generate a strong password",
            "Delete a saved password",
            "Clear all saved passwords",
            "Decrypt a custom password",
            "Export passwords to a file",
            "Import passwords from a file",
            "Reset your PIN",
            "Learn about CipherBox",
            "Logout and return to login screen"
        };
        Color[] buttonColors = {
            PRIMARY_COLOR, PRIMARY_COLOR, PRIMARY_COLOR, PRIMARY_COLOR,
            PRIMARY_COLOR, PRIMARY_COLOR, PRIMARY_COLOR, PRIMARY_COLOR,
            PRIMARY_COLOR, PRIMARY_COLOR, DANGER_COLOR
        };

        // Simple icon implementation
        Icon actionIcon = new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(SECONDARY_TEXT_COLOR);
                g2d.fillRect(x + 4, y + 4, 8, 8);
                g2d.dispose();
            }
            @Override
            public int getIconWidth() { return 16; }
            @Override
            public int getIconHeight() { return 16; }
        };

        JButton[] buttons = new JButton[buttonLabels.length];
        for (int i = 0; i < buttonLabels.length; i++) {
            buttons[i] = createSidebarButton(buttonLabels[i], buttonColors[i], actionIcon);
            buttons[i].setToolTipText(tooltips[i]);
            buttons[i].setAlignmentX(Component.CENTER_ALIGNMENT);
            buttons[i].setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            sidebarPanel.add(buttons[i]);
            sidebarPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        }
        sidebarPanel.add(Box.createVerticalGlue());

        // Content Panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header Card
        JPanel headerCard = new JPanel(new GridBagLayout());
        headerCard.setBackground(CARD_COLOR);
        headerCard.setBorder(BorderFactory.createCompoundBorder(
            new ModernBorder(12, BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel welcomeLabel = new JLabel("Welcome, " + manager.getName());
        welcomeLabel.setFont(HEADER_FONT);
        welcomeLabel.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        headerCard.add(welcomeLabel, gbc);

        JLabel subtitleLabel = new JLabel("Your secure password manager");
        subtitleLabel.setFont(PRIMARY_FONT);
        subtitleLabel.setForeground(SECONDARY_TEXT_COLOR);
        gbc.gridy = 1;
        headerCard.add(subtitleLabel, gbc);

        // Dashboard Cards
        JPanel dashboardPanel = new JPanel(new GridBagLayout());
        dashboardPanel.setOpaque(false);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;

        // Card 1: Password Stats
        JPanel statsCard = new JPanel(new BorderLayout());
        statsCard.setBackground(CARD_COLOR);
        statsCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        JLabel statsTitle = new JLabel("Password Stats");
        statsTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        statsTitle.setForeground(TEXT_COLOR);
        statsCard.add(statsTitle, BorderLayout.NORTH);
        statsContentLabel = new JLabel("<html>Total Passwords: " + manager.count + "<br>Storage Left: " + (15 - manager.count) + "</html>");
        statsContentLabel.setFont(PRIMARY_FONT);
        statsContentLabel.setForeground(SECONDARY_TEXT_COLOR);
        statsContentLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        statsCard.add(statsContentLabel, BorderLayout.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        dashboardPanel.add(statsCard, gbc);

        // Card 2: Recent Activity
        JPanel activityCard = new JPanel(new BorderLayout());
        activityCard.setBackground(CARD_COLOR);
        activityCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        JLabel activityTitle = new JLabel("Recent Activity");
        activityTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        activityTitle.setForeground(TEXT_COLOR);
        activityCard.add(activityTitle, BorderLayout.NORTH);
        activityContentLabel = new JLabel("<html>" + getActivityText() + "</html>");
        activityContentLabel.setFont(PRIMARY_FONT);
        activityContentLabel.setForeground(SECONDARY_TEXT_COLOR);
        activityContentLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        activityCard.add(activityContentLabel, BorderLayout.CENTER);
        gbc.gridx = 1;
        gbc.gridy = 0;
        dashboardPanel.add(activityCard, gbc);

        contentPanel.add(headerCard, BorderLayout.NORTH);
        contentPanel.add(dashboardPanel, BorderLayout.CENTER);

        // Status Bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        statusBar.setOpaque(false);
        statusBar.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        JLabel timeLabel = new JLabel(getCurrentTime());
        timeLabel.setFont(PRIMARY_FONT);
        timeLabel.setForeground(SECONDARY_TEXT_COLOR);
        statusMessageLabel = new JLabel("Ready");
        statusMessageLabel.setFont(PRIMARY_FONT);
        statusMessageLabel.setForeground(SECONDARY_TEXT_COLOR);
        statusBar.add(timeLabel);
        statusBar.add(Box.createHorizontalStrut(15));
        statusBar.add(statusMessageLabel);

        mainPanel.add(sidebarPanel, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        // Button Actions
        buttons[0].addActionListener(e -> showPasswordsDialog());
        buttons[1].addActionListener(e -> savePasswordDialog());
        buttons[2].addActionListener(e -> generatePasswordDialog());
        buttons[3].addActionListener(e -> deletePasswordDialog());
        buttons[4].addActionListener(e -> clearAllPasswordsDialog());
        buttons[5].addActionListener(e -> decryptPasswordDialog());
        buttons[6].addActionListener(e -> exportPasswordsDialog());
        buttons[7].addActionListener(e -> importPasswordsDialog());
        buttons[8].addActionListener(e -> forgetPinDialog());
        buttons[9].addActionListener(e -> showAboutDialog());
        buttons[10].addActionListener(e -> {
            mainFrame.dispose();
            showLoginScreen();
        });

        mainFrame.setVisible(true);
        new Timer(1000, e -> timeLabel.setText(getCurrentTime())).start();
    }

    private String getActivityText() {
        List<String> activities = manager.getRecentActivities();
        if (activities.isEmpty()) {
            return "No recent activity.";
        }
        StringBuilder sb = new StringBuilder();
        for (String activity : activities) {
            sb.append(activity.replace("<", "<").replace(">", ">")).append("<br>");
        }
        return sb.toString();
    }

    private void updateDashboard() {
        statsContentLabel.setText("<html>Total Passwords: " + manager.count + "<br>Storage Left: " + (15 - manager.count) + "</html>");
        activityContentLabel.setText("<html>" + getActivityText() + "</html>");
    }

    private void setStatusMessage(String message) {
        statusMessageLabel.setText(message);
        Timer timer = new Timer(3000, e -> statusMessageLabel.setText("Ready"));
        timer.setRepeats(false);
        timer.start();
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("MMM dd, yyyy HH:mm:ss").format(new Date());
    }

    private JPanel buildDialogPanel(JLabel label, JComponent input) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        label.setFont(PRIMARY_FONT);
        label.setForeground(TEXT_COLOR);
        panel.add(label, gbc);
        gbc.gridy = 1;
        input.setFont(PRIMARY_FONT);
        input.setBorder(new ModernBorder(8, BORDER_COLOR));
        input.setBackground(Color.WHITE);
        input.setForeground(TEXT_COLOR);
        panel.add(input, gbc);
        label.setLabelFor(input);
        return panel;
    }

    private void showAboutDialog() {
        JDialog dialog = new JDialog(mainFrame, "About CipherBox", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setLayout(new BorderLayout());

        ModernGradientPanel mainPanel = new ModernGradientPanel(BACKGROUND_START, BACKGROUND_END);
        mainPanel.setLayout(new BorderLayout());
        dialog.setContentPane(mainPanel);

        JLabel title = new JLabel("CipherBox");
        title.setFont(HEADER_FONT);
        title.setForeground(TEXT_COLOR);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.add(title, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        String[] info = {
            "Version: 1.1",
            "Developed by: CipherBox Team",
            "Purpose: Secure Password Management",
            "Contact: support@cipherbox.com"
        };

        for (int i = 0; i < info.length; i++) {
            gbc.gridy = i;
            JLabel label = new JLabel(info[i]);
            label.setFont(PRIMARY_FONT);
            label.setForeground(SECONDARY_TEXT_COLOR);
            infoPanel.add(label, gbc);
        }

        mainPanel.add(infoPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setOpaque(false);
        JButton closeBtn = createButton("Close", DANGER_COLOR);
        closeBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.getRootPane().setDefaultButton(closeBtn);
        dialog.setVisible(true);
    }

    private void showPasswordsDialog() {
        JPasswordField pinField = new JPasswordField(20);
        JPanel panel = buildDialogPanel(new JLabel("Enter your PIN"), pinField);
        int result = JOptionPane.showConfirmDialog(mainFrame, panel, "Verify PIN", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String pin = new String(pinField.getPassword()).trim();
        if (pin.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "PIN is required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!manager.validatePinAndCheckPasswords(pin)) {
            return;
        }

        Object[][] data = new Object[manager.count][2];
        for (int i = 0; i < manager.count; i++) {
            data[i][0] = manager.getPasswordNames()[i];
            data[i][1] = manager.getPasswords()[i];
        }
        String[] columnNames = {"Password Name", "Encrypted Password"};
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFont(PRIMARY_FONT);
        table.setRowHeight(32);
        table.setBackground(Color.WHITE);
        table.setForeground(TEXT_COLOR);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(BORDER_COLOR);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        table.getTableHeader().setBackground(BACKGROUND_START);
        table.getTableHeader().setForeground(TEXT_COLOR);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(500, 350));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);
        JButton decryptButton = createButton("Decrypt Selected", PRIMARY_COLOR);
        JButton backButton = createButton("Back", DANGER_COLOR);
        buttonPanel.add(decryptButton);
        buttonPanel.add(backButton);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        JDialog dialog = new JDialog(mainFrame, "Saved Passwords", true);
        dialog.setSize(600, 450);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(BACKGROUND_START);
        dialog.add(mainPanel, BorderLayout.CENTER);

        decryptButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                String keyStr = JOptionPane.showInputDialog(dialog, "Enter the decryption key for '" + manager.getPasswordNames()[selectedRow] + "':", "Decrypt Key", JOptionPane.QUESTION_MESSAGE);
                if (keyStr != null && !keyStr.trim().isEmpty()) {
                    String message = manager.decryptPassword(selectedRow, keyStr.trim());
                    if (message.startsWith("Invalid") || message.startsWith("Error")) {
                        JOptionPane.showMessageDialog(dialog, message, "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(dialog, message, "Decrypted Password", JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(dialog, "Decryption key is required.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "Please select a password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        backButton.addActionListener(e -> dialog.dispose());
        dialog.getRootPane().setDefaultButton(decryptButton);
        dialog.setVisible(true);
        updateDashboard();
    }

    private void exportPasswordsDialog() {
        JPasswordField pinField = new JPasswordField(20);
        JPanel panel = buildDialogPanel(new JLabel("Enter your PIN to export"), pinField);
        int result = JOptionPane.showConfirmDialog(mainFrame, panel, "Export Passwords", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String pin = new String(pinField.getPassword()).trim();
        if (pin.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "PIN is required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(mainFrame, "Exporting Passwords", true);
        dialog.setSize(400, 150);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(BACKGROUND_START);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setString("Exporting...");
        progressBar.setStringPainted(true);
        progressBar.setForeground(PRIMARY_COLOR);
        progressBar.setBackground(BACKGROUND_START);
        progressBar.setBorder(new EmptyBorder(20, 20, 20, 20));
        dialog.add(progressBar, BorderLayout.CENTER);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    manager.saveToFile(pin);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(dialog, "Export failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }

            @Override
            protected void done() {
                dialog.dispose();
                setStatusMessage("Passwords exported");
            }
        }.execute();

        dialog.setVisible(true);
    }

    private void importPasswordsDialog() {
        JPasswordField pinField = new JPasswordField(20);
        JPanel panel = buildDialogPanel(new JLabel("Enter your PIN to import"), pinField);
        int result = JOptionPane.showConfirmDialog(mainFrame, panel, "Import Passwords", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String pin = new String(pinField.getPassword()).trim();
        if (pin.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "PIN is required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(mainFrame, "Importing Passwords", true);
        dialog.setSize(400, 150);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(BACKGROUND_START);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setString("Importing...");
        progressBar.setStringPainted(true);
        progressBar.setForeground(PRIMARY_COLOR);
        progressBar.setBackground(BACKGROUND_START);
        progressBar.setBorder(new EmptyBorder(20, 20, 20, 20));
        dialog.add(progressBar, BorderLayout.CENTER);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    manager.loadFromFile(pin);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(dialog, "Import failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                return null;
            }

            @Override
            protected void done() {
                dialog.dispose();
                updateDashboard();
                setStatusMessage("Passwords imported");
            }
        }.execute();

        dialog.setVisible(true);
    }

    private void savePasswordDialog() {
        JTextField nameField = new JTextField(20);
        JTextField passwordField = new JTextField(20);
        JLabel strengthLabel = new JLabel("Password Strength: Not evaluated");
        strengthLabel.setFont(PRIMARY_FONT);
        strengthLabel.setForeground(TEXT_COLOR);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Password Name");
        nameLabel.setFont(PRIMARY_FONT);
        nameLabel.setForeground(TEXT_COLOR);
        panel.add(nameLabel, gbc);
        nameLabel.setLabelFor(nameField);

        gbc.gridx = 1;
        nameField.setFont(PRIMARY_FONT);
        nameField.setBorder(new ModernBorder(8, BORDER_COLOR));
        nameField.setBackground(Color.WHITE);
        nameField.setForeground(TEXT_COLOR);
        panel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(PRIMARY_FONT);
        passwordLabel.setForeground(TEXT_COLOR);
        panel.add(passwordLabel, gbc);
        passwordLabel.setLabelFor(passwordField);

        gbc.gridx = 1;
        passwordField.setFont(PRIMARY_FONT);
        passwordField.setBorder(new ModernBorder(8, BORDER_COLOR));
        passwordField.setBackground(Color.WHITE);
        passwordField.setForeground(TEXT_COLOR);
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(strengthLabel, gbc);

        passwordField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateStrength(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateStrength(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateStrength(); }
            private void updateStrength() {
                String password = passwordField.getText().trim();
                String strength = manager.evaluatePasswordStrength(password);
                strengthLabel.setText("Password Strength: " + strength);
                strengthLabel.setForeground(switch (strength) {
                    case "Strong" -> SUCCESS_COLOR;
                    case "Good" -> PRIMARY_COLOR;
                    case "Moderate" -> new Color(255, 167, 38);
                    default -> DANGER_COLOR;
                });
            }
        });

        int result = JOptionPane.showConfirmDialog(mainFrame, panel, "Save Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String password = passwordField.getText().trim();
            if (name.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "Both fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (password.length() < 6) {
                JOptionPane.showMessageDialog(mainFrame, "Password must be at least 6 characters.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            manager.save(name, password);
            updateDashboard();
            setStatusMessage("Password saved");
        }
    }

    private void generatePasswordDialog() {
        JDialog dialog = new JDialog(mainFrame, "Generate Password", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(500, 600);
        dialog.setMinimumSize(new Dimension(350, 400));
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(BACKGROUND_START);
        dialog.setResizable(true);

        ModernGradientPanel headerPanel = new ModernGradientPanel(PRIMARY_COLOR, SECONDARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel title = new JLabel("Generate Secure Password");
        title.setFont(HEADER_FONT);
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(title);
        dialog.add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel lengthPanel = new JPanel(new GridBagLayout());
        lengthPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel lengthLabel = new JLabel("Password Length");
        lengthLabel.setFont(PRIMARY_FONT);
        lengthLabel.setForeground(TEXT_COLOR);
        lengthLabel.setToolTipText("Select a password length between 8 and 32 characters");
        gbc.gridx = 0;
        gbc.gridy = 0;
        lengthPanel.add(lengthLabel, gbc);

        JSlider lengthSlider = new JSlider(JSlider.HORIZONTAL, 8, 32, 12);
        lengthSlider.setFont(PRIMARY_FONT);
        lengthSlider.setMajorTickSpacing(8);
        lengthSlider.setMinorTickSpacing(1);
        lengthSlider.setPaintTicks(true);
        lengthSlider.setPaintLabels(true);
        lengthSlider.setBackground(Color.WHITE);
        lengthSlider.setForeground(TEXT_COLOR);
        gbc.gridy = 1;
        lengthPanel.add(lengthSlider, gbc);

        JLabel lengthValueLabel = new JLabel("12");
        lengthValueLabel.setFont(PRIMARY_FONT);
        lengthValueLabel.setForeground(TEXT_COLOR);
        gbc.gridy = 2;
        lengthPanel.add(lengthValueLabel, gbc);

        lengthSlider.addChangeListener(e -> lengthValueLabel.setText(String.valueOf(lengthSlider.getValue())));

        JCheckBox upperCaseCheck = new JCheckBox("Include Uppercase Letters", true);
        JCheckBox lowerCaseCheck = new JCheckBox("Include Lowercase Letters", true);
        JCheckBox numbersCheck = new JCheckBox("Include Numbers", true);
        JCheckBox symbolsCheck = new JCheckBox("Include Symbols", true);

        upperCaseCheck.setFont(PRIMARY_FONT);
        lowerCaseCheck.setFont(PRIMARY_FONT);
        numbersCheck.setFont(PRIMARY_FONT);
        symbolsCheck.setFont(PRIMARY_FONT);
        upperCaseCheck.setOpaque(false);
        lowerCaseCheck.setOpaque(false);
        numbersCheck.setOpaque(false);
        symbolsCheck.setOpaque(false);
        upperCaseCheck.setForeground(TEXT_COLOR);
        lowerCaseCheck.setForeground(TEXT_COLOR);
        numbersCheck.setForeground(TEXT_COLOR);
        symbolsCheck.setForeground(TEXT_COLOR);

        JTextField generatedPasswordField = new JTextField(20);
        generatedPasswordField.setFont(MONOSPACE_FONT);
        generatedPasswordField.setEditable(false);
        generatedPasswordField.setBorder(new ModernBorder(8, BORDER_COLOR));
        generatedPasswordField.setBackground(Color.WHITE);
        generatedPasswordField.setForeground(TEXT_COLOR);
        generatedPasswordField.setHorizontalAlignment(JTextField.CENTER);

        JLabel strengthLabel = new JLabel("Password Strength: Not evaluated");
        strengthLabel.setFont(PRIMARY_FONT);
        strengthLabel.setForeground(TEXT_COLOR);
        strengthLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton generateButton = createButton("Generate", PRIMARY_COLOR);
        JButton copyButton = createButton("Copy to Clipboard", SUCCESS_COLOR);
        JButton saveButton = createButton("Save Password", PRIMARY_COLOR);
        JButton closeButton = createButton("Close", DANGER_COLOR);

        formPanel.add(lengthPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(upperCaseCheck);
        formPanel.add(lowerCaseCheck);
        formPanel.add(numbersCheck);
        formPanel.add(symbolsCheck);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(new JLabel("Generated Password:"));
        formPanel.add(generatedPasswordField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(strengthLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.add(generateButton);
        buttonPanel.add(copyButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);

        contentPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(contentPanel, BorderLayout.CENTER);

        generateButton.addActionListener(e -> {
            int length = lengthSlider.getValue();
            boolean useUpper = upperCaseCheck.isSelected();
            boolean useLower = lowerCaseCheck.isSelected();
            boolean useNumbers = numbersCheck.isSelected();
            boolean useSymbols = symbolsCheck.isSelected();

            if (!useUpper && !useLower && !useNumbers && !useSymbols) {
                JOptionPane.showMessageDialog(dialog, "At least one character type must be selected.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String password = generatePassword(length, useUpper, useLower, useNumbers, useSymbols);
            generatedPasswordField.setText(password);
            String strength = manager.evaluatePasswordStrength(password);
            strengthLabel.setText("Password Strength: " + strength);
            strengthLabel.setForeground(switch (strength) {
                case "Strong" -> SUCCESS_COLOR;
                case "Good" -> PRIMARY_COLOR;
                case "Moderate" -> new Color(255, 167, 38);
                default -> DANGER_COLOR;
            });
        });

        copyButton.addActionListener(e -> {
            String password = generatedPasswordField.getText();
            if (!password.isEmpty()) {
                StringSelection stringSelection = new StringSelection(password);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
                JOptionPane.showMessageDialog(dialog, "Password copied to clipboard.", "Success", JOptionPane.INFORMATION_MESSAGE);
                setStatusMessage("Password copied");
            } else {
                JOptionPane.showMessageDialog(dialog, "No password to copy.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        saveButton.addActionListener(e -> {
            String password = generatedPasswordField.getText();
            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Generate a password first.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String name = JOptionPane.showInputDialog(dialog, "Enter a name for this password:", "Save Password", JOptionPane.QUESTION_MESSAGE);
            if (name != null && !name.trim().isEmpty()) {
                manager.save(name.trim(), password);
                updateDashboard();
                setStatusMessage("Password saved");
                JOptionPane.showMessageDialog(dialog, "Password saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else if (name != null) {
                JOptionPane.showMessageDialog(dialog, "Password name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        closeButton.addActionListener(e -> dialog.dispose());

        dialog.getRootPane().setDefaultButton(generateButton);
        dialog.setVisible(true);
    }

    private String generatePassword(int length, boolean useUpper, boolean useLower, boolean useNumbers, boolean useSymbols) {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String symbols = "!@#$%^&*(),.?\":{}|<>";

        StringBuilder chars = new StringBuilder();
        if (useUpper) chars.append(upper);
        if (useLower) chars.append(lower);
        if (useNumbers) chars.append(numbers);
        if (useSymbols) chars.append(symbols);

        Random random = new Random();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    private void deletePasswordDialog() {
        JPasswordField pinField = new JPasswordField(20);
        JPanel pinPanel = buildDialogPanel(new JLabel("Enter your PIN"), pinField);
        int result = JOptionPane.showConfirmDialog(mainFrame, pinPanel, "Verify PIN", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String pin = new String(pinField.getPassword()).trim();
        if (pin.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "PIN is required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!manager.validatePinAndCheckPasswords(pin)) {
            return;
        }

        JDialog dialog = new JDialog(mainFrame, "Delete Password", true);
        dialog.setSize(600, 450);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(BACKGROUND_START);

        Object[][] data = new Object[manager.count][2];
        for (int i = 0; i < manager.count; i++) {
            data[i][0] = manager.getPasswordNames()[i];
            data[i][1] = manager.getPasswords()[i];
        }
        String[] columnNames = {"Password Name", "Encrypted Password"};
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFont(PRIMARY_FONT);
        table.setRowHeight(32);
        table.setBackground(Color.WHITE);
        table.setForeground(TEXT_COLOR);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(BORDER_COLOR);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        table.getTableHeader().setBackground(BACKGROUND_START);
        table.getTableHeader().setForeground(TEXT_COLOR);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(500, 350));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);
        JButton deleteButton = createButton("Delete Selected", PRIMARY_COLOR);
        JButton backButton = createButton("Back", DANGER_COLOR);
        buttonPanel.add(deleteButton);
        buttonPanel.add(backButton);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(mainPanel, BorderLayout.CENTER);

        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Are you sure you want to delete the password '" + manager.getPasswordNames()[selectedRow] + "'?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    manager.deletePassword(selectedRow);
                    dialog.dispose();
                    updateDashboard();
                    setStatusMessage("Password deleted");
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "Please select a password to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        backButton.addActionListener(e -> dialog.dispose());
        dialog.getRootPane().setDefaultButton(deleteButton);
        dialog.setVisible(true);
    }

    private void clearAllPasswordsDialog() {
        JPasswordField pinField = new JPasswordField(20);
        JPanel panel = buildDialogPanel(new JLabel("Enter your PIN to clear all passwords"), pinField);
        int result = JOptionPane.showConfirmDialog(mainFrame, panel, "Clear All Passwords", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String pin = new String(pinField.getPassword()).trim();
        if (pin.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "PIN is required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(mainFrame,
            "Are you sure you want to clear all passwords? This action cannot be undone.",
            "Confirm Clear All",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            manager.clearAllPasswords(pin);
            updateDashboard();
            setStatusMessage("All passwords cleared");
        }
    }

    private void decryptPasswordDialog() {
        JTextField passwordField = new JTextField(20);
        JTextField keyField = new JTextField(20);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel passwordLabel = new JLabel("Encrypted Password");
        passwordLabel.setFont(PRIMARY_FONT);
        passwordLabel.setForeground(TEXT_COLOR);
        panel.add(passwordLabel, gbc);
        passwordLabel.setLabelFor(passwordField);

        gbc.gridx = 1;
        passwordField.setFont(PRIMARY_FONT);
        passwordField.setBorder(new ModernBorder(8, BORDER_COLOR));
        passwordField.setBackground(Color.WHITE);
        passwordField.setForeground(TEXT_COLOR);
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel keyLabel = new JLabel("Decryption Key");
        keyLabel.setFont(PRIMARY_FONT);
        keyLabel.setForeground(TEXT_COLOR);
        panel.add(keyLabel, gbc);
        keyLabel.setLabelFor(keyField);

        gbc.gridx = 1;
        keyField.setFont(PRIMARY_FONT);
        keyField.setBorder(new ModernBorder(8, BORDER_COLOR));
        keyField.setBackground(Color.WHITE);
        keyField.setForeground(TEXT_COLOR);
        panel.add(keyField, gbc);

        int result = JOptionPane.showConfirmDialog(mainFrame, panel, "Decrypt Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String password = passwordField.getText().trim();
            String key = keyField.getText().trim();
            if (password.isEmpty() || key.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "Both fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            manager.decryption(password, key);
            setStatusMessage("Password decrypted");
        }
    }

    private void forgetPinDialog() {
        JLabel questionLabel = new JLabel("Security Question: " + manager.getPersonalQuestion());
        questionLabel.setFont(PRIMARY_FONT);
        questionLabel.setForeground(TEXT_COLOR);
        JTextField answerField = new JTextField(20);
        JPasswordField newPinField = new JPasswordField(20);
        JPasswordField confirmPinField = new JPasswordField(20);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(questionLabel, gbc);

        gbc.gridy = 1;
        JLabel answerLabel = new JLabel("Answer");
        answerLabel.setFont(PRIMARY_FONT);
        answerLabel.setForeground(TEXT_COLOR);
        panel.add(answerLabel, gbc);
        answerLabel.setLabelFor(answerField);

        gbc.gridy = 2;
        answerField.setFont(PRIMARY_FONT);
        answerField.setBorder(new ModernBorder(8, BORDER_COLOR));
        answerField.setBackground(Color.WHITE);
        answerField.setForeground(TEXT_COLOR);
        panel.add(answerField, gbc);

        gbc.gridy = 3;
        JLabel newPinLabel = new JLabel("New PIN");
        newPinLabel.setFont(PRIMARY_FONT);
        newPinLabel.setForeground(TEXT_COLOR);
        panel.add(newPinLabel, gbc);
        newPinLabel.setLabelFor(newPinField);

        gbc.gridy = 4;
        newPinField.setFont(PRIMARY_FONT);
        newPinField.setBorder(new ModernBorder(8, BORDER_COLOR));
        newPinField.setBackground(Color.WHITE);
        newPinField.setForeground(TEXT_COLOR);
        panel.add(newPinField, gbc);

        gbc.gridy = 5;
        JLabel confirmPinLabel = new JLabel("Confirm New PIN");
        confirmPinLabel.setFont(PRIMARY_FONT);
        confirmPinLabel.setForeground(TEXT_COLOR);
        panel.add(confirmPinLabel, gbc);
        confirmPinLabel.setLabelFor(confirmPinField);

        gbc.gridy = 6;
        confirmPinField.setFont(PRIMARY_FONT);
        confirmPinField.setBorder(new ModernBorder(8, BORDER_COLOR));
        confirmPinField.setBackground(Color.WHITE);
        confirmPinField.setForeground(TEXT_COLOR);
        panel.add(confirmPinField, gbc);

        int result = JOptionPane.showConfirmDialog(mainFrame, panel, "Reset PIN", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String answer = answerField.getText().trim();
            String newPin = new String(newPinField.getPassword()).trim();
            String confirmPin = new String(confirmPinField.getPassword()).trim();
            if (answer.isEmpty() || newPin.isEmpty() || confirmPin.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            manager.forget(answer, newPin, confirmPin);
            setStatusMessage("PIN reset");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CipherBox::new);
    }
}