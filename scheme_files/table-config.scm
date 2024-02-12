(define event-edit-table
  (List:of
    (TableUtil:createColumn
      (JString "Index")
      (KFunction[Pair[Integer EventEntry] JString]
        (lambda (entry ::Pair[Integer EventEntry]) (JString (entry:first)))))
    (TableUtil:createColumn
      (JString "Event Name")
      (KFunction[Pair[Integer EventEntry] JString]
        (lambda (entry ::Pair[Integer EventEntry]) (JString ((entry:second):name)))))
    (TableUtil:createColumn
      (JString "Tags")
      (KFunction[Pair[Integer EventEntry] JString]
        (lambda (entry ::Pair[Integer EventEntry]) (JString ((entry:second):tags)))))
    (TableUtil:createColumn
      (JString "Start Time")
      (KFunction[Pair[Integer EventEntry] JString]
        (lambda (entry ::Pair[Integer EventEntry]) (JString (DateTimeUtil:printDateTimeFull ((entry:second):startTime))))))
    (TableUtil:createColumn
      (JString "End Time")
      (KFunction[Pair[Integer EventEntry] JString]
        (lambda (entry ::Pair[Integer EventEntry]) (JString (DateTimeUtil:printDateTimeFull ((entry:second):endTime))))))
    (TableUtil:createColumn
      (JString "Has Happened")
      (KFunction[Pair[Integer EventEntry] JString]
        (lambda (entry ::Pair[Integer EventEntry]) (JString (((entry:second):endTime):isBefore (LocalDateTime:now))))))
    ))


(define (load-table-configs)
  (begin
  (set-static TableConfig `EVENT_EDIT_TABLE event-edit-table)
    ))