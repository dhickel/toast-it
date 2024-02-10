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


;; Application Classes
(define-alias Calendar calendar.Calendar)
(define-alias CalendarCell calendar.CalendarCell)
(define-alias TaskEntry entries.task.TaskEntry)
(define-alias SubTaskEntry entries.task.SubTaskEntry)
(define-alias TextEntry entries.text.TextEntry)
(define-alias ProjectEntry entries.project.ProjectEntry)
(define-alias EventEntry entries.event.EventEntry)
(define-alias AppShell shell.ApplicationShell)
(define-alias ShellMode shell.ShellMode)
(define-alias DirectoryEval shell.DirectoryEval)
(define-alias ShellEvaluator shell.ShellEvaluator)
(define-alias SchemeEval shell.SchemeEval)

;; Application Enums
(define-alias EntryType enums.EntryType)
(define-alias NotificationLevel enums.NotificationLevel)
(define-alias NotificationType enums.NotificationType)


;; Application Util
(define-alias JSON util.JSON)
(define-alias Settings util.Settings)