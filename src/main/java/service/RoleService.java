package service;

import model.Role;
import repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Busca todos os papéis (roles) disponíveis
     */
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    /**
     * Busca um papel pelo ID
     */
    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Papel não encontrado com id: " + id));
    }

    /**
     * Busca um papel pelo nome
     */
    public Role getRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Papel não encontrado com nome: " + name));
    }

    /**
     * Cria um novo papel
     */
    public Role createRole(Role role) {
        // Verificar se já existe um papel com o mesmo nome
        if (roleRepository.findByName(role.getName()).isPresent()) {
            throw new IllegalArgumentException("Já existe um papel com o nome: " + role.getName());
        }
        return roleRepository.save(role);
    }

    /**
     * Atualiza um papel existente
     */
    public Role updateRole(Long id, Role roleDetails) {
        Role role = getRoleById(id);

        // Verificar se está tentando mudar o nome para um que já existe
        if (!role.getName().equals(roleDetails.getName()) &&
                roleRepository.findByName(roleDetails.getName()).isPresent()) {
            throw new IllegalArgumentException("Já existe um papel com o nome: " + roleDetails.getName());
        }

        role.setName(roleDetails.getName());
        role.setDescription(roleDetails.getDescription());
        role.setCanManageUsers(roleDetails.getCanManageUsers());
        role.setCanManageRoles(roleDetails.getCanManageRoles());
        role.setCanManageStages(roleDetails.getCanManageStages());
        role.setCanManageDocuments(roleDetails.getCanManageDocuments());

        return roleRepository.save(role);
    }

    /**
     * Exclui um papel
     */
    public void deleteRole(Long id) {
        Role role = getRoleById(id);

        // Verificar se existem usuários usando este papel
        if (role.getUsers() != null && !role.getUsers().isEmpty()) {
            throw new IllegalStateException("Não é possível excluir um papel que está sendo usado por usuários");
        }

        roleRepository.delete(role);
    }
}