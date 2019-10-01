package main.java;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.locks.ReentrantLock;

public class RecordMaster {
    private Duration fileDuration;
    private Protobuf.RecordStore.Builder currentRecordStore;
    private final ReentrantLock lock = new ReentrantLock();
    private LocalDateTime storeCreationTime;

    RecordMaster(long rolloverTimeInMinutes) {
        fileDuration = Duration.ofMinutes(rolloverTimeInMinutes);
        newStore();
    }

    private void newStore() {
        currentRecordStore = Protobuf.RecordStore.newBuilder();
        storeCreationTime = LocalDateTime.now();
    }

    public void closeStore() {
        if (currentRecordStore.getRecordCount() != 0) {
            try (FileOutputStream output = new FileOutputStream("/recordStore/" + storeCreationTime.toString() + ".proto.bin")) {
                currentRecordStore.build().writeTo(output);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void validateRecordStore() throws IOException {
        LocalDateTime currentTime = LocalDateTime.now();

        if (Duration.between(storeCreationTime, currentTime).compareTo(fileDuration) >= 0) {
            System.out.println("Initiating Rollover.");
            closeStore();
            newStore();
            System.out.println("Rollover complete.");
        }
    }

    public void addRecord(String name, long id) {
        lock.lock();
        try {
            validateRecordStore();

            Protobuf.Record.Builder pendingRecord = Protobuf.Record.newBuilder();
            pendingRecord.setName(name);
            pendingRecord.setId(id);

            currentRecordStore.addRecord(pendingRecord);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
