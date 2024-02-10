import application.App;
import application.sqlite.DBConnection;
import entries.event.EventEntry;
import entries.project.ProjectEntry;
import entries.task.SubTaskEntry;
import entries.task.TaskEntry;
import entries.text.TextEntry;
import enums.EntryType;
import enums.NotificationLevel;
import org.junit.BeforeClass;
import org.junit.Test;
import util.Util;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class DatabaseTests {

    final UUID eventUUID = UUID.fromString("6fc5690a-7ccb-46ad-af8a-1bb2b60661b7");
    final UUID taskUUID = UUID.fromString("397c8687-a82d-466a-84fa-2d742367552e");
    final UUID task2UUID = UUID.fromString("e5207779-8db6-43b1-89ce-009901fa2a06");
    final UUID projectUUID = UUID.fromString("7f7bc42c-1353-40ae-ae51-01858ca4d12c");
    final UUID noteUUID = UUID.fromString("44463dbe-d6ac-491f-97b2-0d9495049427");
    final UUID journalUUID = UUID.fromString("8570ac56-ac80-4882-ae1d-8314a9259c5d");

    final List<String> tags = List.of("tag1", "tag2", "tag3");


    private static  App app;
    final DBConnection db = app.getDatabase();

    @BeforeClass
    public static void init(){
        try {
            app = App.instance().init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    final SubTaskEntry subTaskNest = new SubTaskEntry(
            "Nested task",
            "Description of a nested task",
            List.of(),
            true,
            LocalDateTime.now().plusHours(1)
    );

    final SubTaskEntry subTask1 = new SubTaskEntry(
            "Sub Task 1",
            "Basic description of a subtask 1",
            List.of(subTaskNest),
            true,
            LocalDateTime.now().plusHours(2)
    );

    final SubTaskEntry subTask2 = new SubTaskEntry(
            "Sub Task 2",
            "Basic description of a subtask 2",
            List.of(),
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
                List.of(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusHours(2)),
                NotificationLevel.CRITICAL,
                false
        );

        EventEntry.Stub ogStub = event.getStub();
        db.upsertEvent(event);

        EventEntry.Stub dbStub = db.getEventStubByUUID(event.uuid());
        assert ogStub.equals(dbStub);

        EventEntry fullEntry = db.getEventByUUID(eventUUID);

        assert event.equals(fullEntry);
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
                taskUUID,
                path
        );

        db.upsertTask(task);

        // Assert db storage/retrieval
        TaskEntry.Stub ogStub = task.getStub();
        TaskEntry.Stub dbStub = db.getTaskStubByUUID(taskUUID);
        assert ogStub.equals(dbStub);

        //Write and assert Json load
        task.flushToDisk();
        TaskEntry readTask = db.getTaskByUUID(task.uuid());
        assert task.equals(readTask);
    }

    @Test
    public void projectTest() throws IOException {

        Path taskPath = Util.getEntriesPath(EntryType.TASK);

        Path projectPath = Util.getEntriesPath(EntryType.PROJECT );

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
                projectUUID,
                projectPath,
                "code"

        );

        db.upsertProject(project);

        // asset from db
        ProjectEntry.Stub ogStub = project.getStub();
        ProjectEntry.Stub dbStub = db.getProjectStubByUUID(project.uuid());
        assert ogStub.equals(dbStub);

        //assert from disk
        project.flushToDisk();
        ProjectEntry readProject = db.getProjectByUUID(projectUUID);

        // Remove the task objects since they are not in the json
        ProjectEntry woTasks = project.updateBuilder().setTaskObjs(List.of()).build();
        assert woTasks.equals(readProject);
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
        assert ogStub.equals(dbStub);

        journal.flushToDisk();
        TextEntry readJournal = db.getJournalEntryByUUID(journalUUID);
        assert journal.equals(readJournal);

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
        assert ogStub.equals(dbStub);

        note.flushToDisk();
        TextEntry readNote = db.getNoteEntryByUUID(noteUUID);
        assert note.equals(readNote);

    }
}
