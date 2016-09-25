#lang racket
(require "algebra.rkt")
(require rackunit)

; Test left-parse-prefix-expr
(check-equal?
  (left-parse-prefix-expr '(* a b c (+ a c) (^ a 3)))
  '(* (* (* (* a b) c) (+ a c)) (^ a 3)))

; Test right-parse-prefix-expr
(check-equal?
  (right-parse-prefix-expr '(* a b c (+ a c) (^ a 3)))
  '(* a (* b (* c (* (+ a c) (^ a 3))))))

; Test deriv-prefix-expr
(check-equal?
  (deriv-prefix-expr '(* x y (+ x 3)) 'x)
  '(+ (* x y) (* (+ x 3) y)))
