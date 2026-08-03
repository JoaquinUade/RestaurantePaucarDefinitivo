package paucar.ventas.ui;

import com.uade.tpo.demo.entity.TipoCliente;

import javafx.geometry.Pos;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

public final class SelectorTipoCliente {

    private final ToggleGroup toggleGroup;
    private final HBox contenedor;

    private SelectorTipoCliente(
            ToggleGroup toggleGroup,
            HBox contenedor) {

        this.toggleGroup = toggleGroup;
        this.contenedor = contenedor;
    }

    public static SelectorTipoCliente crear() {

        ToggleGroup tgTipoCliente =
                new ToggleGroup();

        ToggleButton btnMesa =
                new ToggleButton("Mesa");

        ToggleButton btnCliente =
                new ToggleButton("Cliente");

        ToggleButton btnEmpresa =
                new ToggleButton("Empresa");

        btnMesa.setToggleGroup(tgTipoCliente);
        btnCliente.setToggleGroup(tgTipoCliente);
        btnEmpresa.setToggleGroup(tgTipoCliente);

        btnCliente.setSelected(true);

        btnMesa.setUserData(TipoCliente.MESA);
        btnCliente.setUserData(TipoCliente.CLIENTE);
        btnEmpresa.setUserData(TipoCliente.EMPRESA);

        btnMesa.getStyleClass()
                .add("segmented-left");

        btnCliente.getStyleClass()
                .add("segmented-center");

        btnEmpresa.getStyleClass()
                .add("segmented-right");

        HBox selector =
                new HBox(
                        6,
                        btnMesa,
                        btnCliente,
                        btnEmpresa);

        selector.setAlignment(
                Pos.CENTER_LEFT);

        return new SelectorTipoCliente(
                tgTipoCliente,
                selector);
    }

    public ToggleGroup getToggleGroup() {
        return toggleGroup;
    }

    public HBox getContenedor() {
        return contenedor;
    }
}