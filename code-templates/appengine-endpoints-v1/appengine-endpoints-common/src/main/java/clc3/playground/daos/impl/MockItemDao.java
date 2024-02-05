package clc3.playground.daos.impl;

import clc3.playground.daos.ItemDao;
import clc3.playground.models.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockItemDao implements ItemDao {
    private final Map<String, Item> data = new HashMap<>();

    @Override
    public Item insert(Item item) {
        return update(item);
    }

    @Override
    public Item findById(String id) {
        return data.get(id);
    }

    @Override
    public Item update(Item item) {
        data.put(item.getId(), item);
        return item;
    }

    @Override
    public List<Item> findAll() {
        return new ArrayList<>(data.values());
    }
}
