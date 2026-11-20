package api.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import model.Rol;
import model.Torneo;
import model.Usuario;
import service.AuthService;
import service.TorneoService;
import static spark.Spark.get;
import static spark.Spark.post;

public class TorneoController {
    private final AuthService auth;
    private final TorneoService torneos = new TorneoService();

    public TorneoController(AuthService auth) {
        this.auth = auth;
    }

    public void registrarRutas() {
        post("/torneos", (req, res) -> {
            Respuestas.usuario(req, res, auth, Rol.ADMIN);
            DatosTorneo datos = Respuestas.leer(req, DatosTorneo.class);
            Respuestas.exigir(datos.inicioInscripciones != null && datos.finInscripciones != null
                    && datos.fechaEvento != null, "Las fechas son obligatorias");

            LocalDateTime inicio = LocalDateTime.parse(datos.inicioInscripciones);
            LocalDateTime fin = LocalDateTime.parse(datos.finInscripciones);
            LocalDate fecha = LocalDate.parse(datos.fechaEvento);
            Respuestas.exigir(!fin.toLocalDate().isAfter(fecha),
                    "La inscripción debe cerrar a más tardar el día del torneo");

            Torneo torneo = new Torneo(UUID.randomUUID().toString(), datos.nombre,
                    inicio, fin, fecha, datos.maxParticipantes);
            torneos.crear(torneo);
            return Respuestas.json(res, 201, verTorneo(torneo));
        });

        get("/torneos", (req, res) -> {
            Respuestas.usuario(req, res, auth, null);
            List<Map<String, Object>> resultado = new ArrayList<>();
            for (Torneo torneo : torneos.listar()) {
                resultado.add(verTorneo(torneo));
            }
            return Respuestas.json(res, 200, resultado);
        });

        post("/torneos/:id/inscripciones", (req, res) -> {
            Usuario cliente = Respuestas.usuario(req, res, auth, Rol.CLIENTE);
            if (!torneos.inscribir(req.params(":id"), cliente.getId())) {
                Respuestas.error(res, 404, "Torneo inexistente");
            }
            return Respuestas.json(res, 201, Map.of("mensaje", "Inscripción registrada"));
        });
    }

    private Map<String, Object> verTorneo(Torneo torneo) {
        return Map.of("id", torneo.getId(), "nombre", torneo.getNombre(),
                "fechaEvento", torneo.getFechaEvento().toString(),
                "inicioInscripciones", torneo.getInicioInscripciones().toString(),
                "finInscripciones", torneo.getFinInscripciones().toString(),
                "maxParticipantes", torneo.getMaxParticipantes());
    }

    private static class DatosTorneo {
        String nombre;
        String inicioInscripciones;
        String finInscripciones;
        String fechaEvento;
        int maxParticipantes;
    }
}
//-------------------------