-- Schema for MySQL: creates tables with explicit column order

CREATE TABLE IF NOT EXISTS clientes (
  id_cliente BIGINT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(255) NOT NULL,
  tipo_cliente VARCHAR(50) NOT NULL,
  PRIMARY KEY (id_cliente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ventas (
  id_venta BIGINT NOT NULL AUTO_INCREMENT,
  fecha DATETIME NOT NULL,
  dia VARCHAR(20) NOT NULL,  -- dia en string
  id_cliente BIGINT NOT NULL,
  descripcion VARCHAR(255) NOT NULL,
  estado VARCHAR(50) NOT NULL,
  monto DOUBLE NOT NULL,
  observaciones TEXT,
  PRIMARY KEY (id_venta),
  CONSTRAINT fk_ventas_cliente FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla productos (orden: id_producto, nombre, precio, categoria)
CREATE TABLE IF NOT EXISTS productos (
  id_producto BIGINT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(255) NOT NULL,
  precio DOUBLE NOT NULL,
  categoria VARCHAR(50) NOT NULL,
  PRIMARY KEY (id_producto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS stock (
  id_stock BIGINT NOT NULL AUTO_INCREMENT,

  id_categoria BIGINT NOT NULL,
  id_gasto_variable BIGINT NOT NULL,

  nombre_producto VARCHAR(255) NOT NULL,

  cantidad DECIMAL(19,4) NOT NULL,
  stock_minimo DECIMAL(19,4) NOT NULL,

  unidad_cantidad VARCHAR(50),
  unidad_stock_minimo VARCHAR(50),

  PRIMARY KEY (id_stock),

  UNIQUE KEY uk_stock_gasto_variable (id_gasto_variable),

  CONSTRAINT fk_stock_categoria
    FOREIGN KEY (id_categoria)
    REFERENCES categoria_gasto_variable(id_categoria),

  CONSTRAINT fk_stock_gasto_variable
    FOREIGN KEY (id_gasto_variable)
    REFERENCES gastos_variables(id_gasto_variable)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;