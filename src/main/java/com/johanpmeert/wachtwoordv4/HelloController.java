package com.johanpmeert.wachtwoordv4;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.security.SecureRandom;

public class HelloController {

    private enum BUILDUP {
        LETTERS("abcdefghijkmnopqrstuvwxyzABCDEFGHIJKLMNPQRSTUVWXYZ"),
        DIGITS("1234567890"),
        SPECIAL("&#$-+*%!?@<>°(){}£="),
        UPPERCASE("ABCDEFGHIJKLMNOPQRSTUVWXYZ"),
        LOWERCASE("abcdefghijklmnopqrstuvwxyz");

        public final String content;

        BUILDUP(String content) {
            this.content = content;
        }
    }

    private enum POSSIBILITIES {
        LETTERS, LETTERS_DIGITS, LETTERS_DIGITS_SPECIALS;
    }

    @FXML
    private Label entropyScore;

    @FXML
    private ChoiceBox<String> passwordBuildup;

    @FXML
    private ChoiceBox<Integer> passwordLength;

    @FXML
    private ChoiceBox<Integer> specials;

    @FXML
    private TextFlow gpTextFlow; // this supports showing text in color to improve password readability

    @FXML
    protected void onGenerateButtonClick() {
        // generate new password
        String password = createPassword(POSSIBILITIES.valueOf(passwordBuildup.getValue()), passwordLength.getValue(), specials.getValue());
        // show password
        gpTextFlow.getChildren().clear(); // delete previous password
        convertToColors(gpTextFlow, password);
        // copy password to clipboard
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(password);
        clipboard.setContent(content);
    }

    @FXML
    protected void onQuitButtonClick() {
        System.exit(0);
    }

    @FXML
    protected void calculateEntropyScore() {
        // runs every time when Length or content are changed
        String pos = passwordBuildup.getValue();
        if (pos != null) {  // null only possible during startup, skip calculation
            POSSIBILITIES choice = POSSIBILITIES.valueOf(pos);
            int passwordVariationLength = switch (choice) {
                case LETTERS -> BUILDUP.LETTERS.content.length();
                case LETTERS_DIGITS -> BUILDUP.LETTERS.content.length() + BUILDUP.DIGITS.content.length();
                case LETTERS_DIGITS_SPECIALS ->
                        BUILDUP.LETTERS.content.length() + BUILDUP.DIGITS.content.length() + BUILDUP.SPECIAL.content.length();
            };
            entropyScore.setText((int) (passwordLength.getValue() * Math.log(passwordVariationLength) / Math.log(2)) + " bits of entropy");
        }
    }

    public void initialize() {
        passwordLength.getItems().addAll(6, 8, 10, 12, 15, 20, 25, 30, 35, 40, 50);
        passwordLength.setValue(25); // password length is set at 25
        passwordBuildup.getItems().addAll(POSSIBILITIES.LETTERS.name(), POSSIBILITIES.LETTERS_DIGITS.name(), POSSIBILITIES.LETTERS_DIGITS_SPECIALS.name());
        passwordBuildup.setValue(POSSIBILITIES.LETTERS_DIGITS.name());
        specials.getItems().addAll(1, 2, 3, 4, 5);
        specials.setValue(1); // number of special characters is set at 1
        calculateEntropyScore();
    }

    private String createPassword(POSSIBILITIES pos, int length, int maxSpecials) {
        // First create the password with only letters or letters+digits, then later mix in the specials
        String content = switch (pos) {
            case LETTERS -> BUILDUP.LETTERS.content;
            case LETTERS_DIGITS, LETTERS_DIGITS_SPECIALS -> BUILDUP.LETTERS.content + BUILDUP.DIGITS.content;
        };
        SecureRandom sr = new SecureRandom();
        boolean stop = false;
        String generatedPassword = "";
        while (!stop) {
            StringBuilder password = new StringBuilder();
            // create password
            for (int counter = 0; counter < length; counter++) {
                password.append(content.charAt(sr.nextInt(content.length())));
            }
            // mix in the special characters if needed
            if (pos.equals(POSSIBILITIES.LETTERS_DIGITS_SPECIALS)) {
                for (int counter = 0; counter < maxSpecials; counter++) {
                    password.setCharAt(sr.nextInt(length), BUILDUP.SPECIAL.content.charAt(sr.nextInt(BUILDUP.SPECIAL.content.length() - 1)));
                }
            }
            generatedPassword = password.toString();
            // test the password to see if all types of characters (UPPER and lower) and digits are present
            stop = generatedPassword.matches(".*[" + BUILDUP.UPPERCASE.content + "].*") && generatedPassword.matches(".*[" + BUILDUP.LOWERCASE.content + "].*"); // must contain at least 1 uppercase and 1 lowercase
            if (!pos.equals(POSSIBILITIES.LETTERS)) {
                stop = stop && generatedPassword.matches(".*[" + BUILDUP.DIGITS.content + "].*");
            }
        }
        return generatedPassword;
    }

    void convertToColors(TextFlow txtFlow, String text) {
        // letters in BLACK
        // digits in RED
        // specials in BLUE
        for (int counter = 0; counter < text.length(); counter++) {
            Text txt = new Text(String.valueOf(text.charAt(counter)));
            txt.setFont(Font.font("Monospace", 13));
            if (Character.isDigit(text.charAt(counter))) {
                txt.setFill(Color.RED);
            } else if (Character.isLetter(text.charAt(counter))) {
                txt.setFill(Color.BLACK);
            } else {
                txt.setFill(Color.BLUE);
            }
            txtFlow.getChildren().add(txt);
        }
    }

}