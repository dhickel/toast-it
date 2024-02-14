(define root-path "DATA")

(define (load-file-paths)
  (begin
    (set-static Settings 'ROOT_PATH root-path)
    (set-static Settings 'DATABASE_PATH (string-append root-path (file-sep) "DATABASE" (file-sep) "database.db"))
    (set-static Settings 'TASK_PATH (string-append root-path (file-sep) "TASK"))
    (set-static Settings 'NOTE_PATH (string-append root-path (file-sep) "NOTE"))
    (set-static Settings 'JOURNAL_PATH (string-append root-path (file-sep) "JOURNAL"))
    (set-static Settings 'PROJECT_PATH (string-append root-path (file-sep) "PROJECT"))
    ))


(define (load-app-settings)
  (begin
    (set-static Settings 'EXEC_THREADS 1)
    (set-static Settings 'DATE_INPUT_PATTERNS (List[JString]:of "MM/dd/yy"))
    (set-static Settings `TIME_INPUT_PATTERNS (List[JString]:of "HH:mm" "H:mm" "h:mm a" "h:mma"))
    (set-static Settings `DATE_TIME_FULL_PATTERN "EEEE, MMM dd, yyyy '@' HH:mm")
    (set-static Settings `DATE_TIME_SHORT_PATTERN "EEEE, MMM dd '@' HH:mm")
    ))


(define (load-global-table-settings)
  (begin
    (set-static Settings 'TABLE_MAX_COLUMN_WIDTH 40)
    (set-static Settings 'TABLE_DEFAULT_ALIGNMENT (HorizontalAlign:.LEFT))
    ))

(define (load-calendar-settings)
  (begin
    (set-static Settings 'CALENDAR_HEADER_LEADING_SPACES 8)
    (set-static Settings `CALENDER_HEADER_HEIGHT 3)
    (set-static Settings `CALENDER_CELL_HEIGHT 12)
    (set-static Settings `CALENDER_CELL_WIDTH 28)
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
    (set-static Settings `EVENT_DASHBOARD_FORMATTER
      (KFunction[EventEval String]
        (lambda (eval ::EventEval)
          (TableUtil:mergeAndPadTable 10 (eval:pastEventTable) (eval:futureEventTable)))))
    ))

(define (load-task-settings)
  (begin
    (set-static Settings `TASK_REFRESH_INV_MIN 240)
    (set-static Settings `TASK_NOTIFY_FADE_TIME_SEC (* 60 60))
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