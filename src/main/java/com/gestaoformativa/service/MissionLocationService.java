package com.gestaoformativa.service;

import com.gestaoformativa.model.MissionLocation;
import com.gestaoformativa.model.User;
import com.gestaoformativa.repository.MissionLocationRepository;
import com.gestaoformativa.repository.UserRepository;
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

    public List<MissionLocation> getAllLocations() {
        return locationRepository.findAll();
    }

    public MissionLocation getLocationById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Local de missão não encontrado com id: " + id));
    }

    public List<MissionLocation> getLocationsByCity(String city) {
        return locationRepository.findByCity(city);
    }

    public List<MissionLocation> getLocationsByState(String state) {
        return locationRepository.findByState(state);
    }

    public MissionLocation createLocation(MissionLocation location) {
        if (location.getCoordinator() != null && location.getCoordinator().getId() != null) {
            User coordinator = userRepository.findById(location.getCoordinator().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Coordenador não encontrado com id: "
                            + location.getCoordinator().getId()));
            location.setCoordinator(coordinator);
        }

        return locationRepository.save(location);
    }

    public MissionLocation updateLocation(Long id, MissionLocation locationDetails) {
        MissionLocation location = getLocationById(id);

        location.setName(locationDetails.getName());
        location.setDescription(locationDetails.getDescription());
        location.setCity(locationDetails.getCity());
        location.setState(locationDetails.getState());
        location.setCountry(locationDetails.getCountry());
        location.setAddress(locationDetails.getAddress());
        location.setPostalCode(locationDetails.getPostalCode());

        if (locationDetails.getCoordinator() != null && locationDetails.getCoordinator().getId() != null) {
            User coordinator = userRepository.findById(locationDetails.getCoordinator().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Coordenador não encontrado com id: "
                            + locationDetails.getCoordinator().getId()));
            location.setCoordinator(coordinator);
        }

        return locationRepository.save(location);
    }

    public void deleteLocation(Long id) {
        MissionLocation location = getLocationById(id);

        if (location.getAssignedUsers() != null && !location.getAssignedUsers().isEmpty()) {
            throw new IllegalStateException("Não é possível excluir um local de missão que possui usuários associados");
        }

        locationRepository.delete(location);
    }

    public MissionLocation assignCoordinator(Long locationId, Long userId) {
        MissionLocation location = getLocationById(locationId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com id: " + userId));

        location.setCoordinator(user);
        return locationRepository.save(location);
    }
}