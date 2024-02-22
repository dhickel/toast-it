(define (set-static class field value)
  (set! (static-field class field) value))

(define (file-sep)
  (File:.separator))

(define (fori start ::int end ::int operation ::IntConsumer)
  ((IntStream:range start end):forEach operation))

(define (filter filter-predicate ::Predicate lst ::List)
  (((lst:stream):filter filter-predicate):toList))


(define (now-time) (LocalDateTime:now))

(define (replace-eval-alias evaluator ::ShellEvaluator oldAlias ::String newAlias ::String)
  (evaluator:replaceAlias oldAlias newAlias))

(define (add-eval-alias evaluator ::ShellEvaluator existingAlias ::String newAlias ::String)
  (evaluator:addAlias existingAlias newAlias))
