package notes.service.view.impl;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import notes.service.controller.NoteService;
import notes.service.view.NotePopupService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NotePopupServiceImpl implements NotePopupService {

    private NoteService noteService = ServiceManager.getService(NoteService.class);
    private JBPopup jbPopup;
    private JTextArea noteTextArea;
    private Boolean editable = false;
    private Boolean editted = false;
    private String path;
    private int lineNo;

    public void create(AnActionEvent e, String _path, int _lineNo){

        Project project = e.getProject();
        if(project==null) return;
        Module[] modules = ModuleManager.getInstance(project).getModules();
        noteService.setProjectAndModules(project, modules);
        Editor editor = e.getData(CommonDataKeys.EDITOR);

        path = _path;
        lineNo = _lineNo;

        final JBPopupFactory jbPopupFactory = JBPopupFactory.getInstance();
        JComponent popupContent = createContent();
        jbPopup = jbPopupFactory.createComponentPopupBuilder(popupContent, noteTextArea)
                .setResizable(false)
                .setTitle("Note")
                .setMovable(true)
                .setFocusable(true)
                .setRequestFocus(true)
                .createPopup();

        jbPopup.addListener(new JBPopupListener.Adapter(){
            @Override
            public void onClosed(LightweightWindowEvent event) {
                if (editted) saveUpdate();
                reset();
            }
        });

        if(editor!=null){
            jbPopup.showInBestPositionFor(editor);
        }else{
            jbPopup.showInFocusCenter();
        }
    }

    private JComponent createContent(){
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        noteTextArea = new JTextArea(20, 50);

        final String noteText = noteService.getNoteContent(path, lineNo);

        if(noteText==null) editable=true;

        noteTextArea.setText(noteText);
        noteTextArea.setColumns(75);
        noteTextArea.setLineWrap(true);
        noteTextArea.setEnabled(editable);
        noteTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                editted = true;
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                editted = true;
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        JBScrollPane scrollPane = new JBScrollPane(noteTextArea);
        mainPanel.add(scrollPane);


        JPanel buttonGroup = new JPanel();

        JButton closeButton = new JButton();
        closeButton.setText("Close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jbPopup.closeOk(null);
            }
        });
        buttonGroup.add(closeButton);

        JButton editButton = new JButton();
        editButton.setText("Edit");
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleEdit();
            }
        });
        buttonGroup.add(editButton);

        JButton deleteButton = new JButton();
        deleteButton.setText("Delete");
        deleteButton.setBackground(JBColor.RED);
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                noteService.deleteNote(path, lineNo);
                editted=false;
                jbPopup.closeOk(null);
            }
        });
        buttonGroup.add(deleteButton);
        mainPanel.add(buttonGroup);

        return mainPanel;
    }

    private void toggleEdit() {
        editable=!editable;
        noteTextArea.setEnabled(editable);
    }

    private void saveUpdate() {
        String noteText = noteTextArea.getText();
        noteService.putNote(path, lineNo, noteText);
    }

    private void reset(){
        jbPopup = null;
        noteTextArea = null;
        editable = false;
        editted = false;
        path = null;
        lineNo = -1;
    }
}