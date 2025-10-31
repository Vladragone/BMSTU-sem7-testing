package com.example.game.service.interfaces;

import com.example.game.model.LocationGroup;

import java.util.List;

public interface ILocationGroupService {
    LocationGroup addGroup(LocationGroup group);
    List<LocationGroup> getAllGroups();
    LocationGroup getGroupByName(String name);
    void deleteGroup(Long id);
}
