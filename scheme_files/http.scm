(let* ((builder (http-client:jsonRequestBuilder))
        (builder:address "https://spacefarmers.io/api/farmers/cf89a500019164aee77189a8009065cb51697e6863bbc9a41a0dd00e9d01b747")
        (builder:asGet))
  (builder:makeAndG etJson))


(define (make-req client ::HttpClient)
  (let* ((builder (http-client:jsonRequestBuilder)))
    (begin
      (builder:address "https://spacefarmers.io/api/farmers/cf89a500019164aee77189a8009065cb51697e6863bbc9a41a0dd00e9d01b747")
      (builder:asGet)
      (builder:makeAndGetJson))))

(define (sf-get-tib client)
  (let* ((builder (client:jsonRequestBuilder)))
    (let* ((response (begin
                       (builder:address "https://spacefarmers.io/api/farmers/cf89a500019164aee77189a8009065cb51697e6863bbc9a41a0dd00e9d01b747")
                       (builder:asGet)
                       (builder:makeAndGetJson))))
      (let* ((data (response:get "data"))
              (attributes (data:get "attributes"))
              (tib (attributes:get "tib_24h"))
              (effort (attributes:get "current_effort")))
        (ArrayList
          (Pair:of (JString "TiB")  (JString (tib:toString)))
          (Pair:of (JString "Effort")  (JString (string-append (effort:toString) "%"))))))))

(define (sf-get-payout client)
  (let* ((builder (client:jsonRequestBuilder))) ; Assume `client` has a method `jsonRequestBuilder`
    (let* ((response (begin
                       (builder:address "https://spacefarmers.io/api/farmers/cf89a500019164aee77189a8009065cb51697e6863bbc9a41a0dd00e9d01b747/payouts")
                       (builder:asGet)
                       (builder:makeAndGetJson))))
      (let* ((data (response:get "meta"))
              (unpaid (data:get "sum_amount_unpaid"))
              (paid (data:get "sum_amount_paid")))
        (ArrayList
          (Pair:of (JString "Unpaid")  (JString (round-decimal (mojo-to-xch (unpaid:longValue)) 3):toString))
          (Pair:of (JString "Paid")  (JString (round-decimal (mojo-to-xch (paid:longValue)) 3):toString)))))))


(define (mojo-to-xch mojos)
  (exact->inexact (/ mojos 1000000000000)))

(define (round-decimal num decimal-places)
  (let ((scale (expt 10 decimal-places)))
    (/ (round (* num scale)) scale)))


(define (sf-get-info client)
  (let* ((data (ArrayList)))
    (begin
      (data:addAll (sf-get-tib client))
      (data:addAll (sf-get-payout client)))
    data))


(define (sf-get-table items ::ArrayList)
  (TableUtil:generateKeyPairTable
    "Farm Info"
    items
    (lambda (item) ((item:first):toString))
    (lambda (item) ((item:second):toString))))


(define update-pool-info
    (lambda ()
      (begin
        (sf-pool-info:clear)
        (sf-pool-info:addAll (sf-get-info http-client)))
    )))


(define x (((App:instance):getExec):submit
            (lambda ()
                (sf-pool-info:clear)
                (sf-pool-info:addAll (sf-get-info http-client))
                (void)))  ; Ensure the lambda ends with (void).




;;;;;;


          (define-alias HttpClient io.mindspice.mindlib.http.clients.HttpClient)
          (define-alias JsonRequestBuilder io.mindspice.mindlib.http.clients.JsonRequestBuilder)


          (define (sf-get-tib client)
            (let* ((builder (client:jsonRequestBuilder)))
              (let* ((response (begin
                                 (builder:address "https://spacefarmers.io/api/farmers/cf89a500019164aee77189a8009065cb51697e6863bbc9a41a0dd00e9d01b747")
                                 (builder:asGet)
                                 (builder:makeAndGetJson))))
                (let* ((data (response:get "data"))
                        (attributes (data:get "attributes"))
                        (tib (attributes:get "tib_24h"))
                        (effort (attributes:get "current_effort")))
                  (ArrayList
                    (Pair:of (JString "TiB")  (JString (tib:toString)))
                    (Pair:of (JString "Effort")  (JString (string-append (effort:toString) "%"))))))))

          (define (sf-get-payout client)
            (let* ((builder (client:jsonRequestBuilder))) ; Assume `client` has a method `jsonRequestBuilder`
              (let* ((response (begin
                                 (builder:address "https://spacefarmers.io/api/farmers/cf89a500019164aee77189a8009065cb51697e6863bbc9a41a0dd00e9d01b747/payouts")
                                 (builder:asGet)
                                 (builder:makeAndGetJson))))
                (let* ((data (response:get "meta"))
                        (unpaid (data:get "sum_amount_unpaid"))
                        (paid (data:get "sum_amount_paid")))
                  (ArrayList
                    (Pair:of (JString "Unpaid")  (JString (round-decimal (mojo-to-xch (unpaid:longValue)) 3):toString))
                    (Pair:of (JString "Paid")  (JString (round-decimal (mojo-to-xch (paid:longValue)) 3):toString)))))))


          (define (mojo-to-xch mojos)
            (exact->inexact (/ mojos 1000000000000)))

          (define (round-decimal num decimal-places)
            (let ((scale (expt 10 decimal-places)))
              (/ (round (* num scale)) scale)))


          (define (sf-get-info client)
            (let* ((data (ArrayList)))
              (begin
                (data:addAll (sf-get-tib client))
                (data:addAll (sf-get-payout client)))
              data))


          (define (sf-get-table items ::List)
            (TableUtil:generateKeyPairTable
              "Farm Info"
              items
              (lambda (item) ((item:first):toString))
              (lambda (item) ((item:second):toString))))



          (define sf-pool-info (java.util.concurrent.CopyOnWriteArrayList))
          (define http-client (HttpClient))

          (define (update-pool-info)
            (begin
              (sf-pool-info:clear)
              (sf-pool-info:addAll (sf-get-info http-client))))

          ;; No sure why but after maannyyy attempts I can't get scheduled exec to work with lambdas, functions, or runnable wrapped logic
          ;; But launching an actual thread works fine, and also works when launch from the exec, so launching a thread from the exec
          ;; as to be able ot have schedule async updates to pool info
          (((App:instance):getExec):scheduleAtFixedRate
            ((java.lang.Thread:ofVirtual):start (runnable update-pool-info))
            0
            5
            (java.util.concurrent:TimeUnit:.MINUTES))
