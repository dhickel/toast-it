(load "scheme_files/table-config.scm")

(define (load-shell-modes)
  (let ((modes
          (list
            (ShellMode[DirectoryEval] "DIRECTORY" (Set[JString]:of "dir") (DirectoryEval) "⛘|▹ " "⛘▹▹ ")
            (ShellMode[SchemeEval] "SCHEME" (Set[JString]:of "scheme") (SchemeEval SchemeInstance) "λ|▹ " "λ▹▹ ")
            (ShellMode[EventEval] "EVENT" (Set[JString]:of "event") (EventEval) "🗓|▹ " "🗓▹▹ ")
            (ShellMode[TaskEval] "TASK" (Set[JString]:of "task") (TaskEval) "🗓|▹ " "🗓▹▹ ")
            )))
    (set-static Settings `SHELL_MODES modes)))

(load-shell-modes)
(load-table-configs)