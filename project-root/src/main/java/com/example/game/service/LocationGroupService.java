package com.example.game.service;

import com.example.game.model.LocationGroup;
import com.example.game.repository.LocationGroupRepository;
import com.example.game.service.interfaces.ILocationGroupService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LocationGroupService implements ILocationGroupService {

    private final LocationGroupRepository repository;

    public LocationGroupService(LocationGroupRepository repository) {
        this.repository = repository;
    }

    @Override
    public LocationGroup addGroup(LocationGroup group) {
        try {
            return repository.save(group);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при сохранении группы");
        }
    }

    @Override
    public List<LocationGroup> getAllGroups() {
        try {
            return repository.findAll();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при получении списка групп");
        }
    }

    @Override
    public LocationGroup getGroupByName(String name) {
        try {
            LocationGroup group = repository.findByName(name);
            if (group == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Группа не найдена");
            }
            return group;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при поиске группы");
        }
    }

    @Override
    public void deleteGroup(Long id) {
        try {
            if (!repository.existsById(id)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Группа не найдена");
            }
            repository.deleteById(id);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка при удалении группы");
        }
    }
}
