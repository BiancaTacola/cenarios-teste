module br.com.fiap.testesautomatizados {
    requires javafx.controls;
    requires javafx.fxml;


    opens br.com.fiap.testesautomatizados to javafx.fxml;
    exports br.com.fiap.testesautomatizados;
}