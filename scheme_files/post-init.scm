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

 #|
 Some function definitions that depend on initalization are place here.
 For example, the run-process functions redirect the output of the process to the shells output stream
 this allows for the output of the commands to display directly in the shell.
 This also means come config definitions need put here as well;
|#

;; Post init functions

(define ShellInstance (AppInstance:getShell))

(define (sys-exec cmd . args)
  (run-process shell: #t out-to: (ShellInstance:getOutput) (apply list cmd args)))

(define (sys-runnable cmd . args)
  (KRunnable
    (lambda ()
      (run-process shell: #t out-to: (ShellInstance:getOutput) (apply list cmd args)))))

(define (editor-consumer cmd . args)
  (KConsumer:of
    (lambda (path ::Path)
      (run-process shell: #t out-to: (ShellInstance:getOutput)
        (apply list (cons cmd (append args (list ((path:toAbsolutePath):toString)))))))))



;; Post init config options

;; This is an example of how to define an editor command, you can use multiple args. When ran the path of the file
;; will be passed as the last arguement, so if the editor you plan on launching needs a flag to specifiy the file
;; put it at the end for example: (define ex-editor (editor-consumer "myeditor" "-f") will run "myeditor -f /a/path/to/file.file"
(define vs-code-editor
  (editor-consumer "code" "-n"))

(define (load-post-config)
  (begin
    (Settings:addEditor (JString "vs-code") (Editor (List[JString]:of "code" "-n")))

    ))

(load-shell-modes)
(load-table-configs)
(load-post-config)