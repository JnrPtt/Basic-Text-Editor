import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.undo.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;

public class Main extends JFrame implements ActionListener {
    private JTextPane textPane;
    private JFileChooser fileChooser;
    private File currentFile;
    private JLabel statusLabel;
    private UndoManager undoManager;

    public Main() {
        setTitle("Openpad");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textPane = new JTextPane();
        JScrollPane scrollPane = new JScrollPane(textPane);
        add(scrollPane, BorderLayout.CENTER);

        statusLabel = new JLabel("Caracteres: 0, Palabras: 0");
        add(statusLabel, BorderLayout.SOUTH);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Archivo");
        JMenuItem openMenuItem = new JMenuItem("Abrir existente");
        JMenuItem saveMenuItem = new JMenuItem("Guardar archivo");
        openMenuItem.addActionListener(this);
        saveMenuItem.addActionListener(this);
        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);
        menuBar.add(fileMenu);

        JMenu editMenu = new JMenu("Edición");
        JMenuItem undoMenuItem = new JMenuItem("Deshacer");
        JMenuItem redoMenuItem = new JMenuItem("Rehacer");
        undoMenuItem.addActionListener(this);
        redoMenuItem.addActionListener(this);
        editMenu.add(undoMenuItem);
        editMenu.add(redoMenuItem);
        menuBar.add(editMenu);

        JMenu formatMenu = new JMenu("Formato");
        JMenuItem alignLeftMenuItem = new JMenuItem("Alinear a la izquierda");
        JMenuItem alignCenterMenuItem = new JMenuItem("Alinear al centro");
        JMenuItem alignRightMenuItem = new JMenuItem("Alinear a la derecha");
        JMenuItem fontSizeMenuItem = new JMenuItem("Tamaño de la fuente");
        JMenuItem fontTypeMenuItem = new JMenuItem("Tipo de fuente");
        JMenuItem colorMenuItem = new JMenuItem("Color del texto");
        JMenuItem boldMenuItem = new JMenuItem("Negrita");
        JMenuItem italicMenuItem = new JMenuItem("Cursiva");
        JMenuItem underlineMenuItem = new JMenuItem("Subrayado");
        JMenuItem strikethroughMenuItem = new JMenuItem("Tachado");

        alignLeftMenuItem.addActionListener(this);
        alignCenterMenuItem.addActionListener(this);
        alignRightMenuItem.addActionListener(this);
        fontSizeMenuItem.addActionListener(this);
        fontTypeMenuItem.addActionListener(this);
        colorMenuItem.addActionListener(this);
        boldMenuItem.addActionListener(this);
        italicMenuItem.addActionListener(this);
        underlineMenuItem.addActionListener(this);
        strikethroughMenuItem.addActionListener(this);

        formatMenu.add(alignLeftMenuItem);
        formatMenu.add(alignCenterMenuItem);
        formatMenu.add(alignRightMenuItem);
        formatMenu.addSeparator();
        formatMenu.add(fontSizeMenuItem);
        formatMenu.add(fontTypeMenuItem);
        formatMenu.addSeparator();
        formatMenu.add(colorMenuItem);
        formatMenu.addSeparator();
        formatMenu.add(boldMenuItem);
        formatMenu.add(italicMenuItem);
        formatMenu.add(underlineMenuItem);
        formatMenu.add(strikethroughMenuItem);
        menuBar.add(formatMenu);

        setJMenuBar(menuBar);

        fileChooser = new JFileChooser();
        undoManager = new UndoManager();
        Document doc = textPane.getDocument();
        doc.addUndoableEditListener(undoManager);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        switch (command) {
            case "Abrir existente":
                openFile();
                break;
            case "Guardar archivo":
                saveFile();
                break;
            case "Deshacer":
                undo();
                break;
            case "Rehacer":
                redo();
                break;
            case "Alinear a la izquierda":
                alignText(SwingConstants.CENTER);
                break;
            case "Alinear al centro":
                alignText(SwingConstants.RIGHT);
                break;
            case "Alinear a la derecha":
                alignText(SwingConstants.LEFT);
                break;
            case "Tamaño de la fuente":
                String sizeStr = JOptionPane.showInputDialog(this, "Enter font size:");
                try {
                    int size = Integer.parseInt(sizeStr);
                    setFontSize(size);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid font size!");
                }
                break;
            case "Tipo de fuente":
                String[] fontOptions = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
                String selectedFont = (String) JOptionPane.showInputDialog(this, "Choose font type:", "Font Type",
                        JOptionPane.PLAIN_MESSAGE, null, fontOptions, textPane.getFont().getFamily());
                if (selectedFont != null) {
                    textPane.setFont(new Font(selectedFont, textPane.getFont().getStyle(), textPane.getFont().getSize()));
                    updateStatus();
                }
                break;
            case "Color del texto":
                Color selectedColor = JColorChooser.showDialog(this, "Choose Text Color", textPane.getForeground());
                if (selectedColor != null) {
                    StyledDocument doc = textPane.getStyledDocument();
                    int start = textPane.getSelectionStart();
                    int end = textPane.getSelectionEnd();
                    if (start != end) {
                        Style style = textPane.addStyle("Color", null);
                        StyleConstants.setForeground(style, selectedColor);
                        doc.setCharacterAttributes(start, end - start, style, false);
                    }
                }
                break;
            case "Negrita":
                setBold();
                break;
            case "Cursiva":
                setItalic();
                break;
            case "Subrayado":
                setUnderline();
                break;
            case "Tachado":
                setStrikethrough();
                break;
        }
    }

    private void openFile() {
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));

        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                currentFile = file;
                textPane.setEditorKit(new StyledEditorKit());
                textPane.setDocument(new DefaultStyledDocument());

                FileReader reader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(reader);
                textPane.read(bufferedReader, null);
                bufferedReader.close();
                updateStatus();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void saveFile() {
        if (currentFile != null) {
            try {
                FileWriter writer = new FileWriter(currentFile);
                textPane.write(writer);
                writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            int returnVal = fileChooser.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = fileChooser.getSelectedFile();
                    if (!file.getName().endsWith(".txt")) {
                        file = new File(file.getAbsolutePath() + ".txt");
                    }
                    FileWriter writer = new FileWriter(file);
                    textPane.write(writer);
                    writer.close();
                    currentFile = file;
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void alignText(int alignment) {
        StyledDocument doc = textPane.getStyledDocument();
        MutableAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setAlignment(attrs, alignment);
        doc.setParagraphAttributes(0, doc.getLength(), attrs, false);
        updateStatus();
    }

    private void setStrikethrough() {
        StyledDocument doc = textPane.getStyledDocument();
        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        if (start != end) {
            MutableAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setStrikeThrough(attrs, true);
            doc.setCharacterAttributes(start, end - start, attrs, false);
            updateStatus();
        }
    }

    private void setBold() {
        StyledEditorKit.BoldAction action = new StyledEditorKit.BoldAction();
        action.actionPerformed(new ActionEvent(textPane, ActionEvent.ACTION_PERFORMED, ""));
        updateStatus();
    }

    private void setItalic() {
        StyledEditorKit.ItalicAction action = new StyledEditorKit.ItalicAction();
        action.actionPerformed(new ActionEvent(textPane, ActionEvent.ACTION_PERFORMED, ""));
        updateStatus();
    }

    private void setUnderline() {
        StyledEditorKit.UnderlineAction action = new StyledEditorKit.UnderlineAction();
        action.actionPerformed(new ActionEvent(textPane, ActionEvent.ACTION_PERFORMED, ""));
        updateStatus();
    }

    private void setFontSize(int size) {
        textPane.setFont(textPane.getFont().deriveFont((float) size));
        updateStatus();
    }

    private void updateStatus() {
        StyledDocument doc = textPane.getStyledDocument();
        int totalCharacters = doc.getLength();
        String text = "";
        try {
            text = doc.getText(0, doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        int wordCount = text.isEmpty() ? 0 : text.trim().split("\\s+").length;
        statusLabel.setText("Caracteres: " + totalCharacters + ", Palabras: " + wordCount);
    }

    private void undo() {
        if (undoManager.canUndo()) {
            undoManager.undo();
            updateStatus();
        }
    }

    private void redo() {
        if (undoManager.canRedo()) {
            undoManager.redo();
            updateStatus();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main editor = new Main();
            editor.setVisible(true);
        });
    }
}

