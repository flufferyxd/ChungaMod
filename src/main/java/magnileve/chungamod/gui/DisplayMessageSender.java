package magnileve.chungamod.gui;

/**
 * Simple interface for displaying messages and hiding them.
 * @author Magnileve
 */
public interface DisplayMessageSender {

/**
 * Displays a message.
 * @param message the message
 */
public void displayMessage(String message);

/**
 * If currently displaying a message, hides it.
 */
public void hideDisplayedMessage();

}
