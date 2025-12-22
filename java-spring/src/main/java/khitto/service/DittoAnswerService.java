package khitto.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.ditto.java.Ditto;
import com.ditto.java.DittoError;
import com.ditto.java.DittoQueryResult;
import com.ditto.java.serialization.DittoCborSerializable;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class DittoAnswerService {

    private static final String ANSWERS_COLLECTION_NAME = "answers";
    private final DittoService dittoService;
    private final DittoObservationService observationService;

    public DittoAnswerService(DittoService dittoService,
                              DittoObservationService observationService) {
        this.dittoService = dittoService;
        this.observationService = observationService;

        Ditto ditto = dittoService.getDitto();
        try {
            ditto.getSync().registerSubscription("SELECT * FROM %s".formatted(ANSWERS_COLLECTION_NAME));
        } catch (DittoError ignored) {}
    }

    public List<khitto.model.Answer> findByQuestionId(int questionId) {
        Ditto ditto = dittoService.getDitto();
        DittoQueryResult result = ditto.getStore()
                                       .execute(
                                               "SELECT * FROM %s WHERE questionId = :qid".formatted(ANSWERS_COLLECTION_NAME),
                                               DittoCborSerializable.Dictionary.buildDictionary()
                                                                               .put("qid", String.valueOf(questionId))
                                                                               .build())
                                       .toCompletableFuture()
                                       .join();

        try {
            return result.getItems().stream()
                         .map(ItemToModel::answer)
                         .collect(Collectors.toList());
        } finally {
            closeQuietly(result);
        }
    }

    public int getNextId() {
        List<khitto.model.Answer> all = findAllInternal();
        return all.stream().mapToInt(khitto.model.Answer::getId).max().orElse(0) + 1;
    }

    public void saveAll(List<khitto.model.Answer> answers) {
        Ditto ditto = dittoService.getDitto();

        for (khitto.model.Answer a : answers) {
            DittoQueryResult result = ditto.getStore()
                                           .execute(
                                                   "INSERT INTO %s DOCUMENTS (:newAnswer)".formatted(ANSWERS_COLLECTION_NAME),
                                                   DittoCborSerializable.Dictionary.buildDictionary()
                                                                                   .put("newAnswer",
                                                                                           DittoCborSerializable.Dictionary.buildDictionary()
                                                                                                                           .put("_id", UUID.randomUUID().toString())
                                                                                                                           .put("id", String.valueOf(a.getId()))
                                                                                                                           .put("questionId", String.valueOf(a.getQuestionId()))
                                                                                                                           .put("content", a.getContent())
                                                                                                                           .put("count", "0")
                                                                                                                           .build())
                                                                                   .build())
                                           .toCompletableFuture()
                                           .join();
            closeQuietly(result);
        }
    }

    public void deleteByQuestionId(int questionId) {
        Ditto ditto = dittoService.getDitto();
        DittoQueryResult result = ditto.getStore()
                                       .execute(
                                               "DELETE FROM %s WHERE questionId = :qid".formatted(ANSWERS_COLLECTION_NAME),
                                               DittoCborSerializable.Dictionary.buildDictionary()
                                                                               .put("qid", String.valueOf(questionId))
                                                                               .build()
                                       )
                                       .toCompletableFuture()
                                       .join();
        closeQuietly(result);
    }

    private List<khitto.model.Answer> findAllInternal() {
        Ditto ditto = dittoService.getDitto();
        DittoQueryResult result = ditto.getStore()
                                       .execute("SELECT * FROM %s".formatted(ANSWERS_COLLECTION_NAME))
                                       .toCompletableFuture()
                                       .join();

        try {
            return result.getItems().stream()
                         .map(ItemToModel::answer)
                         .collect(Collectors.toList());
        } finally {
            closeQuietly(result);
        }
    }

    private void closeQuietly(DittoQueryResult result) {
        if (result == null) return;
        try {result.close();}
        catch (IOException ignored) {}
    }

    //------------------------------------------------------- count handling

    // extracts count from DB for a spesific uid
    public String getCountByUid(String uid) {
        Ditto ditto = dittoService.getDitto();

        DittoQueryResult result = ditto.getStore()
                                       .execute(
                                               "SELECT count FROM %s WHERE _id = :uid"
                                                       .formatted(ANSWERS_COLLECTION_NAME),
                                               DittoCborSerializable.Dictionary.buildDictionary()
                                                                               .put("uid", uid)
                                                                               .build()
                                       )
                                       .toCompletableFuture()
                                       .join();

        try {
            var item  = result.getItems().get(0);
            var value = item.getValue();
            return ItemToModel.getString(value, "count");
        } finally {
            closeQuietly(result);
        }
    }

    //updates count attribute for a uid
    public void updateCountByUid(String uid, int newCount) {
        Ditto ditto = dittoService.getDitto();

        DittoQueryResult result = ditto.getStore()
                                       .execute(
                                               "UPDATE %s SET count = :count WHERE _id = :uid"
                                                       .formatted(ANSWERS_COLLECTION_NAME),
                                               DittoCborSerializable.Dictionary.buildDictionary()
                                                                               .put("uid", uid)
                                                                               .put("count", String.valueOf(newCount))
                                                                               .build()
                                       )
                                       .toCompletableFuture()
                                       .join();

        closeQuietly(result);
    }

    //handles incrementing the count field of a given uid
    public void incrementCount(String uid) {
        String countOld = getCountByUid(uid);
        int count = Integer.parseInt(countOld);
        count++;
        updateCountByUid(uid, count);
    }
}
