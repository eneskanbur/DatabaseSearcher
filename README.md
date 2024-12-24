# Database Search Project - JavaFX-based Database Browser

This project provides a user-friendly JavaFX application for browsing and manipulating data within a relational database.  Users can connect to their database, view available databases and tables, explore table data, insert new records, and delete existing ones.  The application aims to simplify database interaction with a clean and intuitive graphical interface.

## Features

* **Database Connection:** Connect to any relational database using a JDBC URL, username, and password.
* **Database/Table Browsing:**  View available databases and tables within the connected database.
* **Data Viewing:** Display table data in a user-friendly tabular format.
* **Data Editing:**  Edit table cell values directly within the application.
* **Record Insertion:**  Insert new records into tables with an intuitive input dialog.
* **Record Deletion:**  Delete selected records from tables.
* **Error Handling:** Provides clear error messages to the user in case of connection issues or invalid operations.

## Implementation Details

* **JavaFX:** Used for creating the graphical user interface, providing a modern and responsive user experience.
* **FXML:**  FXML markup language is used for defining the UI layout, separating the UI design from application logic.
* **JDBC (Java Database Connectivity):**  JDBC API is used to connect and interact with the database.  Supports various database systems through JDBC drivers.
* **MVC (Model-View-Controller) Pattern (Loosely implemented):** The project loosely follows an MVC-like structure with FXML for the View, Controller classes for handling user interaction, and database queries acting as the Model.

## Technologies Used

* **Java:** Core programming language.
* **JavaFX:** UI framework.
* **FXML:** UI markup language.
* **JDBC:** Database connectivity API.
* **SQL:**  Structured Query Language for database queries.


## Project Structure

db-project/
├── src/
│ ├── main/
│ │ ├── java/
│ │ │ └── com/
│ │ │ └── example/
│ │ │ └── dbproject/
│ │ │ ├── Main.java
│ │ │ ├── MainController.java
│ │ │ └── LoginController.java
│ │ └── resources/
│ │ ├── com/
│ │ │ └── example/
│ │ │ └── dbproject/
│ │ │ ├── login-view.fxml
│ │ │ └── main-browser-view.fxml
│ └── ... (other project files)
└── ... (other files like README.md, pom.xml if using Maven)

## Future Enhancements

* **Data Export/Import:**  Add features to export and import data in various formats (CSV, Excel).
* **SQL Query Editor:**  Allow users to execute custom SQL queries.
* **Data Visualization:** Integrate charting libraries to visualize data.
* **Enhanced Data Editing:**  Provide more sophisticated data editing features (e.g., date pickers, dropdowns).
