(define (set-static class field value)
  (set! (static-field class field) value))

(define (file-sep)
  (File:.separator))

