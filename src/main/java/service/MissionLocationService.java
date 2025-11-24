package service;

import model.MissionLocation;
import model.User;
import repository.MissionLocationRepository;
import repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class MissionLocationService {

    @Autowired
    private MissionLocationRepository locationRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Busca todos os locais de missão
     */
    public List<MissionLocation> getAllLocations() {
        return locationRepository.findAll();
    }

    /**
     * Busca um local de missão pelo ID
     */
    public MissionLocation getLocationById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Local de missão não encontrado com id: " + id));
    }

    /**
     * Busca locais de missão por cidade
     */
    public List<MissionLocation> getLocationsByCity(String city) {
        return locationRepository.findByCity(city);
    }

    /**
     * Busca locais de missão por estado
     */
    public List<MissionLocation> getLocationsByState(String state) {
        return locationRepository.findByState(state);
    }

    /**
     * Cria um novo local de missão
     */
    public MissionLocation createLocation(MissionLocation location) {
        // Definir o coordenador, se fornecido o ID
        if (location.getCoordinator() != null && location.getCoordinator().getId() != null) {
            User coordinator = userRepository.findById(location.getCoordinator().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Coordenador não encontrado com id: "
                            + location.getCoordinator().getId()));
            location.setCoordinator(coordinator);
        }

        return locationRepository.save(location);
    }

    /**
     * Atualiza um local de missão existente
     */
    public MissionLocation updateLocation(Long id, MissionLocation locationDetails) {
        MissionLocation location = getLocationById(id);

        location.setName(locationDetails.getName());
        location.setDescription(locationDetails.getDescription());
        location.setCity(locationDetails.getCity());
        location.setState(locationDetails.getState());
        location.setCountry(locationDetails.getCountry());
        location.setAddress(locationDetails.getAddress());
        location.setPostalCode(locationDetails.getPostalCode());

        // Atualizar o coordenador, se fornecido
        if (locationDetails.getCoordinator() != null && locationDetails.getCoordinator().getId() != null) {
            User coordinator = userRepository.findById(locationDetails.getCoordinator().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Coordenador não encontrado com id: "
                            + locationDetails.getCoordinator().getId()));
            location.setCoordinator(coordinator);
        }

        return locationRepository.save(location);
    }

    /**
     * Exclui um local de missão
     */
    public void deleteLocation(Long id) {
        MissionLocation location = getLocationById(id);

        // Verificar se existem usuários associados a este local
        if (location.getAssignedUsers() != null && !location.getAssignedUsers().isEmpty()) {
            throw new IllegalStateException("Não é possível excluir um local de missão que possui usuários associados");
        }

        locationRepository.delete(location);
    }

    /**
     * Atribui um coordenador a um local de missão
     */
    public MissionLocation assignCoordinator(Long locationId, Long userId) {
        MissionLocation location = getLocationById(locationId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com id: " + userId));

        location.setCoordinator(user);
        return locationRepository.save(location);
    }
}