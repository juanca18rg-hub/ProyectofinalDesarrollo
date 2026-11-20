package api;

import model.Booster;
import model.Carta;
import model.Compra;
import model.Rareza;
import service.BoosterService;
import service.CatalogoService;
import service.CompraService;

public class DemoTienda {
    public static void main(String[] args) {
        CatalogoService catalogo = new CatalogoService();
        BoosterService boosters = new BoosterService(catalogo);
        CompraService compras = new CompraService(catalogo);

        // 1. Registrar cartas en el catálogo
        catalogo.agregar(new Carta("C1", "Dragón", 3.0, Rareza.R));
        catalogo.agregar(new Carta("C2", "Guardián", 2.0, Rareza.N));
        System.out.println("Cartas registradas: " + catalogo.listar().size());

        // 2. Crear un sobre con diez cartas
        Booster sobre = boosters.crear("Sobre inicial", 5.0);
        System.out.println("Sobre creado: " + sobre.getNombre());

        // 3. Comprar una unidad del sobre
        Compra compra = compras.registrar("cliente-demo", sobre, 1);
        System.out.println("Total en COP: " + compra.getTotalCop());
        System.out.println("Compras del cliente: "
                + compras.historial("cliente-demo").size());

        // 4. Abrir el sobre y mostrar su contenido
        System.out.println("Cartas del sobre:");
        for (Carta carta : sobre.abrir()) {
            System.out.println("- " + carta.getNombre() + " (" + carta.getRareza() + ")");
        }
    }
}