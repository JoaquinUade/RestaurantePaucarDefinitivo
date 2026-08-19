package paucar.config;

import paucar.service.AdminService;
import paucar.service.CategoriasGastosService;
import paucar.service.EmpleadoService;
import paucar.service.GastosFijosService;
import paucar.service.GastosIndividualesService;
import paucar.service.GastosVariablesService;
import paucar.service.StockService;
public class ServiceContainer {

    public final AdminService adminService;
    public final GastosVariablesService gastosVariables;
    public final CategoriasGastosService categorias;
    public final EmpleadoService empleados;
    public final GastosIndividualesService gastosIndividuales;
    public final GastosFijosService gastosFijos;
    public final StockService stock;

    public ServiceContainer(String apiBase) {

        adminService = new AdminService(apiBase);
        gastosVariables = new GastosVariablesService(apiBase);
        categorias = new CategoriasGastosService(apiBase);
        empleados = new EmpleadoService(apiBase);
        gastosIndividuales = new GastosIndividualesService(apiBase);
        gastosFijos = new GastosFijosService(apiBase);
        stock = new StockService(apiBase);
    }
}