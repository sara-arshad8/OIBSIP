import javax.swing.*;

import javax.swing.border.*;

import javax.swing.event.DocumentEvent;

import javax.swing.event.DocumentListener;

import javax.swing.table.DefaultTableModel;

import java.awt.*;

import java.awt.datatransfer.StringSelection;

import java.security.MessageDigest;

import java.sql.*;

import java.text.SimpleDateFormat;

import java.util.Date;

import java.util.UUID;



public class TrainReservationSystem {

    private static final String DB_URL = "jdbc:sqlite:train_reservation.db";

    private static final Color NAVY = new Color(20, 36, 58);

    private static final Color NAVY_LIGHT = new Color(34, 54, 78);

    private static final Color GREEN = new Color(16, 185, 129);

    private static final Color GREEN_DARK = new Color(5, 150, 105);

    private static final Color BG = new Color(243, 247, 251);

    private static final Color WHITE = Color.WHITE;

    private static final Color TEXT = new Color(35, 49, 66);

    private static final Color MUTED = new Color(107, 123, 141);

    private static final Color BORDER = new Color(222, 230, 238);

    private static final Font NORMAL = new Font("Segoe UI", Font.PLAIN, 14);

    private static final Font BOLD = new Font("Segoe UI", Font.BOLD, 14);

    private String currentUser;



    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            try {

                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                UIManager.put("OptionPane.messageFont", NORMAL);

                UIManager.put("OptionPane.buttonFont", BOLD);

                initializeDatabase();

                new TrainReservationSystem().showLogin();

            } catch (Exception ex) {

                ex.printStackTrace();

                JOptionPane.showMessageDialog(null,

                    "Application could not start:\n" + ex.getMessage(),

                    "Startup Error", JOptionPane.ERROR_MESSAGE);

            }

        });

    }
    // DATABASE
    private static Connection connect() throws SQLException {

        return DriverManager.getConnection(DB_URL);

    }



    private static void initializeDatabase() throws Exception {

        Class.forName("org.sqlite.JDBC");



        try (Connection con = connect(); Statement st = con.createStatement()) {

            st.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +

                "username TEXT PRIMARY KEY, password TEXT NOT NULL)");



            st.executeUpdate("CREATE TABLE IF NOT EXISTS trains (" +

                "train_number INTEGER PRIMARY KEY, train_name TEXT NOT NULL)");
    // This schema supports both the current and older database versions.
            st.executeUpdate("CREATE TABLE IF NOT EXISTS reservations (" +

                "pnr TEXT PRIMARY KEY, passenger_name TEXT NOT NULL, " +

                "train_number INTEGER NOT NULL, train_name TEXT NOT NULL, " +

                "class_type TEXT NOT NULL, journey_date TEXT NOT NULL, " +

                "source TEXT NOT NULL DEFAULT '', destination TEXT NOT NULL DEFAULT '', " +

                "source_station TEXT NOT NULL DEFAULT '', " +

                "destination_station TEXT NOT NULL DEFAULT '', " +

                "booked_by TEXT NOT NULL DEFAULT 'admin')");
    // Safely add columns to an existing database without deleting saved bookings.
            ensureColumn(st, "source", "source TEXT NOT NULL DEFAULT ''");

            ensureColumn(st, "destination", "destination TEXT NOT NULL DEFAULT ''");

            ensureColumn(st, "source_station", "source_station TEXT NOT NULL DEFAULT ''");

            ensureColumn(st, "destination_station", "destination_station TEXT NOT NULL DEFAULT ''");

            ensureColumn(st, "booked_by", "booked_by TEXT NOT NULL DEFAULT 'admin'");
    // Original demo admin login is preserved.
            st.executeUpdate("INSERT OR IGNORE INTO users(username,password) " +

                "VALUES('admin','admin123')");



            String[][] trains = {

                {"12001", "Green Line Express"},

                {"12002", "Pakistan Express"},

                {"12003", "Awam Express"},

                {"12004", "Tezgam Express"},

                {"12005", "Karakoram Express"}

            };



            try (PreparedStatement ps = con.prepareStatement(

                    "INSERT OR IGNORE INTO trains(train_number,train_name) VALUES(?,?)")) {

                for (String[] train : trains) {

                    ps.setInt(1, Integer.parseInt(train[0]));

                    ps.setString(2, train[1]);

                    ps.executeUpdate();

                }

            }

        }

    }



    private static void ensureColumn(Statement st, String columnName,

                                     String columnDefinition) throws SQLException {

        boolean found = false;



        try (ResultSet rs = st.executeQuery("PRAGMA table_info(reservations)")) {

            while (rs.next()) {

                if (columnName.equalsIgnoreCase(rs.getString("name"))) {

                    found = true;

                    break;

                }

            }

        }



        if (!found) {

            st.executeUpdate("ALTER TABLE reservations ADD COLUMN " + columnDefinition);

        }

    }
    // REUSABLE UI
    private JFrame createFrame(String title, int width, int height) {

        JFrame f = new JFrame(title);

        f.setSize(width, height);

        f.setMinimumSize(new Dimension(650, 520));

        f.setLocationRelativeTo(null);

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Keep every application screen maximized after navigation.

        f.setExtendedState(JFrame.MAXIMIZED_BOTH);

        return f;

    }



    private JPanel pagePanel() {

        JPanel p = new JPanel();

        p.setBackground(BG);

        p.setBorder(new EmptyBorder(28, 32, 28, 32));

        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        return p;

    }



    private JPanel cardPanel() {

        JPanel p = new JPanel();

        p.setBackground(WHITE);

        p.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true),

            new EmptyBorder(26, 28, 26, 28)));

        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        return p;

    }



    private JLabel heading(String text) {

        JLabel l = new JLabel(text);

        l.setFont(new Font("Segoe UI", Font.BOLD, 26));

        l.setForeground(NAVY);

        l.setAlignmentX(Component.LEFT_ALIGNMENT);

        return l;

    }



    private JLabel label(String text) {

        JLabel l = new JLabel(text);

        l.setFont(new Font("Segoe UI", Font.BOLD, 13));

        l.setForeground(TEXT);

        return l;

    }



    private JTextField textField() {

        JTextField f = new JTextField();

        f.setFont(NORMAL);

        f.setForeground(TEXT);

        f.setCaretColor(GREEN_DARK);

        f.setBackground(WHITE);

        f.setPreferredSize(new Dimension(280, 42));

        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        f.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true),

            new EmptyBorder(8, 12, 8, 12)));

        f.addFocusListener(new java.awt.event.FocusAdapter() {

            public void focusGained(java.awt.event.FocusEvent e) {

                f.setBorder(new CompoundBorder(new LineBorder(GREEN, 2, true),

                    new EmptyBorder(7, 11, 7, 11)));

            }

            public void focusLost(java.awt.event.FocusEvent e) {

                f.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true),

                    new EmptyBorder(8, 12, 8, 12)));

            }

        });

        return f;

    }



    private JPasswordField passwordField() {

        JPasswordField f = new JPasswordField();

        f.setFont(NORMAL);

        f.setForeground(TEXT);

        f.setCaretColor(GREEN_DARK);

        f.setBackground(WHITE);

        f.setPreferredSize(new Dimension(280, 42));

        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        f.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true),

            new EmptyBorder(8, 12, 8, 12)));

        f.addFocusListener(new java.awt.event.FocusAdapter() {

            public void focusGained(java.awt.event.FocusEvent e) {

                f.setBorder(new CompoundBorder(new LineBorder(GREEN, 2, true),

                    new EmptyBorder(7, 11, 7, 11)));

            }

            public void focusLost(java.awt.event.FocusEvent e) {

                f.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true),

                    new EmptyBorder(8, 12, 8, 12)));

            }

        });

        return f;

    }



    private JComboBox<String> comboBox(String[] values) {

        JComboBox<String> c = new JComboBox<>(values);

        c.setFont(NORMAL);

        c.setBackground(WHITE);

        c.setForeground(TEXT);

        c.setPreferredSize(new Dimension(280, 42));

        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        return c;

    }



    private JButton button(String text) {

        JButton b = new JButton(text);

        b.setFont(BOLD);

        b.setForeground(WHITE);

        b.setBackground(GREEN_DARK);

        b.setFocusPainted(false);

        b.setCursor(new Cursor(Cursor.HAND_CURSOR));

        b.setBorder(new EmptyBorder(12, 18, 12, 18));

        b.setOpaque(true);

        b.setContentAreaFilled(true);

        b.setBorderPainted(false);

        b.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseEntered(java.awt.event.MouseEvent e) {

                if (b.isEnabled()) b.setBackground(new Color(4, 120, 87));

            }

            public void mouseExited(java.awt.event.MouseEvent e) {

                if (b.isEnabled()) b.setBackground(GREEN_DARK);

            }

        });

        return b;

    }



    private JButton secondaryButton(String text) {

        JButton b = new JButton(text);

        b.setFont(BOLD);

        b.setForeground(WHITE);

        b.setBackground(NAVY_LIGHT);

        b.setFocusPainted(false);

        b.setCursor(new Cursor(Cursor.HAND_CURSOR));

        b.setBorder(new EmptyBorder(12, 18, 12, 18));

        b.setOpaque(true);

        b.setContentAreaFilled(true);

        b.setBorderPainted(false);

        b.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseEntered(java.awt.event.MouseEvent e) {

                if (b.isEnabled()) b.setBackground(NAVY);

            }

            public void mouseExited(java.awt.event.MouseEvent e) {

                if (b.isEnabled()) b.setBackground(NAVY_LIGHT);

            }

        });

        return b;

    }



    private JButton menuCard(String symbol, String title, String description) {

        JButton b = new JButton();

        b.setLayout(new BorderLayout(0, 12));

        b.setBackground(WHITE);

        b.setFocusPainted(false);

        b.setCursor(new Cursor(Cursor.HAND_CURSOR));

        b.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true),

            new EmptyBorder(20, 20, 20, 20)));



        JLabel icon = new JLabel(symbol, SwingConstants.CENTER);

        icon.setFont(new Font("Segoe UI Symbol", Font.BOLD, 28));

        icon.setForeground(GREEN_DARK);

        icon.setOpaque(true);

        icon.setBackground(new Color(226, 250, 241));

        icon.setPreferredSize(new Dimension(58, 58));



        JLabel titleLabel = new JLabel(title);

        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));

        titleLabel.setForeground(NAVY);



        JLabel desc = new JLabel("<html><div style='width:210px'>" +

            description + "</div></html>");

        desc.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        desc.setForeground(MUTED);



        JPanel words = new JPanel();

        words.setBackground(WHITE);

        words.setLayout(new BoxLayout(words, BoxLayout.Y_AXIS));

        words.add(titleLabel);

        words.add(Box.createVerticalStrut(7));

        words.add(desc);



        b.add(icon, BorderLayout.NORTH);

        b.add(words, BorderLayout.CENTER);

        b.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseEntered(java.awt.event.MouseEvent e) {

                b.setBackground(new Color(246, 253, 250));

                words.setBackground(new Color(246, 253, 250));

                b.setBorder(new CompoundBorder(new LineBorder(GREEN, 2, true),

                    new EmptyBorder(19, 19, 19, 19)));

            }

            public void mouseExited(java.awt.event.MouseEvent e) {

                b.setBackground(WHITE);

                words.setBackground(WHITE);

                b.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true),

                    new EmptyBorder(20, 20, 20, 20)));

            }

        });

        return b;

    }



    private void addField(JPanel panel, String title, JComponent field) {

    JLabel l = label(title);

    l.setAlignmentX(Component.LEFT_ALIGNMENT);

    field.setAlignmentX(Component.LEFT_ALIGNMENT);



    Dimension size = new Dimension(280, 34);

    field.setPreferredSize(size);

    field.setMaximumSize(

        new Dimension(Integer.MAX_VALUE, 34)

    );



    panel.add(l);

    panel.add(Box.createVerticalStrut(4));

    panel.add(field);

    panel.add(Box.createVerticalStrut(8));

}



    private JPanel formHeader(String title, String subtitle) {

        JPanel p = new JPanel();

        p.setBackground(BG);

        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel h = heading(title);

        JLabel s = new JLabel(subtitle);

        s.setFont(NORMAL);

        s.setForeground(MUTED);

        s.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(h);

        p.add(Box.createVerticalStrut(7));

        p.add(s);

        return p;

    }



    private void showMessage(String message) {

        JOptionPane.showMessageDialog(null, message, "Railway Reservation",

            JOptionPane.INFORMATION_MESSAGE);

    }



    private void showError(String message) {

        JOptionPane.showMessageDialog(null, message, "Please Check",

            JOptionPane.ERROR_MESSAGE);

    }



    private String hashPassword(String password) throws Exception {

        MessageDigest md = MessageDigest.getInstance("SHA-256");

        byte[] bytes = md.digest(password.getBytes("UTF-8"));

        StringBuilder result = new StringBuilder();

        for (byte b : bytes) result.append(String.format("%02x", b & 0xff));

        return result.toString();

    }



    private boolean passwordMatches(String entered, String saved) throws Exception {

        return saved.equals(entered) || saved.equals(hashPassword(entered));

    }



    private void styleTable(JTable table) {

        table.setFont(NORMAL);

        table.setForeground(TEXT);

        table.setBackground(WHITE);

        table.setRowHeight(34);

        table.setGridColor(BORDER);

        table.setSelectionBackground(new Color(209, 250, 229));

        table.setSelectionForeground(NAVY);

        table.setShowVerticalLines(false);

        table.setIntercellSpacing(new Dimension(8, 0));

        table.getTableHeader().setFont(BOLD);

        table.getTableHeader().setBackground(NAVY);

        table.getTableHeader().setForeground(WHITE);

        table.getTableHeader().setPreferredSize(new Dimension(0, 42));

        table.getTableHeader().setReorderingAllowed(false);

    }
    // LOGIN
    private void showLogin() {

        JFrame frame = createFrame("Railway | Sign In", 940, 650);

        frame.setMinimumSize(new Dimension(800, 580));

        JPanel root = new JPanel(new GridLayout(1, 2));

        root.setBackground(BG);



        JPanel hero = new JPanel();

        hero.setBackground(NAVY);

        hero.setBorder(new EmptyBorder(55, 42, 45, 35));

        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));



        JLabel brand = new JLabel("RAILWAY");

        brand.setFont(new Font("Segoe UI", Font.BOLD, 32));

        brand.setForeground(GREEN);

        brand.setAlignmentX(Component.LEFT_ALIGNMENT);



        JLabel system = new JLabel("RESERVATION SYSTEM");

        system.setFont(new Font("Segoe UI", Font.BOLD, 13));

        system.setForeground(new Color(202, 218, 231));

        system.setAlignmentX(Component.LEFT_ALIGNMENT);



        JLabel heroTitle = new JLabel("<html>Your journey,<br>made simpler.</html>");

        heroTitle.setFont(new Font("Segoe UI", Font.BOLD, 31));

        heroTitle.setForeground(WHITE);

        heroTitle.setAlignmentX(Component.LEFT_ALIGNMENT);



        JLabel heroText = new JLabel("<html>Book train tickets, keep track of your<br>" +

            "PNR, and manage your journeys in one place.</html>");

        heroText.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        heroText.setForeground(new Color(195, 208, 222));

        heroText.setAlignmentX(Component.LEFT_ALIGNMENT);



        JLabel railSymbol = new JLabel("↔");

        railSymbol.setFont(new Font("Segoe UI", Font.BOLD, 78));

        railSymbol.setForeground(GREEN);

        railSymbol.setAlignmentX(Component.LEFT_ALIGNMENT);



        hero.add(brand);

        hero.add(Box.createVerticalStrut(4));

        hero.add(system);

        hero.add(Box.createVerticalGlue());

        hero.add(railSymbol);

        hero.add(Box.createVerticalStrut(22));

        hero.add(heroTitle);

        hero.add(Box.createVerticalStrut(16));

        hero.add(heroText);

        hero.add(Box.createVerticalGlue());



        JPanel right = new JPanel(new GridBagLayout());

        right.setBackground(BG);

        JPanel card = cardPanel();

        card.setPreferredSize(new Dimension(355, 450));



        JLabel title = heading("Welcome back");

        JLabel subtitle = new JLabel("Sign in to continue to your account");

        subtitle.setFont(NORMAL);

        subtitle.setForeground(MUTED);

        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);



        JTextField username = textField();

        JPasswordField password = passwordField();

        JButton login = button("Sign In");

        JButton signup = secondaryButton("Create New Account");

        login.setAlignmentX(Component.LEFT_ALIGNMENT);

        signup.setAlignmentX(Component.LEFT_ALIGNMENT);



        card.add(title);

        card.add(Box.createVerticalStrut(8));

        card.add(subtitle);

        card.add(Box.createVerticalStrut(26));

        addField(card, "Username", username);

        addField(card, "Password", password);

        card.add(Box.createVerticalStrut(3));

        card.add(login);

        card.add(Box.createVerticalStrut(12));

        card.add(signup);

        card.add(Box.createVerticalStrut(18));



        JLabel hint = new JLabel("<html><font color='#6B7B8D'>" +

            "Demo account: admin / admin123</font></html>");

        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(hint);



        right.add(card);

        root.add(hero);

        root.add(right);

        frame.setContentPane(root);



        login.addActionListener(e -> {

            String u = username.getText().trim();

            String p = new String(password.getPassword());

            if (u.isEmpty() || p.isEmpty()) {

                showError("Please enter both username and password.");

                return;

            }

            try (Connection con = connect();

                 PreparedStatement ps = con.prepareStatement(

                     "SELECT password FROM users WHERE username=?")) {

                ps.setString(1, u);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next() && passwordMatches(p, rs.getString("password"))) {

                        currentUser = u;

                        frame.dispose();

                        showMainMenu();

                    } else {

                        showError("Invalid username or password.");

                    }

                }

            } catch (Exception ex) {

                showError("Login failed:\n" + ex.getMessage());

            }

        });

        password.addActionListener(e -> login.doClick());

        signup.addActionListener(e -> {

            frame.dispose();

            showSignup();

        });

        frame.setVisible(true);

    }
    // SIGN UP
    private void showSignup() {

        JFrame frame = createFrame("Railway | Create Account", 900, 650);

        frame.setMinimumSize(new Dimension(760, 570));

        JPanel root = new JPanel(new GridLayout(1, 2));

        root.setBackground(BG);



        JPanel hero = new JPanel();

        hero.setBackground(NAVY);

        hero.setBorder(new EmptyBorder(55, 38, 40, 32));

        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));



        JLabel brand = new JLabel("JOIN RAILWAY");

        brand.setFont(new Font("Segoe UI", Font.BOLD, 28));

        brand.setForeground(GREEN);

        brand.setAlignmentX(Component.LEFT_ALIGNMENT);



        JLabel message = new JLabel("<html>Create your account<br>and travel with ease.</html>");

        message.setFont(new Font("Segoe UI", Font.BOLD, 28));

        message.setForeground(WHITE);

        message.setAlignmentX(Component.LEFT_ALIGNMENT);



        JLabel desc = new JLabel("<html>Your bookings and PNR details will be<br>" +

            "saved with your account.</html>");

        desc.setFont(NORMAL);

        desc.setForeground(new Color(195, 208, 222));

        desc.setAlignmentX(Component.LEFT_ALIGNMENT);



        hero.add(brand);

        hero.add(Box.createVerticalGlue());

        hero.add(message);

        hero.add(Box.createVerticalStrut(18));

        hero.add(desc);

        hero.add(Box.createVerticalGlue());



        JPanel right = new JPanel(new GridBagLayout());

        right.setBackground(BG);

        JPanel card = cardPanel();

        card.setPreferredSize(new Dimension(360, 455));



        JLabel title = heading("Create account");

        JLabel subtitle = new JLabel("Enter your details to get started");

        subtitle.setFont(NORMAL);

        subtitle.setForeground(MUTED);

        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);



        JTextField username = textField();

        JPasswordField password = passwordField();

        JPasswordField confirm = passwordField();

        JButton register = button("Create Account");

        JButton back = secondaryButton("Back to Sign In");

        register.setAlignmentX(Component.LEFT_ALIGNMENT);

        back.setAlignmentX(Component.LEFT_ALIGNMENT);



        card.add(title);

        card.add(Box.createVerticalStrut(7));

        card.add(subtitle);

        card.add(Box.createVerticalStrut(21));

        addField(card, "Choose Username (minimum 3 characters)", username);

        addField(card, "Choose Password (minimum 6 characters)", password);

        addField(card, "Confirm Password", confirm);

        card.add(Box.createVerticalStrut(2));

        card.add(register);

        card.add(Box.createVerticalStrut(12));

        card.add(back);



        right.add(card);

        root.add(hero);

        root.add(right);

        frame.setContentPane(root);



        register.addActionListener(e -> {

            String u = username.getText().trim();

            String p = new String(password.getPassword());

            String c = new String(confirm.getPassword());



            if (u.isEmpty() || p.isEmpty() || c.isEmpty()) {

                showError("Please fill in all fields.");

                return;

            }

            if (u.length() < 3) {

                showError("Username must contain at least 3 characters.");

                return;

            }

            if (u.equalsIgnoreCase("admin")) {

                showError("This username is reserved.");

                return;

            }

            if (p.length() < 6) {

                showError("Password must contain at least 6 characters.");

                return;

            }

            if (!p.equals(c)) {

                showError("Passwords do not match.");

                return;

            }

            try (Connection con = connect();

                 PreparedStatement ps = con.prepareStatement(

                     "INSERT INTO users(username,password) VALUES(?,?)")) {

                ps.setString(1, u);

                ps.setString(2, hashPassword(p));

                ps.executeUpdate();

                showMessage("Account created successfully!\nYou can now sign in.");

                frame.dispose();

                showLogin();

            } catch (SQLException ex) {

                if (ex.getMessage().toLowerCase().contains("unique")) {

                    showError("This username already exists. Choose another.");

                } else {

                    showError("Could not create account:\n" + ex.getMessage());

                }

            } catch (Exception ex) {

                showError("Could not create account:\n" + ex.getMessage());

            }

        });

        back.addActionListener(e -> {

            frame.dispose();

            showLogin();

        });

        frame.setVisible(true);

    }
    // DASHBOARD
    private void showMainMenu() {

        JFrame frame = createFrame("Railway | Dashboard", 1050, 700);

        frame.setMinimumSize(new Dimension(850, 600));

        JPanel root = new JPanel(new BorderLayout());

        root.setBackground(BG);



        JPanel sidebar = new JPanel();

        sidebar.setPreferredSize(new Dimension(235, 0));

        sidebar.setBackground(NAVY);

        sidebar.setBorder(new EmptyBorder(28, 22, 25, 22));

        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));



        JLabel brand = new JLabel("RAILWAY");

        brand.setFont(new Font("Segoe UI", Font.BOLD, 28));

        brand.setForeground(GREEN);

        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brandSub = new JLabel("RESERVATION SYSTEM");

        brandSub.setFont(new Font("Segoe UI", Font.BOLD, 10));

        brandSub.setForeground(new Color(180, 197, 212));

        brandSub.setAlignmentX(Component.LEFT_ALIGNMENT);



        sidebar.add(brand);

        sidebar.add(Box.createVerticalStrut(3));

        sidebar.add(brandSub);

        sidebar.add(Box.createVerticalStrut(48));



        JLabel signedIn = new JLabel("<html><font color='#A8B8C9' size='3'>" +

            "SIGNED IN AS</font><br><br><font color='#FFFFFF' size='5'><b>" +

            currentUser + "</b></font></html>");

        signedIn.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(signedIn);

        sidebar.add(Box.createVerticalGlue());



        JLabel tagline = new JLabel("Your journey, simplified.");

        tagline.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        tagline.setForeground(new Color(151, 171, 191));

        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(tagline);



        JPanel content = new JPanel(new BorderLayout(0, 25));

        content.setBackground(BG);

        content.setBorder(new EmptyBorder(38, 40, 30, 40));



        JPanel header = new JPanel();

        header.setBackground(BG);

        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel welcome = new JLabel("Welcome back, " + currentUser + "!");

        welcome.setFont(new Font("Segoe UI", Font.BOLD, 30));

        welcome.setForeground(NAVY);

        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Manage your train journeys from one place.");

        sub.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        sub.setForeground(MUTED);

        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(welcome);

        header.add(Box.createVerticalStrut(8));

        header.add(sub);



        JPanel center = new JPanel(new BorderLayout(0, 18));

        center.setBackground(BG);

        JLabel section = new JLabel("What would you like to do?");

        section.setFont(new Font("Segoe UI", Font.BOLD, 19));

        section.setForeground(TEXT);

        center.add(section, BorderLayout.NORTH);



        JPanel cards = new JPanel(new GridLayout(2, 2, 18, 18));

        cards.setBackground(BG);

        JButton book = menuCard("↗", "Book a Ticket",

            "Plan your journey and reserve a seat.");

        JButton myBookings = menuCard("▤", "My Bookings",

            "View saved reservations and retrieve your PNR.");

        JButton cancel = menuCard("×", "Cancel a Ticket",

            "Find a booking and request cancellation.");

        JButton logout = menuCard("⇥", "Logout",

            "Safely sign out of your account.");

        cards.add(book);

        cards.add(myBookings);

        cards.add(cancel);

        cards.add(logout);

        center.add(cards, BorderLayout.CENTER);



        JLabel footer = new JLabel("RAILWAY RESERVATION  •  SIMPLE  •  CONVENIENT");

        footer.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        footer.setForeground(MUTED);



        content.add(header, BorderLayout.NORTH);

        content.add(center, BorderLayout.CENTER);

        content.add(footer, BorderLayout.SOUTH);

        root.add(sidebar, BorderLayout.WEST);

        root.add(content, BorderLayout.CENTER);

        frame.setContentPane(root);



        book.addActionListener(e -> {

            frame.dispose();

            showReservationForm();

        });

        myBookings.addActionListener(e -> showMyBookings());

        cancel.addActionListener(e -> showCancellationForm());

        logout.addActionListener(e -> {

            frame.dispose();

            currentUser = null;

            showLogin();

        });

        frame.setVisible(true);

    }



    private void returnToMenu(JFrame frame) {

        frame.dispose();

        showMainMenu();

    }
    // BOOK TICKET
    private void showReservationForm() {

        JFrame frame = createFrame("Railway | Book a Ticket", 850, 800);

        frame.setMinimumSize(new Dimension(700, 650));



        JPanel outer = new JPanel(new BorderLayout());

        outer.setBackground(BG);

        JPanel content = pagePanel();

        content.add(formHeader("Book a Ticket",

            "Fill in your journey details. Your PNR will be generated automatically."));

        content.add(Box.createVerticalStrut(25));



        JPanel form = cardPanel();

        form.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField passenger = textField();

        JComboBox<String> trainSelection = new JComboBox<>();

        trainSelection.addItem("Select a train...");

        trainSelection.setFont(NORMAL);

        trainSelection.setBackground(WHITE);

        trainSelection.setForeground(TEXT);

        trainSelection.setPreferredSize(new Dimension(280, 42));

        trainSelection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));



        JTextField trainName = textField();

        trainName.setEditable(false);

        trainName.setBackground(new Color(239, 244, 247));

        trainName.setForeground(MUTED);
    // Load train numbers and names from the database for easy selection.
        try (Connection con = connect();

             PreparedStatement ps = con.prepareStatement(

                 "SELECT train_number, train_name FROM trains ORDER BY train_number");

             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                trainSelection.addItem(rs.getInt("train_number") + " - " +

                    rs.getString("train_name"));

            }

        } catch (Exception ex) {

            showError("Could not load trains:\n" + ex.getMessage());

        }

        JComboBox<String> classType = comboBox(new String[] {

            "Economy", "AC Standard", "AC Business", "First Class"

        });

        JTextField date = textField();

        date.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

        JTextField source = textField();

        JTextField destination = textField();



        addField(form, "Passenger Name", passenger);

        addField(form, "Select Train (number - name)", trainSelection);

        addField(form, "Train Name (auto-filled)", trainName);

        addField(form, "Class Type", classType);

        addField(form, "Journey Date (yyyy-MM-dd)", date);

        addField(form, "Source Station", source);

        addField(form, "Destination Station", destination);



        trainSelection.addActionListener(e -> {

            String selected = (String) trainSelection.getSelectedItem();

            if (selected == null || selected.equals("Select a train...")) {

                trainName.setText("");

            } else {

                int separator = selected.indexOf(" - ");

                trainName.setText(separator >= 0 ? selected.substring(separator + 3) : "");

            }

        });



        content.add(form);

        content.add(Box.createVerticalStrut(20));

        JButton book = button("Confirm Booking");

        JButton back = secondaryButton("Back to Dashboard");



        JScrollPane scroll = new JScrollPane(content);

scroll.setBorder(null);

scroll.getViewport().setBackground(BG);

scroll.getVerticalScrollBar().setUnitIncrement(16);



outer.add(scroll, BorderLayout.CENTER);



JPanel bottomPanel = new JPanel(

    new FlowLayout(FlowLayout.RIGHT, 12, 12)

);

bottomPanel.setBackground(BG);

bottomPanel.setBorder(new EmptyBorder(4, 20, 8, 20));



bottomPanel.add(book);

bottomPanel.add(back);



outer.add(bottomPanel, BorderLayout.SOUTH);

        frame.setContentPane(outer);



        book.addActionListener(e -> {

            String name = passenger.getText().trim();

            String selectedTrain = (String) trainSelection.getSelectedItem();

            String number = "";

            if (selectedTrain != null && !selectedTrain.equals("Select a train...")) {

                int separator = selectedTrain.indexOf(" - ");

                if (separator > 0) {

                    number = selectedTrain.substring(0, separator).trim();

                }

            }

            String tName = trainName.getText().trim();

            String journeyDate = date.getText().trim();

            String from = source.getText().trim();

            String to = destination.getText().trim();



            if (name.isEmpty() || number.isEmpty() || journeyDate.isEmpty()

                    || from.isEmpty() || to.isEmpty()) {

                showError("Please fill in all required fields.");

                return;

            }

            if (number.isEmpty() || tName.isEmpty()) {

                showError("Please select a train from the list.");

                return;

            }

            try {

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                sdf.setLenient(false);

                Date parsed = sdf.parse(journeyDate);

                if (!sdf.format(parsed).equals(journeyDate)) {

                    showError("Date must use yyyy-MM-dd format.");

                    return;

                }

            } catch (Exception ex) {

                showError("Invalid date. Example: 2026-12-25");

                return;

            }



            String pnr = UUID.randomUUID().toString()

                .replace("-", "").substring(0, 12).toUpperCase();

            try (Connection con = connect();

                 PreparedStatement ps = con.prepareStatement(

                     "INSERT INTO reservations " +

                     "(pnr, passenger_name, train_number, train_name, " +

                     "class_type, journey_date, source, destination, " +

                     "source_station, destination_station, booked_by) " +

                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

                ps.setString(1, pnr);

                ps.setString(2, name);

                ps.setInt(3, Integer.parseInt(number));

                ps.setString(4, tName);

                ps.setString(5, classType.getSelectedItem().toString());

                ps.setString(6, journeyDate);

                ps.setString(7, from);

                ps.setString(8, to);

                ps.setString(9, from);

                ps.setString(10, to);

                ps.setString(11, currentUser);

                ps.executeUpdate();



                frame.dispose();

                showBookingConfirmation(pnr, name, number, tName,

                    classType.getSelectedItem().toString(), journeyDate, from, to);

            } catch (Exception ex) {

                showError("Could not save booking:\n" + ex.getMessage());

            }

        });

        back.addActionListener(e -> returnToMenu(frame));

        frame.setVisible(true);

    }
    // BOOKING CONFIRMATION
    private void showBookingConfirmation(String pnr, String passenger,

            String trainNumber, String trainName, String travelClass,

            String date, String source, String destination) {

        JFrame frame = createFrame("Railway | Booking Confirmed", 650, 660);

        frame.setMinimumSize(new Dimension(580, 580));

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);



        JPanel root = new JPanel(new BorderLayout(15, 15));

        root.setBackground(BG);

        root.setBorder(new EmptyBorder(26, 30, 26, 30));



        JPanel ticket = new JPanel();

        ticket.setBackground(WHITE);

        ticket.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true),

            new EmptyBorder(25, 28, 25, 28)));

        ticket.setLayout(new BoxLayout(ticket, BoxLayout.Y_AXIS));



        JLabel success = new JLabel("✓  BOOKING CONFIRMED");

        success.setFont(new Font("Segoe UI", Font.BOLD, 14));

        success.setForeground(GREEN_DARK);

        success.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Your journey is booked!");

        title.setFont(new Font("Segoe UI", Font.BOLD, 26));

        title.setForeground(NAVY);

        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel pnrCaption = new JLabel("YOUR BOOKING REFERENCE (PNR)");

        pnrCaption.setFont(new Font("Segoe UI", Font.BOLD, 11));

        pnrCaption.setForeground(MUTED);

        pnrCaption.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel pnrLabel = new JLabel(pnr);

        pnrLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));

        pnrLabel.setForeground(GREEN_DARK);

        pnrLabel.setAlignmentX(Component.LEFT_ALIGNMENT);



        ticket.add(success);

        ticket.add(Box.createVerticalStrut(14));

        ticket.add(title);

        ticket.add(Box.createVerticalStrut(22));

        ticket.add(pnrCaption);

        ticket.add(Box.createVerticalStrut(5));

        ticket.add(pnrLabel);

        ticket.add(Box.createVerticalStrut(8));



        JLabel reminder = new JLabel("<html>Save your PNR or use <b>My Bookings</b> " +

            "to find it later.</html>");

        reminder.setFont(NORMAL);

        reminder.setForeground(MUTED);

        reminder.setAlignmentX(Component.LEFT_ALIGNMENT);

        ticket.add(reminder);

        ticket.add(Box.createVerticalStrut(22));

        ticket.add(new JSeparator());

        ticket.add(Box.createVerticalStrut(18));



        String[][] details = {

            {"Passenger", passenger}, {"Train", trainNumber + " - " + trainName},

            {"Class", travelClass}, {"Journey Date", date},

            {"From", source}, {"To", destination}

        };

        for (String[] item : details) {

            JPanel row = new JPanel(new BorderLayout(10, 0));

            row.setBackground(WHITE);

            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

            JLabel key = new JLabel(item[0]);

            key.setFont(NORMAL);

            key.setForeground(MUTED);

            JLabel value = new JLabel(item[1]);

            value.setFont(BOLD);

            value.setForeground(TEXT);

            row.add(key, BorderLayout.WEST);

            row.add(value, BorderLayout.EAST);

            ticket.add(row);

            ticket.add(Box.createVerticalStrut(9));

        }



        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        actions.setBackground(BG);

        JButton copy = button("Copy PNR");

        JButton close = secondaryButton("Back to Dashboard");

        actions.add(copy);

        actions.add(close);

        root.add(ticket, BorderLayout.CENTER);

        root.add(actions, BorderLayout.SOUTH);

        frame.setContentPane(root);



        copy.addActionListener(e -> {

            try {

                Toolkit.getDefaultToolkit().getSystemClipboard()

                    .setContents(new StringSelection(pnr), null);

                showMessage("PNR copied to clipboard:\n" + pnr);

            } catch (Exception ex) {

                showError("Please copy your PNR manually:\n" + pnr);

            }

        });

        close.addActionListener(e -> {

            frame.dispose();

            showMainMenu();

        });

        frame.setVisible(true);

    }
    // MY BOOKINGS
    private void showMyBookings() {

        JFrame frame = createFrame("Railway | My Bookings", 1120, 580);

        frame.setMinimumSize(new Dimension(850, 500));

        JPanel root = new JPanel(new BorderLayout(16, 16));

        root.setBackground(BG);

        root.setBorder(new EmptyBorder(26, 28, 26, 28));

        root.add(formHeader("My Bookings",

            "Your saved reservations and PNR numbers appear here."), BorderLayout.NORTH);



        String[] columns = {"PNR", "Passenger", "Train No.", "Train Name",

            "Class", "Date", "Source", "Destination"};

        DefaultTableModel model = new DefaultTableModel(columns, 0) {

            public boolean isCellEditable(int row, int col) { return false; }

        };

        JTable table = new JTable(model);

        styleTable(table);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);



        Runnable loadBookings = () -> {

            model.setRowCount(0);

            try (Connection con = connect();

                 PreparedStatement ps = con.prepareStatement(

                     "SELECT pnr,passenger_name,train_number,train_name,class_type," +

                     "journey_date,source,destination FROM reservations " +

                     "WHERE booked_by=? ORDER BY rowid DESC")) {

                ps.setString(1, currentUser);

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {

                        model.addRow(new Object[] {

                            rs.getString("pnr"), rs.getString("passenger_name"),

                            rs.getString("train_number"), rs.getString("train_name"),

                            rs.getString("class_type"), rs.getString("journey_date"),

                            rs.getString("source"), rs.getString("destination")

                        });

                    }

                }

            } catch (Exception ex) {

                showError("Could not load bookings:\n" + ex.getMessage());

            }

        };

        loadBookings.run();



        JScrollPane scroll = new JScrollPane(table);

        scroll.setBorder(new LineBorder(BORDER, 1, true));

        scroll.getViewport().setBackground(WHITE);

        root.add(scroll, BorderLayout.CENTER);



        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        actions.setBackground(BG);

        JButton copy = button("Copy Selected PNR");

        JButton refresh = secondaryButton("Refresh");

        JButton close = secondaryButton("Close");

        actions.add(copy);

        actions.add(refresh);

        actions.add(close);

        root.add(actions, BorderLayout.SOUTH);



        copy.addActionListener(e -> {

            int selected = table.getSelectedRow();

            if (selected < 0) {

                showError("Please select a booking first.");

                return;

            }

            String pnr = model.getValueAt(selected, 0).toString();

            try {

                Toolkit.getDefaultToolkit().getSystemClipboard()

                    .setContents(new StringSelection(pnr), null);

                showMessage("PNR copied: " + pnr);

            } catch (Exception ex) {

                showError("Your PNR is:\n" + pnr);

            }

        });

        refresh.addActionListener(e -> loadBookings.run());

        close.addActionListener(e -> frame.dispose());

        frame.setContentPane(root);

        frame.setVisible(true);



        if (model.getRowCount() == 0) {

            SwingUtilities.invokeLater(() ->

                showMessage("No bookings found for your account yet."));

        }

    }
    // CANCEL TICKET
    private void showCancellationForm() {

        JFrame frame = createFrame("Railway | Cancel Ticket", 800, 700);

        frame.setMinimumSize(new Dimension(680, 590));

        JPanel outer = new JPanel(new BorderLayout());

        outer.setBackground(BG);



        JPanel content = pagePanel();

        content.add(formHeader("Cancel a Ticket",

            "Fetch your reservation by PNR before confirming cancellation."));

        content.add(Box.createVerticalStrut(24));



        JPanel card = cardPanel();

        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField pnrField = textField();

        JButton fetch = button("Fetch Booking");

        JPanel pnrRow = new JPanel(new BorderLayout(10, 0));

        pnrRow.setBackground(WHITE);

        pnrRow.add(pnrField, BorderLayout.CENTER);

        pnrRow.add(fetch, BorderLayout.EAST);

        addField(card, "PNR Number", pnrRow);



        JLabel detailsTitle = label("Booking Details");

        detailsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(detailsTitle);

        card.add(Box.createVerticalStrut(9));



        JTextArea details = new JTextArea(8, 34);

        details.setEditable(false);

        details.setFont(NORMAL);

        details.setForeground(TEXT);

        details.setLineWrap(true);

        details.setWrapStyleWord(true);

        details.setBackground(new Color(247, 250, 252));

        details.setBorder(new EmptyBorder(14, 14, 14, 14));

        JScrollPane detailsScroll = new JScrollPane(details);

        detailsScroll.setPreferredSize(new Dimension(400, 190));

        detailsScroll.setBorder(new LineBorder(BORDER, 1, true));

        card.add(detailsScroll);



        content.add(card);

        content.add(Box.createVerticalStrut(20));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        actions.setBackground(BG);

        actions.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton cancel = button("Confirm Cancellation");

        JButton back = secondaryButton("Back to Dashboard");

        actions.add(cancel);

        actions.add(back);

        content.add(actions);



        JScrollPane outerScroll = new JScrollPane(content);

        outerScroll.setBorder(null);

        outerScroll.getViewport().setBackground(BG);

        outerScroll.getVerticalScrollBar().setUnitIncrement(16);

        outer.add(outerScroll, BorderLayout.CENTER);

        frame.setContentPane(outer);



        final String[] fetchedPnr = {null};

        fetch.addActionListener(e -> {

            String pnr = pnrField.getText().trim().toUpperCase();

            if (pnr.isEmpty()) {

                showError("Please enter a PNR.");

                return;

            }

            try (Connection con = connect();

                 PreparedStatement ps = con.prepareStatement(

                     "SELECT * FROM reservations WHERE pnr=? AND booked_by=?")) {

                ps.setString(1, pnr);

                ps.setString(2, currentUser);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        fetchedPnr[0] = pnr;

                        details.setText(

                            "PNR: " + rs.getString("pnr") + "\n\n" +

                            "Passenger: " + rs.getString("passenger_name") + "\n" +

                            "Train: " + rs.getString("train_number") + " - " +

                                rs.getString("train_name") + "\n" +

                            "Class: " + rs.getString("class_type") + "\n" +

                            "Journey Date: " + rs.getString("journey_date") + "\n" +

                            "Source: " + rs.getString("source") + "\n" +

                            "Destination: " + rs.getString("destination"));

                    } else {

                        fetchedPnr[0] = null;

                        details.setText("");

                        showError("Booking not found for your account. Check the PNR.");

                    }

                }

            } catch (Exception ex) {

                showError("Could not fetch booking:\n" + ex.getMessage());

            }

        });



        cancel.addActionListener(e -> {

            if (fetchedPnr[0] == null) {

                showError("Please fetch a valid booking first.");

                return;

            }

            int choice = JOptionPane.showConfirmDialog(frame,

                "Are you sure you want to cancel this ticket?\nThis action cannot be undone.",

                "Confirm Cancellation", JOptionPane.YES_NO_OPTION,

                JOptionPane.WARNING_MESSAGE);

            if (choice != JOptionPane.YES_OPTION) return;



            try (Connection con = connect();

                 PreparedStatement ps = con.prepareStatement(

                     "DELETE FROM reservations WHERE pnr=? AND booked_by=?")) {

                ps.setString(1, fetchedPnr[0]);

                ps.setString(2, currentUser);

                int deleted = ps.executeUpdate();

                if (deleted > 0) {

                    showMessage("Ticket cancelled successfully.");

                    frame.dispose();

                    showMainMenu();

                } else {

                    showError("Booking was not found or was already cancelled.");

                }

            } catch (Exception ex) {

                showError("Cancellation failed:\n" + ex.getMessage());

            }

        });



        back.addActionListener(e -> returnToMenu(frame));

        frame.setVisible(true);

    }

}
