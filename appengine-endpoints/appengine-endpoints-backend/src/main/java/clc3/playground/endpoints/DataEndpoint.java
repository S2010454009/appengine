package clc3.playground.endpoints;

import clc3.playground.daos.ItemDao;
import clc3.playground.daos.impl.DatastoreItemDao;
import clc3.playground.endpoints.responses.SignUrlResponse;
import clc3.playground.models.Item;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.DefaultValue;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Api(
        name = "data",
        version = "v1"
)
public class DataEndpoint {
    private static final Logger LOGGER = Logger.getLogger(DataEndpoint.class.getName());

    private static final String BUCKET_NAME = System.getenv("ITEMS_BUCKET_NAME");

    private final Storage storage = StorageOptions.getDefaultInstance().getService();
    private final ItemDao itemDao = new DatastoreItemDao();

    public DataEndpoint(){

    }

    @ApiMethod(
            path = "items",
            name = "getAll",
            httpMethod = ApiMethod.HttpMethod.GET
    )
    public CollectionResponse<Item> getAllItems(@Named("onlyavailable") @DefaultValue("false") boolean onlyAvailable){
        LOGGER.log(Level.INFO, "getAllItems");
        List<Item> allItems = itemDao.findAll();

        if(onlyAvailable){
            List<Item> onlyAvailableItems = new ArrayList<>();
            for(Item i : allItems){
                if(i.isAvailable()) {
                    onlyAvailableItems.add(i);
                }
            }
            return CollectionResponse.<Item>builder().setItems(onlyAvailableItems).build();
        } else {
            return CollectionResponse.<Item>builder().setItems(allItems).build();
        }
    }

    @ApiMethod(
            path = "items",
            name = "create",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    public Item createItem(){
        LOGGER.log(Level.INFO, "createItem");

        Item item = new Item(UUID.randomUUID().toString());
        return itemDao.insert(item);
    }

    @ApiMethod(
            path = "items/{id}",
            name = "getById",
            httpMethod = ApiMethod.HttpMethod.GET
    )
    public Item getItemWithId(@Named("id") String id) throws NotFoundException {
        LOGGER.log(Level.INFO, "getItemWithId");
        Item item = itemDao.findById(id);
        if(item == null){
            throw new NotFoundException("item with id not found");
        }
        return item;
    }

    @ApiMethod(
            path = "items/{id}",
            name = "updateById",
            httpMethod = ApiMethod.HttpMethod.PUT
    )
    public Item updateItem(@Named("id") String id, Item item) throws BadRequestException, NotFoundException {
        LOGGER.log(Level.INFO, "updateItem");
        if(item == null){
            throw new BadRequestException("item was null");
        }

        if(!id.equals(item.getId())){
            throw new BadRequestException("path and item not matching");
        }

        Item i = itemDao.findById(id);
        if(i == null){
            throw new NotFoundException("item with id not found");
        }

        return itemDao.update(item);
    }

    @ApiMethod(
            path = "items/{id}/uploadurl",
            name = "getUploadUrl",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    public SignUrlResponse getSignedUploadUrl(@Named("id") String id, HttpServletRequest request) throws NotFoundException {
        Item item = itemDao.findById(id);
        if(item == null){
            throw new NotFoundException("item with id not found");
        }

        String contentType = request.getContentType();
        String url = storage.signUrl(
                BlobInfo.newBuilder(BUCKET_NAME, item.getId()).setContentType(contentType).build(),
                3,
                TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                Storage.SignUrlOption.withContentType()
        ).toString();

        return new SignUrlResponse(item.getId(), url);
    }
}
