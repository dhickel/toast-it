import io.mindspice.toastit.App;
import io.mindspice.toastit.notification.Reminder;
import io.mindspice.toastit.sqlite.DBConnection;
import io.mindspice.toastit.entries.event.EventEntry;
import io.mindspice.toastit.entries.project.ProjectEntry;
import io.mindspice.toastit.entries.task.SubTask;
import io.mindspice.toastit.entries.task.TaskEntry;
import io.mindspice.toastit.entries.text.TextEntry;
import io.mindspice.toastit.enums.EntryType;
import io.mindspice.toastit.enums.NotificationLevel;
import org.junit.BeforeClass;
import org.junit.Test;
import io.mindspice.toastit.util.Util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public class DatabaseTests {

    final UUID eventUUID = UUID.fromString("6fc5690a-7ccb-46ad-af8a-1bb2b60661b7");
    final UUID taskUUID = UUID.fromString("397c8687-a82d-466a-84fa-2d742367552e");
    final UUID task2UUID = UUID.fromString("e5207779-8db6-43b1-89ce-009901fa2a06");
    final UUID projectUUID = UUID.fromString("7f7bc42c-1353-40ae-ae51-01858ca4d12c");
    final UUID noteUUID = UUID.fromString("44463dbe-d6ac-491f-97b2-0d9495049427");
    final UUID journalUUID = UUID.fromString("8570ac56-ac80-4882-ae1d-8314a9259c5d");

    final List<String> tags = List.of("tag1", "tag2", "tag3");

    private static App app;
    final DBConnection db = app.getDatabase();

    @BeforeClass
    public static void init() {
        try {
            app = App.instance().init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    final SubTask subTaskNest = new SubTask(
            "Nested task",
            "Description of a nested task",
            true,
            LocalDateTime.now().plusHours(1)
    );

    final SubTask subTask1 = new SubTask(
            "Sub Task 1",
            "Basic description of a subtask 1",
            true,
            LocalDateTime.now().plusHours(2)
    );

    final SubTask subTask2 = new SubTask(
            "Sub Task 2",
            "Basic description of a subtask 2",
            true,
            LocalDateTime.now().plusHours(2)
    );

    public DatabaseTests() throws IOException { }

    @Test
    public void eventTests() throws IOException {
        var event = new EventEntry(
                eventUUID,
                "Test Event",
                tags,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                List.of(new Reminder(LocalDateTime.now().minusDays(1), NotificationLevel.CRITICAL)),
                UUID.randomUUID(),
                false
        );

        EventEntry.Stub ogStub = event.getStub();
        db.upsertEvent(event);

        EventEntry.Stub dbStub = db.getEventStubByUUID(event.uuid());
        assertEquals(ogStub, dbStub);

        EventEntry fullEntry = db.getEventByUUID(eventUUID);

        assertEquals(event, fullEntry);
    }

    @Test
    public void taskTests() throws IOException {

        Path path = Util.getEntriesPath(EntryType.TASK);

        var task = new TaskEntry(
                "Test Task",
                true,
                false,
                List.of(subTask1, subTask2),
                "This is a full description",
                List.of("Do this task on time", "Or else", "More notes"),
                tags,
                LocalDateTime.now().plusMonths(1),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(20),
                List.of(new Reminder(LocalDateTime.now().minusDays(1), NotificationLevel.CRITICAL),
                        new Reminder(LocalDateTime.now().minusDays(2), NotificationLevel.LOW)),
                taskUUID,
                path
        );

        db.upsertTask(task, false);

        // Assert db storage/retrieval
        TaskEntry.Stub ogStub = task.getStub();
        TaskEntry.Stub dbStub = db.getTaskStubByUUID(taskUUID);
        assertEquals(ogStub, dbStub);

        //Write and assert Json load
        task.flushToDisk();
        TaskEntry readTask = db.getTaskByUUID(task.uuid());
        assertEquals(task, readTask);
    }

    @Test
    public void projectTest() throws IOException {

        Path taskPath = Util.getEntriesPath(EntryType.TASK);

        Path projectPath = Util.getEntriesPath(EntryType.PROJECT);

        var task = new TaskEntry(
                "Test Task",
                true,
                false,
                List.of(subTask1, subTask2),
                "This is a full description",
                List.of("Do this task on time", "Or else", "More notes"),
                tags,
                LocalDateTime.now().plusMonths(1),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(20),
                List.of(new Reminder(LocalDateTime.now().minusDays(1), NotificationLevel.CRITICAL),
                        new Reminder(LocalDateTime.now().minusDays(10), NotificationLevel.CRITICAL)),
                taskUUID,
                taskPath
        );

        var task2 = new TaskEntry(
                "Test Task",
                true,
                false,
                List.of(subTask2),
                "This is a full description 2",
                List.of("Do this task on time2", "Or else2", "More notes2"),
                tags,
                LocalDateTime.now().plusMonths(1),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(20),
                List.of(new Reminder(LocalDateTime.now().minusDays(1), NotificationLevel.CRITICAL),
                        new Reminder(LocalDateTime.now().minusDays(2), NotificationLevel.LOW)),
                taskUUID,
                taskPath
        );

        var project = new ProjectEntry(
                "Project Test",
                true,
                true,
                List.of(taskUUID, task2UUID),
                List.of(task, task2),
                "This is a project",
                List.of("ProjectTag1", "ProjectTag2"),
                Path.of("/home/mindspice/code/java"),
                LocalDateTime.now().plusMonths(1),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(40),
                List.of(new Reminder(LocalDateTime.now().minusDays(1), NotificationLevel.CRITICAL),
                        new Reminder(LocalDateTime.now().minusDays(3), NotificationLevel.NORMAL)),
                projectUUID,
                projectPath,
                "code",
                true

        );

        db.upsertProject(project, false);

        // asset from db
        ProjectEntry.Stub ogStub = project.getStub();
        ProjectEntry.Stub dbStub = db.getProjectStubByUUID(project.uuid());
        assertEquals(ogStub, dbStub);

        //assert from disk
        project.flushToDisk();
        ProjectEntry readProject = db.getProjectByUUID(projectUUID);

        // Remove the task objects since they are not in the json
        var builder = project.updateBuilder();
        builder.taskObjs = List.of();
        ProjectEntry woTasks = builder.build();
        assertEquals(woTasks, readProject);
    }

    @Test
    public void journalTest() throws IOException {

        Path path = Util.getEntriesPath(EntryType.JOURNAL);

        TextEntry journal = new TextEntry(
                EntryType.JOURNAL,
                "Journal test",
                LocalDateTime.now(),
                tags,
                journalUUID,
                path
        );

        db.upsertJournal(journal);

        TextEntry.Stub ogStub = journal.getStub();
        TextEntry.Stub dbStub = db.getJournalStubByUUID(journal.uuid());
        assertEquals(ogStub, dbStub);

        journal.flushToDisk();
        TextEntry readJournal = db.getJournalEntryByUUID(journalUUID);
        assertEquals(journal, readJournal);

    }

    @Test
    public void noteTest() throws IOException {

        Path path = Util.getEntriesPath(EntryType.NOTE);

        TextEntry note = new TextEntry(
                EntryType.JOURNAL,
                "Test note",
                LocalDateTime.now(),
                tags,
                noteUUID,
                path
        );

        db.upsertNote(note);

        TextEntry.Stub ogStub = note.getStub();
        TextEntry.Stub dbStub = db.getNoteStubByUUID(note.uuid());
        assertEquals(ogStub, dbStub);

        note.flushToDisk();
        TextEntry readNote = db.getNoteEntryByUUID(noteUUID);
        assertEquals(note, readNote);

    }
}
