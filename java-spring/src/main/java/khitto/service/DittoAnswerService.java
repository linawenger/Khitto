package khitto.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.ditto.java.Ditto;
import com.ditto.java.DittoError;
import com.ditto.java.DittoQueryResult;
import com.ditto.java.serialization.DittoCborSerializable;
import org.springframework.stereotype.Component;

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

    public void saveAll(List<khitto.model.Answer> answers) {
        Ditto ditto = dittoService.getDitto();

        for (khitto.model.Answer a : answers) {
            DittoQueryResult result = ditto.getStore()
                                           .execute(
                                                   "INSERT INTO %s DOCUMENTS (:newAnswer)".formatted(ANSWERS_COLLECTION_NAME),
                                                   DittoCborSerializable.Dictionary.buildDictionary()
                                                                                   .put("newAnswer",
                                                                                           DittoCborSerializable.Dictionary.buildDictionary()
                                                                                                                           .put("uid", a.getUid())
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

    private void closeQuietly(DittoQueryResult result) {
        if (result == null) return;
        try {result.close();}
        catch (IOException ignored) {}
    }

    public String getCountByUid(String uid) {
        Ditto ditto = dittoService.getDitto();

        DittoQueryResult result = ditto.getStore()
                                       .execute(
                                               "SELECT * FROM %s WHERE uid = :uid"
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

    private void updateCount(String uid, String oldCount, String newCount) {
        //only updates if count unchanged
        Ditto ditto = dittoService.getDitto();

        DittoQueryResult result = ditto.getStore()
                                       .execute(
                                               "UPDATE %s SET count = :newCount WHERE uid = :uid AND count = :oldCount"
                                                       .formatted(ANSWERS_COLLECTION_NAME),
                                               DittoCborSerializable.Dictionary.buildDictionary()
                                                                               .put("uid", uid)
                                                                               .put("oldCount", oldCount)
                                                                               .put("newCount", newCount)
                                                                               .build()
                                       )
                                       .toCompletableFuture()
                                       .join();
        closeQuietly(result);
    }

    public void incrementCountWithRetry(String uid) {
        final int maxRetries = 20;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            String oldString = getCountByUid(uid);
            int oldInteger;
            try {
                oldInteger = Integer.parseInt(oldString);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Error reading count from db:" +e);
            }

            int newCount = oldInteger + 1;
            String newString = String.valueOf(newCount);
            updateCount(uid, oldString, newString);
            if (newString.equals(getCountByUid(uid))) return;

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        throw new RuntimeException("increment count failed after max retries");
    }



}
