module tp.gestion_cleints {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    opens tp.gestion_cleints to javafx.fxml;

    exports tp.gestion_cleints;
}