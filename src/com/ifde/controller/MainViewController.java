package com.ifde.controller;
import com.ifde.model.*;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.*;

public class MainViewController {

    @FXML private TextField folderPathField;
    @FXML private ComboBox<String> fileTypeCombo;
    @FXML private Button scanButton, stopButton, deleteSelectedButton, autoSelectButton, rescanButton;
    @FXML private Label scanStatusLabel, progressPercentLabel;
    @FXML private ProgressBar scanProgressBar;
    @FXML private Label totalGroupsLabel, totalDuplicateFilesLabel, totalWastedSpaceLabel;
    @FXML private Label filesScannedLabel, summaryGroupsLabel, summaryDuplicateFilesLabel, summaryWastedSpaceLabel;
    @FXML private TextArea scanLogArea;
    @FXML private CheckBox selectAllCheckBox;
    @FXML private TreeTableView<Row> resultsTable;
    @FXML private TreeTableColumn<Row, Boolean> selectColumn;
    @FXML private TreeTableColumn<Row, String> indexColumn, fileNameColumn, filePathColumn,
            fileSizeColumn, lastModifiedColumn, hashColumn;

    private DuplicateDataStore store;
    private EngineCore engine;

    /** One table row: a group header (rec == null) or a file. */
    static class Row {
        final FileRecord rec;
        final String groupText;
        final BooleanProperty selected = new SimpleBooleanProperty(false);
        Row(FileRecord rec, String groupText) { this.rec = rec; this.groupText = groupText; }
    }

    @FXML
    private void initialize() {
        selectColumn.setCellValueFactory(p -> p.getValue().getValue().selected);
        selectColumn.setCellFactory(c -> new TreeTableCell<Row, Boolean>() {
            private final CheckBox cb = new CheckBox();
            private BooleanProperty bound;

            @Override protected void updateItem(Boolean v, boolean empty) {
                super.updateItem(v, empty);
                if (bound != null) { cb.selectedProperty().unbindBidirectional(bound); bound = null; }
                TreeItem<Row> ti = getTableRow() == null ? null : getTableRow().getTreeItem();
                if (empty || ti == null || ti.getValue().rec == null) { setGraphic(null); return; }
                bound = ti.getValue().selected;
                cb.selectedProperty().bindBidirectional(bound);
                setGraphic(cb);
            }
        });

        col(indexColumn,        r -> r.rec == null ? r.groupText : "");
        col(fileNameColumn,     r -> r.rec == null ? "" : r.rec.getName());
        col(filePathColumn,     r -> r.rec == null ? "" : r.rec.getPath().toString());
        col(fileSizeColumn,     r -> r.rec == null ? "" : r.rec.getSizeString());
        col(lastModifiedColumn, r -> r.rec == null ? "" : r.rec.getLastModified());
        col(hashColumn,         r -> r.rec == null ? "" : r.rec.getHash());

        selectAllCheckBox.setOnAction(e -> setAllFiles(selectAllCheckBox.isSelected()));
    }

    private void col(TreeTableColumn<Row, String> c, Function<Row, String> f) {
        c.setCellValueFactory(p -> new SimpleStringProperty(f.apply(p.getValue().getValue())));
    }

    // ---------------- Browse ----------------
    @FXML
    private void onBrowse() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select folder to scan");
        File current = new File(folderPathField.getText());
        chooser.setInitialDirectory(
                current.isDirectory() ? current : new File(System.getProperty("user.home")));
        File dir = chooser.showDialog(folderPathField.getScene().getWindow());
        if (dir != null) folderPathField.setText(dir.getAbsolutePath());
    }

    // ---------------- Scan / Stop / Rescan ----------------
    @FXML
    private void onScan() {
        String text = folderPathField.getText().trim();
        if (text.isEmpty() || !Files.isDirectory(Path.of(text))) {
            scanLogArea.appendText("Select a valid folder first.\n");
            return;
        }
        Path dir = Path.of(text);
        List<String> types = parseExtensions(fileTypeCombo.getValue());

        store = new DuplicateDataStore();
        engine = new EngineCore(store, new ScanListener() {
            @Override public void onProgress(int p) {
                Platform.runLater(() -> {
                    scanProgressBar.setProgress(p / 100.0);
                    progressPercentLabel.setText(p + "%");
                });
            }
            @Override public void onLogMessage(String m) {
                Platform.runLater(() -> scanLogArea.appendText(m + "\n"));
            }
        });

        scanLogArea.clear();
        scanProgressBar.setProgress(0);
        progressPercentLabel.setText("0%");
        scanStatusLabel.setText("Scanning...");
        scanButton.setDisable(true);
        rescanButton.setDisable(true);

        final EngineCore running = engine;
        Thread t = new Thread(() -> {
            try {
                running.startScan(dir, types);
            } finally {
                Platform.runLater(() -> {
                    showResults();
                    scanStatusLabel.setText("Done");
                    scanButton.setDisable(false);
                    rescanButton.setDisable(false);
                });
            }
        }, "scan-thread");
        t.setDaemon(true);
        t.start();
    }

    @FXML private void onStop()   { if (engine != null) engine.cancelScan(); }
    @FXML private void onRescan() { onScan(); }

    private static List<String> parseExtensions(String selected) {
        List<String> exts = new ArrayList<>();
        if (selected == null) return exts;
        Matcher m = Pattern.compile("\\.[A-Za-z0-9]+").matcher(selected);
        while (m.find()) exts.add(m.group().toLowerCase());
        return exts;
    }

    // ---------------- Results ----------------
    private void showResults() {
        TreeItem<Row> root = new TreeItem<>(new Row(null, ""));
        int groups = 0, dupFiles = 0;

        for (ArrayList<FileRecord> files : store.getMap().values()) {
            if (files.size() <= 1) continue;
            groups++;
            dupFiles += files.size();
            TreeItem<Row> g = new TreeItem<>(new Row(null, "Group " + groups + " (" + files.size() + " files)"));
            g.setExpanded(true);
            for (FileRecord f : files) g.getChildren().add(new TreeItem<>(new Row(f, "")));
            root.getChildren().add(g);
        }
        resultsTable.setRoot(root);

        String wasted = FileRecord.formatSizeString(store.calcWastedSpace());
        totalGroupsLabel.setText(String.valueOf(groups));
        summaryGroupsLabel.setText(String.valueOf(groups));
        totalDuplicateFilesLabel.setText(String.valueOf(dupFiles));
        summaryDuplicateFilesLabel.setText(String.valueOf(dupFiles));
        totalWastedSpaceLabel.setText(wasted);
        summaryWastedSpaceLabel.setText(wasted);
        filesScannedLabel.setText(String.valueOf(engine.getTotalFilesScanned()));
        selectAllCheckBox.setSelected(false);
    }

    // ---------------- Selection ----------------
    private void setAllFiles(boolean value) {
        if (resultsTable.getRoot() == null) return;
        for (TreeItem<Row> g : resultsTable.getRoot().getChildren())
            for (TreeItem<Row> f : g.getChildren()) f.getValue().selected.set(value);
    }

    @FXML
    private void onAutoSelect() {
        if (resultsTable.getRoot() == null) return;
        for (TreeItem<Row> g : resultsTable.getRoot().getChildren()) {
            boolean first = true;
            for (TreeItem<Row> f : g.getChildren()) {
                f.getValue().selected.set(!first);   // keep the first file in each group
                first = false;
            }
        }
    }

    // ---------------- Delete ----------------
    @FXML
    private void onDeleteSelected() {
        if (resultsTable.getRoot() == null) return;
        List<FileRecord> toDelete = new ArrayList<>();
        for (TreeItem<Row> g : resultsTable.getRoot().getChildren())
            for (TreeItem<Row> f : g.getChildren())
                if (f.getValue().selected.get()) toDelete.add(f.getValue().rec);
        if (toDelete.isEmpty()) return;

        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Permanently delete " + toDelete.size() + " file(s)?", ButtonType.OK, ButtonType.CANCEL);
        a.setHeaderText(null);
        if (a.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        store.deleteFiles(toDelete);
        showResults();
    }
}