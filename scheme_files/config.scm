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
    (set-static Settings `SHELL_BIND_PORT 22233)
    (set-static Settings `SHELL_USER "user")
    (set-static Settings `SHELL_PASSWORD "password")
    (set-static Settings `SHELL_KEY_PAIR "hostkey.ser")
    ))

(define (load-event-settings)
  (begin
    (set-static Settings 'EVENT_LOOK_FORWARD_DAYS 8) ; Set to -1 for all
    (set-static Settings 'EVENT_REFRESH_INV_MIN 240)
    (set-static Settings `FADE_TIME_SEC (* 60 60))
    ))

(define (load-shell-modes)
  (let ((modes
          (list
            (ShellMode[DirectoryEval] "DIRECTORY" (Set[JString]:of "dir") (DirectoryEval) "⛘|▹ " "⛘▹▹ ")
            (ShellMode[SchemeEval] "SCHEME" (Set[JString]:of "scheme") (SchemeEval SchemeInstance) "λ|▹ " "λ▹▹ ")
            )))
    (set-static Settings `SHELL_MODES modes)))

(define (load-settings)
  (begin
    (load-file-paths)
    (load-calendar-settings)
    (load-shell-settings)
    (load-shell-modes)
    (load-event-settings)
    ))