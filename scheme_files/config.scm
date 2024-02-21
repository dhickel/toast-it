(define root-path "DATA")

(define (load-file-paths)
  (begin
    (set-static Settings 'ROOT_PATH root-path)
    (set-static Settings 'DATABASE_PATH (string-append root-path (file-sep) "DATABASE" (file-sep) "database.db"))
    (set-static Settings 'TASK_PATH (string-append root-path (file-sep) "TASK"))
    (set-static Settings 'NOTE_PATH (string-append root-path (file-sep) "NOTE"))
    (set-static Settings 'JOURNAL_PATH (string-append root-path (file-sep) "JOURNAL"))
    (set-static Settings 'PROJECT_PATH (string-append root-path (file-sep) "PROJECT"))
    (set-static Settings `TEMP_PATH (string-append root-path (file-sep) ".TEMP"))
    ))


(define (load-app-settings)
  (begin
    (set-static Settings 'EXEC_THREADS 1)
    (set-static Settings 'DATE_INPUT_PATTERNS (List[JString]:of "MM/dd/yy"))
    (set-static Settings `TIME_INPUT_PATTERNS (List[JString]:of "HH:mm" "H:mm" "h:mm a" "h:mma"))
    (set-static Settings `DATE_TIME_FULL_PATTERN "EEEE, MMM dd, yyyy '@' HH:mm")
    (set-static Settings `DATE_TIME_SHORT_PATTERN "EEEE, MMM dd '@' HH:mm")
    ; Use virtual threads for searchs, more efficent if searching many large files
    (set-static Settings `THREADED_SEARCH #t)
    (set-static Settings `SEARCH_TIMEOUT_SEC 60)
    ))


(define (load-global-table-settings)
  (begin
    (set-static Settings 'TABLE_MAX_COLUMN_WIDTH 200)
    (set-static Settings 'TABLE_DEFAULT_ALIGNMENT (HorizontalAlign:.CENTER))
    (set-static Settings ' TABLE_OVERFLOW_BEHAVIOR (OverflowBehaviour:.ELLIPSIS_LEFT))
    ))


(define (calendar-full-data-mapper)
  (KFunction[DatedEntry JString]
    (lambda (entry ::DatedEntry)
      (JString:format (JString "%s %s - %s")
        (if ((entry:tags):isEmpty)
          (JString "")
          ((Settings:getTag ((entry:tags):getFirst)):asciiIcon))
        (((entry:dueBy):toLocalTime):truncatedTo (ChronoUnit:.MINUTES))
        (entry:name)))))

(define (calendar-small-data-mapper)
  (KFunction[DatedEntry JString]
    (lambda (entry ::DatedEntry)
      (if ((entry:tags):isEmpty)
        (JString "**")
        ((Settings:getTag ((entry:tags):getFirst)):asciiIcon))
      )))

(define (calendar-cell-mapper data-mapper)
  (KUnaryOperator[CalendarCell]
    (lambda (cell ::CalendarCell)
      (let* ((rtn-cell ::CalendarCell cell))
        (if (((LocalDate:now):atStartOfDay):isEqual ((cell:date):atStartOfDay))
          (set! rtn-cell (cell:asHighlighted)))
        (rtn-cell:withItems ((App:instance):getCalendarEvents (cell:date) data-mapper))))))



(define (load-calendar-settings)
  (begin
    (set-static Settings `CALENDAR_CELL_HEIGHT 13)
    (set-static Settings `CALENDAR_CELL_WIDTH 34)
    (set-static Settings `CALENDAR_REFRESH_SEC (* 60 30))
    (set-static Settings `CALENDAR_DATA_MAPPER (calendar-full-data-mapper))
    (set-static Settings `CALENDAR_CELL_MAPPER (calendar-cell-mapper (calendar-full-data-mapper)))
    ))

(define (load-shell-settings)
  (begin
    (set-static Settings `SHELL_BIND_ADDRESS "127.0.0.1")
    (set-static Settings `SHELL_BIND_PORT 2233)
    (set-static Settings `SHELL_USER "user")
    (set-static Settings `SHELL_PASSWORD "password")
    (set-static Settings `SHELL_KEY_PAIR "hostkey.ser")
    ))

(define (load-event-settings)
  (begin
    (set-static Settings `EVENT_LOOK_FORWARD_DAYS -1) ; Set to -1 for all
    (set-static Settings `EVENT_REFRESH_INV_MIN 240)
    (set-static Settings `EVENT_NOTIFY_FADE_TIME_SEC (* 60 60))
    ))

(define (load-task-settings)
  (begin
    (set-static Settings `TASK_REFRESH_INV_MIN 240)
    (set-static Settings `TASK_NOTIFY_FADE_TIME_SEC (* 60 60))
    (set-static Settings `MAX_PREVIEW_LENGTH 1000)
    ))



(define (load-settings)
  (begin
    (load-app-settings)
    (load-global-table-settings)
    (load-file-paths)
    (load-calendar-settings)
    (load-shell-settings)
    (load-event-settings)
    (load-task-settings)
    ))