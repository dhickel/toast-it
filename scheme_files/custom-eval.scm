

;(define calendar-eval
;  (DisplayEval
;    (lambda ()
;      (let* ((now (LocalDateTime:now)))
;        (Calendar:generateCalendar (now:getYear) (now:getMonth) 33 13 cal-cell-mapper)))))
;
;
;

;
;(define (cal-cell-mapper cell)
;  (let ((rtn-cell cell))
;    (if (equal? ((LocalDateTime:now):atStartOfDay) (cell:date))
;      (set! rtn-cell (cell:asHighlighted)))
;    (rtn-cell:withItems (AppInstance:getCalendarEvents (cell:date)))
;    rtn-cell))