package clc3.playground.daos;

import clc3.playground.models.Item;

import java.util.List;

public interface ItemDao {
    Item insert(Item item);

    Item findById(String itemId);

    Item update(Item item);

    List<Item> findAll();
}
