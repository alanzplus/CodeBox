Code Box
--
Collections of my implementations for Common Utilities, Data Structures and Algorithms (in different languages)

## Languages
* Java
* Racket
* Ruby
* Scala

## Index
* Java
    * String
        * Levenshtein Distance
        * Damerau-Levenshtein Distance
    * Sparse Array
      * Sparse Immutable Array (Page Based)
      * Sparse Immutable Array (Binary Search Based)
    * MISC
        * Fast-LFU
        * Iterative-Tranformer
        * AsyncIterator
* Racket
    * Parse Prefix Symbolic Algebra Expression
    * Calculate Differentiation of Symbolic Algebra Expression
    * Y Combinator

## Java

### String

#### Levenshtein Distance
[Implementation](https://github.com/alanzplus/CodeBox/blob/master/java/algorithm-tool-box/src/main/java/org/zlambda/sandbox/algtoolbox/string/Levenshtein.java)
```java
String str1 = " Helol distance ?";
-String str2 = "Hello Distance!";
StringDistance levenshtein = new Levenshtein();
System.out.println(levenshtein.distance(str1, str2));
System.out.println(levenshtein.explain());
// output
// 7
// 'DELETE " ", KEEP "H", KEEP "e", KEEP "l", DELETE "o", KEEP "l", INSERT "o", KEEP " ", REPLACE "d" BY "D", KEEP "i", KEEP "s", INSERT "t", KEEP "a", KEEP "n", KEEP "c", KEEP "e", REPLACE " " BY "!", DELETE "?"'
```

#### Damerau-Levenshtein Distance
[Implementation](https://github.com/alanzplus/CodeBox/blob/master/java/algorithm-tool-box/src/main/java/org/zlambda/sandbox/algtoolbox/string/DamerauLevenshtein.java)
```java
String str1 = " Helol distance ?";
String str2 = "Hello Distance!";
StringDistance levenshtein = new DamerauLevenshtein();
System.out.println(levenshtein.distance(str1, str2));
System.out.println(levenshtein.explain());
// output
// 6
// 'DELETE " ", KEEP "H", KEEP "e", KEEP "l", SWAP "o" and "l", KEEP " ", REPLACE "d" BY "D", KEEP "i", KEEP "s", INSERT "t", KEEP "a", KEEP "n", KEEP "c", KEEP "e", REPLACE " " BY "!", DELETE "?"'
```

### Sparse Array

* We can also use `HashMap`, it depends on your requirement.

#### Immutable Sparse Array (Page Based)

[Code Ref](https://github.com/alanzplus/CodeBox/blob/master/java/algorithm-tool-box/src/main/java/org/zlambda/codebox/algtoolbox/misc/ImmutableSparseArray.java)

Memory footprint depends on the actual input data but random access guarantee to be O(1)

Idea of this implementation is like the OS memory management. 

TODO: add graph to illustrate it.

#### Immutable Sparse Array (Binary Search Based)

[Code Ref](https://github.com/alanzplus/CodeBox/blob/master/java/algorithm-tool-box/src/main/java/org/zlambda/codebox/algtoolbox/misc/ImmutableSparseArray.java)

Memory usage is really compact is O(number of elements) and random access guarantee to be O(Log(number of elements))

### MISC

#### FastLFU
A constant time LFU implmentation based on paper [An O(1) algorithm for implementing the LFU cache eviction scheme](http://dhruvbird.com/lfu.pdf).

#### Iterative Transformer
[IterativeTransformer](https://github.com/alanzplus/codebox/blob/master/java/algorithm-tool-box/src/main/java/org/zlambda/sandbox/algtoolbox/IterativeTransformer.java) is a really tiny and simple framework (40 lines of code) for writing iterative program in a recursive like fashion.

For example, given the following binary tree data structure

```java
/**
 *               1
 *            /     \
 *           2       3
 *                 /   \
 *                4     5
 */
private static TreeNode root = new TreeNode(1);
static {
    root.left = new TreeNode(2);
    root.right = new TreeNode(3);
    root.right.left = new TreeNode(4);
    root.right.right = new TreeNode(5);
}

private static class TreeNode {
    private TreeNode(int val) {
        this.val = val;
    }
    private TreeNode left;
    private TreeNode right;
    private final int val;
}
```

We can write typical recursive traversal as

```java
/**
 * The following recursive traversal is not the most elegant one, since we can simplify two functions into one like
 *
 * List<Integer> traverse(TreeNode node) {
 *      List<Integer> ret = new ArrayList<>();
 *      if (null == node) {
 *          return ret;
 *      }
 *      ret.add(node.val);
 *      ret.addAll(traverse(node.left));
 *      ret.addAll(traverse(node.right));
 * }
 * 
 * But writting as the following is just for comparing with one written using the "RecursiveTransformer"
 */
private List<Integer> traverse(TreeNode node) {
    List<Integer> ret = new ArrayList<>();
    preorderRecursive(node, ret);
    return ret;
}

private preorderRecursive(TreeNode node, List<Integer> ret) {
    if (null == node) {
        return ret;
    }
    ret.add(node.val);
    preorderRecursive(node.left, ret);
    preorderRecursive(node.right, ret);
}
```

Then by using the `IterativeTransformer` we can change the above implementation into iterative but writing in a recursive like way

```java
private List<Integer> traverse(TreeNode node) {
    List<Integer> ret = new ArrayList<>();
    IterativeTransformer transformer = new IterativeTransformer();
    transformer.recursiveCall(() -> new Preorder(root, ret, transformer));
    transformer.evaluate();
    return ret;
}

private static class Preorder {
    private Preorder(TreeNode node, List<Integer> ret, IterativeTransformer transformer) {
        if (null == node) {
            return;
        }
        transformer.wrap(() -> ret.add(node.val));
        transformer.recursiveCall(() -> new Preorder(node.left, ret, transformer));
        transformer.recursiveCall(() -> new Preorder(node.right, ret, transformer));
    }
}
```

Parameters and return value are passing using the user define constructor interface. For example, given the original recursive function like

```java
RetType recursiveFun(ArgType1 arg1, ArgType2 arg2, ..., ArgTypeN argN) {
    ...
}
```

then it is recommended to define the recursive object constructor interface like

```java
class RecursiveObject {
    RecursiveObject(ArgType1 arg1, ArgType2 arg2, ..., ArgTypeN argN, Holder<RetType> ret) {
    }
}
```

Another example is writing a Binary Tree Clone procedure

Typical recursive one

```java
TreeNode clone(TreeNode root) {
    if (null == root) {
        return null;
    }
    TreeNode newNode = new TreeNode(node.val);
    newNode.left = clone(root.left);
    newNode.right = clone(root.right);
    return newNode;
}
```

By using `IterativeTransformer`

```java
TreeNode clone(TreeNode root) {
    Holder<TreeNode> ret = new Holder<>();
    IterativeTransformer transformer = new IterativeTransformer();
    transformer.recursiveCall(() -> new Cloner(root, ret, transformer));
    transformer.evaluate();
    return ret.get();
}

class Cloner {
    private Cloner(TreeNode node, Holder<TreeNode> ret, IterativeTransformer transformer) {
        if (null == node) {
            return;
        }
        transformer.wrap(() -> ret.set(new TreeNode(node.val)));
        {
            Holder<TreeNode> rret = new Holder<>();
            transformer.recursiveCall(() -> new Cloner(node.left, rret, transformer));
            transformer.wrap(() -> ret.get().left = rret.get());
        }
        {
            Holder<TreeNode> rret = new Holder<>();
            transformer.recursiveCall(() -> new Cloner(node.right, rret, transformer));
            transformer.wrap(() -> ret.get().right = rret.get());
        }
    }
}
```

#### AsyncIterator
[Implementation](https://github.com/alanzplus/CodeBox/blob/master/java/commons/src/main/java/org/alanzplus/codebox/commons/AsyncIterator.java#L43)

An interator implemented using the producer-consumer pattern.

```java
AsyncIterator asyncIterator = AsyncIterator.<String>custom()
  .ofSuppliers(
    heavyTaskSupplier1(),
    heavyTaskSupplier2(),
    ...
  )
  .create();

while (asyncIterator.hasNext()) {
  doSomething(asyncIterator.next());
}
```

## Racket

### Parse Prefix Symbolic Algebra Expression
[Left Parsing](https://github.com/alanzplus/codebox/blob/master/racket/algebra.rkt#L66)

From `(* a b c (+ a c) (^ a 3))` to `(* (* (* (* a b) c) (+ a c)) (^ a 3))`

[Right Parsing](https://github.com/alanzplus/codebox/blob/master/racket/algebra.rkt#L83)

From `(* a b c (+ a c) (^ a 3))` to `(* a (* b (* c (* (+ a c) (^ a 3)))))`

### Calculate Differentiation of Symbolic Algebra Expression

[Differentiation of Symbolic Expression expressed by Prefix Notation](https://github.com/alanzplus/codebox/blob/master/racket/algebra.rkt#L93)

Given `(* x y (+ x 3)) 'x)`, output `(+ (* x y) (* (+ x 3) y))`

### Y Combinator
The following writting is my reading notes taken from [The Y Combinator (Slight Return)](http://mvanier.livejournal.com/2897.html)

### What is a combinator
A combinator is just a lambda expression but without any free variables.

### How to define a recurisve anoymous function
For example, we have the following recursive factorial function

```scheme
(define fact
  (lambda (n)
    (if (= n 1)
        1
        (* n (fact (- n 1))))))
```

Now if we are asked to define the factorial function without explicitly recursively calls in the defintion. In other words, we are not allowed to use the function name inside the function's definition. But wait, is it possible to do that? Define a recurisve function without calling its name ?

```scheme
(define fact
  (lambda (n)
    (if (= n 1)
        1
        (* n (<???> (- n 1))))))
```

So the thought here is could we use a formal variable to capture the defintion the function, like the following (1)

```scheme
(define fact
  (lambda (f)
    (lambda (n)
      (if (= 0 n)
          1
          (* n (f (- n 1)))))))
```

Then we can use it in this way

```schem
((fact fact) 5) # this will not work
```

But wait, actually the above code is not correct since `fact` now is a function takes two arguments. And as a result `(f (- n 1))` is not corret.

Let's refine its definition

```scheme
(define fact
  (lambda (f)
    (lambda (n)
      (if (= 0 n)
          1
          (* n ((f f) (- n 1)))))))
```

And now it will be interpreted correctly

```scheme
((fact fact) 5) ; => 120
```

And now can we do better? remove the `(f f)` and keep using the defintion of (1) ? Yes we can use another formal variable to capture `(f f)`

```scheme
(define fact
  (lambda (f)
    ((lambda (f)
      (lambda (n)
        (if (= 0 n)
            1
            (* n (f (- n 1))))))
     (f f))))
```

But this still cannot work, because the non-lazy nature of scheme, when we call the following, `(f f)` will cause infinite recursion.

```scheme
((fact fact) 5)
```

But we know that `(f f)` actually returens a function that accept a single argument. So the following definition is same as `(f f)`

```scheme
(lambda (x) ((f f) x))
```

So we can change the defintion into

```scheme
(define fact
  (lambda (f)
    ((lambda (f)
      (lambda (n)
        (if (= 0 n)
            1
            (* n (f (- n 1))))))
     (lambda (x) ((f f) x)))))
```

And here let's define

```scheme
(define special-form-fact
  (lambda (f)
    (lambda (n)
      (if (= 0 n)
          1
          (* n (f (- n 1)))))))
```

Then we will have

```scheme
(define fact
  (lambda (f)
    (special-form-fact
     (lambda (x) ((f f) x)))))
```

and the following will work as expected

```scheme
((fact fact) 5) ; => 120
```

And could we do even better? Could we remove `(fact fact)` ? Yes we can use the following lambda expression to achieve the same thing as `(fact fact)`

```schem
(lambda (x) (x x))
```

So we will have our final factorial function

```scheme
(define fact
    ((lambda (x) (x x))
     (lambda (f)
       (special-form-fact
        (lambda (x) ((f f) x))))))
```

and the following will work as expected

```scheme
(fact 5) ; => 120
```

But wait a minute, the above definition can be generalized. We can replace the `special-form-fact` with a formal argument and make it into a function that taks a non-recursive function and return a its recursive function.

```scheme
(define Y
  (lambda (f)
    ((lambda (x) (x x))
     (lambda (y) (f (lambda (x) ((y y) x)))))))
```

This is the definition of `Applicative-Order Y-Combinator`. (as opposed to the `Normal-Order Y-Combinator`)

Then we can define our `fact` as

```scheme
(define fact (Y special-form-fact))
```

### What is Y ?
Y is a function which accepts a function as an argument and return the fix-point of that function.

What is the fix-point of a function? By definition, given function `f(x)`, then its fix-point is value such that

`f(fix-point) = fix-point`

So

`f(f(f(..(f(fix-point))))) = fix-point`

So how to derive Y ?

As we know, by definition, `Y(f) = fix-point`. Then we can subsutitue it into `f(fix-point)`. So we get

```
Y(f) = f(Y(f))
```

Then could we prove the following definition of Y is a fix-point combinator ?

```scheme
(define Y
  (lambda (f)
    ((lmabda (x) (x x))
     (lambda (y) (f lambda (x) ((y y) x))))))
```

In order to prove it we need to show `Y(f) = f(Y(f))`. So we apply f to Y then

```
(Y f) 
=>
  ((lambda (x) (x x))
   (lambda (y) (f (lambda (x) ((y y) x)))))
   
=>
(
 (lambda (y) (f (lambda (x) ((y y) x))))
 (lambda (y) (f (lambda (x) ((y y) x))))
)

=>
let k = (lambda (y) (f (lambda (x) ((y y) x))))
so (Y f) = (k k)

=>
(f (lambda (x) ((k k) x)))

=>
(f (lambda (x) ((Y f) x)))
```

### Why the original problem related the finding fix-point of function
The original problem is about define a recursive function without explicitly calling its name in its definition. How is it generalized as fix-point problem? Please check the original post.
