package cl.musicmania.api.service;

import cl.musicmania.api.dto.Dtos.MensajeDto;
import cl.musicmania.api.dto.Dtos.UsuarioDto;
import cl.musicmania.api.dto.Dtos.UsuarioRequest;
import cl.musicmania.api.dto.Mapeos;
import cl.musicmania.api.exception.ApiException;
import cl.musicmania.api.model.Usuario;
import cl.musicmania.api.repository.UsuarioRepository;
import cl.musicmania.api.security.SesionUsuario;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/** Mantenedor de usuarios (solo administradores). */
@Service
public class UsuarioService {

    private static final String ADMIN_PRINCIPAL = "admin@duoc.cl";
    private static final Set<String> ROLES = Set.of("Administrador", "Cliente");

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioDto> listar() {
        return usuarios.findAll().stream().map(Mapeos::usuario).toList();
    }

    @Transactional
    public UsuarioDto crear(UsuarioRequest r) {
        if (vacio(r.nombre()) || vacio(r.correo()) || vacio(r.password()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Por favor completa todos los campos obligatorios.");
        String rol = r.rol() == null ? "Cliente" : r.rol();
        if (!ROLES.contains(rol)) throw new ApiException(HttpStatus.BAD_REQUEST, "Rol inválido.");

        String correo = r.correo().trim().toLowerCase();
        if (usuarios.existsByCorreoIgnoreCase(correo))
            throw new ApiException(HttpStatus.CONFLICT, "El correo ingresado ya se encuentra registrado.");

        Usuario u = new Usuario();
        u.setNombre(r.nombre().trim());
        u.setRun(vacio(r.run()) ? null : r.run().trim());
        u.setCorreo(correo);
        u.setPasswordHash(encoder.encode(r.password().trim()));
        u.setRol(rol);
        return Mapeos.usuario(usuarios.save(u));
    }

    @Transactional
    public UsuarioDto actualizar(Long id, UsuarioRequest r) {
        if (vacio(r.nombre()) || vacio(r.correo()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "El nombre y el correo no pueden estar vacíos.");
        if (r.rol() != null && !ROLES.contains(r.rol()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Rol inválido.");

        Usuario u = buscar(id);
        if (ADMIN_PRINCIPAL.equalsIgnoreCase(u.getCorreo()) && r.rol() != null && !r.rol().equals("Administrador"))
            throw new ApiException(HttpStatus.BAD_REQUEST, "El Administrador Principal debe seguir siendo administrador.");

        String correo = r.correo().trim().toLowerCase();
        if (usuarios.existsByCorreoIgnoreCaseAndIdNot(correo, id))
            throw new ApiException(HttpStatus.CONFLICT, "El correo ingresado ya se encuentra registrado.");

        u.setNombre(r.nombre().trim());
        u.setRun(vacio(r.run()) ? null : r.run().trim());
        u.setCorreo(correo);
        if (r.rol() != null) u.setRol(r.rol());
        if (!vacio(r.password())) u.setPasswordHash(encoder.encode(r.password().trim())); // vacío = no cambiar
        return Mapeos.usuario(usuarios.save(u));
    }

    @Transactional
    public MensajeDto eliminar(Long id, SesionUsuario sesion) {
        Usuario u = buscar(id);
        if (ADMIN_PRINCIPAL.equalsIgnoreCase(u.getCorreo()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "No se puede eliminar la cuenta del Administrador Principal.");
        if (sesion != null && u.getId().equals(sesion.idUsuario()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "No puedes eliminar tu propia cuenta.");
        usuarios.delete(u);
        return new MensajeDto("Usuario eliminado.");
    }

    private Usuario buscar(Long id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
    }

    private static boolean vacio(String s) {
        return s == null || s.isBlank();
    }
}
