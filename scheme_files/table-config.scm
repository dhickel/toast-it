


;; createColum args: Header, Function obj field -> string, min width, max width

;; Events
(define event-manage-table
  (List:of
    (TableUtil:createColumn
      (JString "Index")
      (KFunction[Pair[Integer EventEntry] JString]
        (lambda (entry ::Pair[Integer EventEntry]) (JString (entry:first))))
      5 150)
    (TableUtil:createColumn
      (JString "Event Name")
      (KFunction[Pair[Integer EventEntry] JString]
        (lambda (entry ::Pair[Integer EventEntry]) (JString ((entry:second):name))))
      10 150)
    (TableUtil:createColumn
      (JString "Tags")
      (KFunction[Pair[Integer EventEntry] JString]
        (lambda (entry ::Pair[Integer EventEntry]) (JString ((entry:second):tags))))
      10 150)
    (TableUtil:createColumn
      (JString "Start Time")
      (KFunction[Pair[Integer EventEntry] JString]
        (lambda (entry ::Pair[Integer EventEntry]) (JString (DateTimeUtil:printDateTimeFull ((entry:second):startTime)))))
      10 150)
    (TableUtil:createColumn
      (JString "End Time")
      (KFunction[Pair[Integer EventEntry] JString]
        (lambda (entry ::Pair[Integer EventEntry]) (JString (DateTimeUtil:printDateTimeFull ((entry:second):endTime)))))
      10 150)
    (TableUtil:createColumn
      (JString "Has Happened")
      (KFunction[Pair[Integer EventEntry] JString]
        (lambda (entry ::Pair[Integer EventEntry]) (JString (((entry:second):endTime):isBefore (LocalDateTime:now)))))
      10 150)
    ))

(define event-overview-table
  (List:of
    (TableUtil:createColumn
      (JString "Event Name")
      (KFunction[EventEntry JString]
        (lambda (entry ::EventEntry) (JString (entry:name))))
      10 150)
    (TableUtil:createColumn
      (JString "Tags")
      (KFunction[EventEntry JString]
        (lambda (entry ::EventEntry) (JString (entry:tags))))
        10 150)
    (TableUtil:createColumn
      (JString "Start Time")
      (KFunction[EventEntry JString]
        (lambda (entry ::EventEntry) (JString (DateTimeUtil:printDateTimeFull (entry:startTime)))))
      10 150)
    (TableUtil:createColumn
      (JString "End Time")
      (KFunction[EventEntry JString]
        (lambda (entry ::EventEntry) (JString (DateTimeUtil:printDateTimeFull (entry:endTime)))))
      10 150)
    ))

(define (event-table-configs)
  (begin
    (set-static TableConfig `EVENT_MANAGE_TABLE event-manage-table)
    (set-static TableConfig `EVENT_OVERVIEW_TABLE event-overview-table)
    (set-static TableConfig `EVENT_DASHBOARD_FORMATTER
      (KFunction[EventEval String] (lambda (eval ::EventEval)
        (TableUtil:mergeAndPadTable 10 (eval:futureEventTable) (eval:pastEventTable) ))))
    ))

;; Tasks

(define task-overview-table
  (List:of
    (TableUtil:createColumn
      (JString "Task Name")
      (KFunction[TaskEntry JString]
        (lambda (entry ::TaskEntry) (JString (entry:name))))
      10 150)
    (TableUtil:createColumn
      (JString "Tags")
      (KFunction[TaskEntry JString]
        (lambda (entry ::TaskEntry) (JString (entry:tags))))
      10 150)
    (TableUtil:createColumn
      (JString "SubTask #")
      (KFunction[TaskEntry JString]
        (lambda (entry ::TaskEntry) (JString ((entry:subtasks):size))))
      10 150)
    (TableUtil:createColumn
      (JString "Started At")
      (KFunction[TaskEntry JString]
        (lambda (entry ::TaskEntry) (JString (DateTimeUtil:printDateTimeFull (entry:startedAt)))))
      10 150)
    (TableUtil:createColumn
      (JString "Due By")
      (KFunction[TaskEntry JString]
        (lambda (entry ::TaskEntry) (JString (DateTimeUtil:printDateTimeFull (entry:dueBy)))))
      10 150)
    (TableUtil:createColumn
      (JString "Overdue")
      (KFunction[TaskEntry JString]
        (lambda (entry ::TaskEntry) (JString ((entry:dueBy):isBefore (LocalDateTime:now)))))
      10 150)
    (TableUtil:createColumn
      (JString "Completion")
      (KFunction[TaskEntry JString]
        (lambda (entry ::TaskEntry) (JString (entry:completionPct))))
      10 150)
    ))

(define task-manage-table
  (List:of
    (TableUtil:createColumn
      (JString "Idx")
      (KFunction[Pair[Integer TaskEntry] JString]
        (lambda (entry ::Pair[Integer TaskEntry]) (JString (entry:first))))
      5 5)
    (TableUtil:createColumn
      (JString "Task Name")
      (KFunction[Pair[Integer TaskEntry] JString]
        (lambda (entry ::Pair[Integer TaskEntry]) (JString ((entry:second):name))))
      40 40)
    (TableUtil:createColumn
      (JString "Tags")
      (KFunction[Pair[Integer TaskEntry] JString]
        (lambda (entry ::Pair[Integer TaskEntry]) (JString ((entry:second):tags))))
      50 50)
    (TableUtil:createColumn
      (JString "SubTask #")
      (KFunction[Pair[Integer TaskEntry] JString]
        (lambda (entry ::Pair[Integer TaskEntry]) (JString (((entry:second):subtasks):size))))
      12 12)
    (TableUtil:createColumn
      (JString "Note #")
      (KFunction[Pair[Integer TaskEntry] JString]
        (lambda (entry ::Pair[Integer TaskEntry]) (JString  (((entry:second):notes):size))))
      11 11)
    (TableUtil:createColumn
      (JString "Reminder #")
      (KFunction[Pair[Integer TaskEntry] JString]
        (lambda (entry ::Pair[Integer TaskEntry]) (JString  (((entry:second):reminders):size))))
      15 15)
    (TableUtil:createColumn
      (JString "Started At")
      (KFunction[Pair[Integer TaskEntry] JString]
        (lambda (entry ::Pair[Integer TaskEntry]) (JString (DateTimeUtil:printDateTimeFull ((entry:second):startedAt)))))
      32 32)
    (TableUtil:createColumn
      (JString "Due By")
      (KFunction[Pair[Integer TaskEntry] JString]
        (lambda (entry ::Pair[Integer TaskEntry]) (JString (DateTimeUtil:printDateTimeFull ((entry:second):dueBy)))))
      32 32)
    (TableUtil:createColumn
      (JString "Completed At")
      (KFunction[Pair[Integer TaskEntry] JString]
        (lambda (entry ::Pair[Integer TaskEntry])
          (if ((entry:second):completed)
            (JString (DateTimeUtil:printDateTimeFull ((entry:second):completedAt)))
            (JString "Un-completed"))))
      32 32)
    (TableUtil:createColumn
      (JString "Completion")
      (KFunction[Pair[Integer TaskEntry] JString]
        (lambda (entry ::Pair[Integer TaskEntry]) (JString ((entry:second):completionPct))))
      12 12)
    ))

(define subtask-table
  (List:of
    (TableUtil:createColumn
      (JString "Idx")
      (KFunction[Pair[Integer SubTask] JString]
        (lambda (entry ::Pair[Integer SubTask]) (JString (entry:first))))
      5 5)
    (TableUtil:createColumn
      (JString "Name")
      (KFunction[Pair[Integer SubTask] JString]
        (lambda (entry ::Pair[Integer SubTask]) (JString ((entry:second):name))))
      40 40)
    (TableUtil:createColumn
      (JString "Description")
      (KFunction[Pair[Integer SubTask] JString]
        (lambda (entry ::Pair[Integer SubTask])
          (JString (TableUtil:wrapString (TableUtil:truncateString ((entry:second):description)) 170))))
      170 170)
    (TableUtil:createColumn
      (JString "Completed At")
      (KFunction[Pair[Integer SubTask] JString]
        (lambda (entry ::Pair[Integer SubTask])
          (if ((entry:second):completed)
            (JString (DateTimeUtil:printDateTimeFull ((entry:second):completedAt)))
            (JString "Un-completed"))))
      32 32)
    ))

(define (task-table-configs)
  (begin
    (set-static TableConfig `TASK_OVERVIEW_TABLE task-overview-table)
    (set-static TableConfig `TASK_MANAGE_TABLE task-manage-table)
    (set-static TableConfig `TASK_SUBTASK_TABLE subtask-table)
    (set-static TableConfig `TASK_DASHBOARD_FORMATTER
      (KFunction[TaskEval JString] (lambda (eval ::TaskEval) (eval:activeTaskTable))))
    (set-static TableConfig `TASK_VIEW_FORMATTER
      (KBiFunction[TaskEval TaskEntry JString] (lambda (eval ::TaskEval task ::TaskEntry) (eval:taskViewTable task))))
    ))


;; Projects

(define project-overview-table
  (List:of
    (TableUtil:createColumn
      (JString "Project Name")
      (KFunction[ProjectEntry JString]
        (lambda (entry ::ProjectEntry) (JString (entry:name))))
      10 150)
    (TableUtil:createColumn
      (JString "Tags")
      (KFunction[ProjectEntry JString]
        (lambda (entry ::ProjectEntry) (JString (entry:tags))))
      10 150)
    (TableUtil:createColumn
      (JString "Task #")
      (KFunction[ProjectEntry JString]
        (lambda (entry ::ProjectEntry) (JString ((entry:tasks):size))))
      10 150)
    (TableUtil:createColumn
      (JString "Started At")
      (KFunction[ProjectEntry JString]
        (lambda (entry ::ProjectEntry) (JString (DateTimeUtil:printDateTimeFull (entry:startedAt)))))
      10 150)
    (TableUtil:createColumn
      (JString "Due By")
      (KFunction[ProjectEntry JString]
        (lambda (entry ::ProjectEntry) (JString (DateTimeUtil:printDateTimeFull (entry:dueBy)))))
      10 150)
    (TableUtil:createColumn
      (JString "Overdue")
      (KFunction[ProjectEntry JString]
        (lambda (entry ::ProjectEntry) (JString ((entry:dueBy):isBefore (LocalDateTime:now)))))
      10 150)
    (TableUtil:createColumn
      (JString "Completion")
      (KFunction[ProjectEntry JString]
        (lambda (entry ::ProjectEntry) (JString (entry:completionPct))))
      10 150)
    ))

(define project-manage-table
  (List:of
    (TableUtil:createColumn
      (JString "Idx")
      (KFunction[Pair[Integer ProjectEntry] JString]
        (lambda (entry ::Pair[Integer ProjectEntry]) (JString (entry:first))))
      5 5)
    (TableUtil:createColumn
      (JString "Project Name")
      (KFunction[Pair[Integer ProjectEntry] JString]
        (lambda (entry ::Pair[Integer ProjectEntry]) (JString ((entry:second):name))))
      40 40)
    (TableUtil:createColumn
      (JString "Tags")
      (KFunction[Pair[Integer ProjectEntry] JString]
        (lambda (entry ::Pair[Integer ProjectEntry]) (JString ((entry:second):tags))))
      50 50)
    (TableUtil:createColumn
      (JString "Task #")
      (KFunction[Pair[Integer ProjectEntry] JString]
        (lambda (entry ::Pair[Integer ProjectEntry]) (JString (((entry:second):tasks):size))))
      12 12)
    (TableUtil:createColumn
      (JString "Note #")
      (KFunction[Pair[Integer ProjectEntry] JString]
        (lambda (entry ::Pair[Integer ProjectEntry]) (JString  (((entry:second):notes):size))))
      11 11)
    (TableUtil:createColumn
      (JString "Reminder #")
      (KFunction[Pair[Integer ProjectEntry] JString]
        (lambda (entry ::Pair[Integer ProjectEntry]) (JString  (((entry:second):reminders):size))))
      15 15)
    (TableUtil:createColumn
      (JString "Started At")
      (KFunction[Pair[Integer ProjectEntry] JString]
        (lambda (entry ::Pair[Integer ProjectEntry]) (JString (DateTimeUtil:printDateTimeFull ((entry:second):startedAt)))))
      32 32)
    (TableUtil:createColumn
      (JString "Due By")
      (KFunction[Pair[Integer ProjectEntry] JString]
        (lambda (entry ::Pair[Integer ProjectEntry]) (JString (DateTimeUtil:printDateTimeFull ((entry:second):dueBy)))))
      32 32)
    (TableUtil:createColumn
      (JString "Completed At")
      (KFunction[Pair[Integer ProjectEntry] JString]
        (lambda (entry ::Pair[Integer ProjectEntry])
          (if ((entry:second):completed)
            (JString (DateTimeUtil:printDateTimeFull ((entry:second):completedAt)))
            (JString "Un-completed"))))
      32 32)
    (TableUtil:createColumn
      (JString "Completion")
      (KFunction[Pair[Integer ProjectEntry] JString]
        (lambda (entry ::Pair[Integer ProjectEntry]) (JString ((entry:second):completionPct))))
      12 12)
    ))

(define project-task-table
  (List:of
    (TableUtil:createColumn
      (JString "Idx")
      (KFunction[Pair[Integer TaskEntry] JString]
        (lambda (entry ::Pair[Integer TaskEntry]) (JString (entry:first))))
      5 5)
    (TableUtil:createColumn
      (JString "SubTask #")
      (KFunction[Pair[Integer TaskEntry] JString]
        (lambda (entry ::Pair[Integer TaskEntry]) (JString (((entry:second):subtasks):size))))
      12 12)
    (TableUtil:createColumn
      (JString "Task Name")
      (KFunction[Pair[Integer TaskEntry] JString]
        (lambda (entry ::Pair[Integer TaskEntry]) (JString ((entry:second):name))))
      40 40)
    (TableUtil:createColumn
      (JString "Description")
      (KFunction[Pair[Integer TaskEntry] JString]
        (lambda (entry ::Pair[Integer TaskEntry])
          (JString (TableUtil:wrapString (TableUtil:truncateString ((entry:second):description)) 132))))
      134 134)
    (TableUtil:createColumn
      (JString "Started")
      (KFunction[Pair[Integer TaskEntry] JString]
        (lambda (entry ::Pair[Integer TaskEntry]) (JString ((entry:second):started))))
      9 9)
    (TableUtil:createColumn
      (JString "Due By")
      (KFunction[Pair[Integer TaskEntry] JString]
        (lambda (entry ::Pair[Integer TaskEntry]) (JString (DateTimeUtil:printDateTimeFull ((entry:second):dueBy)))))
      32 32)
    (TableUtil:createColumn
      (JString "Completion")
      (KFunction[Pair[Integer TaskEntry] JString]
        (lambda (entry ::Pair[Integer TaskEntry]) (JString ((entry:second):completionPct))))
      12 12)
      ))

(define (project-table-configs)
  (begin
    (set-static TableConfig `PROJECT_OVERVIEW_TABLE project-overview-table)
    (set-static TableConfig `PROJECT_MANAGE_TABLE project-manage-table)
    (set-static TableConfig `PROJECT_TASK_TABLE project-task-table)
    (set-static TableConfig `PROJECT_DASHBOARD_FORMATTER
      (KFunction[ProjectEval JString] (lambda (eval ::ProjectEval) (eval:activeProjectTable))))
    (set-static TableConfig `PROJECT_VIEW_FORMATTER
      (KBiFunction[ProjectEval ProjectEntry JString]
        (lambda (eval ::ProjectEval task ::ProjectEntry) (eval:projectViewTable task))))
    ))


;; Notes

(define text-overview-table
  (List:of
    (TableUtil:createColumn
      (JString "Name")
      (KFunction[TextEntry JString]
        (lambda (entry ::TextEntry) (JString (entry:name))))
      40 100)
    (TableUtil:createColumn
      (JString "Tags")
      (KFunction[TextEntry JString]
        (lambda (entry ::TextEntry) (JString (entry:tags))))
      50 100)
    (TableUtil:createColumn
      (JString "Created At")
      (KFunction[TextEntry JString]
        (lambda (entry ::TextEntry) (JString (DateTimeUtil:printDateTimeFull (entry:createdAt)))))
      32 32)
    ))

(define text-manage-table
  (List:of
    (TableUtil:createColumn
      (JString "Idx")
      (KFunction[Pair[Integer TextEntry] JString]
        (lambda (entry ::Pair[Integer TextEntry]) (JString (entry:first))))
      5 5)
    (TableUtil:createColumn
      (JString "Name")
      (KFunction[Pair[Integer ProjectEntry] JString]
        (lambda (entry ::Pair[TextEntry TextEntry]) (JString ((entry:second):name))))
      40 100)
    (TableUtil:createColumn
      (JString "Tags")
      (KFunction[Pair[Integer TextEntry] JString]
        (lambda (entry ::Pair[Integer TextEntry]) (JString ((entry:second):tags))))
      50 100)
    (TableUtil:createColumn
      (JString "Created At")
      (KFunction[Pair[Integer TextEntry] JString]
        (lambda (entry ::Pair[Integer TextEntry]) (JString (DateTimeUtil:printDateTimeFull ((entry:second):createdAt)))))
      32 32)
    ))

(define text-view
  (List:of
    (TableUtil:createColumn
      (JString "Contents")
      (KFunction[JString JString]
        (lambda (entry ::JString)
          (JString (TableUtil:wrapString entry 248))))
  250 250)
    ))



(define (text-table-configs)
  (begin
    (set-static TableConfig `TEXT_MANAGE_TABLE text-manage-table)
    (set-static TableConfig `TEXT_VIEW text-view)
    (set-static TableConfig `TEXT_OVERVIEW_TABLE text-overview-table)
    (set-static TableConfig `TEXT_DASHBOARD_FORMATTER
      (KFunction[TextEval JString] (lambda (eval ::TextEval) (eval:activeTextTable))))
    ))

;; General
(define description-table
  (List:of
    (TableUtil:createColumn
      (JString "Description")
      (KFunction[Entry JString]
        (lambda (entry ::Entry)
          (JString (TableUtil:wrapString (TableUtil:truncateString (entry:description)) 248))))
      250 250)
    ))

(define note-table
  (List:of
    (TableUtil:createColumn
      (JString "Idx")
      (KFunction[Pair[Integer JString] JString]
        (lambda (entry ::Pair[Integer JString]) (JString (entry:first))))
      5 5)
    (TableUtil:createColumn
      (JString "Notes")
      (KFunction[Pair[Integer JString] JString]
        (lambda (entry ::Pair[Integer JString])
          (JString (TableUtil:wrapString (TableUtil:truncateString (entry:second)) 242))))
      244 244)
    ))

(define reminder-table
  (List:of
    (TableUtil:createColumn
      (JString "Idx")
      (KFunction[Pair[Integer Reminder] JString]
        (lambda (entry ::Pair[Integer Reminder]) (JString (entry:first))))
        5 5)
    (TableUtil:createColumn
      (JString "Level")
      (KFunction[Pair[Integer Reminder] JString]
        (lambda (entry ::Pair[Integer Reminder]) (JString ((entry:second):level))))
      10 20)
    (TableUtil:createColumn
      (JString "Reminder Time")
      (KFunction[Pair[Integer Reminder] JString]
        (lambda (entry ::Pair[Integer Reminder]) (JString (DateTimeUtil:printDateTimeFull ((entry:second):time)))))
      10 50)
    ))

(define search-view-table
  (List:of
    (TableUtil:createColumn
      (JString "Idx")
      (KFunction[Pair[Integer SearchResult] JString]
        (lambda (entry ::Pair[Integer SearchResult]) (JString (entry:first))))
      5 5)
    (TableUtil:createColumn
      (JString "Name")
      (KFunction[Pair[Integer SearchResult] JString]
        (lambda (entry ::Pair[TextEntry SearchResult]) (JString (((entry:second):entry):name))))
      40 40)
    (TableUtil:createColumn
      (JString "Result")
      (KFunction[Pair[Integer SearchResult] JString]
        (lambda (entry ::Pair[Integer SearchResult])
          (JString (TableUtil:wrapString  (JString ((entry:second):matchedLine)) 200))))
      205 205)
    ))

(define todo-view-table
  (List:of
    (TableUtil:createColumn
      (JString "Idx")
      (KFunction[Pair[Integer String] JString]
        (lambda (entry ::Pair[Integer String]) (JString (entry:first))))
      5 5)
    (TableUtil:createColumn
      (JString "Todo")
      (KFunction[Pair[Integer String] JString]
        (lambda (entry ::Pair[Integer String]) (JString (entry:second))))
      50 200)
    ))

(define (general-table-configs)
  (begin
    (set-static TableConfig `DESCRIPTION_TABLE description-table)
    (set-static TableConfig `NOTE_TABLE note-table)
    (set-static TableConfig `REMINDER_TABLE reminder-table)
    (set-static TableConfig `SEARCH_VIEW_TABLE search-view-table)
    (set-static TableConfig `TODO_VIEW_TABLE todo-view-table)
    ))


(define (load-table-configs)
  (begin
    (set-static TableConfig 'BORDER (AsciiTable:.FANCY_ASCII))
    (event-table-configs)
    (task-table-configs)
    (project-table-configs)
    (general-table-configs)
    (text-table-configs)
    ))

