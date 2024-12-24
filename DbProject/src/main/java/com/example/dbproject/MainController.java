package com.example.dbproject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MainController {
    @FXML private ListView<String> databaseListView;
    @FXML private ListView<String> tableListView;
    @FXML private TableView<ObservableList<String>> dataTableView;

    private Connection connection;
    private String currentDatabase;
    private String currentTable;
    private List<String> columnNames = new ArrayList<>();

    public void setConnection(Connection connection) throws SQLException {
        this.connection = connection;
        loadDatabases();
    }

    private void loadDatabases() throws SQLException {
        List<String> databases = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet result = metaData.getCatalogs();

        while (result.next()) {
            databases.add(result.getString("TABLE_CAT"));
        }

        databaseListView.setItems(FXCollections.observableArrayList(databases));
    }

    @FXML
    protected void onDatabaseSelected() {
        String selectedDatabase = databaseListView.getSelectionModel().getSelectedItem();
        if (selectedDatabase != null) {
            try {
                currentDatabase = selectedDatabase;
                connection.setCatalog(currentDatabase);
                loadTables(currentDatabase);
                dataTableView.getItems().clear();
                dataTableView.getColumns().clear();
            } catch (SQLException e) {
                showErrorAlert("Database Selection Error", e.getMessage());
            }
        }
    }

    private void loadTables(String database) throws SQLException {
        List<String> tables = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet rs = metaData.getTables(database, null, "%", new String[]{"TABLE"});

        while (rs.next()) {
            tables.add(rs.getString("TABLE_NAME"));
        }

        tableListView.setItems(FXCollections.observableArrayList(tables));
    }

    @FXML
    protected void onTableSelected() {
        String selectedTable = tableListView.getSelectionModel().getSelectedItem();
        if (selectedTable != null) {
            try {
                currentTable = selectedTable;
                loadTableData(currentTable);
            } catch (SQLException e) {
                showErrorAlert("Table Selection Error", e.getMessage());
            }
        }
    }

    private void loadTableData(String tableName) throws SQLException {
        dataTableView.getColumns().clear();
        dataTableView.getItems().clear();
        columnNames.clear();

        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet columnsRs = metaData.getColumns(currentDatabase, null, tableName, null);

        while (columnsRs.next()) {
            String columnName = columnsRs.getString("COLUMN_NAME");
            columnNames.add(columnName);

            TableColumn<ObservableList<String>, String> column = new TableColumn<>(columnName);
            final int columnIndex = columnNames.size() - 1;

            column.setCellValueFactory(param ->
                    new javafx.beans.property.SimpleStringProperty(
                            param.getValue().get(columnIndex)));

            column.setCellFactory(TextFieldTableCell.forTableColumn());
            column.setOnEditCommit(event -> {
                try {
                    updateTableCell(event, columnIndex);
                } catch (SQLException e) {
                    showErrorAlert("Update Error", e.getMessage());
                }
            });

            dataTableView.getColumns().add(column);
        }

        dataTableView.setEditable(true);

        String query = "SELECT * FROM " + tableName;
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

        try (Statement stmt = connection.createStatement();
             ResultSet result = stmt.executeQuery(query)) {

            while (result.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= columnNames.size(); i++) {
                    row.add(result.getString(i));
                }
                data.add(row);
            }
        }

        dataTableView.setItems(data);
    }

    private void updateTableCell(TableColumn.CellEditEvent<ObservableList<String>, String> event, int columnIndex)
            throws SQLException {
        String primaryKeyColumn = findPrimaryKeyColumn(currentTable);
        if (primaryKeyColumn == null) {
            throw new SQLException("No primary key found for table " + currentTable);
        }

        ObservableList<String> row = event.getRowValue();
        int primaryKeyIndex = columnNames.indexOf(primaryKeyColumn);
        String primaryKeyValue = row.get(primaryKeyIndex);

        String updateQuery = String.format("UPDATE %s SET %s = ? WHERE %s = ?",
                currentTable, columnNames.get(columnIndex), primaryKeyColumn);

        try (PreparedStatement prepared = connection.prepareStatement(updateQuery)) {
            prepared.setString(1, event.getNewValue());
            prepared.setString(2, primaryKeyValue);
            prepared.executeUpdate();

            row.set(columnIndex, event.getNewValue());
        }
    }

    @FXML
    protected void onInsertAction() {
        if (currentTable == null) {
            showErrorAlert("Insert Error", "Please select a table first");
            return;
        }

        Dialog<List<String>> dialog = createInsertDialog();
        Optional<List<String>> result = dialog.showAndWait();

        if (result.isPresent()) {
            try {
                insertRecord(result.get());
                loadTableData(currentTable);
            } catch (SQLException e) {
                showErrorAlert("Insert Error", e.getMessage());
            }
        }
    }

    private Dialog<List<String>> createInsertDialog() {
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("Insert New Record");
        dialog.setHeaderText("Enter values for new record");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        List<TextField> textFields = new ArrayList<>();
        for (int i = 0; i < columnNames.size(); i++) {
            grid.add(new Label(columnNames.get(i)), 0, i);
            TextField field = new TextField();
            grid.add(field, 1, i);
            textFields.add(field);
        }

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return textFields.stream()
                        .map(TextField::getText)
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            }
            return null;
        });

        return dialog;
    }

    private void insertRecord(List<String> values) throws SQLException {
        StringBuilder query = new StringBuilder("INSERT INTO " + currentTable + " (");
        query.append(String.join(", ", columnNames));
        query.append(") VALUES (");
        query.append(String.join(", ", Collections.nCopies(values.size(), "?")));
        query.append(")");

        try (PreparedStatement pstmt = connection.prepareStatement(query.toString())) {
            for (int i = 0; i < values.size(); i++) {
                pstmt.setString(i + 1, values.get(i));
            }
            pstmt.executeUpdate();
        }
    }

    @FXML
    protected void onDeleteAction() {
        if (currentTable == null) {
            showErrorAlert("Delete Error", "Please select a table first");
            return;
        }

        ObservableList<ObservableList<String>> selectedItems =
                dataTableView.getSelectionModel().getSelectedItems();

        if (selectedItems.isEmpty()) {
            showErrorAlert("Delete Error", "Please select at least one row to delete");
            return;
        }

        try {
            String primaryKeyColumn = findPrimaryKeyColumn(currentTable);
            if (primaryKeyColumn == null) {
                throw new SQLException("No primary key found for table " + currentTable);
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete");
            alert.setHeaderText("Delete Selected Records");
            alert.setContentText("Are you sure you want to delete the selected records?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                deleteRecords(selectedItems, primaryKeyColumn);
                loadTableData(currentTable);
            }
        } catch (SQLException e) {
            showErrorAlert("Delete Error", e.getMessage());
        }
    }

    private void deleteRecords(List<ObservableList<String>> records, String primaryKeyColumn) throws SQLException {
        int primaryKeyIndex = columnNames.indexOf(primaryKeyColumn);
        String deleteQuery = "DELETE FROM " + currentTable + " WHERE " + primaryKeyColumn + " = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(deleteQuery)) {
            for (ObservableList<String> record : records) {
                pstmt.setString(1, record.get(primaryKeyIndex));
                pstmt.executeUpdate();
            }
        }
    }

    private String findPrimaryKeyColumn(String tableName) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet primaryKeys = metaData.getPrimaryKeys(currentDatabase, null, tableName);

            if (primaryKeys.next()) {
                return primaryKeys.getString("COLUMN_NAME");
            }
        } catch (SQLException e) {
            showErrorAlert("Primary Key Error", e.getMessage());
        }
        return null;
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        alert.showAndWait();
    }
}