package com.hawolt.ui.login;

import com.hawolt.LeagueClientUI;
import com.hawolt.ui.generic.component.LFlatButton;
import com.hawolt.ui.generic.component.LHintPasswordTextField;
import com.hawolt.ui.generic.component.LHintTextField;
import com.hawolt.ui.generic.component.LTextAlign;
import com.hawolt.ui.generic.themes.ColorPalette;
import com.hawolt.ui.generic.utility.ChildUIComponent;
import com.hawolt.ui.generic.utility.HighlightType;
import com.hawolt.ui.generic.utility.MainUIComponent;
import com.hawolt.util.paint.animation.AnimationVisualizer;
import com.hawolt.util.paint.animation.impl.impl.SpinningAnimation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Created: 06/08/2023 13:10
 * Author: Twitter @hawolt
 **/

public class LoginUI extends MainUIComponent implements ActionListener {

    private final CardLayout cardLayout = new CardLayout();
    private final ChildUIComponent parent = new ChildUIComponent(cardLayout);
    private final AnimationVisualizer animationVisualizer;
    private final LHintPasswordTextField password;
    private final LHintTextField username;
    private final ILoginCallback callback;
    private final JCheckBox rememberMe;
    private final JButton login;

    private LoginUI(LeagueClientUI clientUI) {
        super(clientUI);
        this.setLayout(new BorderLayout());

        SpinningAnimation spinningAnimation = new SpinningAnimation(5, 45);
        this.animationVisualizer = new AnimationVisualizer(spinningAnimation);
        this.parent.add("false", animationVisualizer);
        this.add(parent, BorderLayout.CENTER);
        this.animationVisualizer.start();

        ChildUIComponent login = new ChildUIComponent(new GridLayout(0, 1, 0, 5));
        login.setBorder(new EmptyBorder(5, 5, 5, 5));

        this.username = new LHintTextField("username");
        this.password = new LHintPasswordTextField("password");
        this.login = new LFlatButton("Login", LTextAlign.CENTER, HighlightType.COMPONENT);
        this.login.setActionCommand("REGULAR");
        this.rememberMe = new JCheckBox("Remember Me");
        this.rememberMe.setForeground(Color.WHITE);
        this.rememberMe.setBackground(ColorPalette.backgroundColor);
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setForeground(Color.WHITE);
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setForeground(Color.WHITE);

        login.add(usernameLabel);
        login.add(username);
        login.add(passwordLabel);
        login.add(password);
        login.add(this.login);
        login.add(rememberMe);
        parent.add("true", login);

        this.setPreferredSize(new Dimension(300, 200));
        this.login.addActionListener(this);
        this.container.add(this);

        // Using .setLabelFor() to bind labels to corresponding input fields
        usernameLabel.setLabelFor(username);
        passwordLabel.setLabelFor(password);

        // Enter hook so users can log in with enter
        KeyAdapter enterKeyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() != KeyEvent.VK_ENTER) return;
                actionPerformed(null);
            }
        };

        username.addKeyListener(enterKeyAdapter);
        password.addKeyListener(enterKeyAdapter);

        this.callback = clientUI;
        this.init();
    }

    public static LoginUI create(LeagueClientUI leagueClientUI) {
        return new LoginUI(leagueClientUI);
    }

    public void toggle(boolean state) {
        cardLayout.show(parent, String.valueOf(state));
    }

    public JCheckBox getRememberMe() {
        return rememberMe;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.toggle(false);
        String pass = new String(password.getPassword());
        String user = username.getText();
        LeagueClientUI.service.execute(() -> callback.onLogin(user, pass));
    }

    public AnimationVisualizer getAnimationVisualizer() {
        return animationVisualizer;
    }
}
