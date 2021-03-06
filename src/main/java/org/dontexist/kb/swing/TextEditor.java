package org.dontexist.kb.swing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * A text window for asking a user to edit text. Can be registered with an {@link java.awt.event.ActionListener} for
 * advanced thread-waiting functionality (e.g. using a {@link java.util.concurrent.CountDownLatch}. <p>To use, after
 * creating a new instance, invoke the GUI by calling displayFrame </p> Special thanks to: <a
 * href="http://stackoverflow.com/questions/9690686/save-a-the-text-from-a-jtextarea-ie-save-as-into-a-new-txt-file">http://stackoverflow.com/questions/9690686/save-a-the-text-from-a-jtextarea-ie-save-as-into-a-new-txt-file</a>
 * Modified to retun the edited text, rather than save it to a file.
 */
public class TextEditor extends JFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextEditor.class);

    private static final String SUBMIT_BUTTON_TEXT = "Submit";
    private static final String DEFAULT_TEXT_AREA_TEXT = "";
    private static final int DEFAULT_WINDOW_HEIGHT = 30;
    private static final int DEFAULT_WINDOW_WIDTH = 70;
    private static final Optional<CountDownLatch> NO_OUTSIDE_LISTENER = Optional.<CountDownLatch>empty();
    private final String textFrameText;

    private JTextArea textArea;

    /**
     * Stateful variable to hold result of text window result
     * FIXME static hack because text is dissapearing
     */
    private volatile String submittedText;

    public TextEditor() {
        this.textFrameText = DEFAULT_TEXT_AREA_TEXT;
        init(NO_OUTSIDE_LISTENER);
    }

    public TextEditor(final String windowTitle, final String textFrameText, final CountDownLatch outsideListener) {
        super(windowTitle);
        this.textFrameText = textFrameText;
        init(Optional.of(outsideListener));
    }

    private void init(final Optional<CountDownLatch> optionalOutsideListener) {

        textArea = new JTextArea(textFrameText, DEFAULT_WINDOW_HEIGHT, DEFAULT_WINDOW_WIDTH);

        // save frame reference for anonymous class access
        final Frame frame = this;

        add(new JScrollPane(textArea));
        add(new JPanel() {{
            final JButton button = new JButton(new AbstractAction(SUBMIT_BUTTON_TEXT) {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    LOGGER.info("User pressed [{}] button", SUBMIT_BUTTON_TEXT);
                    submittedText = saveInput();
                    // close the window
                    frame.dispose();
                    optionalOutsideListener.ifPresent(l -> l.countDown());
                }
            });

            add(button);
        }}, BorderLayout.SOUTH);
        add(new JPanel() {{
            final JButton button = new JButton(new AbstractAction("Cancel") {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    LOGGER.info("User pressed [{}] button", "Cancel");
                    // do not save output and close the window
                    frame.dispose();
                    optionalOutsideListener.ifPresent(l -> l.countDown());
                }
            });

            add(button);
        }}, BorderLayout.NORTH);
    }

    /**
     * @return the resulting text submitted by the user
     * @throws java.lang.IllegalArgumentException if cannot read the text
     */
    private String saveInput() {

        try (final Writer outFile = new StringWriter()) {
            textArea.write(outFile);
            outFile.flush();
            final String submittedText = outFile.toString();
            LOGGER.debug("Submitted text from JTextArea is [{}]", submittedText);
            return submittedText;
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not capture input of sumbitted textArea!", e);
        }
    }

    /**
     * Create the GUI and show it.  For thread safety, this method should be invoked from the event dispatch thread.
     */
    private static void createAndShowGui(final TextEditor frame) {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
        frame.toFront();
    }

    public static void main(String[] args) {
        askUserToEditText(new TextEditor());

    }

    public static void askUserToEditText(final TextEditor textEditor) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGui(textEditor);
            }
        });
    }

    /**
     * @return the text submitted from the window screen, <code>null</code> if text has not been submitted yet or user
     * pressed cancel
     */
    public String getSubmittedText() {
        return submittedText;
    }
}