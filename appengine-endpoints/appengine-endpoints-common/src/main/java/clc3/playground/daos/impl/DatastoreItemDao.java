package clc3.playground.daos.impl;

import clc3.playground.daos.ItemDao;
import clc3.playground.models.Item;
import com.google.cloud.datastore.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DatastoreItemDao implements ItemDao {
    private static final String ITEM_KIND = "Item";
    private final Datastore datastore;
    private final KeyFactory keyFactory;

    public DatastoreItemDao() {
        datastore = DatastoreOptions.getDefaultInstance().getService();
        keyFactory = datastore.newKeyFactory().setKind(ITEM_KIND);
    }

    @Override
    public Item insert(Item item) {
        Entity itemEntity = buildEntity(item);
        datastore.add(itemEntity);
        return item;
    }

    public Item update(Item item) {
        Entity itemEntity = buildEntity(item);
        datastore.update(itemEntity);
        return item;
    }

    @Override
    public Item findById(String itemId) {
        Entity itemEntity = datastore.get(keyFactory.newKey(itemId));
        if (itemEntity != null) {
            return convertEntityToItem(itemEntity);
        }
        return null;
    }

    @Override
    public List<Item> findAll() {
        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind(ITEM_KIND)
                .setOrderBy(StructuredQuery.OrderBy.asc(Item.NAME))
                .build();
        QueryResults<Entity> resultList = datastore.run(query);
        return convertEntitiesToItems(resultList);
    }

    private Entity buildEntity(Item item){
        Key key = keyFactory.newKey(item.getId());
        return Entity.newBuilder(key)
                .set(Item.NAME, item.getName())
                .set(Item.AVAILABLE, item.isAvailable())
                .set(Item.PUBLICURL, item.getPublicUrl())
                .set(Item.TAGS, convertListToValueList(item.getTags()))
                .build();
    }

    private Item convertEntityToItem(Entity entity) {
        return new Item(
                entity.getKey().getName(),
                entity.getString(Item.NAME),
                entity.getBoolean(Item.AVAILABLE),
                entity.getString(Item.PUBLICURL),
                convertValueListToList(entity.getList(Item.TAGS)));
    }

    private List<Item> convertEntitiesToItems(QueryResults<Entity> resultList) {
        List<Item> results = new ArrayList<>();
        while (resultList.hasNext()) {
            results.add(convertEntityToItem(resultList.next()));
        }
        return results;
    }

    private List<Value<String>> convertListToValueList(List<String> list) {
        List<Value<String>> valueList = new ArrayList<>();
        for (String s : list) {
            valueList.add(StringValue.of(s));
        }
        return valueList;
    }

    private List<String> convertValueListToList(List<Value<String>> valueList){
        List<String> list = new LinkedList<>();
        for(Value<String> s : valueList){
            list.add(s.get());
        }
        return list;
    }
}
