-- Schema for MySQL: creates tables with explicit column order

CREATE TABLE IF NOT EXISTS clientes (
  id_cliente BIGINT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(255) NOT NULL,
  tipo_cliente VARCHAR(50) NOT NULL,
  periodicidad_pago VARCHAR(50) NULL,
  PRIMARY KEY (id_cliente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ventas (
  id_venta BIGINT NOT NULL AUTO_INCREMENT,
  fecha DATETIME NOT NULL,
  dia VARCHAR(20) NOT NULL,
  id_cliente BIGINT NOT NULL,
  descripcion VARCHAR(255) NOT NULL,
  estado VARCHAR(50) NOT NULL,
  monto DOUBLE NOT NULL,
  observaciones TEXT,
  consumidor VARCHAR(255),

  PRIMARY KEY (id_venta),

  CONSTRAINT fk_ventas_cliente
      FOREIGN KEY (id_cliente)
      REFERENCES clientes(id_cliente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla productos (orden: id_producto, nombre, precio, categoria)
CREATE TABLE IF NOT EXISTS productos (
  id_producto BIGINT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(255) NOT NULL,
  precio DOUBLE NOT NULL,
  categoria VARCHAR(50) NOT NULL,
  PRIMARY KEY (id_producto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS gastos_variables (
    id_gasto_variable BIGINT NOT NULL AUTO_INCREMENT,

    fecha DATE NOT NULL,

    producto VARCHAR(255) NOT NULL,

    cant_comprada DECIMAL(19,4) NOT NULL,

    medida VARCHAR(255),

    monto DECIMAL(19,2) NOT NULL,

    cargado_en_stock BOOLEAN,

    id_categoria BIGINT,

    id_stock BIGINT,

    PRIMARY KEY (id_gasto_variable),

    CONSTRAINT fk_gastos_variables_categoria
        FOREIGN KEY (id_categoria)
        REFERENCES categoria_gasto_variable(id_categoria),

    CONSTRAINT fk_gastos_variables_stock
        FOREIGN KEY (id_stock)
        REFERENCES stock(id_stock)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS stock (
  id_stock BIGINT NOT NULL AUTO_INCREMENT,

  id_categoria BIGINT NOT NULL,
  id_gasto_variable BIGINT NULL,

  nombre_producto VARCHAR(255) NOT NULL,

  cantidad DECIMAL(19,4) NOT NULL,
  stock_minimo DECIMAL(19,4) NOT NULL,

  unidad_cantidad VARCHAR(50),
  unidad_stock_minimo VARCHAR(50),

  fecha DATE NOT NULL,

  PRIMARY KEY (id_stock),

  UNIQUE KEY uk_stock_gasto_variable (id_gasto_variable),

  CONSTRAINT fk_stock_categoria
    FOREIGN KEY (id_categoria)
    REFERENCES categoria_gasto_variable(id_categoria),

  CONSTRAINT fk_stock_gasto_variable
    FOREIGN KEY (id_gasto_variable)
    REFERENCES gastos_variables(id_gasto_variable)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS historial_stock (

    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    id_stock BIGINT NOT NULL,

    id_gasto_variable BIGINT NULL,

    movimiento DECIMAL(19,4) NOT NULL,

    cantidad DECIMAL(19,4) NOT NULL,

    fecha DATE NOT NULL,

    CONSTRAINT fk_historial_stock
        FOREIGN KEY (id_stock)
        REFERENCES stock(id_stock),

    CONSTRAINT fk_historial_gasto_variable
        FOREIGN KEY (id_gasto_variable)
        REFERENCES gastos_variables(id_gasto_variable)

);
CREATE TABLE IF NOT EXISTS pago_parcial (

    id BIGINT NOT NULL AUTO_INCREMENT,

    fecha_pago DATETIME NOT NULL,

    payer_name VARCHAR(255),

    cuit VARCHAR(50),

    factura VARCHAR(255),

    observaciones TEXT,

    monto_total DECIMAL(19,2) NOT NULL,

    PRIMARY KEY (id)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS pago_parcial_venta (

    pago_parcial_id BIGINT NOT NULL,

    id_venta BIGINT NOT NULL,

    PRIMARY KEY (pago_parcial_id, id_venta),

    CONSTRAINT fk_ppv_pago
        FOREIGN KEY (pago_parcial_id)
        REFERENCES pago_parcial(id),

    CONSTRAINT fk_ppv_venta
        FOREIGN KEY (id_venta)
        REFERENCES ventas(id_venta)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;