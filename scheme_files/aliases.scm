;; Kawa Wrappers
(define-alias KConsumer io.mindspice.kawautils.wrappers.functional.consumers.KawaConsumer)
(define-alias KBiConsumer io.mindspice.kawautils.wrappers.functional.consumers.KawaBiConsumer)
(define-alias KFunction io.mindspice.kawautils.wrappers.functional.functions.KawaFunction)
(define-alias KBiFunction io.mindspice.kawautils.wrappers.functional.functions.KawaBiFunction)
(define-alias KPredicate io.mindspice.kawautils.wrappers.functional.predicates.KawaPredicate)
(define-alias KBiPredicate io.mindspice.kawautils.wrappers.functional.predicates.KawaBiPredicate)
(define-alias KSupplier io.mindspice.kawautils.wrappers.functional.suppliers.KawaSupplier)


;; Java
(define-alias ArrayList java.util.ArrayList)
(define-alias List java.util.List)
(define-alias HashSet java.util.HashSet)
(define-alias Set java.util.Set)
(define-alias HashMap java.util.HashMap)
(define-alias Enum java.lang.Enum)
(define-alias Arrays java.util.Arrays)
(define-alias File java.io.File)
(define-alias JString java.lang.String)
(define-alias Instance java.time.Instant)
(define-alias LocalDateTime java.time.LocalDateTime)
(define-alias Integer java.lang.Integer)

;; Custom Types
(define-alias Pair io.mindspice.mindlib.data.tuples.Pair)


;; Application Classes
(define-alias Calendar io.mindspice.toastit.calendar.Calendar)
(define-alias CalendarCell io.mindspice.toastit.calendar.CalendarCell)
(define-alias TaskEntry io.mindspice.toastit.entries.task.TaskEntry)
(define-alias SubTask io.mindspice.toastit.entries.task.SubTask)
(define-alias TextEntry io.mindspice.toastit.entries.text.TextEntry)
(define-alias ProjectEntry io.mindspice.toastit.entries.project.ProjectEntry)
(define-alias EventEntry io.mindspice.toastit.entries.event.EventEntry)
(define-alias AppShell io.mindspice.toastit.shell.ApplicationShell)
(define-alias ShellMode io.mindspice.toastit.shell.ShellMode)
(define-alias DirectoryEval io.mindspice.toastit.shell.evaluators.DirectoryEval)
(define-alias ShellEvaluator io.mindspice.toastit.shell.evaluators.ShellEvaluator)
(define-alias SchemeEval io.mindspice.toastit.shell.evaluators.SchemeEval)
(define-alias EventEval io.mindspice.toastit.shell.evaluators.EventEval)

;; Application Enums
(define-alias EntryType io.mindspice.toastit.enums.EntryType)
(define-alias NotificationLevel io.mindspice.toastit.enums.NotificationLevel)
(define-alias NotificationType io.mindspice.toastit.enums.NotificationType)


;; Application Util
(define-alias JSON io.mindspice.toastit.util.JSON)
(define-alias Settings io.mindspice.toastit.util.Settings)
(define-alias DateTimeUtil io.mindspice.toastit.util.DateTimeUtil)
(define-alias TableUtil io.mindspice.toastit.util.TableUtil)
(define-alias Util io.mindspice.toastit.util.Util)

;; Ascii table
(define-alias Column com.github.freva.asciitable.Column)
(define-alias ColumnData com.github.freva.asciitable.ColumnData)
(define-alias HorizontalAlign com.github.freva.asciitable.HorizontalAlign)

;; Application Tables
(define-alias TableConfig io.mindspice.toastit.util.TableConfig)