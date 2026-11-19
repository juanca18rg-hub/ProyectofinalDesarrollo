package service;

import model.Booster;
import model.Carta;
import model.Producto;
import model.Rareza;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class BoosterService {
    private final CatalogoService catalogo;
    private final Random random = new Random();

    public BoosterService(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }

    public Booster crear(String nombre, double precioUsd) {
        List<Carta> cartas = new ArrayList<>();
        for (Producto producto : catalogo.listar()) {
            if (producto instanceof Carta carta) {
                cartas.add(carta);
            }
        }

        if (cartas.isEmpty()) {
            throw new IllegalStateException("Registra al menos una carta");
        }

        List<Carta> contenido = new ArrayList<>();
        for (int i = 0; i < Booster.CANTIDAD_CARTAS; i++) {
            contenido.add(elegirCarta(cartas));
        }

        Booster booster = new Booster(UUID.randomUUID().toString(), nombre, precioUsd, contenido);
        catalogo.agregar(booster);
        return booster;
    }

    private Carta elegirCarta(List<Carta> cartas) {
        Rareza[] rarezas = Rareza.values();
        int numero = random.nextInt(Rareza.sumaProbabilidades());
        int indice = 0;
        while (numero >= rarezas[indice].getProbabilidad()) {
            numero -= rarezas[indice].getProbabilidad();
            indice++;
        }

        for (int i = indice; i >= 0; i--) {
            List<Carta> candidatas = cartasDeRareza(cartas, rarezas[i]);
            if (!candidatas.isEmpty()) {
                return candidatas.get(random.nextInt(candidatas.size()));
            }
        }
        for (int i = indice + 1; i < rarezas.length; i++) {
            List<Carta> candidatas = cartasDeRareza(cartas, rarezas[i]);
            if (!candidatas.isEmpty()) {
                return candidatas.get(random.nextInt(candidatas.size()));
            }
        }
        throw new IllegalStateException("No hay cartas disponibles");
    }

    private List<Carta> cartasDeRareza(List<Carta> cartas, Rareza rareza) {
        List<Carta> resultado = new ArrayList<>();
        for (Carta carta : cartas) {
            if (carta.getRareza() == rareza) {
                resultado.add(carta);
            }
        }
        return resultado;
    }
}
