package clc3.playground.functions;

import clc3.playground.daos.ItemDao;
import clc3.playground.daos.impl.DatastoreItemDao;
import clc3.playground.functions.events.GcsEvent;
import clc3.playground.models.Item;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.cloud.storage.*;
import com.google.cloud.vision.v1.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ImageAnalysis implements BackgroundFunction<GcsEvent> {
    private static final Logger LOGGER = Logger.getLogger(ImageAnalysis.class.getName());

    private static final String FORMAT_GCS_PATH = "gs://%s/%s";
    private static final String FORMAT_GCS_PUBLIC = "https://storage.googleapis.com/%s/%s";

    private final Storage storage = StorageOptions.getDefaultInstance().getService();
    private final ItemDao itemDao = new DatastoreItemDao();

    @Override
    public void accept(GcsEvent gcsEvent, Context context) throws Exception {
        if (gcsEvent.getBucket() == null || gcsEvent.getName() == null) {
            LOGGER.severe("GCS Event is null");
            return;
        }

        String itemId = gcsEvent.getName();
        String bucketName = gcsEvent.getBucket();

        LOGGER.info(String.format("Analyzing %s", itemId));

        String gcsPath = String.format(FORMAT_GCS_PATH, bucketName, itemId);
        ImageSource imgSource = ImageSource.newBuilder().setImageUri(gcsPath).build();
        Image img = Image.newBuilder().setSource(imgSource).build();

        Feature feat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        List<AnnotateImageRequest> requests = new ArrayList<>();
        requests.add(request);

        // ImageAnnotatorClient: Service that performs Google Cloud Vision API detection tasks
        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            // Run image detection and annotation for a batch of images. 
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            // Individual responses to image annotation requests within the batch.
            List<AnnotateImageResponse> responses = response.getResponsesList();

            Item item = itemDao.findById(itemId);
            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    LOGGER.severe(String.format("Error: %s", res.getError().getMessage()));
                    return;
                }
                // add annotations to item
                for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
                    item.addTag(annotation.getDescription());
                }
            }

            // Set item as public accessible and define public url
            BlobId blobId = BlobId.of(bucketName, itemId);
            storage.createAcl(blobId, Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
            String publicUrl = String.format(FORMAT_GCS_PUBLIC, bucketName, itemId);

            item.setPublicUrl(publicUrl);
            item.setAvailable(true);
            itemDao.update(item);
        }
    }
}
