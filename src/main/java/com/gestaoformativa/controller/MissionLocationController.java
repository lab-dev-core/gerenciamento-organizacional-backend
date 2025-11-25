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

@RestController
@RequestMapping("/api/locations")
public class MissionLocationController {

    @Autowired
    private MissionLocationService locationService;

    @Autowired
    private UserService userService;

    // Obter todos os locais
    @GetMapping
    public ResponseEntity<List<MissionLocationDTO>> getAllLocations(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        List<MissionLocation> locations = locationService.getAllLocations();

        List<MissionLocationDTO> locationDTOs = locations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(locationDTOs);
    }

    // Obter um local específico por ID
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

    // Obter locais por cidade
    @GetMapping("/by-city/{city}")
    public ResponseEntity<List<MissionLocationDTO>> getLocationsByCity(@PathVariable String city) {
        List<MissionLocation> locations = locationService.getLocationsByCity(city);

        List<MissionLocationDTO> locationDTOs = locations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(locationDTOs);
    }

    // Obter locais por estado
    @GetMapping("/by-state/{state}")
    public ResponseEntity<List<MissionLocationDTO>> getLocationsByState(@PathVariable String state) {
        List<MissionLocation> locations = locationService.getLocationsByState(state);

        List<MissionLocationDTO> locationDTOs = locations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(locationDTOs);
    }

    // Criar um novo local
    @PostMapping
    public ResponseEntity<MissionLocationDTO> createLocation(@Valid @RequestBody MissionLocationDTO locationDTO,
                                                             @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // Verificar se o usuário tem permissão para gerenciar usuários (assumindo que quem gerencia usuários pode gerenciar locais)
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

    // Atualizar um local existente
    @PutMapping("/{id}")
    public ResponseEntity<MissionLocationDTO> updateLocation(@PathVariable Long id,
                                                             @Valid @RequestBody MissionLocationDTO locationDTO,
                                                             @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // Verificar se o usuário tem permissão para gerenciar usuários
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

    // Excluir um local
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // Verificar se o usuário tem permissão para gerenciar usuários
        if (!currentUser.hasPermission("users")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            locationService.deleteLocation(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            // Não é possível excluir um local que tem usuários associados
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    // Atribuir um coordenador a um local
    @PutMapping("/{locationId}/coordinator/{userId}")
    public ResponseEntity<MissionLocationDTO> assignCoordinator(@PathVariable Long locationId,
                                                                @PathVariable Long userId,
                                                                @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByUsername(userDetails.getUsername());

        // Verificar se o usuário tem permissão para gerenciar usuários
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

    // Obter usuários de um local específico
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

        // Adicionar informações resumidas
        dto.setFullAddress(location.getFullAddress());
        dto.setUserCount(location.getUserCount());

        return dto;
    }

    private MissionLocation convertToEntity(MissionLocationDTO dto) {
        MissionLocation location = new MissionLocation();

        // Não definimos o ID ao criar uma nova entidade
        // Se estamos atualizando, o ID será definido pelo método de serviço

        location.setName(dto.getName());
        location.setDescription(dto.getDescription());
        location.setCity(dto.getCity());
        location.setState(dto.getState());
        location.setCountry(dto.getCountry());
        location.setAddress(dto.getAddress());
        location.setPostalCode(dto.getPostalCode());

        // O coordenador será definido pelo serviço
        if (dto.getCoordinatorId() != null) {
            User coordinator = new User();
            coordinator.setId(dto.getCoordinatorId());
            location.setCoordinator(coordinator);
        }

        return location;
    }

    private UserDTO convertUserToDTO(User user) {
        UserDTO dto = new UserDTO();
        // Definir apenas os campos básicos para uma visualização resumida
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