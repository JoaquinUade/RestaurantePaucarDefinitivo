-- Esquema inicial de Cuentas Paucar (MySQL 8+).
-- El orden evita referencias a tablas que todavía no existen.
-- Este archivo no borra ni modifica datos de una base ya existente.

CREATE TABLE IF NOT EXISTS clientes (
    id_cliente BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    tipo_cliente VARCHAR(50) NOT NULL,
    periodicidad_pago VARCHAR(50) NULL,
    PRIMARY KEY (id_cliente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS productos (
    id_producto BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    precio DECIMAL(19,2) NOT NULL,
    categoria VARCHAR(50) NOT NULL,
    PRIMARY KEY (id_producto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS categoria_gasto_variable (
    id_categoria BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    PRIMARY KEY (id_categoria)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS empleados (
    id_empleado BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    apellido VARCHAR(255) NULL,
    PRIMARY KEY (id_empleado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Un stock nace en cero desde la interfaz; no se relaciona directamente
-- con gastos_variables. La relación se guarda del lado del gasto variable.
CREATE TABLE IF NOT EXISTS stock (
    id_stock BIGINT NOT NULL AUTO_INCREMENT,
    id_categoria BIGINT NOT NULL,
    nombre_producto VARCHAR(255) NOT NULL,
    cant_comprada DECIMAL(19,4) NOT NULL,
    cantidad DECIMAL(19,4) NOT NULL,
    unidad_cant_comprada VARCHAR(255) NULL,
    unidad_cantidad VARCHAR(255) NULL,
    stock_minimo DECIMAL(19,4) NOT NULL,
    fecha DATE NULL,
    PRIMARY KEY (id_stock),
    CONSTRAINT fk_stock_categoria
        FOREIGN KEY (id_categoria) REFERENCES categoria_gasto_variable(id_categoria)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS gastos_variables (
    id_gasto_variable BIGINT NOT NULL AUTO_INCREMENT,
    fecha DATE NOT NULL,
    producto VARCHAR(255) NOT NULL,
    cant_comprada DECIMAL(19,4) NOT NULL,
    medida VARCHAR(255) NULL,
    monto DECIMAL(19,2) NOT NULL,
    cargado_en_stock BOOLEAN NULL,
    id_categoria BIGINT NULL,
    id_stock BIGINT NULL,
    PRIMARY KEY (id_gasto_variable),
    CONSTRAINT fk_gastos_variables_categoria
        FOREIGN KEY (id_categoria) REFERENCES categoria_gasto_variable(id_categoria),
    CONSTRAINT fk_gastos_variables_stock
        FOREIGN KEY (id_stock) REFERENCES stock(id_stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS historial_stock (
    id BIGINT NOT NULL AUTO_INCREMENT,
    id_stock BIGINT NOT NULL,
    cantidad DECIMAL(19,4) NOT NULL,
    fecha DATE NOT NULL,
    id_gasto_variable BIGINT NULL,
    movimiento DECIMAL(19,4) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_historial_stock
        FOREIGN KEY (id_stock) REFERENCES stock(id_stock),
    CONSTRAINT fk_historial_gasto_variable
        FOREIGN KEY (id_gasto_variable) REFERENCES gastos_variables(id_gasto_variable)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ventas (
    id_venta BIGINT NOT NULL AUTO_INCREMENT,
    fecha DATETIME NOT NULL,
    dia VARCHAR(20) NOT NULL,
    id_cliente BIGINT NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    monto DECIMAL(19,2) NOT NULL,
    estado VARCHAR(50) NOT NULL,
    observaciones TEXT NULL,
    consumidor VARCHAR(255) NULL,
    fecha_pago DATETIME NULL,
    PRIMARY KEY (id_venta),
    CONSTRAINT fk_ventas_cliente
        FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS pago_empresa (
    id BIGINT NOT NULL AUTO_INCREMENT,
    empresa_id BIGINT NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    tipo_periodicidad VARCHAR(50) NOT NULL,
    cuit VARCHAR(255) NULL,
    fecha DATETIME NOT NULL,
    numero_pago INT NULL,
    monto DECIMAL(19,2) NOT NULL,
    monto_con_iva DECIMAL(19,2) NULL,
    factura VARCHAR(255) NULL,
    estado VARCHAR(50) NOT NULL,
    observacion VARCHAR(255) NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS pago_parcial (
    id BIGINT NOT NULL AUTO_INCREMENT,
    fecha_pago DATETIME NOT NULL,
    payer_name VARCHAR(255) NULL,
    cuit VARCHAR(50) NULL,
    factura VARCHAR(255) NULL,
    observaciones TEXT NULL,
    monto_total DECIMAL(19,2) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS pago_parcial_venta (
    pago_parcial_id BIGINT NOT NULL,
    id_venta BIGINT NOT NULL,
    PRIMARY KEY (pago_parcial_id, id_venta),
    CONSTRAINT fk_ppv_pago
        FOREIGN KEY (pago_parcial_id) REFERENCES pago_parcial(id),
    CONSTRAINT fk_ppv_venta
        FOREIGN KEY (id_venta) REFERENCES ventas(id_venta)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS gastos (
    id_gasto BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(255) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    monto DECIMAL(19,2) NOT NULL,
    pagar BOOLEAN NULL,
    fecha DATETIME NOT NULL,
    dia VARCHAR(255) NOT NULL,
    observacion VARCHAR(255) NULL,
    PRIMARY KEY (id_gasto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS gastos_fijos (
    id_gasto_fijo BIGINT NOT NULL AUTO_INCREMENT,
    fecha DATE NOT NULL,
    detalle VARCHAR(255) NOT NULL,
    estado BOOLEAN NOT NULL,
    monto DECIMAL(19,2) NOT NULL,
    es_personal BOOLEAN NOT NULL,
    observacion VARCHAR(255) NULL,
    PRIMARY KEY (id_gasto_fijo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS gastos_individuales (
    id_gasto_individual BIGINT NOT NULL AUTO_INCREMENT,
    fecha DATE NOT NULL,
    detalle VARCHAR(255) NOT NULL,
    monto DECIMAL(19,2) NOT NULL,
    id_empleado BIGINT NOT NULL,
    PRIMARY KEY (id_gasto_individual),
    CONSTRAINT fk_gastos_individuales_empleado
        FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
