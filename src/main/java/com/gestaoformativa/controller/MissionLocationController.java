package com.gestaoformativa.controller;

import com.gestaoformativa.dto.MissionLocationDTO;
import com.gestaoformativa.dto.UserDTO;
import com.gestaoformativa.model.MissionLocation;
import com.gestaoformativa.model.User;
import com.gestaoformativa.service.MissionLocationService;
import com.gestaoformativa.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/locations")
@Tag(name = "Locais de Missão", description = "Gerenciamento de locais de missão")
@SecurityRequirement(name = "bearer-jwt")
public class MissionLocationController {

    @Autowired
    private MissionLocationService locationService;

    @Autowired
    private UserService userService;

    @Operation(summary = "Listar locais", description = "Retorna todos os locais de missão")
    @ApiResponse(responseCode = "200", description = "Locais listados com sucesso")
    @GetMapping
    public ResponseEntity<List<MissionLocationDTO>> getAllLocations(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        List<MissionLocation> locations = locationService.getAllLocations();

        List<MissionLocationDTO> locationDTOs = locations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(locationDTOs);
    }

    @Operation(summary = "Obter local por ID", description = "Retorna um local específico pelo seu ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Local encontrado"),
            @ApiResponse(responseCode = "404", description = "Local não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MissionLocationDTO> getLocationById(@PathVariable Long id,
                                                              @AuthenticationPrincipal UserDetails userDetails) {
        try {
            MissionLocation location = locationService.getLocationById(id);
            return ResponseEntity.ok(convertToDTO(location));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Locais por cidade", description = "Retorna locais filtrados por cidade")
    @ApiResponse(responseCode = "200", description = "Locais listados com sucesso")
    @GetMapping("/by-city/{city}")
    public ResponseEntity<List<MissionLocationDTO>> getLocationsByCity(@PathVariable String city) {
        List<MissionLocation> locations = locationService.getLocationsByCity(city);

        List<MissionLocationDTO> locationDTOs = locations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(locationDTOs);
    }

    @Operation(summary = "Locais por estado", description = "Retorna locais filtrados por estado")
    @ApiResponse(responseCode = "200", description = "Locais listados com sucesso")
    @GetMapping("/by-state/{state}")
    public ResponseEntity<List<MissionLocationDTO>> getLocationsByState(@PathVariable String state) {
        List<MissionLocation> locations = locationService.getLocationsByState(state);

        List<MissionLocationDTO> locationDTOs = locations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(locationDTOs);
    }

    @Operation(summary = "Criar local", description = "Cria um novo local de missão")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Local criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para gerenciar locais")
    })
    @PostMapping
    public ResponseEntity<MissionLocationDTO> createLocation(@Valid @RequestBody MissionLocationDTO locationDTO,
                                                             @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        if (!currentUser.hasPermission("users")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            MissionLocation location = convertToEntity(locationDTO);
            MissionLocation createdLocation = locationService.createLocation(location);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(createdLocation));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Atualizar local", description = "Atualiza um local existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Local atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para gerenciar locais"),
            @ApiResponse(responseCode = "404", description = "Local não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<MissionLocationDTO> updateLocation(@PathVariable Long id,
                                                             @Valid @RequestBody MissionLocationDTO locationDTO,
                                                             @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        if (!currentUser.hasPermission("users")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            MissionLocation location = convertToEntity(locationDTO);
            MissionLocation updatedLocation = locationService.updateLocation(id, location);
            return ResponseEntity.ok(convertToDTO(updatedLocation));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Excluir local", description = "Exclui um local existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Local excluído com sucesso"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para gerenciar locais"),
            @ApiResponse(responseCode = "404", description = "Local não encontrado"),
            @ApiResponse(responseCode = "409", description = "Conflito - local não pode ser excluído")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        if (!currentUser.hasPermission("users")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            locationService.deleteLocation(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @Operation(summary = "Atribuir coordenador", description = "Atribui um coordenador a um local")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Coordenador atribuído com sucesso"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para gerenciar locais"),
            @ApiResponse(responseCode = "404", description = "Local ou usuário não encontrado")
    })
    @PutMapping("/{locationId}/coordinator/{userId}")
    public ResponseEntity<MissionLocationDTO> assignCoordinator(@PathVariable Long locationId,
                                                                @PathVariable Long userId,
                                                                @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        if (!currentUser.hasPermission("users")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            MissionLocation updatedLocation = locationService.assignCoordinator(locationId, userId);
            return ResponseEntity.ok(convertToDTO(updatedLocation));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Usuários do local", description = "Retorna usuários associados a um local")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuários listados com sucesso"),
            @ApiResponse(responseCode = "404", description = "Local não encontrado")
    })
    @GetMapping("/{id}/users")
    public ResponseEntity<List<UserDTO>> getUsersByLocation(@PathVariable Long id,
                                                            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            MissionLocation location = locationService.getLocationById(id);

            List<UserDTO> userDTOs = location.getAssignedUsers().stream()
                    .map(this::convertUserToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(userDTOs);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Métodos auxiliares
    private MissionLocationDTO convertToDTO(MissionLocation location) {
        MissionLocationDTO dto = new MissionLocationDTO();
        dto.setId(location.getId());
        dto.setName(location.getName());
        dto.setDescription(location.getDescription());
        dto.setCity(location.getCity());
        dto.setState(location.getState());
        dto.setCountry(location.getCountry());
        dto.setAddress(location.getAddress());
        dto.setPostalCode(location.getPostalCode());

        if (location.getCoordinator() != null) {
            dto.setCoordinatorId(location.getCoordinator().getId());
            dto.setCoordinatorName(location.getCoordinator().getName());
        }

        dto.setFullAddress(location.getFullAddress());
        dto.setUserCount(location.getUserCount());

        return dto;
    }

    private MissionLocation convertToEntity(MissionLocationDTO dto) {
        MissionLocation location = new MissionLocation();

        location.setName(dto.getName());
        location.setDescription(dto.getDescription());
        location.setCity(dto.getCity());
        location.setState(dto.getState());
        location.setCountry(dto.getCountry());
        location.setAddress(dto.getAddress());
        location.setPostalCode(dto.getPostalCode());

        if (dto.getCoordinatorId() != null) {
            User coordinator = new User();
            coordinator.setId(dto.getCoordinatorId());
            location.setCoordinator(coordinator);
        }

        return location;
    }

    private UserDTO convertUserToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setLifeStage(user.getLifeStage());

        if (user.getRole() != null) {
            dto.setRoleId(user.getRole().getId());
            dto.setRoleName(user.getRole().getName());
        }

        return dto;
    }
}