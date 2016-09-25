#lang racket
(provide (all-defined-out))
; Commons
(define (op? ele)
  (and (symbol? ele)
       (or (eq? '^ ele) (eq? '+ ele) (eq? '- ele) (eq? '* ele) (eq? '/ ele))))

(define (op-precedence op)
  (cond ((eq? '^ op) 3)
        ((or (eq? '*) (eq? '/)) 2)
        ((or (eq? '+ op) (eq? '- op)) 1)
        (else (error "not a valid op" op))))

(define (>op? op1 op2)
  (cond ((null? op1) false)
        ((null? op2) true)
        ((> (op-precedence op1) (op-precedence op2)) true)
        (else false)))

(define (=op? op1 op2)
  (cond ((or (null? op1) (null? op2)) true)
        ((= (op-precedence op1) (op-precedence op2)) true)
        (else false)))

(define (<op? op1 op2)
  (and (not (>op? op1 op2)) (not (=op? op1 op2))))

(define (variable? x) (symbol? x))

(define (same-variable? v1 v2)
  (and (variable? v1) (eq? v1 v2)))

(define (=number? expr num) (and (number? expr) (= expr num)))

(define (prefix-sum? x) (and (pair? x) (eq? (car x) '+)))
(define (prefix-addend s) (cadr s))
(define (prefix-augend s) (caddr s))
(define (make-prefix-sum a1 a2)
  (cond ((=number? a1 0) a2)
        ((=number? a2 0) a1)
        ((and (number? a1) (number? a2)) (+ a1 a2))
        (else (list '+ a1 a2))))

(define (prefix-product? x) (and (pair? x) (eq? (car x) '*)))
(define (prefix-multiplier p) (cadr p))
(define (prefix-multiplicand p) (caddr p))
(define (make-prefix-product m1 m2)
  (cond ((or (=number? m1 0) (=number? m2 0)) 0)
        ((=number? m1 1) m2)
        ((=number? m2 1) m1)
        ((and (number? m1) (number? m2)) (* m1 m2))
        (else (list '* m1 m2))))

(define (prefix-exponentiation? x) (and (pair? x) (eq? (car x) '^)))
(define (prefix-base p) (cadr p))
(define (prefix-exponent p) (caddr p))
(define (make-prefix-exponentiation b e)
  (cond ((=number? b 1) 1)
        ((=number? e 0) 1)
        ((=number? e 1) b)
        (else (list '^ b e))))

; Left parse a symbolic prefix expression
; for example, given (* a b c (+ a c) (^ a 3))
; then the output will be (* (* (* (* a b) c) (+ a c)) (^ a 3))
(define (left-parse-prefix-expr expr)
  (define (iter op ans expr)
    (if (null? expr)
        ans
        (iter
          op
          (if (null? ans)
              (left-parse-prefix-expr (car expr))
              (list op ans (left-parse-prefix-expr (car expr))))
          (cdr expr))))
  (if (not (list? expr))
      expr
      (iter (car expr) null (cdr expr))))

; Right parse a symbolic prefix expression
; for example, given (* a b c (+ a c) (^ a 3))
; then the output will be (* a (* b (* c (* (+ a c) (^ a 3)))))
(define (right-parse-prefix-expr expr)
  (if (not (pair? expr))
      expr
      (let ((op (car expr))
            (r (cdr expr)))
        (if (= 2 (length r))
            (list op (right-parse-prefix-expr (car r)) (right-parse-prefix-expr (cadr r)))
            (list op (right-parse-prefix-expr (car r)) (right-parse-prefix-expr (cons op (cdr r))))))))

; Calculate the differentiation of polynomial function (expressed in prefix notation)
(define (deriv-prefix-expr expr var)
  (define (deriv expr)
    (cond ((number? expr) 0)
          ((variable? expr) (if (same-variable? expr var) 1 0))
          ((prefix-sum? expr)
           (make-prefix-sum
             (deriv-prefix-expr (prefix-addend expr) var)
             (deriv-prefix-expr (prefix-augend expr) var)))
          ((prefix-product? expr)
           (make-prefix-sum
             (make-prefix-product (prefix-multiplier expr)
                                  (deriv-prefix-expr (prefix-multiplicand expr) var))
             (make-prefix-product (prefix-multiplicand expr)
                                  (deriv-prefix-expr (prefix-multiplier expr) var))))
          ((prefix-exponentiation? expr)
           (make-prefix-product
             (make-prefix-product
               (prefix-exponent expr)
               (make-prefix-exponentiation (prefix-base expr)
                                           (- (prefix-exponent expr) 1)))
             (deriv-prefix-expr (prefix-base expr) var)))
          (else (error "unknow expression" expr))))
  (deriv (left-parse-prefix-expr expr)))
