(define (set-static class field value)
  (set! (static-field class field) value))

(define (file-sep)
  (File:.separator))

(define (replace-eval-alias evaluator ::ShellEvaluator oldAlias ::String newAlias ::String)
  (evaluator:replaceAlias oldAlias newAlias))


(define (add-eval-alias evaluator ::ShellEvaluator existingAlias ::String newAlias ::String)
  (evaluator:addAlias existingAlias newAlias))
