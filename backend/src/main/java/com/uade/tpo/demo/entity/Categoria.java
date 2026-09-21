package com.uade.tpo.demo.entity;


public enum Categoria {
    BEBIDA("Bebida"),
CARNE("Carnes"),
DESAYUNO("Desayuno"),
ENSALADAS("Ensaladas"),
ENTRADA("Entrada"),
FAJITAS("Fajitas"),
GUARNICIONES("Guarniciones"),
MILANESAS("Milanesas"),
OTROS("Otros"),
PASTAS("Pastas"),
POSTRES("Postres"),
PRINCIPAL("Principal"),
SANDWICHES("Sandwiches"),
VINOS("Vinos"),
WOKS("Woks");

    private final String descripcion;

    Categoria(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}