

;; Scales all tables 0.75->(1920x1080) 1->(2560x1440) 1.5->(3849x2169
;; May need tweeked some depending on console and text size
(define T_SCALAR 1)


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
        (TableUtil:mergeAndPadTable 10 (eval:pastEventTable) (eval:futureEventTable)))))
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
        (lambda (entry ::Pair[Integer SubTask]) (JString (TableUtil:wrapString ((entry:second):description) 170))))
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


;; General
(define description-table
  (List:of
    (TableUtil:createColumn
      (JString "Description")
      (KFunction[TaskEntry JString]
        (lambda (entry ::TaskEntry) (JString (TableUtil:wrapString (entry:description) 248))))
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
        (lambda (entry ::Pair[Integer JString]) (JString (TableUtil:wrapString (entry:second) 242))))
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

(define (general-table-configs)
  (begin
    (set-static TableConfig `DESCRIPTION_TABLE description-table)
    (set-static TableConfig `NOTE_TABLE note-table)
    (set-static TableConfig `REMINDER_TABLE reminder-table)
    ))


(define (load-table-configs)
  (begin
    (set-static TableConfig 'BORDER (AsciiTable:.FANCY_ASCII))
    (event-table-configs)
    (task-table-configs)
    (general-table-configs)
    ))

